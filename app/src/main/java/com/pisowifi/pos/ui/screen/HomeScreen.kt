package com.pisowifi.pos.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.pisowifi.pos.data.dao.PisowifiSalesSummary
import com.pisowifi.pos.data.repository.PisoRepository
import com.pisowifi.pos.ui.navigation.Screen
import com.pisowifi.pos.util.DateUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(repository: PisoRepository, navController: NavController) {
    val scope = rememberCoroutineScope()
    val devices by repository.getAllPisowifi().collectAsState(initial = emptyList())
    val areas by repository.getAllAreas().collectAsState(initial = emptyList())

    val todayTotal by repository.getTotalSales(DateUtils.todayStart(), DateUtils.todayEnd())
        .collectAsState(initial = 0.0)
    val weekTotal by repository.getTotalSales(DateUtils.weekStart(), DateUtils.weekEnd())
        .collectAsState(initial = 0.0)
    val monthTotal by repository.getTotalSales(DateUtils.monthStart(), DateUtils.monthEnd())
        .collectAsState(initial = 0.0)
    val allTimeTotal by repository.getTotalSales(0, System.currentTimeMillis())
        .collectAsState(initial = 0.0)

    val totalCapital = devices.sumOf { it.capital }
    val profit = allTimeTotal - totalCapital

    val pendingHarvests by repository.getPendingShares().collectAsState(initial = emptyList())

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Pisowifi POS", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(DateUtils.formatDate(System.currentTimeMillis()), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        item {
            Text("Today's Income", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(DateUtils.formatCurrency(todayTotal), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "This Week",
                    value = DateUtils.formatCurrency(weekTotal),
                    icon = Icons.Default.DateRange,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "This Month",
                    value = DateUtils.formatCurrency(monthTotal),
                    icon = Icons.Default.CalendarMonth,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Total Capital",
                    value = DateUtils.formatCurrency(totalCapital),
                    icon = Icons.Default.AccountBalance,
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Profit",
                    value = DateUtils.formatCurrency(profit),
                    icon = Icons.Default.TrendingUp,
                    containerColor = if (profit >= 0) MaterialTheme.colorScheme.secondaryContainer
                    else MaterialTheme.colorScheme.errorContainer,
                    contentColor = if (profit >= 0) MaterialTheme.colorScheme.onSecondaryContainer
                    else MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        item {
            Text("Quick Actions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionChip(
                    onClick = { navController.navigate(Screen.Pisowifi.route) },
                    label = { Text("Devices") },
                    icon = Icons.Default.Router,
                    modifier = Modifier.weight(1f)
                )
                ActionChip(
                    onClick = { navController.navigate(Screen.AddSale.route) },
                    label = { Text("Add Sale") },
                    icon = Icons.Default.Payments,
                    modifier = Modifier.weight(1f)
                )
                ActionChip(
                    onClick = { navController.navigate(Screen.Analytics.route) },
                    label = { Text("Analytics") },
                    icon = Icons.Default.BarChart,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionChip(
                    onClick = { navController.navigate(Screen.SharedHarvest.route) },
                    label = { Text("Shared Harvest") },
                    icon = Icons.Default.Share,
                    modifier = Modifier.weight(1f)
                )
                ActionChip(
                    onClick = { navController.navigate(Screen.Areas.route) },
                    label = { Text("Areas") },
                    icon = Icons.Default.LocationOn,
                    modifier = Modifier.weight(1f)
                )
                ActionChip(
                    onClick = { navController.navigate(Screen.Harvest.route) },
                    label = { Text("Harvest") },
                    icon = Icons.Default.Inventory2,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (pendingHarvests.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate(Screen.Harvest.route) }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.onErrorContainer)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "${pendingHarvests.size} pending share(s) to settle",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        item {
            var showClearConfirm by remember { mutableStateOf(false) }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text("Data", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                TextButton(
                    onClick = { showClearConfirm = true },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.DeleteForever, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Clear All Sales")
                }
            }
            if (showClearConfirm) {
                AlertDialog(
                    onDismissRequest = { showClearConfirm = false },
                    title = { Text("Clear All Sales?") },
                    text = { Text("This will permanently delete ALL sale and harvest records. This cannot be undone.") },
                    confirmButton = {
                        TextButton(onClick = {
                            scope.launch {
                                repository.clearAllSales()
                                repository.clearAllHarvests()
                            }
                            showClearConfirm = false
                        }) { Text("Delete All", color = MaterialTheme.colorScheme.error) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showClearConfirm = false }) { Text("Cancel") }
                    }
                )
            }
        }

        item {
            Text("Overview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MiniStat("Devices", "${devices.size}", Icons.Default.Router, Modifier.weight(1f))
                MiniStat("Areas", "${areas.size}", Icons.Default.LocationOn, Modifier.weight(1f))
                MiniStat("All Time", DateUtils.formatCurrency(allTimeTotal), Icons.Default.TrendingUp, Modifier.weight(1f))
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor, contentColor = contentColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(icon, null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(4.dp))
            Text(title, fontSize = 11.sp, color = contentColor.copy(alpha = 0.7f))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
private fun MiniStat(title: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(4.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ActionChip(
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(4.dp))
            label()
        }
    }
}
