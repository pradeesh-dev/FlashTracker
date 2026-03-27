package com.devx.flashtrack.ui.screens.debts

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.devx.flashtrack.data.local.entity.DebtEntity
import com.devx.flashtrack.data.local.entity.DebtType
import com.devx.flashtrack.ui.components.EmptyState
import com.devx.flashtrack.ui.theme.*
import com.devx.flashtrack.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtsScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val debts         by viewModel.debts.collectAsStateWithLifecycle()
    val totalLent     by viewModel.totalLent.collectAsStateWithLifecycle()
    val totalBorrowed by viewModel.totalBorrowed.collectAsStateWithLifecycle()

    var selectedTab  by remember { mutableIntStateOf(0) }
    var showAddSheet by remember { mutableStateOf(false) }
    var settlingDebt by remember { mutableStateOf<DebtEntity?>(null) }

    val lent     = debts.filter { it.type == DebtType.LENT     && !it.isSettled }
    val borrowed = debts.filter { it.type == DebtType.BORROWED && !it.isSettled }
    val settled  = debts.filter { it.isSettled }
    val current  = when (selectedTab) { 0 -> lent; 1 -> borrowed; else -> settled }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Debts & IOUs", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBackIosNew, "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddSheet = true }, containerColor = PurpleDebt,
                                 contentColor = Color.White, shape = RoundedCornerShape(16.dp)) {
                Icon(Icons.Rounded.Add, "Add")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Summary
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DebtSummaryBox("You'll Receive", totalLent ?: 0.0,     GreenIncome, "🤝", Modifier.weight(1f))
                    DebtSummaryBox("You Owe",        totalBorrowed ?: 0.0, RedExpense,  "💸", Modifier.weight(1f))
                }
            }

            // Tabs
            item {
                Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                                       .background(MaterialTheme.colorScheme.surfaceVariant)) {
                    listOf("Lent (${lent.size})", "Borrowed (${borrowed.size})", "Settled (${settled.size})")
                        .forEachIndexed { i, label ->
                            Box(
                                modifier = Modifier.weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (selectedTab == i) PurpleDebt.copy(alpha = 0.85f) else Color.Transparent)
                                    .clickable { selectedTab = i }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(label, style = MaterialTheme.typography.labelSmall,
                                     fontWeight = if (selectedTab == i) FontWeight.Bold else FontWeight.Normal,
                                     color = if (selectedTab == i) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                }
            }

            if (current.isEmpty()) {
                item {
                    EmptyState(
                        emoji    = if (selectedTab == 2) "✅" else "🤝",
                        title    = when (selectedTab) { 0 -> "No money lent"; 1 -> "No money borrowed"; else -> "Nothing settled yet" },
                        subtitle = "Tap + to add an IOU entry"
                    )
                }
            } else {
                items(current, key = { it.id }) { debt ->
                    DebtCard(debt = debt, onSettle = { settlingDebt = debt }, onDelete = { viewModel.deleteDebt(debt) })
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    if (showAddSheet) {
        AddDebtSheet(onDismiss = { showAddSheet = false }, onSave = { viewModel.addDebt(it); showAddSheet = false })
    }

    settlingDebt?.let { debt ->
        SettleDialog(debt = debt, onDismiss = { settlingDebt = null },
                     onSettle = { amount -> viewModel.settleDebt(debt, amount); settlingDebt = null })
    }
}

@Composable
private fun DebtSummaryBox(label: String, amount: Double, color: Color, emoji: String, modifier: Modifier) {
    Box(modifier = modifier.clip(RoundedCornerShape(16.dp)).background(color.copy(alpha = 0.1f))
                           .border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(16.dp)).padding(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(emoji, fontSize = 20.sp)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("₹${"%,.0f".format(amount)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
private fun DebtCard(debt: DebtEntity, onSettle: () -> Unit, onDelete: () -> Unit) {
    val color     = if (debt.type == DebtType.LENT) GreenIncome else RedExpense
    val remaining = debt.amount - debt.settledAmount
    val progress  = if (debt.amount > 0) (debt.settledAmount / debt.amount).toFloat().coerceIn(0f, 1f) else 0f

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border   = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center) {
                        Text(debt.personName.take(1).uppercase(), style = MaterialTheme.typography.titleMedium,
                             fontWeight = FontWeight.Bold, color = color)
                    }
                    Column {
                        Text(debt.personName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Text(if (debt.type == DebtType.LENT) "You lent" else "You borrowed",
                             style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("₹${"%,.0f".format(debt.amount)}", style = MaterialTheme.typography.titleMedium,
                         fontWeight = FontWeight.Bold, color = color)
                    Text(SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(debt.date)),
                         style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            if (debt.notes.isNotBlank()) {
                Text(debt.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (debt.settledAmount > 0) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Settled: ₹${"%,.0f".format(debt.settledAmount)}", style = MaterialTheme.typography.labelSmall, color = GreenIncome)
                        Text("Remaining: ₹${"%,.0f".format(remaining)}", style = MaterialTheme.typography.labelSmall, color = color)
                    }
                    LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)), color = GreenIncome, trackColor = GreenIncome.copy(alpha = 0.15f))
                }
            }

            if (!debt.isSettled) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onDelete, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp),
                                   border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))) {
                        Text("Delete", style = MaterialTheme.typography.labelMedium)
                    }
                    Button(onClick = onSettle, modifier = Modifier.weight(2f), shape = RoundedCornerShape(10.dp),
                           colors = ButtonDefaults.buttonColors(containerColor = PurpleDebt)) {
                        Text("Settle Up 💜", style = MaterialTheme.typography.labelMedium)
                    }
                }
            } else {
                Row(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(GreenIncome.copy(alpha = 0.1f)).padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Rounded.CheckCircle, null, tint = GreenIncome, modifier = Modifier.size(14.dp))
                    Text("Fully Settled", style = MaterialTheme.typography.labelSmall, color = GreenIncome)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddDebtSheet(onDismiss: () -> Unit, onSave: (DebtEntity) -> Unit) {
    var personName by remember { mutableStateOf("") }
    var amount     by remember { mutableStateOf("") }
    var notes      by remember { mutableStateOf("") }
    var type       by remember { mutableStateOf(DebtType.LENT) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(24.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Add IOU", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
                listOf(DebtType.LENT to "I Lent", DebtType.BORROWED to "I Borrowed").forEach { (t, label) ->
                    Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp))
                                           .background(if (type == t) PurpleDebt.copy(alpha = 0.85f) else Color.Transparent)
                                           .clickable { type = t }.padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center) {
                        Text(label, style = MaterialTheme.typography.labelLarge,
                             fontWeight = if (type == t) FontWeight.Bold else FontWeight.Normal,
                             color = if (type == t) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            OutlinedTextField(value = personName, onValueChange = { personName = it },
                label = { Text("Person's Name") }, modifier = Modifier.fillMaxWidth(),
                singleLine = true, shape = RoundedCornerShape(12.dp))
            OutlinedTextField(value = amount, onValueChange = { amount = it },
                label = { Text("Amount") }, prefix = { Text("₹") }, modifier = Modifier.fillMaxWidth(),
                singleLine = true, shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
            OutlinedTextField(value = notes, onValueChange = { notes = it },
                label = { Text("Notes (optional)") }, modifier = Modifier.fillMaxWidth(),
                singleLine = true, shape = RoundedCornerShape(12.dp))

            Button(
                onClick = {
                    val amt = amount.toDoubleOrNull()?.takeIf { it > 0 } ?: return@Button
                    if (personName.isBlank()) return@Button
                    onSave(DebtEntity(type = type, personName = personName, amount = amt, notes = notes))
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PurpleDebt),
                shape = RoundedCornerShape(14.dp)
            ) { Text("Save IOU", fontWeight = FontWeight.Bold) }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SettleDialog(debt: DebtEntity, onDismiss: () -> Unit, onSettle: (Double) -> Unit) {
    val remaining = debt.amount - debt.settledAmount
    var amount    by remember { mutableStateOf(remaining.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Settle with ${debt.personName}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Remaining: ₹${"%,.2f".format(remaining)}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(value = amount, onValueChange = { amount = it },
                    label = { Text("Settlement Amount") }, prefix = { Text("₹") },
                    singleLine = true, shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
            }
        },
        confirmButton = {
            Button(onClick = { onSettle(amount.toDoubleOrNull() ?: remaining) },
                   colors = ButtonDefaults.buttonColors(containerColor = PurpleDebt)) { Text("Settle") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
