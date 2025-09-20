package de.tebbeubben.remora.lib.model.status

import kotlin.time.Instant

data class StatusSnapshot(
    val bg: Float,
    val iob: Float,
    val cob: Float,
    val lastBolusTime: Instant,
    val lastBolusAmount: Double
)