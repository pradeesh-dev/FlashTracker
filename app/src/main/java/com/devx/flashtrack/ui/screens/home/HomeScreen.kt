package com.devx.flashtrack.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.devx.flashtrack.data.local.entity.TransactionType
import com.devx.flashtrack.ui.components.iconEmojiFor
import com.devx.flashtrack.ui.navigation.Screen
import com.devx.flashtrack.ui.theme.*
import com.devx.flashtrack.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.util.*

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onAddTransaction: (String) -> Unit,
    onNavigate: (String) -> Unit
) {
    val accounts       by viewModel.accounts.collectAsStateWithLifecycle()
    val recentTxns     by viewModel.recentTransactions.collectAsStateWithLifecycle()
    val categories     by viewModel.categories.collectAsStateWithLifecycle()
    val showBalance    by viewModel.showBalance.collectAsStateWithLifecycle()
    val totalBalance   by viewModel.totalBalance.collectAsStateWithLifecycle()
    val totalCredit    by viewModel.totalCredit.collectAsStateWithLifecycle()
    val monthExpense   by viewModel.currentMonthExpense.collectAsStateWithLifecycle()
    val monthIncome    by viewModel.currentMonthIncome.collectAsStateWithLifecycle()

    val hour = LocalTime.now().hour
    val greeting = when (hour) {
        in 5..11  -> "Good Morning ☀️"
        in 12..16 -> "Good Afternoon 🌤"
        in 17..20 -> "Good Evening 🌅"
        else      -> "Good Night 🌙"
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            HomeBottomBar(
                onNavigate = { route ->
                    when (route) {
                        "more" -> onNavigate(Screen.Settings.route)
                        else   -> onNavigate(route)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick   = { onAddTransaction("EXPENSE") },
                containerColor = GreenIncome,
                contentColor   = Color.Black,
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

            // ── Header ───────────────────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(greeting, style = MaterialTheme.typography.bodyMedium,
                             color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("FlashTrack", style = MaterialTheme.typography.headlineMedium,
                             fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onBackground)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = { viewModel.toggleBalance() }) {
                            Icon(
                                if (showBalance) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                                "Toggle balance", tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { onNavigate(Screen.Settings.route) }) {
                            Icon(Icons.Rounded.Settings, "Settings",
                                 tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // ── Balance card ─────────────────────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Brush.linearGradient(colors = listOf(Color(0xFF0D1F12), Color(0xFF0A1A20))))
                        .border(1.dp, DarkBorder, RoundedCornerShape(24.dp))
                        .padding(24.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Total Balance", style = MaterialTheme.typography.bodySmall, color = DarkTextTertiary)
                        Text(
                            text = if (showBalance) "₹${"%,.2f".format(totalBalance ?: 0.0)}" else "₹ ••••••",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            MiniStatCard(
                                label = "Month In", amount = monthIncome ?: 0.0,
                                color = GreenIncome, showBalance = showBalance, modifier = Modifier.weight(1f)
                            )
                            MiniStatCard(
                                label = "Month Out", amount = monthExpense ?: 0.0,
                                color = RedExpense, showBalance = showBalance, modifier = Modifier.weight(1f)
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
                                    "Available Credit: ${if (showBalance) "₹${"%,.0f".format(totalCredit ?: 0.0)}" else "₹ ••••"}",
                                    style = MaterialTheme.typography.labelSmall, color = BlueCard
                                )
                            }
                        }
                    }
                }
            }

            // ── Quick actions ────────────────────────────────────────────────
            item {
                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionChip("➕ Expense", RedExpense,   Modifier.weight(1f)) { onAddTransaction("EXPENSE") }
                    QuickActionChip("💰 Income",  GreenIncome,  Modifier.weight(1f)) { onAddTransaction("INCOME") }
                    QuickActionChip("↔️ Transfer", BlueCard,   Modifier.weight(1f)) { onAddTransaction("TRANSFER") }
                }
            }

            // ── Accounts carousel ─────────────────────────────────────────────
            item {
                Spacer(Modifier.height(24.dp))
                HomeSectionHeader(
                    title = "My Accounts", action = "View All",
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
                    items(accounts.take(6)) { account ->
                        val accentColor = runCatching {
                            Color(android.graphics.Color.parseColor(account.colorHex))
                        }.getOrDefault(GreenIncome)
                        HomeAccountCard(
                            name = account.name, balance = account.balance,
                            type = account.type.name, icon = iconEmojiFor(account.iconName),
                            accent = accentColor, showBalance = showBalance
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
                                    .padding(20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("+ Add Account", color = MaterialTheme.colorScheme.primary,
                                     fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // ── Recent transactions ───────────────────────────────────────────
            item {
                Spacer(Modifier.height(24.dp))
                HomeSectionHeader(
                    title = "Recent Transactions", action = "See All",
                    onAction = { onNavigate(Screen.Analysis.route) },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(Modifier.height(8.dp))
            }

            if (recentTxns.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("💸", fontSize = 48.sp)
                        Text("No transactions yet", style = MaterialTheme.typography.titleMedium,
                             fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                        Text("Tap + to add your first transaction",
                             style = MaterialTheme.typography.bodySmall,
                             color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                items(recentTxns, key = { it.id }) { txn ->
                    val cat = categories.find { it.id == txn.categoryId }
                    HomeTxnRow(
                        title       = txn.title,
                        category    = cat?.name ?: "Other",
                        icon        = iconEmojiFor(cat?.iconName ?: "category"),
                        iconColor   = runCatching { Color(android.graphics.Color.parseColor(cat?.colorHex ?: "#9E9E9E")) }.getOrDefault(GreenIncome),
                        amount      = txn.amount,
                        type        = txn.type,
                        date        = txn.date,
                        showBalance = showBalance,
                        modifier    = Modifier.padding(horizontal = 4.dp)
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                        thickness = 0.5.dp
                    )
                }
            }

            // ── IOU teaser ────────────────────────────────────────────────────
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
                                Text("Track money lent & borrowed", style = MaterialTheme.typography.bodySmall,
                                     color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Icon(Icons.Rounded.ChevronRight, null, tint = PurpleDebt)
                    }
                }
                Spacer(Modifier.height(80.dp))
            }
        }
    }
}

// ── Private sub-composables ───────────────────────────────────────────────────

@Composable
private fun HomeBottomBar(onNavigate: (String) -> Unit) {
    val items = listOf(
        Triple("Home",     Icons.Rounded.Home,                  "home"),
        Triple("Analysis", Icons.Rounded.BarChart,              "analysis"),
        Triple("Accounts", Icons.Rounded.AccountBalanceWallet,  "accounts"),
        Triple("More",     Icons.Rounded.GridView,              "more")
    )
    // We highlight "Home" by default here; real selected state handled by NavController
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) {
        items.forEach { (label, icon, route) ->
            NavigationBarItem(
                selected = route == "home",
                onClick  = { onNavigate(route) },
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor   = MaterialTheme.colorScheme.primary,
                    selectedTextColor   = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor      = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                )
            )
        }
    }
}

@Composable
private fun MiniStatCard(label: String, amount: Double, color: Color, showBalance: Boolean, modifier: Modifier) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(Modifier.size(7.dp).clip(CircleShape).background(color))
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
private fun QuickActionChip(label: String, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = color)
    }
}

@Composable
fun HomeSectionHeader(title: String, action: String? = null, onAction: () -> Unit = {}, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
             color = MaterialTheme.colorScheme.onBackground)
        if (action != null) {
            TextButton(onClick = onAction) {
                Text(action, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun HomeAccountCard(name: String, balance: Double, type: String, icon: String, accent: Color, showBalance: Boolean) {
    Box(
        modifier = Modifier
            .width(158.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(listOf(accent.copy(alpha = 0.2f), accent.copy(alpha = 0.05f))))
            .border(1.dp, accent.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(icon, fontSize = 24.sp)
            Text(name, style = MaterialTheme.typography.labelMedium,
                 color = MaterialTheme.colorScheme.onSurfaceVariant,
                 maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                text = if (showBalance) "₹${"%,.0f".format(balance)}" else "₹ ••••",
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(type.replace("_", " "), style = MaterialTheme.typography.labelSmall, color = accent)
        }
    }
}

@Composable
fun HomeTxnRow(
    title: String, category: String, icon: String, iconColor: Color,
    amount: Double, type: TransactionType, date: Long,
    showBalance: Boolean, modifier: Modifier = Modifier
) {
    val amountColor = when (type) { TransactionType.INCOME -> GreenIncome; TransactionType.EXPENSE -> RedExpense; else -> BlueCard }
    val prefix      = when (type) { TransactionType.INCOME -> "+"; TransactionType.EXPENSE -> "-"; else -> "↔" }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(iconColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) { Text(icon, fontSize = 18.sp) }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold,
                 maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurface)
            Text(
                "$category • ${SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(date))}",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = if (showBalance) "$prefix₹${"%,.0f".format(amount)}" else "₹••••",
            style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = amountColor
        )
    }
}
