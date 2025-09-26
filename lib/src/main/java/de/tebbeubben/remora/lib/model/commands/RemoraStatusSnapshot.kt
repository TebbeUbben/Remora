package de.tebbeubben.remora.lib.model.commands

import kotlin.time.Instant

data class RemoraStatusSnapshot(
    val bg: Float,
    val iob: Float,
    val cob: Float,
    val lastBolusTime: Instant,
    val lastBolusAmount: Float
)