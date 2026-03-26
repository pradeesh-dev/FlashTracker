package com.devx.flashtrack.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import com.devx.flashtrack.data.local.entity.*
import com.devx.flashtrack.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

// ─── Bottom Navigation ─────────────────────────────────────────────────────────

data class BottomNavItem(val label: String, val icon: ImageVector, val route: String)

val bottomNavItems = listOf(
    BottomNavItem("Home",     Icons.Rounded.Home,        "home"),
    BottomNavItem("Analysis", Icons.Rounded.BarChart,    "analysis"),
    BottomNavItem("Accounts", Icons.Rounded.AccountBalanceWallet, "accounts"),
    BottomNavItem("More",     Icons.Rounded.GridView,    "more")
)

@Composable
fun FlashTrackBottomBar(currentRoute: String, onNavigate: (String) -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottomNavItems.forEach { item ->
                val selected = currentRoute == item.route
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onNavigate(item.route) }
                        .padding(vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = if (selected) MaterialTheme.colorScheme.primary
                               else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

// ─── Amount display with masked option ────────────────────────────────────────

@Composable
fun AmountText(
    amount: Double,
    showBalance: Boolean = true,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.headlineLarge,
    color: Color = MaterialTheme.colorScheme.onBackground,
    prefix: String = "₹"
) {
    Text(
        text = if (showBalance) "$prefix${"%,.2f".format(amount)}" else "₹ ••••••",
        style = style,
        color = color,
        fontWeight = FontWeight.Bold
    )
}

// ─── Glass card ────────────────────────────────────────────────────────────────

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    Card(
        modifier = modifier.then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) DarkSurface2 else LightSurface
        ),
        border = BorderStroke(1.dp, if (isDark) DarkBorder else LightBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), content = content)
    }
}

// ─── Transaction row ───────────────────────────────────────────────────────────

@Composable
fun TransactionRow(
    transaction: TransactionEntity,
    categoryName: String,
    categoryIcon: String,
    categoryColor: String,
    showBalance: Boolean = true,
    onClick: () -> Unit = {}
) {
    val color = try { Color(android.graphics.Color.parseColor(categoryColor)) } catch (e: Exception) { MaterialTheme.colorScheme.primary }
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
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Category icon circle
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = iconEmojiFor(categoryIcon), fontSize = 20.sp)
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "$categoryName • ${formatDate(transaction.date)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = if (showBalance) "$prefix₹${"%,.0f".format(transaction.amount)}" else "₹••••",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = amountColor
        )
    }
}

// ─── Section header ────────────────────────────────────────────────────────────

@Composable
fun SectionHeader(title: String, action: String? = null, onAction: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (action != null) {
            TextButton(onClick = onAction) {
                Text(text = action, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

// ─── Pill chip ─────────────────────────────────────────────────────────────────

@Composable
fun TagChip(tag: String, selected: Boolean = false, onClick: () -> Unit = {}) {
    val bg = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(text = tag, style = MaterialTheme.typography.labelMedium, color = textColor)
    }
}

// ─── Empty state ───────────────────────────────────────────────────────────────

@Composable
fun EmptyState(emoji: String, title: String, subtitle: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = emoji, fontSize = 48.sp)
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
             color = MaterialTheme.colorScheme.onBackground)
        Text(text = subtitle, style = MaterialTheme.typography.bodySmall,
             color = MaterialTheme.colorScheme.onSurfaceVariant,
             textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

// ─── Top app bar ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashTopBar(title: String, onBack: (() -> Unit)? = null, actions: @Composable () -> Unit = {}) {
    TopAppBar(
        title = { Text(title, fontWeight = FontWeight.Bold) },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Rounded.ArrowBackIosNew, contentDescription = "Back")
                }
            }
        },
        actions = { actions() },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground
        )
    )
}

// ─── Helpers ───────────────────────────────────────────────────────────────────

fun formatDate(millis: Long): String {
    val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
    return sdf.format(Date(millis))
}

fun formatDateFull(millis: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return sdf.format(Date(millis))
}

fun iconEmojiFor(iconName: String): String = when (iconName) {
    "shopping_basket"      -> "🛒"
    "restaurant"           -> "🍽️"
    "shopping_bag"         -> "🛍️"
    "receipt_long"         -> "🧾"
    "movie"                -> "🎬"
    "flight"               -> "✈️"
    "local_hospital"       -> "🏥"
    "school"               -> "📚"
    "local_gas_station"    -> "⛽"
    "account_balance"      -> "🏦"
    "payments"             -> "💰"
    "work"                 -> "💼"
    "trending_up"          -> "📈"
    "home"                 -> "🏠"
    "swap_horiz"           -> "↔️"
    "category"             -> "📦"
    "account_balance_wallet"-> "👛"
    "credit_card"          -> "💳"
    else                   -> "💸"
}
