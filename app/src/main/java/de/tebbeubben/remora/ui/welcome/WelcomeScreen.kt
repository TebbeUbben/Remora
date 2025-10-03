package de.tebbeubben.remora.ui.welcome

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import de.tebbeubben.remora.R

@Composable
fun WelcomeScreen(
    onContinue: () -> Unit = {},
) {
    val context = LocalContext.current

    var notificationsAllowed by remember { mutableStateOf(isNotificationsAllowed(context)) }
    val requestNotificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        notificationsAllowed = granted || isNotificationsAllowed(context)
    }

    val openNotificationSettings = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        notificationsAllowed = isNotificationsAllowed(context)
    }

    var isBatteryWhitelisted by remember { mutableStateOf(isIgnoringBatteryOptimizations(context)) }
    val requestBatteryWhitelist = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        isBatteryWhitelisted = isIgnoringBatteryOptimizations(context)
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isBatteryWhitelisted = isIgnoringBatteryOptimizations(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    modifier = Modifier
                        .size(100.dp),
                    painter = painterResource(R.drawable.remora_logo),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                )

                Text(
                    text = stringResource(R.string.welcome),
                    style = MaterialTheme.typography.headlineLarge,
                    textAlign = TextAlign.Center,
                )

                Text(
                    text = stringResource(R.string.intro_text),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                )

                Text(
                    text = stringResource(R.string.before_you_can_proceed_you_need_to_grant_the_following_permissions),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                )

                PermissionRow(
                    title = stringResource(R.string.notifications),
                    description = if (Build.VERSION.SDK_INT >= 33)
                        stringResource(R.string.allow_notifications_so_you_can_see_status_information_command_progress_and_alerts)
                    else
                        stringResource(R.string.enable_notifications_so_you_can_see_status_information_command_progress_and_alerts),
                    granted = notificationsAllowed,
                    actionLabel = if (Build.VERSION.SDK_INT >= 33) stringResource(R.string.grant) else stringResource(R.string.open_settings),
                    onClick = {
                        if (Build.VERSION.SDK_INT >= 33) {
                            requestNotificationPermission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            openNotificationSettings.launch(appNotificationSettingsIntent(context))
                        }
                    }
                )

                PermissionRow(
                    title = stringResource(R.string.battery_optimization),
                    description = stringResource(R.string.whitelist_the_app_to_reduce_delays_for_commands_sync_and_notifications),
                    granted = isBatteryWhitelisted,
                    actionLabel = stringResource(R.string.allow),
                    onClick = {
                        requestBatteryWhitelist.launch(ignoreBatteryOptimizationsIntent(context))
                    }
                )

                Button(
                    onClick = onContinue,
                    enabled = notificationsAllowed && isBatteryWhitelisted
                ) {
                    Text(stringResource(R.string._continue))
                }
            }
        }
    }
}

@Composable
private fun PermissionRow(
    title: String,
    description: String,
    granted: Boolean,
    actionLabel: String,
    onClick: () -> Unit,
) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(8.dp))
            Button(
                modifier = Modifier.align(Alignment.End),
                onClick = onClick,
                enabled = !granted,
            ) {
                Text(actionLabel)
            }
        }
    }
}

private fun isNotificationsAllowed(context: Context): Boolean {
    val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    return nm.areNotificationsEnabled()
}

private fun appNotificationSettingsIntent(context: Context): Intent {
    return Intent().apply {
        action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        putExtra("app_package", context.packageName)
        putExtra("app_uid", context.applicationInfo.uid)
    }
}

private fun isIgnoringBatteryOptimizations(context: Context): Boolean {
    val pm = context.getSystemService(PowerManager::class.java)
    return pm.isIgnoringBatteryOptimizations(context.packageName)
}

@SuppressLint("BatteryLife")
private fun ignoreBatteryOptimizationsIntent(context: Context): Intent {
    return Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
        data = Uri.fromParts("package", context.packageName, null)
        addCategory(Intent.CATEGORY_DEFAULT)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
    }
}