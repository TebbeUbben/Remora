package de.tebbeubben.remora.lib.status

import android.content.Context
import androidx.work.WorkerParameters
import de.tebbeubben.remora.lib.RemoraLib
import de.tebbeubben.remora.lib.messaging.MessageHandler
import de.tebbeubben.remora.lib.util.Crypto
import de.tebbeubben.remora.proto.StatusData
import de.tebbeubben.remora.proto.messages.statusMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

internal class SendShortStatusWorker(
    private val appContext: Context,
    workerParams: WorkerParameters,
) : AbstractStatusWorker(appContext, workerParams) {

    @Inject
    lateinit var crypto: Crypto

    @Inject
    lateinit var messageHandler: MessageHandler

    init {
        RemoraLib.component?.inject(this) ?: error("RemoraLib not initialized")
    }

    override suspend fun doWorkImpl(): Result = withContext(Dispatchers.Default) {
        val dataPath = inputData.getString("data_path") ?: error("data_path must be provided.")

        val followerId = inputData.getLong("follower_id", -1L)
        check(followerId != -1L) { "follower_id must be provided." }

        val statusId = inputData.getLong("status_id", -1L)
        check(statusId != -1L) { "status_id must be provided." }

        val dataFile = File(dataPath)
        val data = dataFile.readBytes()
        val statusData = StatusData.parseFrom(data)

        val statusMessage = statusMessage {
            this.statusId = statusId.toInt()
            this.shortStatusData = statusData.shortStatusData
        }

        messageHandler.sendStatusMessage(followerId, statusMessage)

        Result.success()
    }

}