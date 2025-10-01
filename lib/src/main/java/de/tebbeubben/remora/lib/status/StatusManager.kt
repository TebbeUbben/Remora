package de.tebbeubben.remora.lib.status

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import dagger.Lazy
import de.tebbeubben.remora.lib.FirebaseAppProvider
import de.tebbeubben.remora.lib.PeerDeviceManager
import de.tebbeubben.remora.lib.di.ApplicationContext
import de.tebbeubben.remora.lib.lifecycle.LibraryLifecycleCallback
import de.tebbeubben.remora.lib.model.status.RemoraStatusData
import de.tebbeubben.remora.lib.model.status.RemoraStatusData.Companion.toProtobuf
import de.tebbeubben.remora.lib.model.status.StatusView
import de.tebbeubben.remora.lib.persistence.repositories.MessageRepository
import de.tebbeubben.remora.lib.persistence.repositories.StatusRepository
import de.tebbeubben.remora.lib.util.Crypto
import de.tebbeubben.remora.proto.ShortStatusData
import de.tebbeubben.remora.proto.StatusData
import de.tebbeubben.remora.proto.StatusEnvelope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import javax.crypto.AEADBadTagException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

@Singleton
internal class StatusManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val firebaseAppProvider: FirebaseAppProvider,
    private val peerDeviceManager: Lazy<PeerDeviceManager>,
    private val crypto: Crypto,
    private val messageRepository: MessageRepository,
    private val statusRepository: StatusRepository,
    private val messageIdRepository: MessageRepository,
) : LibraryLifecycleCallback, CoroutineScope by CoroutineScope(Dispatchers.Default) {

    private val firestore get() = firebaseAppProvider.firebaseApp?.let { FirebaseFirestore.getInstance(it) }!!

    companion object {

        const val STATUS_DOCUMENT = "remora/status"

        const val STATUS_VERSION = 1
    }

    val activeStatusFlow
        get() = combine(
            flow = statusRepository.statusFlow,
            flow2 = updateFlow
        ) { statusView, _ -> statusView }

    val passiveStatusFlow get() = statusRepository.statusFlow

    // This is just a helper Flow that doesn't emit any data.
    // While active, it subscribes to the status document in Firestore and handles any updates.
    val updateFlow =
        firebaseAppProvider
            .firebaseAppFlow
            .mapNotNull { it?.let { FirebaseFirestore.getInstance(it).document(STATUS_DOCUMENT) } }
            .flatMapLatest { it.snapshots() }
            .onEach {
                try {
                    handleSnapshot(it)
                } catch (e: Exception) {
                    e.printStackTrace()
                    // TODO: Log
                }
            }
            .map { }
            .onStart { emit(Unit) }
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)
            .shareIn(this, SharingStarted.WhileSubscribed(5000), replay = 1)

    suspend fun refreshOnce() = handleSnapshot(firestore.document(STATUS_DOCUMENT).get().await())

    suspend fun onReceiveShortStatus(statusId: Long, data: ShortStatusData) {
        statusRepository.saveShortStatus(statusId, data)
    }

    private suspend fun handleSnapshot(snapshot: DocumentSnapshot): StatusView {
        if (!snapshot.exists()) return statusRepository.getCached()
        val bytes = snapshot.getBlob("data")?.toBytes() ?: return statusRepository.getCached()
        val res = unwrapStatusEnvelope(bytes) ?: return statusRepository.getCached()
        val (statusId, status) = res
        return statusRepository.saveFullStatus(statusId, status)
    }

    private suspend fun unwrapStatusEnvelope(data: ByteArray): Pair<Long, StatusData>? {
        val statusEnvelope = StatusEnvelope.parseFrom(data)
        if (statusEnvelope.version != STATUS_VERSION) {
            //TODO: Log
            return null
        }
        val mainDevice = peerDeviceManager.get().getMainDevice() ?: return null
        val encryptedStatus = statusEnvelope.encryptedStatusData.toByteArray()
        val wrappedKey = statusEnvelope.wrappedKeysList.firstOrNull() { it.followerId == mainDevice.ownFollowerId } ?: return null
        val statusId = wrappedKey.statusId.toLong() and 0xFFFFFFFF
        val encryptedStatusKey = wrappedKey.encryptedStatusKey
        val iv = ByteArray(12)
        iv[8] = (statusId ushr 24).toByte()
        iv[9] = (statusId ushr 16).toByte()
        iv[10] = (statusId ushr 8).toByte()
        iv[11] = statusId.toByte()
        val decryptedStatusKey = try {
            crypto.decryptStatusKeyAESGCM(
                peerDeviceId = mainDevice.id,
                iv = iv,
                data = encryptedStatusKey.toByteArray(),
                aad = byteArrayOf(STATUS_VERSION.toByte()) + encryptedStatus
            )
        } catch (e: AEADBadTagException) {
            //TODO: Log
            return null
        }
        val decryptedStatusData = crypto.decryptAESCTR(
            key = decryptedStatusKey,
            iv = ByteArray(12),
            data = encryptedStatus
        )
        if (!messageRepository.updateLastStatusId(mainDevice.id, statusId)) {
            //TODO: Log
            return null
        }
        val statusData = StatusData.parseFrom(decryptedStatusData)
        return statusId to statusData
    }

    suspend fun shareStatus(statusData: RemoraStatusData) = withContext(Dispatchers.IO) {
        val workManager = WorkManager.getInstance(context)
        val followers = peerDeviceManager.get().getFollowers()
        if (followers.isEmpty()) {
            workManager.cancelUniqueWork(AbstractStatusWorker.UNIQUE_WORK_NAME)
            // Not uploading status since there are no followers to see it anyways.
            return@withContext ListenableWorker.Result.success()
            //TODO: Log
        }
        val statusIds = followers.map { follower -> follower.id to messageIdRepository.getNextStatusId(follower.id) }

        val file = File(context.cacheDir, "status_data_${statusData.short.timestamp}.bin")
        file.outputStream().use { it.write(statusData.toProtobuf().toByteArray()) }

        val sendShortStatusRequests = statusIds.map {
            OneTimeWorkRequestBuilder<SendShortStatusWorker>()
                .setBackoffCriteria(BackoffPolicy.LINEAR, 1.minutes.toJavaDuration())
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setInputData(workDataOf(
                    "data_path" to file.absolutePath,
                    "follower_id" to it.first,
                    "status_id" to it.second
                ))
                .build()
        }

        val uploadFullStatusRequest = OneTimeWorkRequestBuilder<UploadFullStatusWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setBackoffCriteria(BackoffPolicy.LINEAR, 1.minutes.toJavaDuration())
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setInputData(workDataOf(
                "data_path" to file.absolutePath,
                *statusIds.map { "follower_${it.first}" to it.second }.toTypedArray()
            ))
            .build()

        val cleanupRequest = OneTimeWorkRequestBuilder<CleanupWorker>()
            .setInputData(workDataOf("data_path" to file.absolutePath))
            .build()

        workManager
            .beginUniqueWork(
                AbstractStatusWorker.UNIQUE_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                sendShortStatusRequests + uploadFullStatusRequest
            )
            .then(cleanupRequest)
            .enqueue()
    }

    override suspend fun onReset() {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelUniqueWork(AbstractStatusWorker.UNIQUE_WORK_NAME)
    }

    suspend fun uploadEncryptedStatus(statusEnvelope: StatusEnvelope) {
        firestore.document(STATUS_DOCUMENT).set(buildMap {
            put("data", Blob.fromBytes(statusEnvelope.toByteArray()))
        }).await()
    }

    data class StatusKey(
        val timestamp: Long,
        val key: ByteArray,
    ) {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as StatusKey

            if (timestamp != other.timestamp) return false
            if (!key.contentEquals(other.key)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = timestamp.hashCode()
            result = 31 * result + key.contentHashCode()
            return result
        }
    }
}