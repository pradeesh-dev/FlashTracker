package com.devx.flashtrack.ui.screens.transaction

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.devx.flashtrack.data.local.entity.*
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
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(when (initialType) { "INCOME" -> 1; "TRANSFER" -> 2; else -> 0 }) }
    var amount by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableLongStateOf(0L) }
    var selectedAccountId by remember { mutableLongStateOf(accounts.firstOrNull()?.id ?: 0L) }
    var selectedToAccountId by remember { mutableLongStateOf(0L) }
    var selectedDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var tags by remember { mutableStateOf(setOf<String>()) }
    var customTag by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showCategorySheet by remember { mutableStateOf(false) }
    var showAccountSheet by remember { mutableStateOf(false) }
    var showToAccountSheet by remember { mutableStateOf(false) }
    var isRecurring by remember { mutableStateOf(false) }
    var recurringInterval by remember { mutableStateOf("MONTHLY") }

    val tabTypes = listOf("EXPENSE", "INCOME", "TRANSFER")
    val tabColors = listOf(RedExpense, GreenIncome, BlueCard)
    val tabLabels = listOf("Expense", "Income", "Transfer")
    val activeColor = tabColors[selectedTab]

    val filteredCategories = when (selectedTab) {
        0 -> categories.filter { it.type in listOf("EXPENSE", "BOTH") }
        1 -> categories.filter { it.type in listOf("INCOME", "BOTH") }
        else -> categories.filter { it.type == "BOTH" }
    }

    val quickTags = listOf("amazon", "netflix", "zomato", "grocery", "bills", "travel", "medical", "emi")

    LaunchedEffect(accounts) {
        if (selectedAccountId == 0L) selectedAccountId = accounts.firstOrNull()?.id ?: 0L
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Add Transaction", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.Close, "Close")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // ─── Tab row ────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                horizontalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                tabLabels.forEachIndexed { index, label ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (selectedTab == index) tabColors[index].copy(alpha = 0.9f)
                                else Color.Transparent
                            )
                            .clickable { selectedTab = index }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == index) Color.White
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ─── Amount input ────────────────────────────────────────────────
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Enter Amount",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "₹",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black,
                        color = activeColor
                    )
                    BasicAmountField(
                        value = amount,
                        onValueChange = { amount = it },
                        color = activeColor
                    )
                }
                HorizontalDivider(
                    color = activeColor.copy(alpha = 0.5f),
                    thickness = 2.dp,
                    modifier = Modifier.padding(horizontal = 40.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            // ─── Fields card ─────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                // Title
                FieldRow(icon = "📝", label = "Title") {
                    TextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = { Text("What's this for?", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // Category
                FieldRow(icon = "🗂️", label = "Category", onClick = { showCategorySheet = true }) {
                    val cat = filteredCategories.find { it.id == selectedCategoryId }
                    Text(
                        text = if (cat != null) "${iconEmojiFor(cat.iconName)} ${cat.name}" else "Select Category",
                        color = if (cat != null) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Icon(Icons.Rounded.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // Account
                FieldRow(icon = "🏦", label = "Account", onClick = { showAccountSheet = true }) {
                    val acc = accounts.find { it.id == selectedAccountId }
                    Text(
                        text = acc?.name ?: "Select Account",
                        color = if (acc != null) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Icon(Icons.Rounded.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                if (selectedTab == 2) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    FieldRow(icon = "➡️", label = "To Account", onClick = { showToAccountSheet = true }) {
                        val acc = accounts.find { it.id == selectedToAccountId }
                        Text(
                            text = acc?.name ?: "Select Account",
                            color = if (acc != null) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Icon(Icons.Rounded.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // Date
                FieldRow(icon = "📅", label = "Date", onClick = { showDatePicker = true }) {
                    Text(
                        text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(selectedDate)),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Icon(Icons.Rounded.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // Notes
                FieldRow(icon = "💬", label = "Notes") {
                    TextField(
                        value = notes,
                        onValueChange = { notes = it },
                        placeholder = { Text("Add details...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // ─── Tags ────────────────────────────────────────────────────────
            Spacer(Modifier.height(20.dp))
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text("Tags", style = MaterialTheme.typography.labelMedium,
                     color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(quickTags) { tag ->
                        FilterChip(
                            selected = tags.contains(tag),
                            onClick = {
                                tags = if (tags.contains(tag)) tags - tag else tags + tag
                            },
                            label = { Text(tag, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = activeColor.copy(alpha = 0.2f),
                                selectedLabelColor = activeColor
                            )
                        )
                    }
                }
            }

            // ─── Recurring ───────────────────────────────────────────────────
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
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("🔁", fontSize = 16.sp)
                    Text("Recurring", style = MaterialTheme.typography.bodyMedium,
                         color = MaterialTheme.colorScheme.onSurface)
                }
                Switch(
                    checked = isRecurring,
                    onCheckedChange = { isRecurring = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = activeColor, checkedTrackColor = activeColor.copy(alpha = 0.3f))
                )
            }

            if (isRecurring) {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("WEEKLY", "MONTHLY", "YEARLY").forEach { interval ->
                        FilterChip(
                            selected = recurringInterval == interval,
                            onClick = { recurringInterval = interval },
                            label = { Text(interval.lowercase().replaceFirstChar { it.uppercase() },
                                          style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = activeColor.copy(alpha = 0.2f),
                                selectedLabelColor = activeColor
                            )
                        )
                    }
                }
            }

            // ─── Save button ─────────────────────────────────────────────────
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    val amt = amount.toDoubleOrNull() ?: return@Button
                    if (amt <= 0) return@Button
                    val t = TransactionEntity(
                        type = TransactionType.valueOf(tabTypes[selectedTab]),
                        amount = amt,
                        categoryId = selectedCategoryId.takeIf { it > 0 } ?: 16,
                        accountId = selectedAccountId,
                        toAccountId = if (selectedTab == 2) selectedToAccountId.takeIf { it > 0 } else null,
                        title = title.ifBlank { tabLabels[selectedTab] },
                        notes = notes,
                        tags = tags.joinToString(","),
                        date = selectedDate,
                        isRecurring = isRecurring,
                        recurringInterval = if (isRecurring) recurringInterval else null
                    )
                    viewModel.addTransaction(t)
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = activeColor),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Save ${tabLabels[selectedTab]}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (selectedTab == 1) Color.Black else Color.White
                )
            }
            Spacer(Modifier.height(32.dp))
        }
    }

    // ─── Bottom sheets ────────────────────────────────────────────────────────

    if (showCategorySheet) {
        ModalBottomSheet(onDismissRequest = { showCategorySheet = false }) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Select Category", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                LazyVerticalGrid(
                    columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.heightIn(max = 400.dp)
                ) {
                    items(filteredCategories, key = { it.id }) { cat ->
                        val catColor = try { Color(android.graphics.Color.parseColor(cat.colorHex)) }
                                       catch (e: Exception) { activeColor }
                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (selectedCategoryId == cat.id) catColor.copy(alpha = 0.2f)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .border(
                                    if (selectedCategoryId == cat.id) BorderStroke(1.5.dp, catColor)
                                    else BorderStroke(0.dp, Color.Transparent),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    selectedCategoryId = cat.id
                                    showCategorySheet = false
                                }
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(iconEmojiFor(cat.iconName), fontSize = 24.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(cat.name, style = MaterialTheme.typography.labelSmall,
                                 textAlign = TextAlign.Center, maxLines = 2,
                                 color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }

    if (showAccountSheet || showToAccountSheet) {
        val isTo = showToAccountSheet
        ModalBottomSheet(onDismissRequest = { showAccountSheet = false; showToAccountSheet = false }) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(if (isTo) "Transfer To" else "Select Account",
                     style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                accounts.filter { if (isTo) it.id != selectedAccountId else true }.forEach { acc ->
                    val isSelected = if (isTo) selectedToAccountId == acc.id else selectedAccountId == acc.id
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                            .clickable {
                                if (isTo) selectedToAccountId = acc.id else selectedAccountId = acc.id
                                showAccountSheet = false; showToAccountSheet = false
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(iconEmojiFor(acc.iconName), fontSize = 24.sp)
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

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDate = it }
                    showDatePicker = false
                }) { Text("OK") }
            }
        ) { DatePicker(state = datePickerState) }
    }
}

@Composable
private fun FieldRow(
    icon: String,
    label: String,
    onClick: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = if (onClick != null) 16.dp else 0.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(icon, fontSize = 16.sp)
        Text(label, style = MaterialTheme.typography.bodySmall,
             color = MaterialTheme.colorScheme.onSurfaceVariant,
             modifier = Modifier.width(72.dp))
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

@Composable
private fun BasicAmountField(value: String, onValueChange: (String) -> Unit, color: Color) {
    BasicTextField(
        value = value,
        onValueChange = { new ->
            if (new.matches(Regex("^\\d*\\.?\\d{0,2}$"))) onValueChange(new)
        },
        textStyle = MaterialTheme.typography.displaySmall.copy(
            fontWeight = FontWeight.Black,
            color = color,
            textAlign = TextAlign.Center
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        decorationBox = { inner ->
            if (value.isEmpty()) {
                Text("0", style = MaterialTheme.typography.displaySmall,
                     fontWeight = FontWeight.Black, color = color.copy(alpha = 0.3f))
            }
            inner()
        },
        modifier = Modifier.widthIn(min = 80.dp, max = 260.dp)
    )
}

private fun <T> items(list: List<T>, key: (T) -> Any, itemContent: @Composable (T) -> Unit): androidx.compose.foundation.lazy.grid.LazyGridScope.() -> Unit = {
    items(list.size, key = { key(list[it]) }) { itemContent(list[it]) }
}

// Re-export for grid
@Composable fun LazyVerticalGrid(
    columns: androidx.compose.foundation.lazy.grid.GridCells,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: androidx.compose.foundation.lazy.grid.LazyGridScope.() -> Unit
) = androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
    columns = columns, modifier = modifier,
    horizontalArrangement = horizontalArrangement,
    verticalArrangement = verticalArrangement,
    content = content
)
