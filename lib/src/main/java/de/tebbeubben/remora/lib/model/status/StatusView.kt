package de.tebbeubben.remora.lib.model.status

data class FullStatus(
    val statusId: Long,
    val data: RemoraStatusData
)

data class ShortStatus(
    val statusId: Long,
    val data: RemoraStatusData.Short
)

data class StatusView(
    val full: FullStatus?,
    val short: ShortStatus?,
    val newestKnownStatusId: Long
)