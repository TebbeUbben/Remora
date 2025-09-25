package de.tebbeubben.remora

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.Html
import androidx.core.text.HtmlCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import de.tebbeubben.remora.lib.RemoraLib
import de.tebbeubben.remora.lib.model.commands.RemoraCommand
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
        val channel = NotificationChannel(COMMAND_CHANNEL_ID, "Commands", NotificationManager.IMPORTANCE_HIGH).apply {
            this.description = "Shows the current progress of any active remote commands."
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
                val spanned = HtmlCompat.fromHtml(
                    commandSummarizer.summarizeData(
                        command.constrainedData,
                        command.originalData
                    ),
                    HtmlCompat.FROM_HTML_MODE_LEGACY or HtmlCompat.FROM_HTML_SEPARATOR_LINE_BREAK_PARAGRAPH
                )
                val notification = Notification.Builder(context, COMMAND_CHANNEL_ID)
                    .setSmallIcon(R.drawable.remora_logo)
                    .setCategory(Notification.CATEGORY_STATUS)
                    .setContentTitle("Awaiting confirmation")
                    .setStyle(Notification.BigTextStyle().bigText(spanned))
                    .setContentIntent(commandDialogPendingIntent())
                    .setAutoCancel(true)
                    .build()

                notificationManager.notify(COMMAND_NOTIFICATION_ID, notification)
            }

            is RemoraCommand.Rejected    -> {
                val notification = Notification.Builder(context, COMMAND_CHANNEL_ID)
                    .setSmallIcon(R.drawable.remora_logo)
                    .setCategory(Notification.CATEGORY_ERROR)
                    .setContentTitle("Command failed")
                    .setStyle(Notification.BigTextStyle().bigText(commandSummarizer.translateError(command.error)))
                    .setContentIntent(commandDialogPendingIntent())
                    .setAutoCancel(true)
                    .setDeleteIntent(PendingIntent.getBroadcast(context, 0, Intent(context, DiscardCommandReceiver::class.java), PendingIntent.FLAG_IMMUTABLE))
                    .build()

                notificationManager.notify(COMMAND_NOTIFICATION_ID, notification)
            }

            is RemoraCommand.Progressing -> when (val progress = command.progress) {
                is RemoraCommand.Progress.Connecting -> {
                    val notification = Notification.Builder(context, COMMAND_CHANNEL_ID)
                        .setSmallIcon(R.drawable.remora_logo)
                        .setCategory(Notification.CATEGORY_PROGRESS)
                        .setContentTitle("Connecting to pump…")
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
                        .setContentTitle("Command is waiting in queue…")
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
                        .setContentTitle("Command is being executed…")
                        .setContentText("Progress: ${progress.percent}%")
                        .setContentIntent(commandDialogPendingIntent())
                        .setOnlyAlertOnce(true)
                        .setProgress(100, progress.percent, false)
                        .setAutoCancel(true)
                        .build()

                    notificationManager.notify(COMMAND_NOTIFICATION_ID, notification)
                }
            }

            is RemoraCommand.Final       -> when (val result = command.result) {
                is RemoraCommand.Result.Error   -> {
                    val notification = Notification.Builder(context, COMMAND_CHANNEL_ID)
                        .setSmallIcon(R.drawable.remora_logo)
                        .setCategory(Notification.CATEGORY_ERROR)
                        .setContentTitle("Command failed")
                        .setStyle(Notification.BigTextStyle().bigText(commandSummarizer.translateError(result.error)))
                        .setContentIntent(commandDialogPendingIntent())
                        .setAutoCancel(true)
                        .setDeleteIntent(PendingIntent.getBroadcast(context, 0, Intent(context, DiscardCommandReceiver::class.java), PendingIntent.FLAG_IMMUTABLE))
                        .build()

                    notificationManager.notify(COMMAND_NOTIFICATION_ID, notification)
                }

                is RemoraCommand.Result.Success -> {
                    val spanned = Html.fromHtml(
                        commandSummarizer.summarizeData(
                            result.finalData,
                            command.constrainedData
                        ),
                        Html.FROM_HTML_MODE_LEGACY or Html.FROM_HTML_SEPARATOR_LINE_BREAK_PARAGRAPH
                    )
                    val notification = Notification.Builder(context, COMMAND_CHANNEL_ID)
                        .setSmallIcon(R.drawable.remora_logo)
                        .setCategory(Notification.CATEGORY_STATUS)
                        .setStyle(Notification.BigTextStyle().bigText(spanned))
                        .setContentTitle("Command was successful")
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
        val channel = NotificationChannel(STATUS_CHANNEL_ID, "Status", NotificationManager.IMPORTANCE_DEFAULT).apply {
            this.description = "Shows the current status of the paired AndroidAPS device."
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

                val bg = data.displayBg?.value?.formatBG(data.usesMgdl)
                val delta = data.displayBg?.deltas?.delta?.let { delta -> (if (delta >= 0f) "+" else "") + delta.formatBG(data.usesMgdl) }
                val iob = (data.basalIob + data.bolusIob).formatInsulin() + " U"
                val cob = data.displayCob?.formatCarbs()?.plus(" g") ?: "n/a"

                val title = StringBuilder()
                if (bg != null) {
                    title.append(bg)
                    if (data.usesMgdl) {
                        title.append(" mg/dL")
                    } else {
                        title.append(" mmol/L")
                    }
                }
                else title.append("No BG data")
                if (delta != null) title.append(" $delta")

                val text = StringBuilder()
                text.append("IOB: $iob")
                text.append("  COB: $cob")

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

    sealed class LoadedState {
        object NotLoaded : LoadedState()
        data class Loaded(val command: RemoraCommand?) : LoadedState()
    }

}