package com.devx.flashtrack.ui.screens.transaction

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.devx.flashtrack.data.local.entity.AccountEntity
import com.devx.flashtrack.data.local.entity.TransactionEntity
import com.devx.flashtrack.data.local.entity.TransactionType
import com.devx.flashtrack.ui.components.iconEmojiFor
import com.devx.flashtrack.ui.theme.*
import com.devx.flashtrack.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    viewModel: MainViewModel,
    initialType: String = "EXPENSE",
    onBack: () -> Unit
) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val accounts   by viewModel.accounts.collectAsStateWithLifecycle()

    var selectedTab        by remember { mutableIntStateOf(when (initialType) { "INCOME" -> 1; "TRANSFER" -> 2; else -> 0 }) }
    var amount             by remember { mutableStateOf("") }
    var title              by remember { mutableStateOf("") }
    var notes              by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableLongStateOf(0L) }
    var selectedAccountId  by remember { mutableLongStateOf(0L) }
    var selectedToAccountId by remember { mutableLongStateOf(0L) }
    var selectedDate       by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var tags               by remember { mutableStateOf(setOf<String>()) }
    var isRecurring        by remember { mutableStateOf(false) }
    var recurringInterval  by remember { mutableStateOf("MONTHLY") }

    var showCategorySheet  by remember { mutableStateOf(false) }
    var showAccountSheet   by remember { mutableStateOf(false) }
    var showToAccountSheet by remember { mutableStateOf(false) }
    var showDatePicker     by remember { mutableStateOf(false) }

    val tabTypes  = listOf("EXPENSE", "INCOME", "TRANSFER")
    val tabColors = listOf(RedExpense, GreenIncome, BlueCard)
    val tabLabels = listOf("Expense", "Income", "Transfer")
    val activeColor = tabColors[selectedTab]

    val filteredCategories = categories.filter {
        when (selectedTab) {
            0    -> it.type in listOf("EXPENSE", "BOTH")
            1    -> it.type in listOf("INCOME",  "BOTH")
            else -> it.type == "BOTH"
        }
    }

    val quickTags = listOf("amazon","netflix","zomato","grocery","bills","travel","medical","emi","vacation")

    LaunchedEffect(accounts) {
        if (selectedAccountId == 0L) selectedAccountId = accounts.firstOrNull()?.id ?: 0L
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Add Transaction", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Rounded.Close, "Close") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Tab Row ──────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                tabLabels.forEachIndexed { i, label ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selectedTab == i) tabColors[i] else Color.Transparent)
                            .clickable { selectedTab = i }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (selectedTab == i) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == i) Color.White
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── Amount ───────────────────────────────────────────────────────
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Enter Amount", style = MaterialTheme.typography.bodySmall,
                     color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Text("₹", style = MaterialTheme.typography.displaySmall,
                         fontWeight = FontWeight.Black, color = activeColor)
                    Spacer(Modifier.width(4.dp))
                    BasicTextField(
                        value = amount,
                        onValueChange = { v -> if (v.matches(Regex("^\\d*\\.?\\d{0,2}$"))) amount = v },
                        textStyle = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Black, color = activeColor, textAlign = TextAlign.Start
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        decorationBox = { inner ->
                            Box {
                                if (amount.isEmpty()) {
                                    Text("0", style = MaterialTheme.typography.displaySmall,
                                         fontWeight = FontWeight.Black, color = activeColor.copy(alpha = 0.3f))
                                }
                                inner()
                            }
                        },
                        modifier = Modifier.widthIn(min = 80.dp, max = 240.dp)
                    )
                }
                HorizontalDivider(
                    color = activeColor.copy(alpha = 0.5f), thickness = 2.dp,
                    modifier = Modifier.padding(horizontal = 40.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── Fields ───────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                InlineTextField(icon = "📝", label = "Title",
                    value = title, placeholder = "What's this for?",
                    onValueChange = { title = it })

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))

                FieldRow(icon = "🗂️", label = "Category", onClick = { showCategorySheet = true }) {
                    val cat = filteredCategories.find { it.id == selectedCategoryId }
                    Text(
                        text = if (cat != null) "${iconEmojiFor(cat.iconName)}  ${cat.name}" else "Select Category",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (cat != null) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Icon(Icons.Rounded.ChevronRight, null,
                         tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))

                FieldRow(icon = "🏦", label = "Account", onClick = { showAccountSheet = true }) {
                    val acc = accounts.find { it.id == selectedAccountId }
                    Text(
                        text = acc?.name ?: "Select Account",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (acc != null) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Icon(Icons.Rounded.ChevronRight, null,
                         tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                }

                if (selectedTab == 2) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
                    FieldRow(icon = "➡️", label = "To Account", onClick = { showToAccountSheet = true }) {
                        val acc = accounts.find { it.id == selectedToAccountId }
                        Text(
                            text = acc?.name ?: "Select Account",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (acc != null) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Icon(Icons.Rounded.ChevronRight, null,
                             tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))

                FieldRow(icon = "📅", label = "Date", onClick = { showDatePicker = true }) {
                    Text(
                        text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(selectedDate)),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Icon(Icons.Rounded.ChevronRight, null,
                         tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))

                InlineTextField(icon = "💬", label = "Notes",
                    value = notes, placeholder = "Add details…",
                    onValueChange = { notes = it })
            }

            // ── Tags ─────────────────────────────────────────────────────────
            Spacer(Modifier.height(20.dp))
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text("Tags", style = MaterialTheme.typography.labelMedium,
                     fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(quickTags) { tag ->
                        FilterChip(
                            selected = tag in tags,
                            onClick  = { tags = if (tag in tags) tags - tag else tags + tag },
                            label    = { Text(tag, style = MaterialTheme.typography.labelSmall) },
                            colors   = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = activeColor.copy(alpha = 0.2f),
                                selectedLabelColor     = activeColor
                            )
                        )
                    }
                }
            }

            // ── Recurring ─────────────────────────────────────────────────────
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("🔁", fontSize = 16.sp)
                    Text("Recurring", style = MaterialTheme.typography.bodyMedium)
                }
                Switch(
                    checked = isRecurring, onCheckedChange = { isRecurring = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = activeColor, checkedTrackColor = activeColor.copy(alpha = 0.3f))
                )
            }

            if (isRecurring) {
                Spacer(Modifier.height(10.dp))
                LazyRow(modifier = Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf("DAILY","WEEKLY","MONTHLY","YEARLY")) { iv ->
                        FilterChip(
                            selected = recurringInterval == iv,
                            onClick  = { recurringInterval = iv },
                            label    = { Text(iv.lowercase().replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.labelSmall) },
                            colors   = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = activeColor.copy(alpha = 0.2f),
                                selectedLabelColor     = activeColor
                            )
                        )
                    }
                }
            }

            // ── Save ──────────────────────────────────────────────────────────
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    val amt = amount.toDoubleOrNull()?.takeIf { it > 0 } ?: return@Button
                    val catId = selectedCategoryId.takeIf { it > 0L }
                        ?: filteredCategories.lastOrNull()?.id ?: 1L
                    viewModel.addTransaction(TransactionEntity(
                        type              = TransactionType.valueOf(tabTypes[selectedTab]),
                        amount            = amt,
                        categoryId        = catId,
                        accountId         = selectedAccountId,
                        toAccountId       = if (selectedTab == 2) selectedToAccountId.takeIf { it > 0L } else null,
                        title             = title.ifBlank { tabLabels[selectedTab] },
                        notes             = notes,
                        tags              = tags.joinToString(","),
                        date              = selectedDate,
                        isRecurring       = isRecurring,
                        recurringInterval = if (isRecurring) recurringInterval else null
                    ))
                    onBack()
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = activeColor),
                shape  = RoundedCornerShape(16.dp)
            ) {
                Text("Save ${tabLabels[selectedTab]}", style = MaterialTheme.typography.titleMedium,
                     fontWeight = FontWeight.Bold,
                     color = if (selectedTab == 1) Color.Black else Color.White)
            }
            Spacer(Modifier.height(40.dp))
        }
    }

    // ── Category sheet ────────────────────────────────────────────────────────
    if (showCategorySheet) {
        ModalBottomSheet(onDismissRequest = { showCategorySheet = false }) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Select Category", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement   = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.heightIn(max = 420.dp)
                ) {
                    items(filteredCategories, key = { it.id }) { cat ->
                        val catColor = runCatching {
                            Color(android.graphics.Color.parseColor(cat.colorHex))
                        }.getOrDefault(activeColor)
                        val isSelected = selectedCategoryId == cat.id
                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) catColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
                                .border(if (isSelected) BorderStroke(1.5.dp, catColor) else BorderStroke(0.dp, Color.Transparent), RoundedCornerShape(12.dp))
                                .clickable { selectedCategoryId = cat.id; showCategorySheet = false }
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(iconEmojiFor(cat.iconName), fontSize = 24.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(cat.name, style = MaterialTheme.typography.labelSmall,
                                 textAlign = TextAlign.Center, maxLines = 2)
                        }
                    }
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }

    // ── Account pickers ───────────────────────────────────────────────────────
    if (showAccountSheet) {
        AccountPickerSheet("Select Account", accounts, selectedAccountId, null,
            onPick = { selectedAccountId = it; showAccountSheet = false },
            onDismiss = { showAccountSheet = false })
    }
    if (showToAccountSheet) {
        AccountPickerSheet("Transfer To", accounts, selectedToAccountId, selectedAccountId,
            onPick = { selectedToAccountId = it; showToAccountSheet = false },
            onDismiss = { showToAccountSheet = false })
    }

    // ── Date picker ───────────────────────────────────────────────────────────
    if (showDatePicker) {
        val dateState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let { selectedDate = it }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = dateState) }
    }
}

