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
import kotlin.time.Duration
import kotlin.time.Instant

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

    val statusState = remoraLib.activeStatusFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun clearCommand(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                remoraLib.clearCommand()
            }
            onComplete()
        }
    }

    fun initCarbs(carbsAmount: Int, duration: Duration, tempTarget: Int, timestamp: Instant?) {
        startWorker {
            try {
                val templates = statusState.value!!.short!!.data.tempTargetTemplates
                val tempTargetTemplate = when (tempTarget) {
                    0    -> null
                    1    -> templates.activity
                    2    -> templates.eatingSoon
                    3    -> templates.hypo
                    else -> error("Invalid value: $tempTarget")
                }
                val reason = when (tempTarget) {
                    0    -> null
                    1    -> RemoraCommandData.Treatment.TemporaryTargetType.ACTIVITY
                    2    -> RemoraCommandData.Treatment.TemporaryTargetType.EATING_SOON
                    3    -> RemoraCommandData.Treatment.TemporaryTargetType.HYPO
                    else -> error("Invalid value: $tempTarget")
                }
                remoraLib.initiateCommand(
                    RemoraCommandData.Treatment(
                        timestamp = timestamp,
                        carbsAmount = carbsAmount.toFloat(),
                        carbsDuration = duration,
                        temporaryTarget = if (tempTargetTemplate != null) {
                            RemoraCommandData.Treatment.TemporaryTarget.Set(
                                ttType = reason!!,
                                target = tempTargetTemplate.target,
                                duration = tempTargetTemplate.duration
                            )
                        } else {
                            null
                        }
                    )
                )
                remoraLib.prepareCommand()
            } catch (e: Exception) {
                e.printStackTrace()
                //TODO: Log
            }
        }
    }

    fun initBolus(bolusAmount: Float, enableEatingSoonTT: Boolean) {
        startWorker {
            try {
                val tempTarget = if (enableEatingSoonTT) {
                    val template = statusState.value!!.short!!.data.tempTargetTemplates.eatingSoon
                    RemoraCommandData.Treatment.TemporaryTarget.Set(
                        ttType = RemoraCommandData.Treatment.TemporaryTargetType.EATING_SOON,
                        target = template.target,
                        duration = template.duration
                    )
                } else {
                    null
                }
                remoraLib.initiateCommand(
                    RemoraCommandData.Treatment(
                        bolusAmount = bolusAmount,
                        temporaryTarget = tempTarget
                    )
                )
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
        val workerState: WorkerState,
    )

    sealed class CommandState {
        object NotLoaded : CommandState()
        data class Loaded(val command: RemoraCommand?) : CommandState()
    }
}