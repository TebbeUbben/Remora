package de.tebbeubben.remora.lib.commands

import de.tebbeubben.remora.lib.model.commands.RemoraCommand
import de.tebbeubben.remora.lib.model.commands.RemoraCommandData
import de.tebbeubben.remora.lib.model.commands.RemoraCommandError

interface CommandHandler {

    suspend fun prepareBolus(bolusData: RemoraCommandData.Bolus): Result<RemoraCommandData.Bolus>

    suspend fun ExecutionScope.executeBolus(bolusData: RemoraCommandData.Bolus): Result<RemoraCommandData.Bolus>

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