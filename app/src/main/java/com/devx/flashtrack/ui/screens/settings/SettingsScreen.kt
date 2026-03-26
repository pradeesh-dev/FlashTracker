package com.devx.flashtrack.ui.screens.settings

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.devx.flashtrack.ui.navigation.Screen
import com.devx.flashtrack.ui.theme.*
import com.devx.flashtrack.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onNavigateToDebts: (() -> Unit)? = null,
    onNavigateToReminders: (() -> Unit)? = null,
    onNavigateToCategories: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var darkTheme by remember { mutableStateOf(true) }
    var biometricEnabled by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

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
            // ─── App Navigation shortcuts ──────────────────────────────────────
            SettingsSection("Quick Access") {
                SettingsTileNav("Debts & IOUs", Icons.Rounded.Handshake, PurpleDebt, "Track money lent & borrowed") {
                    onBack(); /* let parent navigate */ }
                SettingsTileNav("Reminders", Icons.Rounded.NotificationsActive, OrangeExpense, "Bills & subscription reminders") { onBack() }
                SettingsTileNav("Categories", Icons.Rounded.Category, BlueCard, "Manage expense categories") { onBack() }
            }

            // ─── Appearance ────────────────────────────────────────────────────
            SettingsSection("Appearance") {
                SettingsTileToggle(
                    title = "Dark Theme",
                    subtitle = "Use dark mode (default)",
                    icon = Icons.Rounded.DarkMode,
                    iconColor = MaterialTheme.colorScheme.tertiary,
                    checked = darkTheme,
                    onToggle = { darkTheme = it }
                )
            }

            // ─── Security ──────────────────────────────────────────────────────
            SettingsSection("Security") {
                SettingsTileToggle(
                    title = "Biometric Lock",
                    subtitle = "Lock app with fingerprint / face",
                    icon = Icons.Rounded.Fingerprint,
                    iconColor = GreenIncome,
                    checked = biometricEnabled,
                    onToggle = { biometricEnabled = it }
                )
                SettingsTileNav(
                    title = "Set PIN",
                    subtitle = "4-digit PIN backup lock",
                    icon = Icons.Rounded.Pin,
                    iconColor = BlueCard
                ) { /* PIN setup dialog */ }
            }

            // ─── Data ──────────────────────────────────────────────────────────
            SettingsSection("Data & Backup") {
                SettingsTileNav(
                    title = "Export CSV",
                    subtitle = "Export all transactions to CSV",
                    icon = Icons.Rounded.FileDownload,
                    iconColor = GreenIncome
                ) { showExportDialog = true }
                SettingsTileNav(
                    title = "Backup Data",
                    subtitle = "Save encrypted local backup",
                    icon = Icons.Rounded.Backup,
                    iconColor = BlueCard
                ) { /* backup logic */ }
                SettingsTileNav(
                    title = "Restore Backup",
                    subtitle = "Restore from encrypted backup",
                    icon = Icons.Rounded.Restore,
                    iconColor = OrangeExpense
                ) { /* restore logic */ }
            }

            // ─── About ─────────────────────────────────────────────────────────
            SettingsSection("About") {
                SettingsTileNav(
                    title = "About FlashTrack",
                    subtitle = "Version 1.0.0 • Privacy-first",
                    icon = Icons.Rounded.Info,
                    iconColor = MaterialTheme.colorScheme.onSurfaceVariant
                ) { showAboutDialog = true }
                SettingsTileNav(
                    title = "Privacy Policy",
                    subtitle = "100% offline, no data collection",
                    icon = Icons.Rounded.Security,
                    iconColor = GreenIncome
                ) { }
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Export Data") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Your transactions will be exported as a CSV file.",
                         style = MaterialTheme.typography.bodyMedium)
                    Text("The file will be saved to your Downloads folder.",
                         style = MaterialTheme.typography.bodySmall,
                         color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        exportCsv(context, viewModel)
                        showExportDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenIncome)
                ) { Text("Export", color = Color.Black) }
            },
            dismissButton = { TextButton(onClick = { showExportDialog = false }) { Text("Cancel") } }
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("⚡ FlashTrack") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Version 1.0.0", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Text("A privacy-first expense tracker.\nAll data stored locally on your device.\nNo cloud. No ads. No sign-up.",
                         style = MaterialTheme.typography.bodySmall,
                         color = MaterialTheme.colorScheme.onSurfaceVariant)
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
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        )
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) { Column(content = content) }
    }
}

@Composable
private fun SettingsTileNav(
    title: String,
    icon: ImageVector,
    iconColor: Color = MaterialTheme.colorScheme.primary,
    subtitle: String? = null,
    onClick: () -> Unit
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
             tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun SettingsTileToggle(
    title: String,
    subtitle: String? = null,
    icon: ImageVector,
    iconColor: Color = MaterialTheme.colorScheme.primary,
    checked: Boolean,
    onToggle: (Boolean) -> Unit
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
            checked = checked,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(checkedThumbColor = iconColor, checkedTrackColor = iconColor.copy(alpha = 0.3f))
        )
    }
}

private fun exportCsv(context: Context, viewModel: MainViewModel) {
    // Build CSV from in-memory transactions
    val sb = StringBuilder()
    sb.appendLine("Date,Type,Title,Amount,Category,Account,Notes,Tags")
    viewModel.allTransactions.value.forEach { t ->
        val cat = viewModel.getCategoryById(t.categoryId)?.name ?: "Other"
        val acc = viewModel.getAccountById(t.accountId)?.name ?: "Unknown"
        val date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date(t.date))
        sb.appendLine("$date,${t.type},\"${t.title}\",${t.amount},\"$cat\",\"$acc\",\"${t.notes}\",\"${t.tags}\"")
    }

    try {
        val file = java.io.File(context.getExternalFilesDir(null), "flashtrack_export_${System.currentTimeMillis()}.csv")
        file.writeText(sb.toString())
        val uri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Export CSV"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
