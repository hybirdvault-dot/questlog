package com.questlog.app.feature.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.questlog.app.R
import com.questlog.app.core.model.WalletState
import com.questlog.app.ui.designsystem.QuestlogSoftBrown
import com.questlog.app.ui.designsystem.QuestlogSpacing
import com.questlog.app.ui.designsystem.QuestlogTerracotta

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val walletState by viewModel.walletState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_settings)) },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            SettingsSection(title = stringResource(R.string.settings_appearance)) {
                SettingsItem(
                    icon = Icons.Filled.Palette,
                    title = stringResource(R.string.settings_theme),
                    subtitle = stringResource(R.string.settings_theme_subtitle),
                    onClick = {},
                )
            }

            SettingsSection(title = stringResource(R.string.settings_notifications)) {
                SettingsItem(
                    icon = Icons.Filled.Notifications,
                    title = stringResource(R.string.settings_reminder),
                    subtitle = stringResource(R.string.settings_reminder_subtitle),
                    onClick = {},
                )
            }

            SettingsSection(title = stringResource(R.string.settings_wallet)) {
                val connected = walletState as? WalletState.Connected
                SettingsItem(
                    icon = Icons.Filled.AccountBalanceWallet,
                    title = if (connected != null) {
                        stringResource(R.string.wallet_ready)
                    } else {
                        stringResource(R.string.settings_connect_wallet)
                    },
                    subtitle = if (connected != null) {
                        truncateAddress(connected.pubkey)
                    } else {
                        stringResource(R.string.settings_connect_wallet_subtitle)
                    },
                    onClick = { activity?.let { viewModel.connectWallet(it) } },
                )
            }

            SettingsSection(title = stringResource(R.string.settings_about)) {
                SettingsItem(
                    icon = Icons.Filled.Info,
                    title = stringResource(R.string.settings_about_questlog),
                    subtitle = stringResource(R.string.settings_version),
                    onClick = {},
                )
                SettingsItem(
                    icon = Icons.Filled.Info,
                    title = stringResource(R.string.settings_rate),
                    subtitle = stringResource(R.string.settings_rate_subtitle),
                    onClick = { openPlayStore(context) },
                )
            }

            SettingsSection(title = stringResource(R.string.settings_data)) {
                SettingsItem(
                    icon = Icons.Filled.Delete,
                    title = stringResource(R.string.settings_delete_all),
                    subtitle = stringResource(R.string.settings_delete_all_subtitle),
                    onClick = { showDeleteDialog = true },
                    isDestructive = true,
                )
            }

            Spacer(modifier = Modifier.height(QuestlogSpacing.Xl))

            Text(
                text = stringResource(R.string.settings_footer),
                style = MaterialTheme.typography.bodySmall,
                color = QuestlogSoftBrown,
                modifier = Modifier
                    .padding(horizontal = QuestlogSpacing.L)
                    .padding(bottom = QuestlogSpacing.Xl),
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_all_title)) },
            text = { Text(stringResource(R.string.delete_all_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteAllData()
                }) {
                    Text(
                        text = stringResource(R.string.action_delete),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(modifier = Modifier.padding(top = QuestlogSpacing.L)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = QuestlogTerracotta,
            modifier = Modifier.padding(horizontal = QuestlogSpacing.L, vertical = QuestlogSpacing.S),
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = QuestlogSpacing.L),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        ) {
            Column { content() }
        }
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false,
) {
    val titleColor = if (isDestructive) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = QuestlogSpacing.L, vertical = QuestlogSpacing.M),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isDestructive) MaterialTheme.colorScheme.error else QuestlogTerracotta,
            modifier = Modifier.size(24.dp),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = QuestlogSpacing.M),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = titleColor,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = QuestlogSoftBrown,
            )
        }
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = QuestlogSoftBrown,
            modifier = Modifier.size(20.dp),
        )
    }
}

private fun truncateAddress(address: String): String =
    if (address.length <= 12) {
        address
    } else {
        "${address.take(4)}…${address.takeLast(4)}"
    }

private fun openPlayStore(context: Context) {
    try {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${context.packageName}"))
        )
    } catch (_: Exception) {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}"))
        )
    }
}
