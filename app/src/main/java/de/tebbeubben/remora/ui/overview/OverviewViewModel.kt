package de.tebbeubben.remora.ui.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.tebbeubben.remora.lib.RemoraLib
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class OverviewViewModel @Inject constructor(
    private val remoraLib: RemoraLib,
) : ViewModel() {

    val statusState = remoraLib.statusFlow
        .flatMapLatest {
            flow {
                do {
                    emit(Clock.System.now() to it)
                    delay(15.seconds)
                } while (true)
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val commandState = remoraLib.commandStateFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
}