// ── Reusable sub-composables ──────────────────────────────────────────────────

@Composable
private fun InlineTextField(icon: String, label: String, value: String,
                             placeholder: String, onValueChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(icon, fontSize = 16.sp)
        Text(label, style = MaterialTheme.typography.bodySmall,
             color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(72.dp))
        TextField(
            value = value, onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor   = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor   = Color.Transparent, unfocusedIndicatorColor = Color.Transparent
            ),
            singleLine = true, modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun FieldRow(icon: String, label: String, onClick: () -> Unit,
                     content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(icon, fontSize = 16.sp)
        Text(label, style = MaterialTheme.typography.bodySmall,
             color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(72.dp))
        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically, content = content)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountPickerSheet(title: String, accounts: List<AccountEntity>,
                                 selectedId: Long, excludeId: Long?,
                                 onPick: (Long) -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            accounts.filter { excludeId == null || it.id != excludeId }.forEach { acc ->
                val isSelected = acc.id == selectedId
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                        .clickable { onPick(acc.id) }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(iconEmojiFor(acc.iconName), fontSize = 22.sp)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(acc.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text("₹${"%,.0f".format(acc.balance)}", style = MaterialTheme.typography.bodySmall,
                             color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (isSelected) Icon(Icons.Rounded.CheckCircle, null, tint = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}
