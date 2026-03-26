package com.devx.flashtrack.ui.screens.accounts

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.devx.flashtrack.data.local.entity.*
import com.devx.flashtrack.ui.components.*
import com.devx.flashtrack.ui.theme.*
import com.devx.flashtrack.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val showBalance by viewModel.showBalance.collectAsStateWithLifecycle()
    val totalBalance by viewModel.totalBalance.collectAsStateWithLifecycle()
    val totalCredit by viewModel.totalCredit.collectAsStateWithLifecycle()

    var showAddSheet by remember { mutableStateOf(false) }
    var editingAccount by remember { mutableStateOf<AccountEntity?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Accounts", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBackIosNew, "Back") } },
                actions = {
                    IconButton(onClick = { viewModel.toggleBalance() }) {
                        Icon(
                            if (showBalance) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                            "Toggle", tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = GreenIncome,
                contentColor = Color.Black,
                shape = RoundedCornerShape(16.dp)
            ) { Icon(Icons.Rounded.Add, "Add Account") }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ─── Summary header ───────────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryCard(
                        label = "Total Balance",
                        amount = totalBalance ?: 0.0,
                        color = GreenIncome,
                        emoji = "💰",
                        showBalance = showBalance,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCard(
                        label = "Credit Available",
                        amount = totalCredit ?: 0.0,
                        color = BlueCard,
                        emoji = "💳",
                        showBalance = showBalance,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ─── Group by type ────────────────────────────────────────────────
            val grouped = accounts.groupBy { it.type }
            val typeOrder = listOf(AccountType.BANK, AccountType.WALLET, AccountType.CASH, AccountType.CREDIT_CARD)
            val typeLabels = mapOf(
                AccountType.BANK to "🏦 Bank Accounts",
                AccountType.WALLET to "👛 Wallets",
                AccountType.CASH to "💵 Cash",
                AccountType.CREDIT_CARD to "💳 Credit Cards"
            )

            typeOrder.forEach { type ->
                val accsOfType = grouped[type]
                if (!accsOfType.isNullOrEmpty()) {
                    item {
                        Text(
                            text = typeLabels[type] ?: type.name,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    items(accsOfType, key = { it.id }) { account ->
                        AccountListCard(
                            account = account,
                            showBalance = showBalance,
                            onEdit = { editingAccount = account },
                            onDelete = { viewModel.deleteAccount(account) }
                        )
                    }
                }
            }

            if (accounts.isEmpty()) {
                item {
                    EmptyState("🏦", "No accounts yet", "Add your bank accounts,\nwallets & cards")
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    if (showAddSheet || editingAccount != null) {
        AddEditAccountSheet(
            existing = editingAccount,
            onDismiss = { showAddSheet = false; editingAccount = null },
            onSave = { account ->
                if (editingAccount != null) viewModel.updateAccount(account)
                else viewModel.addAccount(account)
                showAddSheet = false; editingAccount = null
            }
        )
    }
}

@Composable
private fun SummaryCard(
    label: String, amount: Double, color: Color, emoji: String,
    showBalance: Boolean, modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.1f))
            .border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(emoji, fontSize = 20.sp)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = if (showBalance) "₹${"%,.0f".format(amount)}" else "₹ ••••",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun AccountListCard(
    account: AccountEntity,
    showBalance: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val color = try { Color(android.graphics.Color.parseColor(account.colorHex)) }
                catch (e: Exception) { GreenIncome }
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Icon circle
            Box(
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) { Text(iconEmojiFor(account.iconName), fontSize = 22.sp) }

            Column(modifier = Modifier.weight(1f)) {
                Text(account.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    account.type.name.replace("_", " "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (account.type == AccountType.CREDIT_CARD && account.creditLimit > 0) {
                    Spacer(Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { ((account.creditLimit - account.availableCredit) / account.creditLimit).toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                        color = BlueCard,
                        trackColor = BlueCard.copy(alpha = 0.2f)
                    )
                    Text(
                        "₹${"%,.0f".format(account.availableCredit)} available of ₹${"%,.0f".format(account.creditLimit)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = BlueCard
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (showBalance) "₹${"%,.0f".format(account.balance)}" else "₹ ••••",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (account.balance >= 0) MaterialTheme.colorScheme.onSurface else RedExpense
                )
                if (account.isDefault) {
                    Text("Default", style = MaterialTheme.typography.labelSmall, color = GreenIncome)
                }
            }

            Box {
                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Rounded.MoreVert, null, modifier = Modifier.size(18.dp),
                         tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = { showMenu = false; onEdit() },
                        leadingIcon = { Icon(Icons.Rounded.Edit, null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = RedExpense) },
                        onClick = { showMenu = false; onDelete() },
                        leadingIcon = { Icon(Icons.Rounded.Delete, null, tint = RedExpense) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditAccountSheet(
    existing: AccountEntity?,
    onDismiss: () -> Unit,
    onSave: (AccountEntity) -> Unit
) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var type by remember { mutableStateOf(existing?.type ?: AccountType.BANK) }
    var balance by remember { mutableStateOf(existing?.balance?.toString() ?: "") }
    var creditLimit by remember { mutableStateOf(existing?.creditLimit?.toString() ?: "") }
    var availableCredit by remember { mutableStateOf(existing?.availableCredit?.toString() ?: "") }
    var dueDate by remember { mutableIntStateOf(existing?.dueDate ?: 15) }
    var selectedColor by remember { mutableStateOf(existing?.colorHex ?: "#4CAF50") }
    var selectedIcon by remember { mutableStateOf(existing?.iconName ?: "account_balance") }

    val accountIcons = listOf(
        "account_balance" to "🏦",
        "account_balance_wallet" to "👛",
        "payments" to "💰",
        "credit_card" to "💳",
        "home" to "🏠"
    )
    val paletteColors = listOf("#4CAF50","#2196F3","#FF5722","#9C27B0","#FF9800","#00BCD4","#E91E63","#607D8B")

    ModalBottomSheet(onDismissRequest = onDismiss, modifier = Modifier.fillMaxHeight(0.9f)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                if (existing != null) "Edit Account" else "Add Account",
                style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("Account Name") }, modifier = Modifier.fillMaxWidth(),
                singleLine = true, shape = RoundedCornerShape(12.dp)
            )

            // Type selector
            Text("Account Type", style = MaterialTheme.typography.labelMedium,
                 color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AccountType.values().forEach { t ->
                    val label = when(t) { AccountType.BANK -> "Bank"; AccountType.WALLET -> "Wallet"; AccountType.CASH -> "Cash"; AccountType.CREDIT_CARD -> "Credit" }
                    FilterChip(
                        selected = type == t, onClick = { type = t },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            OutlinedTextField(
                value = balance, onValueChange = { balance = it },
                label = { Text(if (type == AccountType.CREDIT_CARD) "Current Balance (owed)" else "Current Balance") },
                prefix = { Text("₹") }, modifier = Modifier.fillMaxWidth(),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
                singleLine = true, shape = RoundedCornerShape(12.dp)
            )

            if (type == AccountType.CREDIT_CARD) {
                OutlinedTextField(
                    value = creditLimit, onValueChange = { creditLimit = it },
                    label = { Text("Credit Limit") }, prefix = { Text("₹") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = availableCredit, onValueChange = { availableCredit = it },
                    label = { Text("Available Credit") }, prefix = { Text("₹") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = dueDate.toString(), onValueChange = { dueDate = it.toIntOrNull() ?: 15 },
                    label = { Text("Due Date (day of month)") }, modifier = Modifier.fillMaxWidth(),
                    singleLine = true, shape = RoundedCornerShape(12.dp),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )
            }

            // Icon picker
            Text("Icon", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                accountIcons.forEach { (icon, emoji) ->
                    Box(
                        modifier = Modifier.size(44.dp).clip(RoundedCornerShape(10.dp))
                            .background(if (selectedIcon == icon) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                            .border(if (selectedIcon == icon) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else BorderStroke(0.dp, Color.Transparent), RoundedCornerShape(10.dp))
                            .clickable { selectedIcon = icon },
                        contentAlignment = Alignment.Center
                    ) { Text(emoji, fontSize = 20.sp) }
                }
            }

            // Color picker
            Text("Color", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                paletteColors.forEach { hex ->
                    val c = try { Color(android.graphics.Color.parseColor(hex)) } catch(e:Exception){ GreenIncome }
                    Box(
                        modifier = Modifier.size(32.dp).clip(CircleShape)
                            .background(c)
                            .border(if (selectedColor == hex) BorderStroke(3.dp, Color.White) else BorderStroke(0.dp, Color.Transparent), CircleShape)
                            .clickable { selectedColor = hex }
                    )
                }
            }

            Button(
                onClick = {
                    val bal = balance.toDoubleOrNull() ?: 0.0
                    onSave(AccountEntity(
                        id = existing?.id ?: 0,
                        name = name.ifBlank { "Account" },
                        type = type,
                        balance = bal,
                        colorHex = selectedColor,
                        iconName = selectedIcon,
                        creditLimit = creditLimit.toDoubleOrNull() ?: 0.0,
                        availableCredit = availableCredit.toDoubleOrNull() ?: (creditLimit.toDoubleOrNull() ?: 0.0),
                        dueDate = dueDate
                    ))
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenIncome),
                shape = RoundedCornerShape(14.dp)
            ) { Text("Save Account", color = Color.Black, fontWeight = FontWeight.Bold) }

            Spacer(Modifier.height(32.dp))
        }
    }
}
