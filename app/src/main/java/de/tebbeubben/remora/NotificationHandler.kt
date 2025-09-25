package de.tebbeubben.remora

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHandler @Inject constructor(
    private val remoraLib: RemoraLib,
    @param:ApplicationContext
    private val context: Context,
    private val commandSummarizer: CommandSummarizer,
) : CoroutineScope by CoroutineScope(Dispatchers.Main) {

    private val notificationManager by lazy { context.getSystemService(NotificationManager::class.java) }

    val overviewActive = MutableStateFlow(false)
    val commandDialogActive = MutableStateFlow(false)

    companion object {

        const val STATUS_CHANNEL_ID = "remora_status"
        const val STATUS_NOTIFICATION_ID = 51235

        const val COMMAND_CHANNEL_ID = "remora_command"
        const val COMMAND_NOTIFICATION_ID = 51236
    }

    fun start() {
        showStatusNotification()
        showCommandNotification()
    }

    fun showCommandNotification() {
        val channel = NotificationChannel(COMMAND_CHANNEL_ID, context.getString(R.string.commands), NotificationManager.IMPORTANCE_HIGH).apply {
            this.description = context.getString(R.string.shows_the_current_progress_of_any_active_remote_commands)
        }

        notificationManager.createNotificationChannel(channel)

        launch {
            combine(overviewActive, commandDialogActive) { overviewActive, commandDialogActive -> overviewActive || commandDialogActive }
                .filter { it }
                .collectLatest {
                    notificationManager.cancel(COMMAND_NOTIFICATION_ID)
                }
        }

        launch {
            remoraLib.commandStateFlow
                .filter { !it.isCached }
                .map { it.command }
                .collectLatest {
                    if (overviewActive.value || commandDialogActive.value) return@collectLatest
                    showCommand(it)
                }
        }
    }

    private suspend fun showCommand(command: RemoraCommand?) {
        when (command) {
            is RemoraCommand.Initial     -> {
                notificationManager.cancel(COMMAND_NOTIFICATION_ID)
            }

            is RemoraCommand.Prepared    -> {
                val notification = Notification.Builder(context, COMMAND_CHANNEL_ID)
                    .setSmallIcon(R.drawable.remora_logo)
                    .setCategory(Notification.CATEGORY_STATUS)
                    .setContentTitle(context.getString(R.string.awaiting_confirmation))
                    .setStyle(Notification.BigTextStyle().bigText(commandSummarizer.spanned(command.constrainedData, command.originalData)))
                    .setContentIntent(commandDialogPendingIntent())
                    .setAutoCancel(true)
                    .build()

                notificationManager.notify(COMMAND_NOTIFICATION_ID, notification)
            }

            is RemoraCommand.Rejected    -> showCommandFailedNotification(command.error)

            is RemoraCommand.Progressing -> when (val progress = command.progress) {
                is RemoraCommand.Progress.Connecting -> {
                    val notification = Notification.Builder(context, COMMAND_CHANNEL_ID)
                        .setSmallIcon(R.drawable.remora_logo)
                        .setCategory(Notification.CATEGORY_PROGRESS)
                        .setContentTitle(context.getString(R.string.connecting_to_pump))
                        .setWhen(System.currentTimeMillis() - progress.elapsedSeconds * 1000)
                        .setUsesChronometer(true)
                        .setOnlyAlertOnce(true)
                        .setProgress(100, 0, true)
                        .setContentIntent(commandDialogPendingIntent())
                        .setAutoCancel(true)
                        .build()

                    notificationManager.notify(COMMAND_NOTIFICATION_ID, notification)
                }

                RemoraCommand.Progress.Enqueued      -> {
                    val notification = Notification.Builder(context, COMMAND_CHANNEL_ID)
                        .setSmallIcon(R.drawable.remora_logo)
                        .setCategory(Notification.CATEGORY_PROGRESS)
                        .setContentTitle(context.getString(R.string.command_is_waiting_in_queue))
                        .setContentIntent(commandDialogPendingIntent())
                        .setProgress(100, 0, true)
                        .setOnlyAlertOnce(true)
                        .setAutoCancel(true)
                        .build()

                    notificationManager.notify(COMMAND_NOTIFICATION_ID, notification)
                }

                is RemoraCommand.Progress.Percentage -> {
                    val notification = Notification.Builder(context, COMMAND_CHANNEL_ID)
                        .setSmallIcon(R.drawable.remora_logo)
                        .setCategory(Notification.CATEGORY_PROGRESS)
                        .setContentTitle(context.getString(R.string.command_is_being_executed))
                        .setContentText(context.getString(R.string.progress_percent, progress.percent))
                        .setContentIntent(commandDialogPendingIntent())
                        .setOnlyAlertOnce(true)
                        .setProgress(100, progress.percent, false)
                        .setAutoCancel(true)
                        .build()

                    notificationManager.notify(COMMAND_NOTIFICATION_ID, notification)
                }
            }

            is RemoraCommand.Final       -> when (val result = command.result) {
                is RemoraCommand.Result.Error   -> showCommandFailedNotification(result.error)

                is RemoraCommand.Result.Success -> {
                    val notification = Notification.Builder(context, COMMAND_CHANNEL_ID)
                        .setSmallIcon(R.drawable.remora_logo)
                        .setCategory(Notification.CATEGORY_STATUS)
                        .setStyle(Notification.BigTextStyle().bigText(commandSummarizer.spanned(result.finalData, command.constrainedData)))
                        .setContentTitle(context.getString(R.string.command_was_successful))
                        .setContentIntent(commandDialogPendingIntent())
                        .setAutoCancel(true)
                        .setDeleteIntent(PendingIntent.getBroadcast(context, 0, Intent(context, DiscardCommandReceiver::class.java), PendingIntent.FLAG_IMMUTABLE))
                        .build()

                    notificationManager.notify(COMMAND_NOTIFICATION_ID, notification)
                }
            }

            null                         -> {
                notificationManager.cancel(COMMAND_NOTIFICATION_ID)
            }
        }
    }

    private fun showCommandFailedNotification(error: RemoraCommandError) {
        val notification = Notification.Builder(context, COMMAND_CHANNEL_ID)
            .setSmallIcon(R.drawable.remora_logo)
            .setCategory(Notification.CATEGORY_ERROR)
            .setContentTitle(context.getString(R.string.command_failed))
            .setStyle(Notification.BigTextStyle().bigText(commandSummarizer.translateError(error)))
            .setContentIntent(commandDialogPendingIntent())
            .setAutoCancel(true)
            .setDeleteIntent(PendingIntent.getBroadcast(context, 0, Intent(context, DiscardCommandReceiver::class.java), PendingIntent.FLAG_IMMUTABLE))
            .build()

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
            this.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
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

                val bg = data.displayBg?.let { (it.smoothedValue ?: it.value).formatBG(data.usesMgdl) }
                val delta = data.displayBg?.deltas?.delta?.let { delta -> (if (delta >= 0f) "+" else "") + delta.formatBG(data.usesMgdl) }
                val iob = (data.basalIob + data.bolusIob).formatInsulin() + " U"
                val cob = data.displayCob?.formatCarbs()?.plus(" g") ?: "n/a"

                val title = StringBuilder()
                if (bg != null) {
                    title.append(bg)
                    title.append(" ")
                    if (data.usesMgdl) {
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

                val notification = Notification.Builder(context, STATUS_CHANNEL_ID)
                    .setOngoing(true)
                    .setSmallIcon(R.drawable.remora_logo)
                    .setCategory(Notification.CATEGORY_STATUS)
                    .setVisibility(Notification.VISIBILITY_PUBLIC)
                    .setShowWhen(true)
                    .setWhen(data.timestamp.toEpochMilliseconds())
                    .setContentTitle(title.toString())
                    .setContentText(text.toString())
                    .build()

                notificationManager.notify(STATUS_NOTIFICATION_ID, notification)
            }
        }
    }
}