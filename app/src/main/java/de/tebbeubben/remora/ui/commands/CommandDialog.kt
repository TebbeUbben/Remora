package de.tebbeubben.remora.ui.commands

import androidx.activity.compose.LocalActivity
import androidx.biometric.AuthenticationRequest
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.registerForAuthenticationResult
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import de.tebbeubben.remora.COMMAND_TIMEOUT
import de.tebbeubben.remora.R
import de.tebbeubben.remora.lib.model.commands.RemoraCommand
import de.tebbeubben.remora.lib.model.commands.RemoraCommandData
import de.tebbeubben.remora.lib.model.commands.RemoraCommandError
import de.tebbeubben.remora.util.LocalCommandSummarizer
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlin.math.ceil
import kotlin.time.Clock
import kotlin.time.Instant

@Composable
fun CommandDialog(
    onDismiss: () -> Unit,
    initialCommandType: CommandType?,
) {
    val viewModel = hiltViewModel<CommandViewModel>()

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val (commandState, workerState) = uiState

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.setActive(true)
            try {
                awaitCancellation()
            } finally {
                viewModel.setActive(false)
            }
        }
    }

    val status by viewModel.statusState.collectAsStateWithLifecycle()
    val usesMgdl = status?.short?.data?.bgConfig?.usesMgdl ?: true

    val command = when (commandState) {
        is CommandViewModel.CommandState.Loaded -> commandState.command
        CommandViewModel.CommandState.NotLoaded -> return
    }

    if (command == null && initialCommandType == null) {
        onDismiss()
        return
    }

    val discard = {
        viewModel.clearCommand(onDismiss)
    }

    val activity = LocalActivity.current as FragmentActivity
    val biometricManager = remember { BiometricManager.from(activity) }

    val biometricLauncher = remember {
        activity.registerForAuthenticationResult(
            resultCallback = { result ->
                if (result.isSuccess()) {
                    viewModel.confirmCommand()
                }
            }
        )
    }

    val context = LocalContext.current

    val confirm = {
        if (biometricManager.canAuthenticate(BIOMETRIC_WEAK or DEVICE_CREDENTIAL) == BiometricManager.BIOMETRIC_SUCCESS) {
            val biometricRequest = AuthenticationRequest.biometricRequest(
                title = context.getString(R.string.confirm),
                authFallback = AuthenticationRequest.Biometric.Fallback.DeviceCredential
            ) {
                setSubtitle(context.getString(R.string.please_confirm_the_command_using_your_device_s_unlock_method))
                setIsConfirmationRequired(false)
            }
            biometricLauncher.launch(biometricRequest)
        } else {
            viewModel.confirmCommand()
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
                        CommandType.CARBS -> CarbsInputDialogContent(
                            onCancel = discard,
                            onValidate = { carbsAmount, duration, tempTarget, timestamp ->
                                viewModel.initCarbs(carbsAmount, duration, tempTarget, timestamp)
                            }
                        )

                        null              -> Unit
                    }

                    is RemoraCommand.Initial     -> {
                        CountdownContent(
                            usesMgdl = usesMgdl,
                            onDiscard = discard,
                            onAction = viewModel::retryPrepare,
                            lastAttempt = command.lastAttempt,
                            workerState = workerState,
                            data = command.originalData,
                            previousData = null,
                            headline = stringResource(R.string.validating),
                            text = stringResource(R.string.waiting_for_main_device_to_validate_your_request),
                            actionName = stringResource(R.string.retry)
                        )
                    }

                    is RemoraCommand.Prepared    -> {
                        CountdownContent(
                            usesMgdl = usesMgdl,
                            onDiscard = discard,
                            onAction = confirm,
                            lastAttempt = command.lastAttempt,
                            workerState = workerState,
                            data = command.constrainedData,
                            previousData = command.originalData,
                            headline = stringResource(R.string.confirm),
                            text = stringResource(R.string.please_make_sure_that_the_following_values_are_correct),
                            actionName = stringResource(R.string.confirm)
                        )
                    }

                    is RemoraCommand.Progressing -> {
                        Headline(stringResource(R.string.progress))

                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = when (command.progress) {
                                is RemoraCommand.Progress.Connecting -> stringResource(R.string.connecting_to_pump)
                                RemoraCommand.Progress.Enqueued      -> stringResource(R.string.command_is_waiting_in_queue)
                                is RemoraCommand.Progress.Percentage -> stringResource(R.string.command_is_being_executed)
                            },
                            textAlign = TextAlign.Center
                        )

                        val progressText = when (val commandProgress = command.progress) {
                            is RemoraCommand.Progress.Connecting -> commandProgress.elapsedSeconds.toString()
                            RemoraCommand.Progress.Enqueued      -> ""
                            is RemoraCommand.Progress.Percentage -> "${commandProgress.percent}%"
                        }

                        val progress = when (val commandProgress = command.progress) {
                            is RemoraCommand.Progress.Percentage -> commandProgress.percent / 100f
                            else                                 -> null
                        }

                        var timeoutHintVisible by remember(command.receivedAt) {
                            mutableStateOf(Clock.System.now() - command.receivedAt >= COMMAND_TIMEOUT)
                        }

                        LaunchedEffect(command.receivedAt) {
                            if (!timeoutHintVisible) {
                                delay(COMMAND_TIMEOUT - (Clock.System.now() - command.receivedAt))
                                timeoutHintVisible = true
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Box(
                            modifier = Modifier.size(72.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (progress != null) {
                                val animatedProgress by animateFloatAsState(
                                    targetValue = progress,
                                    animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
                                )
                                CircularWavyProgressIndicator(
                                    modifier = Modifier.fillMaxSize(),
                                    progress = { animatedProgress }
                                )
                            } else {
                                CircularWavyProgressIndicator(Modifier.fillMaxSize())
                            }

                            Text(
                                text = progressText,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                        AnimatedVisibility(timeoutHintVisible, Modifier.padding(top = 16.dp)) {
                            Text(
                                text = stringResource(R.string.not_receiving_progress_reports_please_check_system_status),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        Spacer(Modifier.height(24.dp))

                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                        ) {
                            TextButton(discard) {
                                Text(stringResource(R.string.discard))
                            }
                            TextButton(onDismiss) {
                                Text(stringResource(R.string.close))
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
                                Headline(stringResource(R.string.success))

                                Spacer(Modifier.height(16.dp))

                                Icon(
                                    modifier = Modifier.size(48.dp),
                                    painter = painterResource(de.tebbeubben.remora.lib.R.drawable.check_circle_24px),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )

                                Spacer(Modifier.height(8.dp))

                                Text(
                                    text = stringResource(R.string.command_was_executed_successfully),
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyLarge
                                )

                                Spacer(Modifier.height(8.dp))

                                Text(
                                    text = LocalCommandSummarizer.current.annotatedString(usesMgdl, result.finalData, command.constrainedData),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyLarge
                                )

                                Spacer(Modifier.height(24.dp))

                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                                ) {
                                    TextButton(discard) {
                                        Text(stringResource(R.string.discard))
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
    error: RemoraCommandError,
) {
    Headline(stringResource(R.string.failed))

    Spacer(Modifier.height(16.dp))

    Icon(
        modifier = Modifier.size(48.dp),
        painter = painterResource(R.drawable.cancel_24px),
        contentDescription = null,
        tint = MaterialTheme.colorScheme.error,
    )

    Spacer(Modifier.height(8.dp))

    Text(
        text = stringResource(R.string.command_was_not_successful),
        color = MaterialTheme.colorScheme.error,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodyLarge
    )

    Spacer(Modifier.height(8.dp))

    Text(
        text = LocalCommandSummarizer.current.translateError(error),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodyMedium
    )

    Spacer(Modifier.height(24.dp))

    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
    ) {
        TextButton(onDiscard) {
            Text(stringResource(R.string.discard))
        }
    }
}

@Composable
private fun CountdownContent(
    usesMgdl: Boolean,
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
        text = LocalCommandSummarizer.current.annotatedString(usesMgdl, data, previousData),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodyLarge
    )

    if (lastAttempt != null && workerState == CommandViewModel.WorkerState.IDLE) {
        val countdownAnimatable = remember(lastAttempt) {
            val remainingDuration = COMMAND_TIMEOUT - (Clock.System.now() - lastAttempt)
            Animatable(
                remainingDuration
                    .inWholeMilliseconds
                    .toFloat()
                    .coerceAtLeast(0f)
            )
        }

        LaunchedEffect(lastAttempt) {
            if (countdownAnimatable.value > 0f) {
                countdownAnimatable.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(
                        durationMillis = countdownAnimatable.value.toInt(),
                        easing = LinearEasing
                    )
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        if (countdownAnimatable.value == 0f) {
            Text(
                text = stringResource(R.string.it_seems_like_androidaps_is_not_responding_please_try_again),
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
                    progress = { countdownAnimatable.value / COMMAND_TIMEOUT.inWholeMilliseconds }
                )
                Text(
                    text = ceil(countdownAnimatable.value / 1000).toInt().toString(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    } else {
        when (workerState) {
            CommandViewModel.WorkerState.IDLE    -> Unit

            CommandViewModel.WorkerState.RUNNING -> {
                Spacer(Modifier.height(16.dp))
                LoadingIndicator()
            }

            CommandViewModel.WorkerState.FAILED  -> {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.an_error_occurred_while_sending_your_request_please_try_again),
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
            Text(stringResource(R.string.discard))
        }
        TextButton(
            onClick = onAction,
            enabled = workerState != CommandViewModel.WorkerState.RUNNING
        ) {
            Text(actionName)
        }
    }
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
    BOLUS,
    CARBS
}