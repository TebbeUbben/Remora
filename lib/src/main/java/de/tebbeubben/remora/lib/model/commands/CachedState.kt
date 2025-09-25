package de.tebbeubben.remora.lib.model.commands

data class CachedState(
    val isCached: Boolean,
    val command: RemoraCommand?,
)
