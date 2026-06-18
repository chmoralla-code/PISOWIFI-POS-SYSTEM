package com.pisowifi.pos.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.flow.first
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.pisowifi.pos.data.repository.PisoRepository
import com.pisowifi.pos.util.DateUtils
import java.util.Calendar

enum class AnalyticsPeriod { TODAY, WEEKLY, MONTHLY, YEARLY }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(repository: PisoRepository, navController: NavController) {
    var selectedPeriod by remember { mutableStateOf(AnalyticsPeriod.MONTHLY) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analytics") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Income Period", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    AnalyticsPeriod.entries.forEachIndexed { index, period ->
                        SegmentedButton(
                            selected = selectedPeriod == period,
                            onClick = { selectedPeriod = period },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = AnalyticsPeriod.entries.size
                            )
                        ) {
                            Text(
                                when (period) {
                                    AnalyticsPeriod.TODAY -> "Today"
                                    AnalyticsPeriod.WEEKLY -> "Weekly"
                                    AnalyticsPeriod.MONTHLY -> "Monthly"
                                    AnalyticsPeriod.YEARLY -> "Yearly"
                                },
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            item {
                var total by remember { mutableStateOf(0.0) }

                when (selectedPeriod) {
                    AnalyticsPeriod.TODAY -> {
                        val t by repository.getTotalSales(DateUtils.todayStart(), DateUtils.todayEnd())
                            .collectAsState(initial = 0.0)
                        total = t
                        TodayDetail(repository, total)
                    }
                    AnalyticsPeriod.WEEKLY -> {
                        val t by repository.getTotalSales(DateUtils.weekStart(), DateUtils.weekEnd())
                            .collectAsState(initial = 0.0)
                        total = t
                        PeriodSummaryCard("This Week", total, DateUtils.weekStart(), DateUtils.weekEnd())
                        Spacer(Modifier.height(8.dp))
                        PerDeviceBreakdown(repository, DateUtils.weekStart(), DateUtils.weekEnd())
                    }
                    AnalyticsPeriod.MONTHLY -> {
                        val t by repository.getTotalSales(DateUtils.monthStart(), DateUtils.monthEnd())
                            .collectAsState(initial = 0.0)
                        total = t
                        PeriodSummaryCard("This Month", total, DateUtils.monthStart(), DateUtils.monthEnd())

                        // Daily bar chart for current month
                        Spacer(Modifier.height(8.dp))
                        MonthlyBarChart(repository)
                        Spacer(Modifier.height(8.dp))
                        PerDeviceBreakdown(repository, DateUtils.monthStart(), DateUtils.monthEnd())
                    }
                    AnalyticsPeriod.YEARLY -> {
                        val t by repository.getTotalSales(DateUtils.yearStart(), DateUtils.yearEnd())
                            .collectAsState(initial = 0.0)
                        total = t
                        PeriodSummaryCard("This Year", total, DateUtils.yearStart(), DateUtils.yearEnd())
                        Spacer(Modifier.height(8.dp))
                        YearlyBarChart(repository)
                        Spacer(Modifier.height(8.dp))
                        PerDeviceBreakdown(repository, DateUtils.yearStart(), DateUtils.yearEnd())
                    }
                }
            }
        }
    }
}

@Composable
private fun PeriodSummaryCard(label: String, total: Double, start: Long, end: Long) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(DateUtils.formatCurrency(total), style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(4.dp))
            Text("${DateUtils.formatDate(start)} - ${DateUtils.formatDate(end)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun TodayDetail(repository: PisoRepository, total: Double) {
    val summary by repository.getSalesSummaryByPisowifi(DateUtils.todayStart(), DateUtils.todayEnd())
        .collectAsState(initial = emptyList())

    PeriodSummaryCard("Today's Income", total, DateUtils.todayStart(), DateUtils.todayEnd())

    Spacer(Modifier.height(8.dp))
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Per Device Today", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            if (summary.isEmpty()) {
                Text("No sales today", color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium)
            } else {
                summary.forEach { s ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(s.deviceName, modifier = Modifier.weight(1f))
                        Text(DateUtils.formatCurrency(s.total), fontWeight = FontWeight.Medium)
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun PerDeviceBreakdown(repository: PisoRepository, start: Long, end: Long) {
    val summary by repository.getSalesSummaryByPisowifi(start, end)
        .collectAsState(initial = emptyList())

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Per Device", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            if (summary.isEmpty()) {
                Text("No sales in this period", color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium)
            } else {
                summary.forEach { s ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(s.deviceName, modifier = Modifier.weight(1f))
                        Text(DateUtils.formatCurrency(s.total), fontWeight = FontWeight.Medium)
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun MonthlyBarChart(repository: PisoRepository) {
    val now = Calendar.getInstance()
    val daysInMonth = now.getActualMaximum(Calendar.DAY_OF_MONTH)
    val currentDay = now.get(Calendar.DAY_OF_MONTH)

    val dailyTotals = remember {
        mutableStateListOf<Double>().apply {
            for (d in 1..currentDay) {
                add(0.0)
            }
        }
    }

    LaunchedEffect(Unit) {
        for (day in 1..currentDay) {
            val start = DateUtils.dateToMillis(now.get(Calendar.YEAR), now.get(Calendar.MONTH), day)
            val end = start + 86400000
            dailyTotals[day - 1] = repository.getTotalSales(start, end).first()
        }
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Daily Income (This Month)", style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            if (dailyTotals.isEmpty() || dailyTotals.all { it == 0.0 }) {
                Text("No data yet", color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium)
            } else {
                SimpleBarChart(
                    data = dailyTotals,
                    maxVal = dailyTotals.maxOrNull() ?: 1.0,
                    label = { (it + 1).toString() },
                    modifier = Modifier.fillMaxWidth().height(180.dp)
                )
            }
        }
    }
}

@Composable
private fun YearlyBarChart(repository: PisoRepository) {
    val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    val now = Calendar.getInstance()
    val currentMonth = now.get(Calendar.MONTH)

    val monthlyTotals = remember {
        mutableStateListOf<Double>().apply {
            for (m in 0..currentMonth) add(0.0)
        }
    }

    LaunchedEffect(Unit) {
        for (m in 0..currentMonth) {
            val cal = Calendar.getInstance().apply {
                set(Calendar.YEAR, now.get(Calendar.YEAR))
                set(Calendar.MONTH, m)
                set(Calendar.DAY_OF_MONTH, 1)
            }
            val start = DateUtils.dateToMillis(now.get(Calendar.YEAR), m, 1)
            val end = DateUtils.dateToMillis(now.get(Calendar.YEAR), m,
                cal.getActualMaximum(Calendar.DAY_OF_MONTH)) + 86400000
            monthlyTotals[m] = repository.getTotalSales(start, end).first()
        }
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Monthly Income (This Year)", style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            if (monthlyTotals.isEmpty() || monthlyTotals.all { it == 0.0 }) {
                Text("No data yet", color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium)
            } else {
                SimpleBarChart(
                    data = monthlyTotals,
                    maxVal = monthlyTotals.maxOrNull() ?: 1.0,
                    label = { monthNames.getOrElse(it) { "" } },
                    modifier = Modifier.fillMaxWidth().height(200.dp)
                )
            }
        }
    }
}

@Composable
private fun SimpleBarChart(
    data: List<Double>,
    maxVal: Double,
    label: (Int) -> String,
    modifier: Modifier = Modifier
) {
    val barColor = MaterialTheme.colorScheme.primary
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val gridColor = MaterialTheme.colorScheme.outlineVariant

    Canvas(modifier = modifier) {
        if (data.isEmpty() || maxVal == 0.0) return@Canvas
        val padding = 20f
        val chartWidth = size.width - padding * 2
        val chartHeight = size.height - padding * 2
        val barCount = data.size
        if (barCount == 0) return@Canvas
        val barWidth = chartWidth / barCount * 0.7f
        val gap = chartWidth / barCount * 0.3f

        // Grid lines
        for (i in 0..4) {
            val y = padding + chartHeight * (1 - i / 4f)
            drawLine(gridColor, Offset(padding, y), Offset(size.width - padding, y), strokeWidth = 0.5f)
            drawContext.canvas.nativeCanvas.drawText(
                "%.0f".format(maxVal * i / 4),
                2f, y + 4f,
                android.graphics.Paint().apply { color = labelColor.hashCode(); textSize = 18f }
            )
        }

        data.forEachIndexed { index, value ->
            val barHeight = if (maxVal > 0) (value / maxVal * chartHeight).toFloat() else 0f
            val x = padding + index * (barWidth + gap)
            val y = padding + chartHeight - barHeight

            drawRect(
                color = barColor,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight)
            )

            // Label
            if (barCount <= 31) {
                drawContext.canvas.nativeCanvas.drawText(
                    label(index),
                    x + barWidth / 2 - 8f,
                    size.height - 2f,
                    android.graphics.Paint().apply {
                        color = labelColor.hashCode()
                        textSize = if (barCount > 15) 14f else 18f
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                )
            }
        }
    }
}


