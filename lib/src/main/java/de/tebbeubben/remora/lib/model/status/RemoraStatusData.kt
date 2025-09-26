package de.tebbeubben.remora.lib.model.status

import de.tebbeubben.remora.lib.model.status.RemoraStatusData.AutosensData.Companion.toModel
import de.tebbeubben.remora.lib.model.status.RemoraStatusData.AutosensData.Companion.toProtobuf
import de.tebbeubben.remora.lib.model.status.RemoraStatusData.AutosensType.Companion.toModel
import de.tebbeubben.remora.lib.model.status.RemoraStatusData.AutosensType.Companion.toProtobuf
import de.tebbeubben.remora.lib.model.status.RemoraStatusData.BasalDataPoint.Companion.toModel
import de.tebbeubben.remora.lib.model.status.RemoraStatusData.BasalDataPoint.Companion.toProtobuf
import de.tebbeubben.remora.lib.model.status.RemoraStatusData.BgData.Companion.toModel
import de.tebbeubben.remora.lib.model.status.RemoraStatusData.BgData.Companion.toProtobuf
import de.tebbeubben.remora.lib.model.status.RemoraStatusData.BgReading.Companion.toModel
import de.tebbeubben.remora.lib.model.status.RemoraStatusData.BgReading.Companion.toProtobuf
import de.tebbeubben.remora.lib.model.status.RemoraStatusData.Bolus.Companion.toModel
import de.tebbeubben.remora.lib.model.status.RemoraStatusData.Bolus.Companion.toProtobuf
import de.tebbeubben.remora.lib.model.status.RemoraStatusData.BolusType.Companion.toModel
import de.tebbeubben.remora.lib.model.status.RemoraStatusData.BolusType.Companion.toProtobuf
import de.tebbeubben.remora.lib.model.status.RemoraStatusData.BucketedDataPoint.Companion.toModel
import de.tebbeubben.remora.lib.model.status.RemoraStatusData.BucketedDataPoint.Companion.toProtobuf
import de.tebbeubben.remora.lib.model.status.RemoraStatusData.CarbEntry.Companion.toModel
import de.tebbeubben.remora.lib.model.status.RemoraStatusData.CarbEntry.Companion.toProtobuf
import de.tebbeubben.remora.lib.model.status.RemoraStatusData.DisplayBg.Companion.toModel
import de.tebbeubben.remora.lib.model.status.RemoraStatusData.DisplayBg.Companion.toProtobuf
import de.tebbeubben.remora.lib.model.status.RemoraStatusData.ExtendedBolus.Companion.toModel
import de.tebbeubben.remora.lib.model.status.RemoraStatusData.ExtendedBolus.Companion.toProtobuf
import de.tebbeubben.remora.lib.model.status.RemoraStatusData.InsulinData.Companion.toModel
import de.tebbeubben.remora.lib.model.status.RemoraStatusData.InsulinData.Companion.toProtobuf
import de.tebbeubben.remora.lib.model.status.RemoraStatusData.MeterType.Companion.toModel
import de.tebbeubben.remora.lib.model.status.RemoraStatusData.MeterType.Companion.toProtobuf
import de.tebbeubben.remora.lib.model.status.RemoraStatusData.Prediction.Companion.toModel
import de.tebbeubben.remora.lib.model.status.RemoraStatusData.Prediction.Companion.toProtobuf
import de.tebbeubben.remora.lib.model.status.RemoraStatusData.PredictionType.Companion.toModel
import de.tebbeubben.remora.lib.model.status.RemoraStatusData.PredictionType.Companion.toProtobuf
import de.tebbeubben.remora.lib.model.status.RemoraStatusData.ProfileSwitch.Companion.toModel
import de.tebbeubben.remora.lib.model.status.RemoraStatusData.ProfileSwitch.Companion.toProtobuf
import de.tebbeubben.remora.lib.model.status.RemoraStatusData.RunningMode.Companion.toModel
import de.tebbeubben.remora.lib.model.status.RemoraStatusData.RunningMode.Companion.toProtobuf
import de.tebbeubben.remora.lib.model.status.RemoraStatusData.RunningModeDataPoint.Companion.toModel
import de.tebbeubben.remora.lib.model.status.RemoraStatusData.RunningModeDataPoint.Companion.toProtobuf
import de.tebbeubben.remora.lib.model.status.RemoraStatusData.Short.Companion.toModel
import de.tebbeubben.remora.lib.model.status.RemoraStatusData.Short.Companion.toProtobuf
import de.tebbeubben.remora.lib.model.status.RemoraStatusData.StatusLightElement
import de.tebbeubben.remora.lib.model.status.RemoraStatusData.StatusLightElement.Companion.toIntModel
import de.tebbeubben.remora.lib.model.status.RemoraStatusData.StatusLightElement.Companion.toProtobuf
import de.tebbeubben.remora.lib.model.status.RemoraStatusData.StatusLightElement.Companion.toTimeModel
import de.tebbeubben.remora.lib.model.status.RemoraStatusData.TargetDataPoint.Companion.toModel
import de.tebbeubben.remora.lib.model.status.RemoraStatusData.TargetDataPoint.Companion.toProtobuf
import de.tebbeubben.remora.lib.model.status.RemoraStatusData.TemporaryBasal.Companion.toModel
import de.tebbeubben.remora.lib.model.status.RemoraStatusData.TemporaryBasal.Companion.toProtobuf
import de.tebbeubben.remora.lib.model.status.RemoraStatusData.TemporaryTarget.Companion.toModel
import de.tebbeubben.remora.lib.model.status.RemoraStatusData.TemporaryTarget.Companion.toProtobuf
import de.tebbeubben.remora.lib.model.status.RemoraStatusData.TemporaryTargetReason.Companion.toModel
import de.tebbeubben.remora.lib.model.status.RemoraStatusData.TemporaryTargetReason.Companion.toProtobuf
import de.tebbeubben.remora.lib.model.status.RemoraStatusData.TherapyEvent.Companion.toModel
import de.tebbeubben.remora.lib.model.status.RemoraStatusData.TherapyEvent.Companion.toProtobuf
import de.tebbeubben.remora.lib.model.status.RemoraStatusData.TherapyEventType.Companion.toModel
import de.tebbeubben.remora.lib.model.status.RemoraStatusData.TherapyEventType.Companion.toProtobuf
import de.tebbeubben.remora.lib.model.status.RemoraStatusData.TrendArrow.Companion.toModel
import de.tebbeubben.remora.lib.model.status.RemoraStatusData.TrendArrow.Companion.toProtobuf
import de.tebbeubben.remora.proto.ShortStatusData
import de.tebbeubben.remora.proto.StatusData
import de.tebbeubben.remora.proto.autosensData
import de.tebbeubben.remora.proto.autosensDataOrNull
import de.tebbeubben.remora.proto.basalDataPoint
import de.tebbeubben.remora.proto.bgData
import de.tebbeubben.remora.proto.bgDataOrNull
import de.tebbeubben.remora.proto.bgReading
import de.tebbeubben.remora.proto.bolus
import de.tebbeubben.remora.proto.bucketedDataPoint
import de.tebbeubben.remora.proto.carbEntry
import de.tebbeubben.remora.proto.deltas
import de.tebbeubben.remora.proto.deltasOrNull
import de.tebbeubben.remora.proto.displayBg
import de.tebbeubben.remora.proto.displayBgOrNull
import de.tebbeubben.remora.proto.extendedBolus
import de.tebbeubben.remora.proto.insulinData
import de.tebbeubben.remora.proto.insulinDataOrNull
import de.tebbeubben.remora.proto.prediction
import de.tebbeubben.remora.proto.profileSwitch
import de.tebbeubben.remora.proto.runningModeDataPoint
import de.tebbeubben.remora.proto.shortStatusData
import de.tebbeubben.remora.proto.statusData
import de.tebbeubben.remora.proto.statusLightElement
import de.tebbeubben.remora.proto.targetDataPoint
import de.tebbeubben.remora.proto.temporaryBasal
import de.tebbeubben.remora.proto.temporaryTarget
import de.tebbeubben.remora.proto.therapyEvent
import kotlin.Int
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.Instant
import kotlin.time.toDuration

