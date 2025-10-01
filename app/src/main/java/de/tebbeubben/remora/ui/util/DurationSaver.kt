package de.tebbeubben.remora.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import kotlin.time.Duration

@Composable
fun rememberDuration(initialValue: Duration) = rememberSaveable(
    saver = Saver(
        save = { it.value.toIsoString() },
        restore = { mutableStateOf(Duration.parseIsoString(it)) }
    )
) { mutableStateOf(initialValue) }