package de.tebbeubben.remora.ui.commands

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.tebbeubben.remora.NotificationHandler
import de.tebbeubben.remora.lib.RemoraLib
import de.tebbeubben.remora.lib.model.commands.RemoraCommand
import de.tebbeubben.remora.lib.model.commands.RemoraCommandData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class CommandViewModel @Inject constructor(
    private val remoraLib: RemoraLib,
    private val notificationHandler: NotificationHandler,
) : ViewModel() {

    private val mutex = Mutex()
    private val _workerState = MutableStateFlow(WorkerState.IDLE)

    val uiState = combine(remoraLib.commandStateFlow, _workerState) { cachedState, workerState ->
        UiState(
            command = CommandState.Loaded(cachedState.command),
            workerState = workerState
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UiState(CommandState.NotLoaded, _workerState.value),
    )

    fun clearCommand(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                remoraLib.clearCommand()
            }
            onComplete()
        }
    }

    fun initBolus(bolusAmount: Float, enableEatingSoonTT: Boolean) {
        startWorker {
            try {
                remoraLib.initiateCommand(RemoraCommandData.Bolus(bolusAmount, enableEatingSoonTT))
                remoraLib.prepareCommand()
            } catch (e: Exception) {
                e.printStackTrace()
                //TODO: Log
            }
        }
    }

    fun retryPrepare() {
        startWorker(remoraLib::prepareCommand)
    }

    fun confirmCommand() {
        startWorker(remoraLib::confirmCommand)
    }

    private fun startWorker(block: suspend () -> Unit) = viewModelScope.launch {
        if (!mutex.tryLock()) return@launch
        try {
            _workerState.value = WorkerState.RUNNING
            withContext(Dispatchers.IO) {
                block()
            }
            _workerState.value = WorkerState.IDLE
        } catch (e: Exception) {
            e.printStackTrace()
            //TODO: Log
            _workerState.value = WorkerState.FAILED
        } finally {
            mutex.unlock()
        }
    }

    fun setActive(active: Boolean) {
        notificationHandler.commandDialogActive.value = active
    }

    override fun onCleared() {
        notificationHandler.commandDialogActive.value = false
    }

    enum class WorkerState {
        IDLE,
        RUNNING,
        FAILED
    }

    data class UiState(
        val command: CommandState,
        val workerState: WorkerState
    )

    sealed class CommandState {
        object NotLoaded : CommandState()
        data class Loaded(val command: RemoraCommand?) : CommandState()
    }
}