data class RemoraStatusData(
    val short: Short,
    val bucketedData: List<BucketedDataPoint>,
    val basalData: List<BasalDataPoint>,
    val targetData: List<TargetDataPoint>,
    val predictions: List<Prediction>,
    val bgReadings: List<BgReading>,
    val boluses: List<Bolus>,
    val carbs: List<CarbEntry>,
    val profileSwitches: List<ProfileSwitch>,
    val therapyEvents: List<TherapyEvent>,
    val temporaryBasals: List<TemporaryBasal>,
    val temporaryTargets: List<TemporaryTarget>,
    val extendedBoluses: List<ExtendedBolus>,
    val runningModes: List<RunningModeDataPoint>,
    val profiles: List<String>,
    val isFakingTemps: Boolean,
) {

    internal companion object {

        private const val VERSION = 1;

        fun StatusData.toModel(): RemoraStatusData {
            if (version != VERSION) throw IllegalArgumentException("Unsupported version")
            val short = shortStatusData.toModel()
            val timestamp = short.timestamp
            return RemoraStatusData(
                short = short,
                bucketedData = bucketedDataList.toModel(timestamp),
                basalData = basalDataList.map { it.toModel(timestamp) },
                targetData = targetDataList.map { it.toModel(timestamp) },
                predictions = predictionsList.map { it.toModel(timestamp) },
                bgReadings = bgReadingsList.map { it.toModel(timestamp) },
                boluses = bolusesList.map { it.toModel(timestamp) },
                carbs = carbsList.map { it.toModel(timestamp) },
                profileSwitches = profileSwitchesList.map { it.toModel(timestamp) },
                therapyEvents = therapyEventsList.map { it.toModel(timestamp) },
                temporaryBasals = temporaryBasalsList.map { it.toModel(timestamp) },
                temporaryTargets = temporaryTargetsList.map { it.toModel(timestamp) },
                extendedBoluses = extendedBolusesList.map { it.toModel(timestamp) },
                profiles = profilesList.toList(),
                runningModes = runningModesList.map { it.toModel(timestamp) },
                isFakingTemps = fakingTemps
            )
        }

        fun RemoraStatusData.toProtobuf() = statusData {
            version = VERSION
            shortStatusData = this@toProtobuf.short.toProtobuf()
            bucketedData += this@toProtobuf.bucketedData.toProtobuf(short.timestamp)
            basalData += this@toProtobuf.basalData.map { it.toProtobuf(short.timestamp) }
            targetData += this@toProtobuf.targetData.map { it.toProtobuf(short.timestamp) }
            predictions += this@toProtobuf.predictions.map { it.toProtobuf(short.timestamp) }
            bgReadings += this@toProtobuf.bgReadings.map { it.toProtobuf(short.timestamp) }
            boluses += this@toProtobuf.boluses.map { it.toProtobuf(short.timestamp) }
            carbs += this@toProtobuf.carbs.map { it.toProtobuf(short.timestamp) }
            profileSwitches += this@toProtobuf.profileSwitches.map { it.toProtobuf(short.timestamp) }
            therapyEvents += this@toProtobuf.therapyEvents.map { it.toProtobuf(short.timestamp) }
            temporaryBasals += this@toProtobuf.temporaryBasals.map { it.toProtobuf(short.timestamp) }
            temporaryTargets += this@toProtobuf.temporaryTargets.map { it.toProtobuf(short.timestamp) }
            extendedBoluses += this@toProtobuf.extendedBoluses.map { it.toProtobuf(short.timestamp) }
            profiles += this@toProtobuf.profiles
            runningModes += this@toProtobuf.runningModes.map { it.toProtobuf(short.timestamp) }
            fakingTemps = this@toProtobuf.isFakingTemps
        }
    }

    data class Short(
        val timestamp: Instant,
        val timezone: String,

        val cob: Cob,

        val activeProfile: ActiveProfile,

        val bgConfig: BgConfig,

        val displayBg: DisplayBg?,

        val iob: Iob,

        val reservoirLevel: StatusLightElement<Int, Int>?,
        val reservoirChangedAt: StatusLightElement<Instant, Duration>?,
        val sensorChangedAt: StatusLightElement<Instant, Duration>?,
        val sensorBatteryLevel: StatusLightElement<Int, Int>?,
        val pumpBatteryLevel: StatusLightElement<Int, Int>?,
        val pumpBatteryChangedAt: StatusLightElement<Instant, Duration>?,
        val cannulaChangedAt: StatusLightElement<Instant, Duration>?,

        val usesPatchPump: Boolean,

        val activeRunningMode: ActiveRunningMode,

        val basalStatus: BasalStatus,

        val currentTarget: CurrentTarget,

        val autosensRatio: Float,

        val deviceBattery: DeviceBattery,

        val lastBolus: LastBolus?
    ) {

        internal companion object {

            fun ShortStatusData.toModel() = Short(
                timestamp = Instant.fromEpochSeconds(timestamp),
                timezone = timezone,

                cob = Cob(
                    display = if (hasDisplayCob()) displayCob else null,
                    futureCarbs = futureCarbs
                ),

                activeProfile = ActiveProfile(
                    name = activeProfile,
                    percentage = activeProfilePercentage,
                    timeShift = activeProfileShift,
                    start = Instant.fromEpochSeconds(activeProfileStart + timestamp),
                    duration = if (hasActiveProfileDuration()) activeProfileDuration.seconds else null
                ),

                bgConfig = BgConfig(
                    usesMgdl = usesMgdl,
                    lowBgThreshold = lowBgThreshold,
                    highBgThreshold = highBgThreshold
                ),

                displayBg = displayBgOrNull?.toModel(Instant.fromEpochSeconds(timestamp)),

                iob = Iob(
                    bolus = bolusIob,
                    basal = basalIob
                ),

                reservoirLevel = if (hasReservoirLevel()) reservoirLevel.toIntModel() else null,
                reservoirChangedAt = if (hasReservoirChangedAt()) reservoirChangedAt.toTimeModel(Instant.fromEpochSeconds(timestamp)) else null,
                sensorChangedAt = if (hasSensorChangedAt()) sensorChangedAt.toTimeModel(Instant.fromEpochSeconds(timestamp)) else null,
                sensorBatteryLevel = if (hasSensorBatteryLevel()) sensorBatteryLevel.toIntModel() else null,
                pumpBatteryLevel = if (hasPumpBatteryLevel()) pumpBatteryLevel.toIntModel() else null,
                pumpBatteryChangedAt = if (hasPumpBatteryChangedAt()) pumpBatteryChangedAt.toTimeModel(Instant.fromEpochSeconds(timestamp)) else null,
                cannulaChangedAt = if (hasCannulaChangedAt()) cannulaChangedAt.toTimeModel(Instant.fromEpochSeconds(timestamp)) else null,

                usesPatchPump = usesPatchPump,

                activeRunningMode = ActiveRunningMode(
                    mode = runningMode.toModel(),
                    start = Instant.fromEpochSeconds(runningModeStart + timestamp),
                    duration = if (hasRunningModeDuration()) runningModeDuration.seconds else null
                ),

                basalStatus = BasalStatus(
                    baseBasal = baseBasal,
                    tempBasalAbsolute = if (hasTempBasalAbsolute()) tempBasalAbsolute else null,
                ),

                currentTarget = CurrentTarget(
                    target = target,
                    tempTargetStart = if (hasTempTargetStart()) Instant.fromEpochSeconds(tempTargetStart + timestamp) else null,
                    tempTargetDuration = if (hasTempTargetDuration()) tempTargetDuration.seconds else null
                ),

                autosensRatio = autosensRatio,

                deviceBattery = DeviceBattery(deviceBattery, isCharging),

                lastBolus = if (hasLastBolusAt()) LastBolus(Instant.fromEpochSeconds(timestamp) + lastBolusAt.seconds, lastBolusAmount) else null
            )

            fun Short.toProtobuf() = shortStatusData {
                timestamp = this@toProtobuf.timestamp.epochSeconds
                timezone = this@toProtobuf.timezone

                this@toProtobuf.cob.display?.let { displayCob = it }
                futureCarbs = this@toProtobuf.cob.futureCarbs

                usesMgdl = this@toProtobuf.bgConfig.usesMgdl
                lowBgThreshold = this@toProtobuf.bgConfig.lowBgThreshold
                highBgThreshold = this@toProtobuf.bgConfig.highBgThreshold

                activeProfile = this@toProtobuf.activeProfile.name
                activeProfilePercentage = this@toProtobuf.activeProfile.percentage
                activeProfileShift = this@toProtobuf.activeProfile.timeShift
                activeProfileStart = (this@toProtobuf.activeProfile.start - this@toProtobuf.timestamp).inWholeSeconds
                this@toProtobuf.activeProfile.duration?.let { activeProfileDuration = it.inWholeSeconds }

                this@toProtobuf.displayBg?.let { displayBg = it.toProtobuf(this@toProtobuf.timestamp) }
                bolusIob = this@toProtobuf.iob.bolus
                basalIob = this@toProtobuf.iob.basal

                this@toProtobuf.reservoirLevel?.let { reservoirLevel = it.toProtobuf() }
                this@toProtobuf.reservoirChangedAt?.let { reservoirChangedAt = it.toProtobuf(this@toProtobuf.timestamp) }
                this@toProtobuf.sensorBatteryLevel?.let { sensorBatteryLevel = it.toProtobuf() }
                this@toProtobuf.sensorChangedAt?.let { sensorChangedAt = it.toProtobuf(this@toProtobuf.timestamp) }
                this@toProtobuf.pumpBatteryLevel?.let { pumpBatteryLevel = it.toProtobuf() }
                this@toProtobuf.pumpBatteryChangedAt?.let { pumpBatteryChangedAt = it.toProtobuf(this@toProtobuf.timestamp) }
                this@toProtobuf.cannulaChangedAt?.let { cannulaChangedAt = it.toProtobuf(this@toProtobuf.timestamp) }

                usesPatchPump = this@toProtobuf.usesPatchPump

                runningMode = this@toProtobuf.activeRunningMode.mode.toProtobuf()
                runningModeStart = (this@toProtobuf.activeRunningMode.start - this@toProtobuf.timestamp).inWholeSeconds
                this@toProtobuf.activeRunningMode.duration?.let { runningModeDuration = it.inWholeSeconds }

                baseBasal = this@toProtobuf.basalStatus.baseBasal
                this@toProtobuf.basalStatus.tempBasalAbsolute?.let { tempBasalAbsolute = it }

                target = this@toProtobuf.currentTarget.target
                this@toProtobuf.currentTarget.tempTargetStart?.let { tempTargetStart = (it - this@toProtobuf.timestamp).inWholeSeconds }
                this@toProtobuf.currentTarget.tempTargetDuration?.let { tempTargetDuration = it.inWholeSeconds.toInt() }
                autosensRatio = this@toProtobuf.autosensRatio

                deviceBattery = this@toProtobuf.deviceBattery.level
                isCharging = this@toProtobuf.deviceBattery.isCharging

                this@toProtobuf.lastBolus?.let {
                    lastBolusAt = (it.timestamp - this@toProtobuf.timestamp).inWholeSeconds
                    lastBolusAmount = it.amount
                }
            }
        }
    }

    data class LastBolus(
        val timestamp: Instant,
        val amount: Float
    )

    data class Cob(
        val display: Float?,
        val futureCarbs: Float,
    )

    data class Iob(
        val bolus: Float,
        val basal: Float,
    )

    data class BasalStatus(
        val baseBasal: Float,
        val tempBasalAbsolute: Float?
    )

    data class CurrentTarget(
        val target: Float,
        val tempTargetStart: Instant?,
        val tempTargetDuration: Duration?,
    )

    data class ActiveRunningMode(
        val mode: RunningMode,
        val start: Instant,
        val duration: Duration?,
    )

    data class BgConfig(
        val usesMgdl: Boolean,
        val lowBgThreshold: Float,
        val highBgThreshold: Float,
    )

    data class StatusLightElement<V, T>(
        val value: V,
        val warnThreshold: T,
        val criticalThreshold: T,
        val isMax: Boolean? = null,
    ) {

        internal companion object {

            fun StatusLightElement<Instant, Duration>.toProtobuf(baseTimestamp: Instant) = statusLightElement {
                value = (this@toProtobuf.value - baseTimestamp).inWholeSeconds
                warnThreshold = this@toProtobuf.warnThreshold.inWholeHours.toInt()
                criticalThreshold = this@toProtobuf.criticalThreshold.inWholeHours.toInt()
                if (this@toProtobuf.isMax != null) isMax = this@toProtobuf.isMax
            }

            fun StatusLightElement<Int, Int>.toProtobuf() = statusLightElement {
                this.value = this@toProtobuf.value.toLong()
                this.warnThreshold = this@toProtobuf.warnThreshold
                this.criticalThreshold = this@toProtobuf.criticalThreshold
                if (this@toProtobuf.isMax != null) isMax = this@toProtobuf.isMax
            }

            fun de.tebbeubben.remora.proto.StatusLightElement.toTimeModel(baseTimestamp: Instant) =
                StatusLightElement(
                    value = baseTimestamp + this.value.seconds,
                    warnThreshold = this.warnThreshold.hours,
                    criticalThreshold = this.criticalThreshold.hours,
                    isMax = if (this.hasIsMax()) this.isMax else null
                )

            fun de.tebbeubben.remora.proto.StatusLightElement.toIntModel() =
                StatusLightElement(
                    value = this.value.toInt(),
                    warnThreshold = this.warnThreshold,
                    criticalThreshold = this.criticalThreshold,
                    isMax = if (this.hasIsMax()) this.isMax else null
                )
        }
    }

    data class ActiveProfile(
        val name: String,
        val percentage: Int,
        val timeShift: Int,
        val start: Instant,
        val duration: Duration?
    )

    data class DeviceBattery(
        val level: Int,
        val isCharging: Boolean
    )

    data class DisplayBg(
        val timestamp: Instant,
        val value: Float,
        val smoothedValue: Float?,
        val trendArrow: TrendArrow,
        val deltas: Deltas?,
    ) {

        internal companion object {

            fun DisplayBg.toProtobuf(baseTimestamp: Instant) = displayBg {
                offset = (this@toProtobuf.timestamp - baseTimestamp).inWholeSeconds
                value = this@toProtobuf.value
                this@toProtobuf.smoothedValue?.let { smoothed = it }
                trendArrow = this@toProtobuf.trendArrow.toProtobuf()
                this@toProtobuf.deltas?.let {
                    deltas = deltas {
                        delta = it.delta
                        shortAverageDelta = it.shortAverageDelta
                        longAverageDelta = it.longAverageDelta
                    }
                }
            }

            fun de.tebbeubben.remora.proto.DisplayBg.toModel(baseTimestamp: Instant) = DisplayBg(
                timestamp = baseTimestamp + this.offset.seconds,
                value = this.value,
                smoothedValue = if (this.hasSmoothed()) this.smoothed else null,
                trendArrow = this.trendArrow.toModel(),
                deltas = this.deltasOrNull?.let { deltas ->
                    Deltas(
                        delta = deltas.delta,
                        shortAverageDelta = deltas.shortAverageDelta,
                        longAverageDelta = deltas.longAverageDelta
                    )
                }
            )
        }
    }

    data class Deltas(
        val delta: Float,
        val shortAverageDelta: Float,
        val longAverageDelta: Float,
    )

    data class TemporaryTarget(
        val timestamp: Instant,
        val id: Long,
        val reason: TemporaryTargetReason,
        val target: Float,
        val duration: Duration,
    ) {

        internal companion object {

            fun TemporaryTarget.toProtobuf(baseTimestamp: Instant): de.tebbeubben.remora.proto.TemporaryTarget =
                temporaryTarget {
                    offset = (this@toProtobuf.timestamp - baseTimestamp).inWholeSeconds.toInt()
                    id = this@toProtobuf.id
                    reason = this@toProtobuf.reason.toProtobuf()
                    target = this@toProtobuf.target
                    duration = this@toProtobuf.duration.inWholeSeconds.toInt()
                }

            fun de.tebbeubben.remora.proto.TemporaryTarget.toModel(baseTimestamp: Instant): TemporaryTarget {
                return TemporaryTarget(
                    timestamp = baseTimestamp + this.offset.seconds,
                    id = this.id,
                    reason = this.reason.toModel(),
                    target = this.target,
                    duration = this.duration.toDuration(DurationUnit.SECONDS),
                )
            }
        }
    }

    data class TargetDataPoint(
        val timestamp: Instant,
        val target: Float,
    ) {

        internal companion object {

            fun TargetDataPoint.toProtobuf(baseTimestamp: Instant): de.tebbeubben.remora.proto.TargetDataPoint =
                targetDataPoint {
                    offset = (this@toProtobuf.timestamp - baseTimestamp).inWholeSeconds.toInt()
                    target = this@toProtobuf.target
                }

            fun de.tebbeubben.remora.proto.TargetDataPoint.toModel(baseTimestamp: Instant): TargetDataPoint {
                return TargetDataPoint(
                    timestamp = baseTimestamp + this.offset.seconds,
                    target = this.target
                )
            }
        }
    }

    data class TherapyEvent(
        val timestamp: Instant,
        val id: Long,
        val duration: Duration,
        val type: TherapyEventType,
        val note: String?,
        val glucose: Float?,
        val glucoseType: MeterType?,
        val isMgdl: Boolean,
    ) {

        internal companion object {

            fun TherapyEvent.toProtobuf(baseTimestamp: Instant): de.tebbeubben.remora.proto.TherapyEvent =
                therapyEvent {
                    offset = (this@toProtobuf.timestamp - baseTimestamp).inWholeSeconds.toInt()
                    id = this@toProtobuf.id.toLong()
                    duration = this@toProtobuf.duration.inWholeSeconds.toInt()
                    type = this@toProtobuf.type.toProtobuf()
                    this@toProtobuf.note?.let { note = it }
                    this@toProtobuf.glucose?.let { glucose = it }
                    this@toProtobuf.glucoseType?.let { glucoseType = it.toProtobuf() }
                    isMgdl = this@toProtobuf.isMgdl
                }

            fun de.tebbeubben.remora.proto.TherapyEvent.toModel(baseTimestamp: Instant): TherapyEvent {
                return TherapyEvent(
                    timestamp = baseTimestamp + this.offset.seconds,
                    id = this.id,
                    duration = this.duration.toDuration(DurationUnit.SECONDS),
                    type = this.type.toModel(),
                    note = if (this.hasNote()) this.note else null,
                    glucose = if (this.hasGlucose()) this.glucose else null,
                    glucoseType = if (this.hasGlucoseType()) this.glucoseType.toModel() else null,
                    isMgdl = this.isMgdl
                )
            }
        }
    }

    data class BgData(
        val value: Float,
        val filledGap: Boolean,
    ) {

        internal companion object {

            fun BgData.toProtobuf(): de.tebbeubben.remora.proto.BgData =
                bgData {
                    value = this@toProtobuf.value
                    filledGap = this@toProtobuf.filledGap
                }

            fun de.tebbeubben.remora.proto.BgData.toModel(): BgData {
                return BgData(
                    value = this.value,
                    filledGap = this.filledGap
                )
            }
        }
    }

    data class InsulinData(
        val iob: Float,
        val absoluteIob: Float,
        val insulinActivity: Float,
    ) {

        internal companion object {

            fun InsulinData.toProtobuf(): de.tebbeubben.remora.proto.InsulinData =
                insulinData {
                    iob = this@toProtobuf.iob
                    absoluteIob = this@toProtobuf.absoluteIob
                    insulinActivity = this@toProtobuf.insulinActivity
                }

            fun de.tebbeubben.remora.proto.InsulinData.toModel(): InsulinData {
                return InsulinData(
                    iob = this.iob,
                    absoluteIob = this.absoluteIob,
                    insulinActivity = this.insulinActivity
                )
            }
        }
    }

    data class AutosensData(
        val ratio: Float,
        val cob: Float,
        val carbsFromBolus: Float,
        val bgi: Float,
        val deviation: Float,
        val type: AutosensType,
    ) {

        internal companion object {

            fun AutosensData.toProtobuf(): de.tebbeubben.remora.proto.AutosensData =
                autosensData {
                    ratio = this@toProtobuf.ratio
                    cob = this@toProtobuf.cob
                    carbsFromBolus = this@toProtobuf.carbsFromBolus
                    bgi = this@toProtobuf.bgi
                    deviation = this@toProtobuf.deviation
                    type = this@toProtobuf.type.toProtobuf()
                }

            fun de.tebbeubben.remora.proto.AutosensData.toModel(): AutosensData {
                return AutosensData(
                    ratio = this.ratio,
                    cob = this.cob,
                    carbsFromBolus = this.carbsFromBolus,
                    bgi = this.bgi,
                    deviation = this.deviation,
                    type = this.type.toModel()
                )
            }
        }
    }

    data class BucketedDataPoint(
        val timestamp: Instant,
        val bgData: BgData?,
        val insulinData: InsulinData?,
        val autosensData: AutosensData?,
    ) {

        internal companion object {

            fun List<BucketedDataPoint>.toProtobuf(baseTimestamp: Instant) =
                sortedBy { it.timestamp }
                    .mapIndexed { i, it ->
                        if (i != 0 && (this[i - 1].timestamp + 5.minutes) == it.timestamp) {
                            it.toProtobuf(null)
                        } else {
                            it.toProtobuf((it.timestamp - baseTimestamp).inWholeSeconds.toInt())
                        }
                    }

            private fun BucketedDataPoint.toProtobuf(offset: Int?): de.tebbeubben.remora.proto.BucketedDataPoint =
                bucketedDataPoint {
                    offset?.let { this.offset = it }
                    this@toProtobuf.bgData?.let { bgData = it.toProtobuf() }
                    this@toProtobuf.insulinData?.let { insulinData = it.toProtobuf() }
                    this@toProtobuf.autosensData?.let { autosensData = it.toProtobuf() }
                }

            fun List<de.tebbeubben.remora.proto.BucketedDataPoint>.toModel(baseTimestamp: Instant): List<BucketedDataPoint> {
                var lastTimestamp: Instant? = null
                return mapIndexed { i, it ->
                    val timestamp = when {
                        it.hasOffset()        -> baseTimestamp + it.offset.seconds
                        lastTimestamp != null -> lastTimestamp + 5.minutes
                        else                  -> error("Missing initial timestamp")
                    }
                    lastTimestamp = timestamp
                    BucketedDataPoint(
                        timestamp = timestamp,
                        bgData = it.bgDataOrNull?.toModel(),
                        insulinData = it.insulinDataOrNull?.toModel(),
                        autosensData = it.autosensDataOrNull?.toModel()
                    )
                }
            }
        }
    }

    data class TemporaryBasal(
        val timestamp: Instant,
        val id: Long,
        val isAbsolute: Boolean,
        val rate: Float,
        val duration: Duration,
    ) {

        internal companion object {

            fun TemporaryBasal.toProtobuf(baseTimestamp: Instant): de.tebbeubben.remora.proto.TemporaryBasal =
                temporaryBasal {
                    offset = (this@toProtobuf.timestamp - baseTimestamp).inWholeSeconds.toInt()
                    id = this@toProtobuf.id
                    isAbsolute = this@toProtobuf.isAbsolute
                    rate = this@toProtobuf.rate
                    duration = this@toProtobuf.duration.inWholeSeconds.toInt()
                }

            fun de.tebbeubben.remora.proto.TemporaryBasal.toModel(baseTimestamp: Instant): TemporaryBasal {
                return TemporaryBasal(
                    timestamp = baseTimestamp + this.offset.seconds,
                    id = this.id,
                    isAbsolute = this.isAbsolute,
                    rate = this.rate,
                    duration = this.duration.seconds
                )
            }
        }
    }

    data class ExtendedBolus(
        val timestamp: Instant,
        val id: Long,
        val amount: Float,
        val duration: Duration,
    ) {

        internal companion object {

            fun ExtendedBolus.toProtobuf(baseTimestamp: Instant): de.tebbeubben.remora.proto.ExtendedBolus =
                extendedBolus {
                    offset = (this@toProtobuf.timestamp - baseTimestamp).inWholeSeconds.toInt()
                    id = this@toProtobuf.id
                    amount = this@toProtobuf.amount
                    duration = this@toProtobuf.duration.inWholeSeconds.toInt()
                }

            fun de.tebbeubben.remora.proto.ExtendedBolus.toModel(baseTimestamp: Instant): ExtendedBolus {
                return ExtendedBolus(
                    timestamp = baseTimestamp + this.offset.seconds,
                    id = this.id,
                    amount = this.amount,
                    duration = this.duration.seconds
                )
            }
        }
    }

    data class ProfileSwitch(
        val timestamp: Instant,
        val id: Long,
        val profileName: String,
        val timeshift: Duration,
        val percentage: Int,
        val duration: Duration,
    ) {

        internal companion object {

            fun ProfileSwitch.toProtobuf(baseTimestamp: Instant): de.tebbeubben.remora.proto.ProfileSwitch =
                profileSwitch {
                    offset = (this@toProtobuf.timestamp - baseTimestamp).inWholeSeconds.toInt()
                    id = this@toProtobuf.id
                    profileName = this@toProtobuf.profileName
                    timeshift = this@toProtobuf.timeshift.inWholeSeconds.toInt()
                    percentage = this@toProtobuf.percentage
                    duration = this@toProtobuf.duration.inWholeSeconds.toInt()
                }

            fun de.tebbeubben.remora.proto.ProfileSwitch.toModel(baseTimestamp: Instant): ProfileSwitch {
                return ProfileSwitch(
                    timestamp = baseTimestamp + this.offset.seconds,
                    id = this.id,
                    profileName = this.profileName,
                    timeshift = this.timeshift.seconds,
                    percentage = this.percentage,
                    duration = this.duration.seconds
                )
            }
        }
    }

    data class BgReading(
        val timestamp: Instant,
        val id: Long,
        val value: Float,
        val trendArrow: TrendArrow?,
    ) {

        internal companion object {

            fun BgReading.toProtobuf(baseTimestamp: Instant): de.tebbeubben.remora.proto.BgReading =
                bgReading {
                    offset = (this@toProtobuf.timestamp - baseTimestamp).inWholeSeconds.toInt()
                    id = this@toProtobuf.id
                    value = this@toProtobuf.value
                    this@toProtobuf.trendArrow?.let { trendArrow = it.toProtobuf() }
                }

            fun de.tebbeubben.remora.proto.BgReading.toModel(baseTimestamp: Instant): BgReading {
                return BgReading(
                    timestamp = baseTimestamp + this.offset.seconds,
                    id = this.id,
                    value = this.value,
                    trendArrow = if (this.hasTrendArrow()) this.trendArrow.toModel() else null
                )
            }
        }
    }

    data class CarbEntry(
        val timestamp: Instant,
        val id: Long,
        val amount: Float,
        val duration: Duration,
    ) {

        internal companion object {

            fun CarbEntry.toProtobuf(baseTimestamp: Instant): de.tebbeubben.remora.proto.CarbEntry =
                carbEntry {
                    offset = (this@toProtobuf.timestamp - baseTimestamp).inWholeSeconds.toInt()
                    id = this@toProtobuf.id
                    amount = this@toProtobuf.amount
                    duration = this@toProtobuf.duration.inWholeSeconds.toInt()
                }

            fun de.tebbeubben.remora.proto.CarbEntry.toModel(baseTimestamp: Instant): CarbEntry {
                return CarbEntry(
                    timestamp = baseTimestamp + this.offset.seconds,
                    id = this.id,
                    amount = this.amount,
                    duration = this.duration.seconds
                )
            }
        }
    }

    data class Bolus(
        val timestamp: Instant,
        val id: Long,
        val type: BolusType,
        val amount: Float,
    ) {

        internal companion object {

            fun Bolus.toProtobuf(baseTimestamp: Instant): de.tebbeubben.remora.proto.Bolus =
                bolus {
                    offset = (this@toProtobuf.timestamp - baseTimestamp).inWholeSeconds.toInt()
                    id = this@toProtobuf.id
                    type = this@toProtobuf.type.toProtobuf()
                    amount = this@toProtobuf.amount
                }

            fun de.tebbeubben.remora.proto.Bolus.toModel(baseTimestamp: Instant): Bolus {
                return Bolus(
                    timestamp = baseTimestamp + this.offset.seconds,
                    id = this.id,
                    type = this.type.toModel(),
                    amount = this.amount
                )
            }
        }
    }

    data class Prediction(
        val timestamp: Instant,
        val type: PredictionType,
        val value: Float,
    ) {

        internal companion object {

            fun Prediction.toProtobuf(baseTimestamp: Instant): de.tebbeubben.remora.proto.Prediction =
                prediction {
                    offset = (this@toProtobuf.timestamp - baseTimestamp).inWholeSeconds.toInt()
                    type = this@toProtobuf.type.toProtobuf()
                    value = this@toProtobuf.value
                }

            fun de.tebbeubben.remora.proto.Prediction.toModel(baseTimestamp: Instant): Prediction {
                return Prediction(
                    timestamp = baseTimestamp + this.offset.seconds,
                    type = this.type.toModel(),
                    value = this.value
                )
            }
        }
    }

    data class BasalDataPoint(
        val timestamp: Instant,
        val baselineBasal: Float,
        val tempBasalAbsolute: Float?,
    ) {

        internal companion object {

            fun BasalDataPoint.toProtobuf(baseTimestamp: Instant): de.tebbeubben.remora.proto.BasalDataPoint =
                basalDataPoint {
                    offset = (this@toProtobuf.timestamp - baseTimestamp).inWholeSeconds.toInt()
                    baselineBasal = this@toProtobuf.baselineBasal
                    this@toProtobuf.tempBasalAbsolute?.let { tempBasalAbsolute = it }
                }

            fun de.tebbeubben.remora.proto.BasalDataPoint.toModel(baseTimestamp: Instant): BasalDataPoint {
                return BasalDataPoint(
                    timestamp = baseTimestamp + this.offset.seconds,
                    baselineBasal = this.baselineBasal,
                    tempBasalAbsolute = if (this.hasTempBasalAbsolute()) this.tempBasalAbsolute else null
                )
            }
        }
    }

    data class RunningModeDataPoint(
        val timestamp: Instant,
        val runningMode: RunningMode,
    ) {

        internal companion object {

            fun RunningModeDataPoint.toProtobuf(baseTimestamp: Instant) =
                runningModeDataPoint {
                    offset = (this@toProtobuf.timestamp - baseTimestamp).inWholeSeconds.toInt()
                    runningMode = this@toProtobuf.runningMode.toProtobuf()
                }

            fun de.tebbeubben.remora.proto.RunningModeDataPoint.toModel(baseTimestamp: Instant) =
                RunningModeDataPoint(
                    timestamp = baseTimestamp + this.offset.seconds,
                    runningMode = this.runningMode.toModel()
                )
        }
    }

    enum class RunningMode {
        OPEN_LOOP,
        CLOSED_LOOP,
        CLOSED_LOOP_LGS,
        DISABLED_LOOP,
        SUPER_BOLUS,
        DISCONNECTED_PUMP,
        SUSPENDED_BY_PUMP,
        SUSPENDED_BY_USER;

        internal companion object {

            fun RunningMode.toProtobuf() = when (this) {
                OPEN_LOOP         -> de.tebbeubben.remora.proto.RunningMode.MODE_OPEN_LOOP
                CLOSED_LOOP       -> de.tebbeubben.remora.proto.RunningMode.MODE_CLOSED_LOOP
                CLOSED_LOOP_LGS   -> de.tebbeubben.remora.proto.RunningMode.MODE_CLOSED_LOOP_LGS
                DISABLED_LOOP     -> de.tebbeubben.remora.proto.RunningMode.MODE_DISABLED_LOOP
                SUPER_BOLUS       -> de.tebbeubben.remora.proto.RunningMode.MODE_SUPER_BOLUS
                DISCONNECTED_PUMP -> de.tebbeubben.remora.proto.RunningMode.MODE_DISCONNECTED_PUMP
                SUSPENDED_BY_PUMP -> de.tebbeubben.remora.proto.RunningMode.MODE_SUSPENDED_BY_PUMP
                SUSPENDED_BY_USER -> de.tebbeubben.remora.proto.RunningMode.MODE_SUSPENDED_BY_USER
            }

            fun de.tebbeubben.remora.proto.RunningMode.toModel() = when (this) {
                de.tebbeubben.remora.proto.RunningMode.MODE_OPEN_LOOP         -> OPEN_LOOP
                de.tebbeubben.remora.proto.RunningMode.MODE_CLOSED_LOOP       -> CLOSED_LOOP
                de.tebbeubben.remora.proto.RunningMode.MODE_CLOSED_LOOP_LGS   -> CLOSED_LOOP_LGS
                de.tebbeubben.remora.proto.RunningMode.MODE_DISABLED_LOOP     -> DISABLED_LOOP
                de.tebbeubben.remora.proto.RunningMode.MODE_SUPER_BOLUS       -> SUPER_BOLUS
                de.tebbeubben.remora.proto.RunningMode.MODE_DISCONNECTED_PUMP -> DISCONNECTED_PUMP
                de.tebbeubben.remora.proto.RunningMode.MODE_SUSPENDED_BY_PUMP -> SUSPENDED_BY_PUMP
                de.tebbeubben.remora.proto.RunningMode.MODE_SUSPENDED_BY_USER -> SUSPENDED_BY_USER
                de.tebbeubben.remora.proto.RunningMode.UNRECOGNIZED           -> error("Unrecognized RunningMode")
            }
        }
    }

    enum class TrendArrow {
        NONE,
        TRIPLE_UP,
        DOUBLE_UP,
        SINGLE_UP,
        FORTY_FIVE_UP,
        FLAT,
        FORTY_FIVE_DOWN,
        SINGLE_DOWN,
        DOUBLE_DOWN,
        TRIPLE_DOWN;

        internal companion object {

            fun TrendArrow.toProtobuf() = when (this) {
                NONE            -> de.tebbeubben.remora.proto.TrendArrow.TREND_NONE
                TRIPLE_UP       -> de.tebbeubben.remora.proto.TrendArrow.TREND_TRIPLE_UP
                DOUBLE_UP       -> de.tebbeubben.remora.proto.TrendArrow.TREND_DOUBLE_UP
                SINGLE_UP       -> de.tebbeubben.remora.proto.TrendArrow.TREND_SINGLE_UP
                FORTY_FIVE_UP   -> de.tebbeubben.remora.proto.TrendArrow.TREND_FORTY_FIVE_UP
                FLAT            -> de.tebbeubben.remora.proto.TrendArrow.TREND_FLAT
                FORTY_FIVE_DOWN -> de.tebbeubben.remora.proto.TrendArrow.TREND_FORTY_FIVE_DOWN
                SINGLE_DOWN     -> de.tebbeubben.remora.proto.TrendArrow.TREND_SINGLE_DOWN
                DOUBLE_DOWN     -> de.tebbeubben.remora.proto.TrendArrow.TREND_DOUBLE_DOWN
                TRIPLE_DOWN     -> de.tebbeubben.remora.proto.TrendArrow.TREND_TRIPLE_DOWN
            }

            fun de.tebbeubben.remora.proto.TrendArrow.toModel() = when (this) {
                de.tebbeubben.remora.proto.TrendArrow.TREND_NONE            -> NONE
                de.tebbeubben.remora.proto.TrendArrow.TREND_TRIPLE_UP       -> TRIPLE_UP
                de.tebbeubben.remora.proto.TrendArrow.TREND_DOUBLE_UP       -> DOUBLE_UP
                de.tebbeubben.remora.proto.TrendArrow.TREND_SINGLE_UP       -> SINGLE_UP
                de.tebbeubben.remora.proto.TrendArrow.TREND_FORTY_FIVE_UP   -> FORTY_FIVE_UP
                de.tebbeubben.remora.proto.TrendArrow.TREND_FLAT            -> FLAT
                de.tebbeubben.remora.proto.TrendArrow.TREND_FORTY_FIVE_DOWN -> FORTY_FIVE_DOWN
                de.tebbeubben.remora.proto.TrendArrow.TREND_SINGLE_DOWN     -> SINGLE_DOWN
                de.tebbeubben.remora.proto.TrendArrow.TREND_DOUBLE_DOWN     -> DOUBLE_DOWN
                de.tebbeubben.remora.proto.TrendArrow.TREND_TRIPLE_DOWN     -> TRIPLE_DOWN
                de.tebbeubben.remora.proto.TrendArrow.UNRECOGNIZED          -> error("Unrecognized TrendArrow")
            }
        }
    }

    enum class TemporaryTargetReason {
        CUSTOM,
        HYPOGLYCEMIA,
        ACTIVITY,
        EATING_SOON,
        AUTOMATION,
        WEAR;

        internal companion object {

            fun TemporaryTargetReason.toProtobuf(): de.tebbeubben.remora.proto.TemporaryTargetReason = when (this) {
                CUSTOM       -> de.tebbeubben.remora.proto.TemporaryTargetReason.REASON_CUSTOM
                HYPOGLYCEMIA -> de.tebbeubben.remora.proto.TemporaryTargetReason.REASON_HYPOGLYCEMIA
                ACTIVITY     -> de.tebbeubben.remora.proto.TemporaryTargetReason.REASON_ACTIVITY
                EATING_SOON  -> de.tebbeubben.remora.proto.TemporaryTargetReason.REASON_EATING_SOON
                AUTOMATION   -> de.tebbeubben.remora.proto.TemporaryTargetReason.REASON_AUTOMATION
                WEAR         -> de.tebbeubben.remora.proto.TemporaryTargetReason.REASON_WEAR
            }

            fun de.tebbeubben.remora.proto.TemporaryTargetReason.toModel(): TemporaryTargetReason = when (this) {
                de.tebbeubben.remora.proto.TemporaryTargetReason.REASON_CUSTOM       -> CUSTOM
                de.tebbeubben.remora.proto.TemporaryTargetReason.REASON_HYPOGLYCEMIA -> HYPOGLYCEMIA
                de.tebbeubben.remora.proto.TemporaryTargetReason.REASON_ACTIVITY     -> ACTIVITY
                de.tebbeubben.remora.proto.TemporaryTargetReason.REASON_EATING_SOON  -> EATING_SOON
                de.tebbeubben.remora.proto.TemporaryTargetReason.REASON_AUTOMATION   -> AUTOMATION
                de.tebbeubben.remora.proto.TemporaryTargetReason.REASON_WEAR         -> WEAR
                de.tebbeubben.remora.proto.TemporaryTargetReason.UNRECOGNIZED        -> error("Unrecognized TemporaryTargetReason")
            }
        }
    }

    enum class MeterType {
        FINGER,
        SENSOR,
        MANUAL;

        internal companion object {

            fun MeterType.toProtobuf(): de.tebbeubben.remora.proto.MeterType = when (this) {
                FINGER -> de.tebbeubben.remora.proto.MeterType.METER_FINGER
                SENSOR -> de.tebbeubben.remora.proto.MeterType.METER_SENSOR
                MANUAL -> de.tebbeubben.remora.proto.MeterType.METER_MANUAL
            }

            fun de.tebbeubben.remora.proto.MeterType.toModel(): MeterType = when (this) {
                de.tebbeubben.remora.proto.MeterType.METER_FINGER -> FINGER
                de.tebbeubben.remora.proto.MeterType.METER_SENSOR -> SENSOR
                de.tebbeubben.remora.proto.MeterType.METER_MANUAL -> MANUAL
                de.tebbeubben.remora.proto.MeterType.UNRECOGNIZED -> error("Unrecognized MeterType")
            }
        }
    }

    enum class TherapyEventType {
        NONE,
        CANNULA_CHANGE,
        INSULIN_CHANGE,
        PUMP_BATTERY_CHANGE,
        SENSOR_CHANGE,
        SENSOR_STARTED,
        SENSOR_STOPPED,
        FINGER_STICK_BG_VALUE,
        EXERCISE,
        ANNOUNCEMENT,
        QUESTION,
        NOTE,
        APS_OFFLINE,
        DAD_ALERT,
        NS_MBG,
        CARBS_CORRECTION,
        BOLUS_WIZARD,
        CORRECTION_BOLUS,
        MEAL_BOLUS,
        COMBO_BOLUS,
        TEMPORARY_TARGET,
        TEMPORARY_TARGET_CANCEL,
        PROFILE_SWITCH,
        SNACK_BOLUS,
        TEMPORARY_BASAL,
        TEMPORARY_BASAL_START,
        TEMPORARY_BASAL_END,
        TUBE_CHANGE,
        FALLING_ASLEEP,
        BATTERY_EMPTY,
        RESERVOIR_EMPTY,
        OCCLUSION,
        PUMP_STOPPED,
        PUMP_STARTED,
        PUMP_PAUSED,
        SETTINGS_EXPORT,
        WAKING_UP,
        SICKNESS,
        STRESS,
        PRE_PERIOD,
        ALCOHOL,
        CORTISONE,
        FEELING_LOW,
        FEELING_HIGH,
        LEAKING_INFUSION_SET;

        internal companion object {

            fun TherapyEventType.toProtobuf(): de.tebbeubben.remora.proto.TherapyEventType = when (this) {
                NONE                    -> de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_NONE
                CANNULA_CHANGE          -> de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_CANNULA_CHANGE
                INSULIN_CHANGE          -> de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_INSULIN_CHANGE
                PUMP_BATTERY_CHANGE     -> de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_PUMP_BATTERY_CHANGE
                SENSOR_CHANGE           -> de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_SENSOR_CHANGE
                SENSOR_STARTED          -> de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_SENSOR_STARTED
                SENSOR_STOPPED          -> de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_SENSOR_STOPPED
                FINGER_STICK_BG_VALUE   -> de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_FINGER_STICK_BG_VALUE
                EXERCISE                -> de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_EXERCISE
                ANNOUNCEMENT            -> de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_ANNOUNCEMENT
                QUESTION                -> de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_QUESTION
                NOTE                    -> de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_NOTE
                APS_OFFLINE             -> de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_APS_OFFLINE
                DAD_ALERT               -> de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_DAD_ALERT
                NS_MBG                  -> de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_NS_MBG
                CARBS_CORRECTION        -> de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_CARBS_CORRECTION
                BOLUS_WIZARD            -> de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_BOLUS_WIZARD
                CORRECTION_BOLUS        -> de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_CORRECTION_BOLUS
                MEAL_BOLUS              -> de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_MEAL_BOLUS
                COMBO_BOLUS             -> de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_COMBO_BOLUS
                TEMPORARY_TARGET        -> de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_TEMPORARY_TARGET
                TEMPORARY_TARGET_CANCEL -> de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_TEMPORARY_TARGET_CANCEL
                PROFILE_SWITCH          -> de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_PROFILE_SWITCH
                SNACK_BOLUS             -> de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_SNACK_BOLUS
                TEMPORARY_BASAL         -> de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_TEMPORARY_BASAL
                TEMPORARY_BASAL_START   -> de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_TEMPORARY_BASAL_START
                TEMPORARY_BASAL_END     -> de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_TEMPORARY_BASAL_END
                TUBE_CHANGE             -> de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_TUBE_CHANGE
                FALLING_ASLEEP          -> de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_FALLING_ASLEEP
                BATTERY_EMPTY           -> de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_BATTERY_EMPTY
                RESERVOIR_EMPTY         -> de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_RESERVOIR_EMPTY
                OCCLUSION               -> de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_OCCLUSION
                PUMP_STOPPED            -> de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_PUMP_STOPPED
                PUMP_STARTED            -> de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_PUMP_STARTED
                PUMP_PAUSED             -> de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_PUMP_PAUSED
                SETTINGS_EXPORT         -> de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_SETTINGS_EXPORT
                WAKING_UP               -> de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_WAKING_UP
                SICKNESS                -> de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_SICKNESS
                STRESS                  -> de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_STRESS
                PRE_PERIOD              -> de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_PRE_PERIOD
                ALCOHOL                 -> de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_ALCOHOL
                CORTISONE               -> de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_CORTISONE
                FEELING_LOW             -> de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_FEELING_LOW
                FEELING_HIGH            -> de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_FEELING_HIGH
                LEAKING_INFUSION_SET    -> de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_LEAKING_INFUSION_SET
            }

            fun de.tebbeubben.remora.proto.TherapyEventType.toModel(): TherapyEventType = when (this) {
                de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_NONE                    -> NONE
                de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_CANNULA_CHANGE          -> CANNULA_CHANGE
                de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_INSULIN_CHANGE          -> INSULIN_CHANGE
                de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_PUMP_BATTERY_CHANGE     -> PUMP_BATTERY_CHANGE
                de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_SENSOR_CHANGE           -> SENSOR_CHANGE
                de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_SENSOR_STARTED          -> SENSOR_STARTED
                de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_SENSOR_STOPPED          -> SENSOR_STOPPED
                de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_FINGER_STICK_BG_VALUE   -> FINGER_STICK_BG_VALUE
                de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_EXERCISE                -> EXERCISE
                de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_ANNOUNCEMENT            -> ANNOUNCEMENT
                de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_QUESTION                -> QUESTION
                de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_NOTE                    -> NOTE
                de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_APS_OFFLINE             -> APS_OFFLINE
                de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_DAD_ALERT               -> DAD_ALERT
                de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_NS_MBG                  -> NS_MBG
                de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_CARBS_CORRECTION        -> CARBS_CORRECTION
                de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_BOLUS_WIZARD            -> BOLUS_WIZARD
                de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_CORRECTION_BOLUS        -> CORRECTION_BOLUS
                de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_MEAL_BOLUS              -> MEAL_BOLUS
                de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_COMBO_BOLUS             -> COMBO_BOLUS
                de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_TEMPORARY_TARGET        -> TEMPORARY_TARGET
                de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_TEMPORARY_TARGET_CANCEL -> TEMPORARY_TARGET_CANCEL
                de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_PROFILE_SWITCH          -> PROFILE_SWITCH
                de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_SNACK_BOLUS             -> SNACK_BOLUS
                de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_TEMPORARY_BASAL         -> TEMPORARY_BASAL
                de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_TEMPORARY_BASAL_START   -> TEMPORARY_BASAL_START
                de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_TEMPORARY_BASAL_END     -> TEMPORARY_BASAL_END
                de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_TUBE_CHANGE             -> TUBE_CHANGE
                de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_FALLING_ASLEEP          -> FALLING_ASLEEP
                de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_BATTERY_EMPTY           -> BATTERY_EMPTY
                de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_RESERVOIR_EMPTY         -> RESERVOIR_EMPTY
                de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_OCCLUSION               -> OCCLUSION
                de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_PUMP_STOPPED            -> PUMP_STOPPED
                de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_PUMP_STARTED            -> PUMP_STARTED
                de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_PUMP_PAUSED             -> PUMP_PAUSED
                de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_SETTINGS_EXPORT         -> SETTINGS_EXPORT
                de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_WAKING_UP               -> WAKING_UP
                de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_SICKNESS                -> SICKNESS
                de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_STRESS                  -> STRESS
                de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_PRE_PERIOD              -> PRE_PERIOD
                de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_ALCOHOL                 -> ALCOHOL
                de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_CORTISONE               -> CORTISONE
                de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_FEELING_LOW             -> FEELING_LOW
                de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_FEELING_HIGH            -> FEELING_HIGH
                de.tebbeubben.remora.proto.TherapyEventType.THERAPY_EVENT_LEAKING_INFUSION_SET    -> LEAKING_INFUSION_SET
                de.tebbeubben.remora.proto.TherapyEventType.UNRECOGNIZED                          -> error("Unrecognized TherapyEventType")
            }
        }
    }

    enum class BolusType {
        NORMAL,
        SMB,
        PRIMING;

        internal companion object {

            fun BolusType.toProtobuf(): de.tebbeubben.remora.proto.BolusType = when (this) {
                NORMAL  -> de.tebbeubben.remora.proto.BolusType.BOLUS_NORMAL
                SMB     -> de.tebbeubben.remora.proto.BolusType.BOLUS_SMB
                PRIMING -> de.tebbeubben.remora.proto.BolusType.BOLUS_PRIMING
            }

            fun de.tebbeubben.remora.proto.BolusType.toModel(): BolusType = when (this) {
                de.tebbeubben.remora.proto.BolusType.BOLUS_NORMAL  -> NORMAL
                de.tebbeubben.remora.proto.BolusType.BOLUS_SMB     -> SMB
                de.tebbeubben.remora.proto.BolusType.BOLUS_PRIMING -> PRIMING
                de.tebbeubben.remora.proto.BolusType.UNRECOGNIZED  -> error("Unrecognized BolusType")
            }
        }
    }

    enum class PredictionType {
        IOB,
        COB,
        A_COB,
        UAM,
        ZT;

        internal companion object {

            fun PredictionType.toProtobuf(): de.tebbeubben.remora.proto.PredictionType = when (this) {
                IOB   -> de.tebbeubben.remora.proto.PredictionType.PREDICTION_IOB
                COB   -> de.tebbeubben.remora.proto.PredictionType.PREDICTION_COB
                A_COB -> de.tebbeubben.remora.proto.PredictionType.PREDICTION_A_COB
                UAM   -> de.tebbeubben.remora.proto.PredictionType.PREDICTION_UAM
                ZT    -> de.tebbeubben.remora.proto.PredictionType.PREDICTION_ZT
            }

            fun de.tebbeubben.remora.proto.PredictionType.toModel(): PredictionType = when (this) {
                de.tebbeubben.remora.proto.PredictionType.PREDICTION_IOB   -> IOB
                de.tebbeubben.remora.proto.PredictionType.PREDICTION_COB   -> COB
                de.tebbeubben.remora.proto.PredictionType.PREDICTION_A_COB -> A_COB
                de.tebbeubben.remora.proto.PredictionType.PREDICTION_UAM   -> UAM
                de.tebbeubben.remora.proto.PredictionType.PREDICTION_ZT    -> ZT
                de.tebbeubben.remora.proto.PredictionType.UNRECOGNIZED     -> error("Unrecognized PredictionType")
            }
        }
    }

    enum class AutosensType {
        NEUTRAL,
        POSITIVE,
        NEGATIVE,
        UAM,
        CSF;

        internal companion object {

            fun AutosensType.toProtobuf(): de.tebbeubben.remora.proto.AutosensType = when (this) {
                NEUTRAL  -> de.tebbeubben.remora.proto.AutosensType.TYPE_NEUTRAL
                POSITIVE -> de.tebbeubben.remora.proto.AutosensType.TYPE_POSITIVE
                NEGATIVE -> de.tebbeubben.remora.proto.AutosensType.TYPE_NEGATIVE
                UAM      -> de.tebbeubben.remora.proto.AutosensType.TYPE_UAM
                CSF      -> de.tebbeubben.remora.proto.AutosensType.TYPE_CSF
            }

            fun de.tebbeubben.remora.proto.AutosensType.toModel(): AutosensType = when (this) {
                de.tebbeubben.remora.proto.AutosensType.TYPE_NEUTRAL  -> NEUTRAL
                de.tebbeubben.remora.proto.AutosensType.TYPE_POSITIVE -> POSITIVE
                de.tebbeubben.remora.proto.AutosensType.TYPE_NEGATIVE -> NEGATIVE
                de.tebbeubben.remora.proto.AutosensType.TYPE_UAM      -> UAM
                de.tebbeubben.remora.proto.AutosensType.TYPE_CSF      -> CSF
                de.tebbeubben.remora.proto.AutosensType.UNRECOGNIZED  -> error("Unrecognized AutosensType")
            }
        }
    }
}