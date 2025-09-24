package de.tebbeubben.remora.ui.commands

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.tebbeubben.remora.R
import de.tebbeubben.remora.lib.model.commands.RemoraCommand
import de.tebbeubben.remora.lib.model.commands.RemoraCommandData
import de.tebbeubben.remora.lib.model.commands.RemoraCommandError
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

@Composable
fun CommandDialog(
    viewModelStoreOwner: ViewModelStoreOwner,
    onDismiss: () -> Unit,
    initialCommandType: CommandType,
) {
    val viewModel = hiltViewModel<CommandViewModel>(viewModelStoreOwner)

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val (commandState, workerState) = uiState

    val command = when (commandState) {
        is CommandViewModel.CommandState.Loaded -> commandState.command
        CommandViewModel.CommandState.NotLoaded -> return
    }

    val discard = {
        viewModel.clearCommand(onDismiss)
    }

    LaunchedEffect(command) {
        if (command is RemoraCommand.Rejected) {
            viewModel.clearCommand()
        }
    }

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceContainerHigh),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (command) {
                    null                         -> when (initialCommandType) {
                        CommandType.BOLUS -> BolusInputDialogContent(
                            onCancel = discard,
                            onValidate = { bolusAmount, eatingSoonTT ->
                                viewModel.initBolus(bolusAmount, eatingSoonTT)
                            }
                        )
                    }

                    is RemoraCommand.Initial     -> {
                        CountdownContent(
                            onDiscard = discard,
                            onAction = viewModel::retryPrepare,
                            lastAttempt = command.lastAttempt,
                            workerState = workerState,
                            data = command.originalData,
                            previousData = null,
                            headline = "Validating…",
                            text = "Waiting for main device to validate your request:",
                            actionName = "Retry"
                        )
                    }

                    is RemoraCommand.Prepared    -> {
                        CountdownContent(
                            onDiscard = discard,
                            onAction = viewModel::confirmCommand,
                            lastAttempt = command.lastAttempt,
                            workerState = workerState,
                            data = command.constrainedData,
                            previousData = command.originalData,
                            headline = "Confirm",
                            text = "Please make sure that the following values are correct:",
                            actionName = "Confirm"
                        )
                    }

                    is RemoraCommand.Progressing -> {
                        Headline("Progress")

                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = "Command is being executed…",
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(16.dp))

                        val animatedProgress by animateFloatAsState(
                            targetValue = command.progress?.let { it / 100f } ?: 0f,
                            animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
                        )
                        if (command.progress == null) {
                            CircularWavyProgressIndicator(Modifier.size(72.dp))
                        } else {
                            Box(
                                modifier = Modifier.size(72.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularWavyProgressIndicator(
                                    modifier = Modifier.fillMaxSize(),
                                    progress = { animatedProgress }
                                )
                                Text(
                                    text = command.progress.toString() + "%",
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                        ) {
                            TextButton(discard) {
                                Text("Discard")
                            }
                            TextButton(onDismiss) {
                                Text("Close")
                            }
                        }
                    }

                    is RemoraCommand.Final       -> {
                        when (val result = command.result) {
                            is RemoraCommand.Result.Error   ->
                                FailureContent(
                                    onDiscard = discard,
                                    error = result.error
                                )
                            is RemoraCommand.Result.Success -> {
                                Headline("Success")

                                Spacer(Modifier.height(16.dp))

                                Icon(
                                    modifier = Modifier.size(48.dp),
                                    painter = painterResource(de.tebbeubben.remora.lib.R.drawable.check_circle_24px),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )

                                Spacer(Modifier.height(8.dp))

                                Text(
                                    text = "Command was executed successfully.",
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyLarge
                                )

                                Spacer(Modifier.height(8.dp))

                                Text(
                                    text = summarizeData(result.finalData, command.constrainedData),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyLarge
                                )

                                Spacer(Modifier.height(24.dp))

                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                                ) {
                                    TextButton(discard) {
                                        Text("Discard")
                                    }
                                }
                            }
                        }
                    }

                    is RemoraCommand.Rejected    -> {
                        FailureContent(
                            onDiscard = discard,
                            error = command.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FailureContent(
    onDiscard: () -> Unit,
    error: RemoraCommandError
) {
    Headline("Failure")

    Spacer(Modifier.height(16.dp))

    Icon(
        modifier = Modifier.size(48.dp),
        painter = painterResource(R.drawable.cancel_24px),
        contentDescription = null,
        tint = MaterialTheme.colorScheme.error,
    )

    Spacer(Modifier.height(8.dp))

    Text(
        text = "Command was not successful.",
        color = MaterialTheme.colorScheme.error,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodyLarge
    )

    Spacer(Modifier.height(8.dp))

    Text(
        text = translateError(error),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodyMedium
    )

    Spacer(Modifier.height(24.dp))

    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
    ) {
        TextButton(onDiscard) {
            Text("Discard")
        }
    }
}

@Composable
private fun CountdownContent(
    onDiscard: () -> Unit,
    onAction: () -> Unit,
    lastAttempt: Instant?,
    workerState: CommandViewModel.WorkerState,
    data: RemoraCommandData,
    previousData: RemoraCommandData?,
    headline: String,
    text: String,
    actionName: String,
) {

    Headline(headline)

    Spacer(Modifier.height(16.dp))

    Text(
        text = text,
        textAlign = TextAlign.Center
    )

    Spacer(Modifier.height(16.dp))

    Text(
        text = summarizeData(data, previousData),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodyLarge
    )

    var enableActionButton by remember { mutableStateOf(false) }

    if (lastAttempt != null && workerState == CommandViewModel.WorkerState.IDLE) {
        val totalDuration = 60.seconds
        val remainingDuration = totalDuration - (Clock.System.now() - lastAttempt)
        var countdown by remember {
            mutableStateOf(
                if (remainingDuration <= Duration.ZERO) null
                else remainingDuration.inWholeSeconds.coerceAtLeast(0).toInt()
            )
        }
        val progress = remember { Animatable((remainingDuration / totalDuration).toFloat().coerceIn(0f, 1f)) }
        val scope = rememberCoroutineScope()

        LaunchedEffect(lastAttempt) {
            val remainingDuration = totalDuration - (Clock.System.now() - lastAttempt)
            if (remainingDuration <= Duration.ZERO) {
                scope.launch { progress.snapTo(0f) }
                countdown = null
                enableActionButton = true
            } else {
                enableActionButton = false
                scope.launch {
                    progress.snapTo((remainingDuration / totalDuration).toFloat())
                    progress.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(
                            durationMillis = remainingDuration.inWholeMilliseconds.toInt(),
                            easing = LinearEasing
                        )
                    )
                }
                while (true) {
                    val remainingDuration = totalDuration - (Clock.System.now() - lastAttempt)
                    if (remainingDuration <= Duration.ZERO) {
                        scope.launch { progress.snapTo(0f) }
                        countdown = null
                        enableActionButton = true
                        break
                    }
                    countdown = remainingDuration.inWholeSeconds.coerceAtLeast(0).toInt()
                    delay(1000 - remainingDuration.inWholeMilliseconds % 1000)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        if (countdown == null) {
            Text(
                text = "It seems like AndroidAPS is not responding. Please try again.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
        } else {
            Box(
                modifier = Modifier.size(56.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.fillMaxSize(),
                    progress = { progress.value }
                )
                Text(
                    text = countdown.toString(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    } else {
        LaunchedEffect(workerState) {
            enableActionButton = workerState != CommandViewModel.WorkerState.RUNNING
        }
        when (workerState) {
            CommandViewModel.WorkerState.IDLE    -> Unit

            CommandViewModel.WorkerState.RUNNING -> {
                Spacer(Modifier.height(16.dp))
                LoadingIndicator()
            }

            CommandViewModel.WorkerState.FAILED  -> {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "An error occurred while sending your request. Please try again",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    Spacer(Modifier.height(24.dp))

    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
    ) {
        TextButton(onDiscard) {
            Text("Discard")
        }
        TextButton(
            onClick = onAction,
            enabled = enableActionButton
        ) {
            Text(actionName)
        }
    }
}

private fun translateError(error: RemoraCommandError) = when (error) {
    RemoraCommandError.UNKNOWN             -> "An unknown error occurred."
    RemoraCommandError.BOLUS_IN_PROGRESS   -> "Another bolus is already in progress."
    RemoraCommandError.PUMP_SUSPENDED      -> "The pump is suspended."
    RemoraCommandError.BG_MISMATCH         -> TODO()
    RemoraCommandError.IOB_MISMATCH        -> TODO()
    RemoraCommandError.COB_MISMATCH        -> TODO()
    RemoraCommandError.LAST_BOLUS_MISMATCH -> TODO()
    RemoraCommandError.PUMP_TIMEOUT        -> "The pump did not respond in time."
    RemoraCommandError.WRONG_SEQUENCE_ID   -> "Wrong sequence number. Make sure that no other follower is issuing commands at the same time."
    RemoraCommandError.EXPIRED             -> "The validated command has expired. Please try again."
    RemoraCommandError.ACTIVE_COMMAND      -> "Another command is already being executed. Make sure that no other follower is issuing commands at the same time."
}

private fun summarizeData(data: RemoraCommandData, previous: RemoraCommandData?) = when (data) {
    is RemoraCommandData.Bolus -> bolusSummary(data, previous as RemoraCommandData.Bolus?)
}

@Composable
private fun Headline(
    text: String,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineSmall,
        textAlign = TextAlign.Center
    )
}

enum class CommandType {
    BOLUS
}