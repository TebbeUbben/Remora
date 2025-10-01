package de.tebbeubben.remora.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import kotlin.time.Instant

@Composable
fun rememberInstant(initialValue: Instant?): MutableState<Instant?> = rememberSaveable(
    saver = mapSaver(
        save = {
            it.value?.let { instant ->
                mapOf(
                    "epochSeconds" to instant.epochSeconds,
                    "nanosecondsOfSecond" to instant.nanosecondsOfSecond
                )
            } ?: mapOf()
        },
        restore = {
            if (it.isEmpty()) {
                mutableStateOf(null)
            } else {
                mutableStateOf(
                    Instant.fromEpochSeconds(
                        it["epochSeconds"] as Long,
                        it["nanosecondsOfSecond"] as Int
                    )
                )
            }
        }
    )
) { mutableStateOf(initialValue) }