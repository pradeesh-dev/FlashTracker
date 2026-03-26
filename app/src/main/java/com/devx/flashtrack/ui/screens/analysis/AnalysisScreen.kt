package com.devx.flashtrack.ui.screens.analysis

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.yml.charts.common.model.PlotType
import co.yml.charts.ui.piechart.charts.PieChart
import co.yml.charts.ui.piechart.models.PieChartConfig
import co.yml.charts.ui.piechart.models.PieChartData
import com.devx.flashtrack.data.local.entity.TransactionType
import com.devx.flashtrack.ui.components.*
import com.devx.flashtrack.ui.theme.*
import com.devx.flashtrack.viewmodel.MainViewModel
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val selectedMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()
    val monthExpense by viewModel.monthlyExpense.collectAsStateWithLifecycle()
    val monthIncome by viewModel.monthlyIncome.collectAsStateWithLifecycle()
    val catSpending by viewModel.categorySpending.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val txns by viewModel.monthlyTransactions.collectAsStateWithLifecycle()
    val showBalance by viewModel.showBalance.collectAsStateWithLifecycle()

    var activeTab by remember { mutableIntStateOf(0) } // 0=Overview, 1=Transactions

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
            // ─── Month picker ──────────────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.selectMonth(selectedMonth.minusMonths(1)) }) {
                        Icon(Icons.Rounded.ChevronLeft, "Prev month")
                    }
                    Text(
                        text = selectedMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(180.dp),
                        textAlign = TextAlign.Center
                    )
                    IconButton(
                        onClick = { viewModel.selectMonth(selectedMonth.plusMonths(1)) },
                        enabled = selectedMonth.isBefore(YearMonth.now())
                    ) {
                        Icon(Icons.Rounded.ChevronRight, "Next month",
                             tint = if (selectedMonth.isBefore(YearMonth.now())) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                    }
                }
            }

            // ─── Summary row ───────────────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatPill("Income", income, GreenIncome, "📈", showBalance, Modifier.weight(1f))
                    StatPill("Expense", expense, RedExpense, "📉", showBalance, Modifier.weight(1f))
                    StatPill("Balance", balance, if (balance >= 0) GreenIncome else RedExpense, "💰", showBalance, Modifier.weight(1f))
                }
            }

            // ─── Pie chart ────────────────────────────────────────────────────
            if (catSpending.isNotEmpty()) {
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
                        val color = try {
                            Color(android.graphics.Color.parseColor(cat?.colorHex ?: ChartColors[i % ChartColors.size].toHexString()))
                        } catch (e: Exception) { ChartColors[i % ChartColors.size] }
                        PieChartData.Slice(
                            label = cat?.name ?: "Other",
                            value = cs.total.toFloat(),
                            color = color
                        )
                    }

                    if (slices.isNotEmpty()) {
                        val pieData = PieChartData(
                            slices = slices,
                            plotType = PlotType.Donut
                        )
                        val pieConfig = PieChartConfig(
                            isAnimationEnable = true,
                            showSliceLabels = false,
                            animationDuration = 600,
                            backgroundColor = Color.Transparent,
                            isSumVisible = true,
                            sumUnit = "₹",
                            activeSliceAlpha = 0.9f,
                            isClickOnSliceEnabled = false,
                            labelVisible = false,
                            strokeWidth = 100f,
                            labelFontSize = 14.sp
                        )

                        PieChart(
                            modifier = Modifier.fillMaxWidth().height(300.dp).padding(horizontal = 20.dp),
                            pieChartData = pieData,
                            pieChartConfig = pieConfig
                        )
                    }
                }

                // Category legend + amounts
                item {
                    Spacer(Modifier.height(8.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        catSpending.forEachIndexed { i, cs ->
                            val cat = categories.find { it.id == cs.categoryId }
                            val catColor = try {
                                Color(android.graphics.Color.parseColor(cat?.colorHex ?: "#9E9E9E"))
                            } catch (e: Exception) { ChartColors[i % ChartColors.size] }
                            val pct = if (expense > 0) (cs.total / expense * 100).toInt() else 0

                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.size(8.dp).clip(CircleShape).background(catColor)
                                    )
                                    Text(
                                        iconEmojiFor(cat?.iconName ?: "category"),
                                        fontSize = 14.sp
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            cat?.name ?: "Other",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        LinearProgressIndicator(
                                            progress = { pct / 100f },
                                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp).height(3.dp).clip(RoundedCornerShape(2.dp)),
                                            color = catColor,
                                            trackColor = catColor.copy(alpha = 0.15f)
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            if (showBalance) "₹${"%,.0f".format(cs.total)}" else "₹••••",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text("$pct%", style = MaterialTheme.typography.labelSmall,
                                             color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                if (i < catSpending.size - 1) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
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

            // ─── Transaction list ─────────────────────────────────────────────
            item {
                Spacer(Modifier.height(24.dp))
                Text(
                    "Transactions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(Modifier.height(4.dp))
            }

            if (txns.isEmpty()) {
                item {
                    EmptyState("💸", "No transactions", "No transactions found\nfor this month")
                }
            } else {
                items(txns, key = { it.id }) { txn ->
                    val cat = categories.find { it.id == txn.categoryId }
                    com.devx.flashtrack.ui.screens.home.TransactionRow(
                        transaction = txn,
                        categoryName = cat?.name ?: "Other",
                        categoryIcon = cat?.iconName ?: "category",
                        categoryColor = cat?.colorHex ?: "#9E9E9E",
                        showBalance = showBalance,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        thickness = 0.5.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun StatPill(
    label: String, amount: Double, color: Color, emoji: String,
    showBalance: Boolean, modifier: Modifier = Modifier
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
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = if (showBalance) "₹${"%,.0f".format(amount)}" else "₹••••",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

fun Color.toHexString(): String = "#${Integer.toHexString(this.toArgb()).substring(2).uppercase()}"
