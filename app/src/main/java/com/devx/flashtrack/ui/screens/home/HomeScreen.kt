package com.devx.flashtrack.ui.screens.home

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
import com.devx.flashtrack.data.local.entity.TransactionType
import com.devx.flashtrack.ui.components.*
import com.devx.flashtrack.ui.navigation.Screen
import com.devx.flashtrack.ui.theme.*
import com.devx.flashtrack.viewmodel.MainViewModel
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onAddTransaction: (String) -> Unit,
    onNavigate: (String) -> Unit
) {
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val recentTxns by viewModel.recentTransactions.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val showBalance by viewModel.showBalance.collectAsStateWithLifecycle()
    val totalBalance by viewModel.totalBalance.collectAsStateWithLifecycle()
    val totalCredit by viewModel.totalCredit.collectAsStateWithLifecycle()
    val monthExpense by viewModel.currentMonthExpense.collectAsStateWithLifecycle()
    val monthIncome by viewModel.currentMonthIncome.collectAsStateWithLifecycle()

    val now = LocalDate.now()
    val greeting = when (now.hour) {
        in 5..11 -> "Good Morning ☀️"
        in 12..16 -> "Good Afternoon 🌤"
        in 17..20 -> "Good Evening 🌅"
        else -> "Good Night 🌙"
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Column {
                FlashTrackBottomBar(currentRoute = "home") { route ->
                    when (route) {
                        "more" -> onNavigate(Screen.Settings.route)
                        else -> onNavigate(route)
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAddTransaction("EXPENSE") },
                containerColor = GreenIncome,
                contentColor = Color.Black,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add", modifier = Modifier.size(28.dp))
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // ─── Header ────────────────────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = greeting,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "FlashTrack",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(onClick = { viewModel.toggleBalance() }) {
                            Icon(
                                imageVector = if (showBalance) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                                contentDescription = "Toggle balance",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { onNavigate(Screen.Settings.route) }) {
                            Icon(Icons.Rounded.Settings, contentDescription = "Settings",
                                 tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // ─── Balance Card ──────────────────────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF0D1F12), Color(0xFF0A1A20))
                            )
                        )
                        .border(1.dp, DarkBorder, RoundedCornerShape(24.dp))
                        .padding(24.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Total Balance",
                            style = MaterialTheme.typography.bodySmall,
                            color = DarkTextTertiary
                        )
                        AmountText(
                            amount = (totalBalance ?: 0.0),
                            showBalance = showBalance,
                            style = MaterialTheme.typography.displaySmall,
                            color = Color.White
                        )
                        Spacer(Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            MiniStat(
                                label = "This Month In",
                                amount = monthIncome ?: 0.0,
                                color = GreenIncome,
                                showBalance = showBalance,
                                modifier = Modifier.weight(1f)
                            )
                            MiniStat(
                                label = "This Month Out",
                                amount = monthExpense ?: 0.0,
                                color = RedExpense,
                                showBalance = showBalance,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if ((totalCredit ?: 0.0) > 0) {
                            Spacer(Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(BlueCard.copy(alpha = 0.12f))
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("💳", fontSize = 12.sp)
                                Text(
                                    text = "Available Credit: ${if (showBalance) "₹${"%,.0f".format(totalCredit ?: 0.0)}" else "₹ ••••"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BlueCard
                                )
                            }
                        }
                    }
                }
            }

            // ─── Quick actions ─────────────────────────────────────────────────
            item {
                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickAction("➕ Expense", RedExpense, Modifier.weight(1f)) { onAddTransaction("EXPENSE") }
                    QuickAction("💰 Income", GreenIncome, Modifier.weight(1f)) { onAddTransaction("INCOME") }
                    QuickAction("↔️ Transfer", BlueCard, Modifier.weight(1f)) { onAddTransaction("TRANSFER") }
                }
            }

            // ─── Accounts preview ──────────────────────────────────────────────
            item {
                Spacer(Modifier.height(24.dp))
                SectionHeader(
                    title = "My Accounts",
                    action = "View All",
                    onAction = { onNavigate(Screen.Accounts.route) },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(Modifier.height(12.dp))
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(accounts.take(5)) { account ->
                        val accentColor = try {
                            Color(android.graphics.Color.parseColor(account.colorHex))
                        } catch (e: Exception) { GreenIncome }
                        AccountCard(
                            name = account.name,
                            balance = account.balance,
                            type = account.type.name,
                            icon = iconEmojiFor(account.iconName),
                            accent = accentColor,
                            showBalance = showBalance
                        )
                    }
                    if (accounts.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .width(160.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { onNavigate(Screen.Accounts.route) }
                                    .padding(16.dp)
                            ) {
                                Text("+ Add Account", color = MaterialTheme.colorScheme.primary,
                                     fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // ─── Recent Transactions ───────────────────────────────────────────
            item {
                Spacer(Modifier.height(24.dp))
                SectionHeader(
                    title = "Recent Transactions",
                    action = "See All",
                    onAction = { onNavigate(Screen.Analysis.route) },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(Modifier.height(8.dp))
            }

            if (recentTxns.isEmpty()) {
                item {
                    EmptyState("💸", "No transactions yet", "Add your first transaction\nusing the + button below")
                }
            } else {
                items(recentTxns, key = { it.id }) { txn ->
                    val cat = categories.find { it.id == txn.categoryId }
                    TransactionRow(
                        transaction = txn,
                        categoryName = cat?.name ?: "Other",
                        categoryIcon = cat?.iconName ?: "category",
                        categoryColor = cat?.colorHex ?: "#9E9E9E",
                        showBalance = showBalance,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        thickness = 0.5.dp
                    )
                }
            }

            // ─── IOUs teaser ──────────────────────────────────────────────────
            item {
                Spacer(Modifier.height(24.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(PurpleDebt.copy(alpha = 0.1f))
                        .border(1.dp, PurpleDebt.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .clickable { onNavigate(Screen.Debts.route) }
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("🤝", fontSize = 24.sp)
                            Column {
                                Text("Debts & IOUs", style = MaterialTheme.typography.titleSmall,
                                     fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
                                Text("Track money lent & borrowed",
                                     style = MaterialTheme.typography.bodySmall,
                                     color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = PurpleDebt)
                    }
                }
                Spacer(Modifier.height(80.dp)) // FAB clearance
            }
        }
    }
}

@Composable
private fun MiniStat(
    label: String, amount: Double, color: Color,
    showBalance: Boolean, modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(color))
            Text(label, style = MaterialTheme.typography.labelSmall, color = DarkTextTertiary)
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = if (showBalance) "₹${"%,.0f".format(amount)}" else "₹ ••••",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun QuickAction(label: String, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

@Composable
private fun AccountCard(
    name: String, balance: Double, type: String, icon: String,
    accent: Color, showBalance: Boolean
) {
    Box(
        modifier = Modifier
            .width(160.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(colors = listOf(accent.copy(alpha = 0.2f), accent.copy(alpha = 0.05f)))
            )
            .border(1.dp, accent.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(icon, fontSize = 24.sp)
            Text(name, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                 maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            Text(
                text = if (showBalance) "₹${"%,.0f".format(balance)}" else "₹ ••••",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(type.replace("_", " "), style = MaterialTheme.typography.labelSmall, color = accent)
        }
    }
}

// Extension with modifier
@Composable
fun SectionHeader(title: String, action: String? = null, onAction: () -> Unit = {}, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
             color = MaterialTheme.colorScheme.onBackground)
        if (action != null) {
            TextButton(onClick = onAction) {
                Text(action, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun TransactionRow(
    transaction: com.devx.flashtrack.data.local.entity.TransactionEntity,
    categoryName: String,
    categoryIcon: String,
    categoryColor: String,
    showBalance: Boolean = true,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val color = try { Color(android.graphics.Color.parseColor(categoryColor)) } catch (e: Exception) { GreenIncome }
    val amountColor = when (transaction.type) {
        TransactionType.INCOME -> GreenIncome
        TransactionType.EXPENSE -> RedExpense
        TransactionType.TRANSFER -> BlueCard
    }
    val prefix = when (transaction.type) {
        TransactionType.INCOME -> "+"
        TransactionType.EXPENSE -> "-"
        TransactionType.TRANSFER -> "↔"
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp))
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) { Text(iconEmojiFor(categoryIcon), fontSize = 18.sp) }
        Column(modifier = Modifier.weight(1f)) {
            Text(transaction.title, style = MaterialTheme.typography.bodyMedium,
                 fontWeight = FontWeight.SemiBold, maxLines = 1,
                 overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                 color = MaterialTheme.colorScheme.onSurface)
            Text("$categoryName • ${formatDate(transaction.date)}",
                 style = MaterialTheme.typography.bodySmall,
                 color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(
            text = if (showBalance) "$prefix₹${"%,.0f".format(transaction.amount)}" else "₹••••",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = amountColor
        )
    }
}
