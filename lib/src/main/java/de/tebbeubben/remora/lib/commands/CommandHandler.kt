package de.tebbeubben.remora.lib.commands

import de.tebbeubben.remora.lib.model.commands.RemoraCommand
import de.tebbeubben.remora.lib.model.commands.RemoraCommandData
import de.tebbeubben.remora.lib.model.commands.RemoraCommandError
import de.tebbeubben.remora.lib.model.commands.RemoraStatusSnapshot

interface CommandHandler {

    suspend fun validateStatusSnapshot(snapshot: RemoraStatusSnapshot): RemoraCommandError?

    suspend fun prepareTreatment(data: RemoraCommandData.Treatment): Result<RemoraCommandData.Treatment>

    suspend fun ExecutionScope.executeTreatment(data: RemoraCommandData.Treatment): Result<RemoraCommandData.Treatment>

    sealed class Result<out T> {
        data class Success<out T>(val data: T) : Result<T>()
        data class Error<out T>(val error: RemoraCommandError) : Result<T>()
    }

    interface ExecutionScope {
        suspend fun reportIntermediateProgress(progress: RemoraCommand.Progress)
    }
}

fun <T> CommandHandler.wrapSuccess(data: T): CommandHandler.Result<T> = CommandHandler.Result.Success(data)
fun <T> CommandHandler.wrapError(error: RemoraCommandError): CommandHandler.Result<T> = CommandHandler.Result.Error(error)