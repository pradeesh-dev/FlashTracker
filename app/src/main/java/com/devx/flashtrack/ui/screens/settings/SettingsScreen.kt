package com.devx.flashtrack.ui.screens.settings

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.devx.flashtrack.ui.theme.*
import com.devx.flashtrack.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val context          = LocalContext.current
    var darkTheme        by remember { mutableStateOf(true) }
    var biometricEnabled by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showAboutDialog  by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBackIosNew, "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SettingsGroup("Appearance") {
                ToggleTile(
                    title    = "Dark Theme",
                    subtitle = "Use dark mode (recommended)",
                    icon     = Icons.Rounded.DarkMode,
                    iconColor = MaterialTheme.colorScheme.tertiary,
                    checked  = darkTheme,
                    onToggle = { darkTheme = it }
                )
            }

            SettingsGroup("Security") {
                ToggleTile(
                    title    = "Biometric Lock",
                    subtitle = "Fingerprint / Face unlock",
                    icon     = Icons.Rounded.Fingerprint,
                    iconColor = GreenIncome,
                    checked  = biometricEnabled,
                    onToggle = { biometricEnabled = it }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), thickness = 0.5.dp)
                NavTile(
                    title    = "Set PIN",
                    subtitle = "4-digit PIN backup lock",
                    icon     = Icons.Rounded.Pin,
                    iconColor = BlueCard,
                    onClick  = { }
                )
            }

            SettingsGroup("Data & Backup") {
                NavTile("Export CSV",     "All transactions as CSV file",      Icons.Rounded.FileDownload, GreenIncome)  { showExportDialog = true }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), thickness = 0.5.dp)
                NavTile("Backup Data",    "Save encrypted local backup",       Icons.Rounded.Backup,       BlueCard)     { }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), thickness = 0.5.dp)
                NavTile("Restore Backup", "Restore from encrypted backup",     Icons.Rounded.Restore,      OrangeExpense){ }
            }

            SettingsGroup("About") {
                NavTile("About FlashTrack", "Version 1.0.0 · Privacy-first", Icons.Rounded.Info,     MaterialTheme.colorScheme.onSurfaceVariant) { showAboutDialog = true }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), thickness = 0.5.dp)
                NavTile("Privacy Policy",   "100% offline, no data collection", Icons.Rounded.Security, GreenIncome) { }
            }

            Spacer(Modifier.height(40.dp))
        }
    }

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Export Data") },
            text  = { Text("Transactions will be exported as a CSV file shared via your default app.") },
            confirmButton = {
                Button(
                    onClick = { exportCsv(context, viewModel); showExportDialog = false },
                    colors  = ButtonDefaults.buttonColors(containerColor = GreenIncome)
                ) { Text("Export", color = Color.Black) }
            },
            dismissButton = { TextButton(onClick = { showExportDialog = false }) { Text("Cancel") } }
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("⚡ FlashTrack") },
            text  = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Version 1.0.0", fontWeight = FontWeight.Bold)
                    Text(
                        "Privacy-first expense tracker.\nAll data stored locally.\nNo cloud. No ads. No sign-up.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text("Built with ❤️ using Kotlin + Jetpack Compose",
                         style = MaterialTheme.typography.labelSmall,
                         color = MaterialTheme.colorScheme.primary)
                }
            },
            confirmButton = { TextButton(onClick = { showAboutDialog = false }) { Text("Close") } }
        )
    }
}

@Composable
private fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
        Text(
            title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
        )
        Card(
            shape  = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) { Column(content = content) }
    }
}

@Composable
private fun NavTile(
    title: String, subtitle: String? = null,
    icon: ImageVector, iconColor: Color, onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                               .background(iconColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) { Icon(icon, null, tint = iconColor, modifier = Modifier.size(18.dp)) }

        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall,
                     color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Icon(Icons.Rounded.ChevronRight, null,
             tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
             modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun ToggleTile(
    title: String, subtitle: String? = null,
    icon: ImageVector, iconColor: Color,
    checked: Boolean, onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                               .background(iconColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) { Icon(icon, null, tint = iconColor, modifier = Modifier.size(18.dp)) }

        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall,
                     color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Switch(
            checked = checked, onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = iconColor, checkedTrackColor = iconColor.copy(alpha = 0.3f))
        )
    }
}

private fun exportCsv(context: Context, viewModel: MainViewModel) {
    val sb = StringBuilder()
    sb.appendLine("Date,Type,Title,Amount,Category,Account,Notes,Tags")
    viewModel.allTransactions.value.forEach { t ->
        val cat  = viewModel.getCategoryById(t.categoryId)?.name ?: "Other"
        val acc  = viewModel.getAccountById(t.accountId)?.name  ?: "Unknown"
        val date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                       .format(java.util.Date(t.date))
        sb.appendLine("$date,${t.type},\"${t.title}\",${t.amount},\"$cat\",\"$acc\",\"${t.notes}\",\"${t.tags}\"")
    }
    try {
        val file = java.io.File(context.getExternalFilesDir(null), "flashtrack_${System.currentTimeMillis()}.csv")
        file.writeText(sb.toString())
        val uri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        context.startActivity(
            Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }, "Export CSV"
            )
        )
    } catch (e: Exception) { e.printStackTrace() }
}
