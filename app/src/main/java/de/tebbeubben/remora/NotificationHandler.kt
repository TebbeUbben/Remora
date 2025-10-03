package de.tebbeubben.remora

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.SpannableStringBuilder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import de.tebbeubben.remora.lib.RemoraLib
import de.tebbeubben.remora.lib.model.commands.RemoraCommand
import de.tebbeubben.remora.lib.model.commands.RemoraCommandError
import de.tebbeubben.remora.util.CommandSummarizer
import de.tebbeubben.remora.util.formatBG
import de.tebbeubben.remora.util.formatCarbs
import de.tebbeubben.remora.util.formatInsulin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

@Singleton
class NotificationHandler @Inject constructor(
    private val remoraLib: RemoraLib,
    @param:ApplicationContext
    private val context: Context,
    private val commandSummarizer: CommandSummarizer,
) : CoroutineScope by CoroutineScope(Dispatchers.Main) {

    private val notificationManager by lazy { NotificationManagerCompat.from(context) }
    private val alarmManager by lazy { context.getSystemService(AlarmManager::class.java) }

    private val sharedPreferences by lazy { context.getSharedPreferences("notification_handler", Context.MODE_PRIVATE) }

    val commandDialogActive = MutableStateFlow(false)

    companion object {

        const val STATUS_CHANNEL_ID = "remora_status"
        const val STATUS_NOTIFICATION_ID = 51235

        const val COMMAND_CHANNEL_ID = "remora_command"
        const val COMMAND_NOTIFICATION_ID = 51236
        const val PROGRESS_TIMEOUT_NOTIFICATION_ID = 51237
    }

    fun start() {
        showStatusNotification()
        showCommandNotification()
    }

    fun showCommandNotification() {
        val channel = NotificationChannel(COMMAND_CHANNEL_ID, context.getString(R.string.commands), NotificationManager.IMPORTANCE_HIGH).apply {
            description = context.getString(R.string.shows_the_current_progress_of_any_active_remote_commands)
            setShowBadge(true)
        }

        notificationManager.createNotificationChannel(channel)

        launch {
            remoraLib.commandStateFlow
                .flatMapLatest { state ->
                    var isFirst = true
                    commandDialogActive.map { active ->
                        // To correctly flag a state object as old when only the ui active state changes
                        val combined = state.copy(isCached = state.isCached || !isFirst) to active
                        isFirst = false
                        combined
                    }
                }
                .collectLatest {
                    val usesMgdl = remoraLib.passiveStatusFlow.first().short?.data?.bgConfig?.usesMgdl ?: true
                    updateTimer(it.first.command)
                    showCommand(it.first.command, !it.first.isCached, it.second, usesMgdl)
                }
        }
    }

    private fun getTimeoutPendingIntent() =
        PendingIntent.getBroadcast(context, 0, Intent(context, CommandTimeoutReceiver::class.java), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT)

    fun onTimeout() {
        if (commandDialogActive.value) return
        val command = runBlocking { remoraLib.commandStateFlow.first().command }
        when (command) {
            is RemoraCommand.Initial, is RemoraCommand.Prepared -> {
                val text = context.getString(R.string.it_seems_like_androidaps_is_not_responding_tap_to_open_app_and_try_again)
                val notification = NotificationCompat.Builder(context, COMMAND_CHANNEL_ID)
                    .setSmallIcon(R.drawable.remora_logo)
                    .setCategory(NotificationCompat.CATEGORY_ERROR)
                    .setContentTitle(context.getString(R.string.timeout_for_command))
                    .setContentText(text)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(text))
                    .setContentIntent(commandDialogPendingIntent())
                    .setAutoCancel(true)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .build()

                @SuppressLint("MissingPermission")
                notificationManager.notify(COMMAND_NOTIFICATION_ID, notification)
            }

            is RemoraCommand.Progressing                        -> {
                val text = context.getString(R.string.please_check_system_status)
                val notification = NotificationCompat.Builder(context, COMMAND_CHANNEL_ID)
                    .setSmallIcon(R.drawable.remora_logo)
                    .setCategory(NotificationCompat.CATEGORY_ERROR)
                    .setContentTitle(context.getString(R.string.not_receiving_progress_reports))
                    .setContentText(text)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(text))
                    .setContentIntent(commandDialogPendingIntent())
                    .setAutoCancel(true)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .build()

                @SuppressLint("MissingPermission")
                notificationManager.notify(PROGRESS_TIMEOUT_NOTIFICATION_ID, notification)
            }

            else                                                -> {}
        }
    }

    private fun setTimeoutAlarm(at: Instant) {
        if (at <= Clock.System.now()) return
        val triggerAtMillis = at.toEpochMilliseconds()
        @SuppressLint("MissingPermission")
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, getTimeoutPendingIntent())
    }

    private fun updateTimer(command: RemoraCommand?) {
        alarmManager.cancel(getTimeoutPendingIntent())
        when (command) {
            is RemoraCommand.Initial if command.lastAttempt != null  -> setTimeoutAlarm(command.lastAttempt!! + COMMAND_TIMEOUT)
            is RemoraCommand.Prepared if command.lastAttempt != null -> setTimeoutAlarm(command.lastAttempt!! + COMMAND_TIMEOUT)
            is RemoraCommand.Progressing                             -> setTimeoutAlarm(command.receivedAt + COMMAND_TIMEOUT)
            else                                                     -> Unit
        }
    }

    private fun cancelCommandNotification() {
        notificationManager.cancel(COMMAND_NOTIFICATION_ID)
        sharedPreferences.edit { putString("progress_stage", null) }
    }

    private fun showProgressNotification(
        stage: String,
        silent: Boolean,
        title: CharSequence,
        text: CharSequence? = null,
        progress: Int? = null,
        shortCriticalText: String?,
        countdownStart: Instant?,
    ) {
        if (progress == null && sharedPreferences.getString("progress_stage", null) == stage) return
        sharedPreferences.edit { putString("progress_stage", stage) }
        val notification = NotificationCompat.Builder(context, COMMAND_CHANNEL_ID)
            .setSmallIcon(R.drawable.remora_logo)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setContentTitle(title)
            .setContentText(text)
            .setSilent(silent)
            .setOnlyAlertOnce(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setRequestPromotedOngoing(true)
            .setContentIntent(commandDialogPendingIntent())
            .setProgress(100, progress ?: 0, progress == null)
            .setStyle(
                NotificationCompat.ProgressStyle()
                    .setProgressSegments(listOf(NotificationCompat.ProgressStyle.Segment(100)))
                    .setProgress(progress ?: 0)
                    .setProgressIndeterminate(progress == null)
            )
            .setShortCriticalText(shortCriticalText)
            .apply {
                if (countdownStart != null) {
                    setWhen(countdownStart.toEpochMilliseconds())
                    setUsesChronometer(true)
                }
            }
            .build()

        @SuppressLint("MissingPermission")
        notificationManager.notify(COMMAND_NOTIFICATION_ID, notification)
    }

    private fun showCommand(command: RemoraCommand?, isNew: Boolean, uiVisible: Boolean, usesMgdl: Boolean) {
        if (isNew || uiVisible) notificationManager.cancel(PROGRESS_TIMEOUT_NOTIFICATION_ID)
        when (command) {
            is RemoraCommand.Initial     -> {
                val lastAttempt = command.lastAttempt
                if (lastAttempt == null || Clock.System.now() - lastAttempt >= COMMAND_TIMEOUT) {
                    cancelCommandNotification()
                } else {
                    showProgressNotification(
                        stage = "preparing",
                        silent = uiVisible,
                        title = context.getString(R.string.validating_command),
                        text = commandSummarizer.spanned(usesMgdl, command.originalData, null),
                        progress = null,
                        shortCriticalText = null,
                        countdownStart = lastAttempt
                    )
                }
            }

            is RemoraCommand.Prepared    -> {
                val lastAttempt = command.lastAttempt
                if (lastAttempt == null) {
                    if (!isNew || uiVisible) {
                        cancelCommandNotification()
                        return
                    }
                    val spanned = SpannableStringBuilder()
                        .append(context.getString(R.string.tap_to_confirm_or_cancel_in_the_app))
                        .append(commandSummarizer.spanned(usesMgdl, command.constrainedData, command.originalData))
                    val notification = NotificationCompat.Builder(context, COMMAND_CHANNEL_ID)
                        .setSmallIcon(R.drawable.remora_logo)
                        .setCategory(NotificationCompat.CATEGORY_STATUS)
                        .setContentTitle(context.getString(R.string.awaiting_confirmation))
                        .setContentText(spanned)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setStyle(NotificationCompat.BigTextStyle().bigText(spanned))
                        .setContentIntent(commandDialogPendingIntent())
                        .setAutoCancel(true)
                        .build()

                    @SuppressLint("MissingPermission")
                    notificationManager.notify(COMMAND_NOTIFICATION_ID, notification)
                } else if (Clock.System.now() - lastAttempt < COMMAND_TIMEOUT) {
                    showProgressNotification(
                        stage = "confirming",
                        silent = uiVisible,
                        title = context.getString(R.string.confirming_command),
                        text = commandSummarizer.spanned(usesMgdl, command.constrainedData, null),
                        progress = null,
                        shortCriticalText = null,
                        countdownStart = lastAttempt
                    )
                } else {
                    cancelCommandNotification()
                }
            }

            is RemoraCommand.Rejected    -> {
                if (uiVisible) {
                    cancelCommandNotification()
                } else if (isNew) {
                    showCommandFailedNotification(command.error)
                }
            }

            is RemoraCommand.Progressing -> {
                when (val progress = command.progress) {
                    is RemoraCommand.Progress.Connecting -> showProgressNotification(
                        stage = "connecting",
                        silent = uiVisible,
                        title = context.getString(R.string.connecting_to_pump),
                        text = null,
                        progress = null,
                        shortCriticalText = null,
                        countdownStart = command.receivedAt - progress.elapsedSeconds.seconds
                    )

                    RemoraCommand.Progress.Enqueued      -> showProgressNotification(
                        stage = "enqueued",
                        silent = uiVisible,
                        title = context.getString(R.string.command_is_waiting_in_queue),
                        text = null,
                        progress = null,
                        shortCriticalText = "⏳",
                        countdownStart = null
                    )

                    is RemoraCommand.Progress.Percentage -> showProgressNotification(
                        stage = "percentage",
                        silent = uiVisible,
                        title = context.getString(R.string.command_is_being_executed),
                        text = context.getString(R.string.progress_percent, progress.percent),
                        progress = progress.percent,
                        shortCriticalText = "${progress.percent}%",
                        countdownStart = null
                    )
                }
            }

            is RemoraCommand.Final       -> {
                if (uiVisible) {
                    cancelCommandNotification()
                } else if (isNew) {
                    when (val result = command.result) {
                        is RemoraCommand.Result.Error   -> showCommandFailedNotification(result.error)

                        is RemoraCommand.Result.Success -> {
                            sharedPreferences.edit { putString("progress_stage", null) }
                            val spanned = commandSummarizer.spanned(usesMgdl, result.finalData, command.constrainedData)
                            val notification = NotificationCompat.Builder(context, COMMAND_CHANNEL_ID)
                                .setSmallIcon(R.drawable.remora_logo)
                                .setCategory(NotificationCompat.CATEGORY_STATUS)
                                .setStyle(NotificationCompat.BigTextStyle().bigText(spanned))
                                .setContentTitle(context.getString(R.string.command_was_successful))
                                .setContentText(spanned)
                                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                                .setContentIntent(commandDialogPendingIntent())
                                .setAutoCancel(true)
                                .setDeleteIntent(PendingIntent.getBroadcast(context, 0, Intent(context, DiscardCommandReceiver::class.java), PendingIntent.FLAG_IMMUTABLE))
                                .build()

                            @SuppressLint("MissingPermission")
                            notificationManager.notify(COMMAND_NOTIFICATION_ID, notification)
                        }
                    }
                }
            }

            null                         -> cancelCommandNotification()
        }
    }

    private fun showCommandFailedNotification(error: RemoraCommandError) {
        val notification = NotificationCompat.Builder(context, COMMAND_CHANNEL_ID)
            .setSmallIcon(R.drawable.remora_logo)
            .setCategory(NotificationCompat.CATEGORY_ERROR)
            .setContentTitle(context.getString(R.string.command_failed))
            .setStyle(NotificationCompat.BigTextStyle().bigText(commandSummarizer.translateError(error)))
            .setContentIntent(commandDialogPendingIntent())
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setDeleteIntent(PendingIntent.getBroadcast(context, 0, Intent(context, DiscardCommandReceiver::class.java), PendingIntent.FLAG_IMMUTABLE))
            .build()

        sharedPreferences.edit { putString("progress_stage", null) }
        @SuppressLint("MissingPermission")
        notificationManager.notify(COMMAND_NOTIFICATION_ID, notification)
    }

    private fun commandDialogPendingIntent(): PendingIntent {
        val uri = Uri.Builder()
            .scheme("remora")
            .authority("dialog_command")
            .build()

        val intent = Intent(Intent.ACTION_VIEW, uri, context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun showStatusNotification() {
        val channel = NotificationChannel(STATUS_CHANNEL_ID, context.getString(R.string.status), NotificationManager.IMPORTANCE_LOW).apply {
            this.description = context.getString(R.string.shows_the_current_status_of_the_paired_androidaps_device)
            this.lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            this.enableVibration(false)
            this.enableLights(false)
            this.setSound(null, null)
            this.setShowBadge(false)
        }

        notificationManager.createNotificationChannel(channel)

        launch {
            remoraLib.passiveStatusFlow.collectLatest { statusView ->
                val data = statusView.short?.data
                if (data == null) {
                    notificationManager.cancel(STATUS_NOTIFICATION_ID)
                    return@collectLatest
                }

                val bg = data.displayBg?.let { (it.smoothedValue ?: it.value).formatBG(data.bgConfig.usesMgdl) }
                val delta = data.displayBg?.deltas?.delta?.let { delta -> (if (delta >= 0f) "+" else "") + delta.formatBG(data.bgConfig.usesMgdl) }
                val iob = (data.iob.basal + data.iob.bolus).formatInsulin() + " U"
                val cob = data.cob.display?.formatCarbs()?.plus("g") ?: "n/a"

                val title = StringBuilder()
                if (bg != null) {
                    title.append(bg)
                    title.append(" ")
                    if (data.bgConfig.usesMgdl) {
                        title.append("mg/dL")
                    } else {
                        title.append("mmol/L")
                    }
                } else title.append(context.getString(R.string.no_bg_data))
                if (delta != null) title.append(" $delta")

                val text = StringBuilder()
                text.append(context.getString(R.string.iob, iob))
                text.append("  ")
                text.append(context.getString(R.string.cob, cob))

                val notification = NotificationCompat.Builder(context, STATUS_CHANNEL_ID)
                    .setOngoing(true)
                    .setSmallIcon(R.drawable.remora_logo)
                    .setCategory(NotificationCompat.CATEGORY_STATUS)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setShowWhen(true)
                    .setWhen(data.timestamp.toEpochMilliseconds())
                    .setContentIntent(PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE))
                    .setContentTitle(title.toString())
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setContentText(text.toString())
                    .setSilent(true)
                    .setOnlyAlertOnce(true)
                    .build()

                @SuppressLint("MissingPermission")
                notificationManager.notify(STATUS_NOTIFICATION_ID, notification)
            }
        }
    }
}