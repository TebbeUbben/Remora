package de.tebbeubben.remora.lib.model.commands

import kotlin.time.Instant

internal data class PreparedCommand(
    val peerId: Long,
    val followerSequenceId: Int,
    val mainSequenceId: Int,
    val validUntil: Instant,
    val data: RemoraCommandData
)