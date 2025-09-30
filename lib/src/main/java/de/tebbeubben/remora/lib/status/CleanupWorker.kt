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

internal class CleanupWorker(
    private val appContext: Context,
    workerParams: WorkerParameters,
) : AbstractStatusWorker(appContext, workerParams) {

    init {
        RemoraLib.component?.inject(this) ?: error("RemoraLib not initialized")
    }

    override suspend fun doWorkImpl(): Result = withContext(Dispatchers.Default) {
        val dataPath = inputData.getString("data_path") ?: error("data_path must be provided.")
        val dataFile = File(dataPath)
        dataFile.delete()
        Result.success()
    }

}