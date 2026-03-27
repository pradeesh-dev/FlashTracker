package com.devx.flashtrack.ui.screens.reminders

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.devx.flashtrack.data.local.entity.ReminderEntity
import com.devx.flashtrack.data.local.entity.ReminderInterval
import com.devx.flashtrack.ui.components.EmptyState
import com.devx.flashtrack.ui.theme.*
import com.devx.flashtrack.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val reminders   by viewModel.reminders.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAdd     by remember { mutableStateOf(false) }

    val active   = reminders.filter { it.isActive }
    val inactive = reminders.filter { !it.isActive }
    val current  = if (selectedTab == 0) active else inactive

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Reminders", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBackIosNew, "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }, containerColor = OrangeExpense,
                                 contentColor = Color.White, shape = RoundedCornerShape(16.dp)) {
                Icon(Icons.Rounded.Add, "Add")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
                    listOf("Active (${active.size})", "Inactive (${inactive.size})").forEachIndexed { i, label ->
                        Box(
                            modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp))
                                .background(if (selectedTab == i) OrangeExpense.copy(alpha = 0.9f) else Color.Transparent)
                                .clickable { selectedTab = i }.padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(label, style = MaterialTheme.typography.labelLarge,
                                 fontWeight = if (selectedTab == i) FontWeight.Bold else FontWeight.Normal,
                                 color = if (selectedTab == i) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            if (current.isEmpty()) {
                item { EmptyState("🔔", "No reminders", "Add reminders for bills,\nsubscriptions & recurring payments") }
            } else {
                items(current, key = { it.id }) { reminder ->
                    ReminderCard(reminder, onToggle = { viewModel.toggleReminder(reminder) },
                                 onDelete = { viewModel.deleteReminder(reminder) })
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    if (showAdd) {
        AddReminderSheet(onDismiss = { showAdd = false }, onSave = { viewModel.addReminder(it); showAdd = false })
    }
}

@Composable
private fun ReminderCard(reminder: ReminderEntity, onToggle: () -> Unit, onDelete: () -> Unit) {
    val fmt = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val isPast = reminder.nextDueDate < System.currentTimeMillis()
    val accent = if (!reminder.isActive) MaterialTheme.colorScheme.onSurfaceVariant
                 else if (isPast) RedExpense else OrangeExpense

    val intervalLabel = when (reminder.interval) {
        ReminderInterval.DAILY   -> "Daily"
        ReminderInterval.WEEKLY  -> "Weekly"
        ReminderInterval.MONTHLY -> "Monthly"
        ReminderInterval.YEARLY  -> "Yearly"
        ReminderInterval.ONCE    -> "Once"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border   = BorderStroke(1.dp, accent.copy(alpha = 0.3f))
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(accent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center) { Text("🔔", fontSize = 20.sp) }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(reminder.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                if (reminder.description.isNotBlank())
                    Text(reminder.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("📅 ${fmt.format(Date(reminder.nextDueDate))}", style = MaterialTheme.typography.labelSmall, color = accent)
                    Text("🔁 $intervalLabel", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (reminder.amount > 0)
                    Text("₹${"%,.0f".format(reminder.amount)}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = accent)
            }

            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Switch(checked = reminder.isActive, onCheckedChange = { onToggle() },
                       colors = SwitchDefaults.colors(checkedThumbColor = OrangeExpense, checkedTrackColor = OrangeExpense.copy(alpha = 0.3f)))
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Rounded.Delete, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddReminderSheet(onDismiss: () -> Unit, onSave: (ReminderEntity) -> Unit) {
    var title         by remember { mutableStateOf("") }
    var description   by remember { mutableStateOf("") }
    var amount        by remember { mutableStateOf("") }
    var interval      by remember { mutableStateOf(ReminderInterval.MONTHLY) }
    var selectedDate  by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showDatePick  by remember { mutableStateOf(false) }

    val allIntervals  = ReminderInterval.entries.toList()

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(24.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Add Reminder", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            OutlinedTextField(value = title, onValueChange = { title = it },
                label = { Text("Title (e.g. Netflix, Rent)") }, modifier = Modifier.fillMaxWidth(),
                singleLine = true, shape = RoundedCornerShape(12.dp))
            OutlinedTextField(value = description, onValueChange = { description = it },
                label = { Text("Description (optional)") }, modifier = Modifier.fillMaxWidth(),
                singleLine = true, shape = RoundedCornerShape(12.dp))
            OutlinedTextField(value = amount, onValueChange = { amount = it },
                label = { Text("Amount (optional)") }, prefix = { Text("₹") },
                modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))

            Text("Repeat", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(allIntervals) { iv ->
                    FilterChip(
                        selected = interval == iv, onClick = { interval = iv },
                        label = { Text(iv.name.lowercase().replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = OrangeExpense.copy(alpha = 0.2f), selectedLabelColor = OrangeExpense)
                    )
                }
            }

            OutlinedButton(onClick = { showDatePick = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Text("📅 Due: ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(selectedDate))}")
            }

            Button(
                onClick = {
                    if (title.isBlank()) return@Button
                    onSave(ReminderEntity(title = title, description = description,
                        amount = amount.toDoubleOrNull() ?: 0.0, interval = interval, nextDueDate = selectedDate))
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = OrangeExpense),
                shape    = RoundedCornerShape(14.dp)
            ) { Text("Save Reminder", fontWeight = FontWeight.Bold) }
            Spacer(Modifier.height(16.dp))
        }
    }

    if (showDatePick) {
        val dateState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)
        DatePickerDialog(
            onDismissRequest = { showDatePick = false },
            confirmButton = {
                TextButton(onClick = { dateState.selectedDateMillis?.let { selectedDate = it }; showDatePick = false }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePick = false }) { Text("Cancel") } }
        ) { DatePicker(state = dateState) }
    }
}
