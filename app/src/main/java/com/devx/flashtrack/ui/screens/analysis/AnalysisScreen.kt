package com.devx.flashtrack.ui.screens.analysis

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.yml.charts.common.model.PlotType
import co.yml.charts.ui.piechart.charts.PieChart
import co.yml.charts.ui.piechart.models.PieChartConfig
import co.yml.charts.ui.piechart.models.PieChartData
import com.devx.flashtrack.ui.components.EmptyState
import com.devx.flashtrack.ui.components.iconEmojiFor
import com.devx.flashtrack.ui.screens.home.HomeTxnRow
import com.devx.flashtrack.ui.theme.*
import com.devx.flashtrack.viewmodel.MainViewModel
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val selectedMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()
    val monthExpense  by viewModel.monthlyExpense.collectAsStateWithLifecycle()
    val monthIncome   by viewModel.monthlyIncome.collectAsStateWithLifecycle()
    val catSpending   by viewModel.categorySpending.collectAsStateWithLifecycle()
    val categories    by viewModel.categories.collectAsStateWithLifecycle()
    val txns          by viewModel.monthlyTransactions.collectAsStateWithLifecycle()
    val showBalance   by viewModel.showBalance.collectAsStateWithLifecycle()

    val expense = monthExpense ?: 0.0
    val income  = monthIncome  ?: 0.0
    val balance = income - expense

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Analysis", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBackIosNew, "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {

            // ── Month navigation ──────────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.selectMonth(selectedMonth.minusMonths(1)) }) {
                        Icon(Icons.Rounded.ChevronLeft, "Previous month")
                    }
                    Text(
                        text = selectedMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(180.dp),
                        textAlign = TextAlign.Center
                    )
                    val canGoForward = selectedMonth.isBefore(YearMonth.now())
                    IconButton(onClick = { if (canGoForward) viewModel.selectMonth(selectedMonth.plusMonths(1)) }) {
                        Icon(
                            Icons.Rounded.ChevronRight, "Next month",
                            tint = if (canGoForward) MaterialTheme.colorScheme.onSurface
                                   else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        )
                    }
                }
            }

            // ── Summary pills ─────────────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AnalysisPill("Income",  income,  GreenIncome, "📈", showBalance, Modifier.weight(1f))
                    AnalysisPill("Expense", expense, RedExpense,  "📉", showBalance, Modifier.weight(1f))
                    AnalysisPill("Balance", balance,
                        if (balance >= 0) GreenIncome else RedExpense, "💰", showBalance, Modifier.weight(1f))
                }
            }

            // ── Pie chart ─────────────────────────────────────────────────────
            if (catSpending.isNotEmpty() && expense > 0) {
                item {
                    Spacer(Modifier.height(20.dp))
                    Text(
                        "Spending by Category",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    Spacer(Modifier.height(12.dp))

                    val slices = catSpending.mapIndexed { i, cs ->
                        val cat = categories.find { it.id == cs.categoryId }
                        val color = runCatching {
                            Color(android.graphics.Color.parseColor(cat?.colorHex ?: "#9E9E9E"))
                        }.getOrDefault(ChartColors[i % ChartColors.size])
                        PieChartData.Slice(
                            label = cat?.name ?: "Other",
                            value = cs.total.toFloat().coerceAtLeast(0.001f),
                            color = color
                        )
                    }

                    val pieConfig = PieChartConfig(
                        isAnimationEnable    = true,
                        showSliceLabels      = false,
                        animationDuration    = 600,
                        backgroundColor      = Color.Transparent,
                        isSumVisible         = true,
                        sumUnit              = "₹",
                        activeSliceAlpha     = 0.9f,
                        isClickOnSliceEnabled = false,
                        labelVisible         = false,
                        strokeWidth          = 90f,
                        labelFontSize        = 14.sp
                    )

                    PieChart(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .padding(horizontal = 40.dp),
                        pieChartData   = PieChartData(slices = slices, plotType = PlotType.Donut),
                        pieChartConfig = pieConfig
                    )
                }

                // Category breakdown list
                item {
                    Spacer(Modifier.height(8.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        catSpending.forEachIndexed { i, cs ->
                            val cat = categories.find { it.id == cs.categoryId }
                            val catColor = runCatching {
                                Color(android.graphics.Color.parseColor(cat?.colorHex ?: "#9E9E9E"))
                            }.getOrDefault(ChartColors[i % ChartColors.size])
                            val pct = if (expense > 0) (cs.total / expense * 100).toInt() else 0

                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(Modifier.size(8.dp).clip(CircleShape).background(catColor))
                                    Text(iconEmojiFor(cat?.iconName ?: "category"), fontSize = 14.sp)
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            cat?.name ?: "Other",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        LinearProgressIndicator(
                                            progress        = { (pct / 100f).coerceIn(0f, 1f) },
                                            modifier        = Modifier.fillMaxWidth().padding(top = 4.dp)
                                                                .height(3.dp).clip(RoundedCornerShape(2.dp)),
                                            color           = catColor,
                                            trackColor      = catColor.copy(alpha = 0.15f)
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            if (showBalance) "₹${"%,.0f".format(cs.total)}" else "₹••••",
                                            style      = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text("$pct%", style = MaterialTheme.typography.labelSmall,
                                             color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                if (i < catSpending.size - 1) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        color    = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                                        thickness = 0.5.dp
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                item {
                    Spacer(Modifier.height(40.dp))
                    EmptyState("📊", "No data for this month", "Add transactions to see\nyour spending analysis")
                }
            }

            // ── Transaction list ───────────────────────────────────────────────
            item {
                Spacer(Modifier.height(24.dp))
                Text(
                    "Transactions (${txns.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(Modifier.height(4.dp))
            }

            if (txns.isEmpty()) {
                item {
                    EmptyState("💸", "No transactions", "No transactions found for this month")
                }
            } else {
                items(txns, key = { it.id }) { txn ->
                    val cat = categories.find { it.id == txn.categoryId }
                    HomeTxnRow(
                        title       = txn.title,
                        category    = cat?.name ?: "Other",
                        icon        = iconEmojiFor(cat?.iconName ?: "category"),
                        iconColor   = runCatching { Color(android.graphics.Color.parseColor(cat?.colorHex ?: "#9E9E9E")) }.getOrDefault(GreenIncome),
                        amount      = txn.amount,
                        type        = txn.type,
                        date        = txn.date,
                        showBalance = showBalance
                    )
                    HorizontalDivider(
                        modifier  = Modifier.padding(horizontal = 20.dp),
                        color     = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        thickness = 0.5.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun AnalysisPill(
    label: String, amount: Double, color: Color,
    emoji: String, showBalance: Boolean, modifier: Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(color.copy(alpha = 0.1f))
            .border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(emoji, fontSize = 16.sp)
            Text(label, style = MaterialTheme.typography.labelSmall,
                 color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = if (showBalance) "₹${"%,.0f".format(amount)}" else "₹••••",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
