package de.tebbeubben.remora.lib.persistence.repositories

import android.content.Context
import android.content.SharedPreferences
import android.util.AtomicFile
import androidx.core.content.edit
import de.tebbeubben.remora.lib.di.ApplicationContext
import de.tebbeubben.remora.lib.model.FullStatus
import de.tebbeubben.remora.lib.model.ShortStatus
import de.tebbeubben.remora.lib.model.StatusView
import de.tebbeubben.remora.lib.model.RemoraStatusData.Companion.toModel
import de.tebbeubben.remora.lib.model.RemoraStatusData.Short.Companion.toModel
import de.tebbeubben.remora.proto.ShortStatusData
import de.tebbeubben.remora.proto.StatusData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatusRepository @Inject constructor(
    @param:ApplicationContext
    private val appContext: Context
) {

    private val prefs: SharedPreferences = appContext.getSharedPreferences("status_repository", Context.MODE_PRIVATE)

    private val dir = File(appContext.filesDir, "status_repo").apply { mkdirs() }
    private val fullFile = AtomicFile(File(dir, "full_status.pb"))
    private val shortFile = AtomicFile(File(dir, "short_status.pb"))

    private object K {

        const val FULL_ID = "full_status_id"
        const val SHORT_ID = "short_status_id"
    }

    private fun SharedPreferences.getLongOrNull(key: String): Long? =
        if (contains(key)) getLong(key, 0L) else null

    private var loaded = false
    private val statusMutex = Mutex()

    private val _shortStatus = MutableStateFlow<ShortStatus?>(null)
    private val _fullStatus = MutableStateFlow<FullStatus?>(null)

    val statusFlow = combine(
        _shortStatus.distinctUntilChangedBy { it?.statusId },
        _fullStatus.distinctUntilChangedBy { it?.statusId }
    ) { short, full ->
        getStatusView(short, full)
    }
        .onStart { statusMutex.withLock { load() } }

    suspend fun saveShortStatus(statusId: Long, short: ShortStatusData) = withContext(Dispatchers.IO) {
        statusMutex.withLock {
            val current = load()
            if (current.short == null || statusId > current.short.statusId) {
                val shortStatus = ShortStatus(statusId, short.toModel())
                writeAtomic(shortFile, short.toByteArray())
                prefs.edit { putLong(K.SHORT_ID, statusId) }
                _shortStatus.value = shortStatus
                return@withContext getStatusView(shortStatus, current.full)
            }
            return@withContext current
        }
    }

    suspend fun saveFullStatus(statusId: Long, full: StatusData) = withContext(Dispatchers.IO) {
        statusMutex.withLock {
            val current = load()
            if (current.full == null || statusId > current.full.statusId) {
                val fullStatus = FullStatus(statusId, full.toModel())
                writeAtomic(fullFile, full.toByteArray())
                prefs.edit { putLong(K.FULL_ID, statusId) }
                _fullStatus.value = fullStatus
                return@withContext getStatusView(current.short, fullStatus)
            }
            return@withContext current
        }
    }

    private fun getStatusView(short: ShortStatus?, full: FullStatus?) = StatusView(
        full = full,
        short = when {
            full == null -> short
            short != null && short.statusId >= full.statusId -> short
            else -> ShortStatus(full.statusId, full.data.short)
        },
        newestKnownStatusId = maxOf(full?.statusId ?: Long.MIN_VALUE, short?.statusId ?: Long.MIN_VALUE)
    )

    private suspend fun load() = withContext(Dispatchers.IO) {
        if (!loaded) {
            val diskFull = loadFullFromDisk()
            val cachedFull = _fullStatus.value
            if (cachedFull == null || diskFull != null && cachedFull.statusId < diskFull.statusId) {
                _fullStatus.value = diskFull
            }
            val diskShort = loadShortFromDisk()
            val cachedShort = _shortStatus.value
            if (cachedShort == null || diskShort != null && cachedShort.statusId < diskShort.statusId) {
                _shortStatus.value = diskShort
            }
            loaded = true
        }
        return@withContext getStatusView(_shortStatus.value, _fullStatus.value)
    }

    suspend fun getCached() = statusMutex.withLock { load() }

    private fun loadFullFromDisk(): FullStatus? = try {
        val id = prefs.getLongOrNull(K.FULL_ID) ?: return null
        val bytes = readAtomicOrNull(fullFile) ?: return null
        val data = StatusData.parseFrom(bytes)
        FullStatus(id, data.toModel())
    } catch (_: Throwable) {
        null
    }

    private fun loadShortFromDisk(): ShortStatus? = try {
        val id = prefs.getLongOrNull(K.SHORT_ID) ?: return null
        val bytes = readAtomicOrNull(shortFile) ?: return null
        val data = ShortStatusData.parseFrom(bytes)
        ShortStatus(id, data.toModel())
    } catch (_: Throwable) {
        null
    }

    private fun writeAtomic(file: AtomicFile, bytes: ByteArray) {
        val out: FileOutputStream = file.startWrite()
        try {
            out.write(bytes)
            out.fd.sync()
            file.finishWrite(out)
        } catch (t: Throwable) {
            file.failWrite(out); throw t
        }
    }

    private fun readAtomicOrNull(file: AtomicFile): ByteArray? = try {
        file.openRead().use { it.readBytes() }
    } catch (_: Throwable) {
        null
    }
}