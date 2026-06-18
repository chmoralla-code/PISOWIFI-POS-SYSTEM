package com.pisowifi.pos.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pisowifi.pos.data.entity.Area
import com.pisowifi.pos.data.repository.PisoRepository
import com.pisowifi.pos.util.DateUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedHarvestScreen(repository: PisoRepository, navController: NavController) {
    val scope = rememberCoroutineScope()
    val allAreas by repository.getAllAreas().collectAsState(initial = emptyList())
    val pendingAreas by repository.getAreasPendingShare().collectAsState(initial = emptyList())
    val completedAreas by repository.getAreasCompletedShare().collectAsState(initial = emptyList())

    var selectedTab by remember { mutableIntStateOf(0) }
    var showResetDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shared Harvest") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (completedAreas.isNotEmpty()) {
                        IconButton(onClick = { showResetDialog = true }) {
                            Icon(Icons.Default.Refresh, "Reset All")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Pending (${pendingAreas.size})") },
                    icon = { Icon(Icons.Default.HourglassEmpty, null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Shared (${completedAreas.size})") },
                    icon = { Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp)) }
                )
            }

            if (allAreas.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Share, null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                        Spacer(Modifier.height(8.dp))
                        Text("No areas yet", style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Add areas first to track shared harvest", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                val displayAreas = if (selectedTab == 0) pendingAreas else completedAreas

                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(displayAreas, key = { it.id }) { area ->
                        AreaShareCard(
                            area = area,
                            repository = repository,
                            onToggleShare = { given ->
                                scope.launch {
                                    repository.updateAreaShareStatus(area.id, given)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset All") },
            text = { Text("Mark all areas as pending (not yet shared)? This will reset the checklist for all areas.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        completedAreas.forEach { area ->
                            repository.updateAreaShareStatus(area.id, false)
                        }
                    }
                    showResetDialog = false
                }) { Text("Reset All") }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun AreaShareCard(
    area: Area,
    repository: PisoRepository,
    onToggleShare: (Boolean) -> Unit
) {
    var totalSales by remember { mutableStateOf(0.0) }
    var totalHarvestShare by remember { mutableStateOf(0.0) }

    LaunchedEffect(area.id) {
        totalSales = repository.getTotalSalesByArea(area.id)
        totalHarvestShare = repository.getTotalHarvestShareByArea(area.id)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (area.shareGiven) MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (area.shareGiven) Icons.Default.CheckCircle else Icons.Default.HourglassEmpty,
                    null,
                    tint = if (area.shareGiven) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(area.name, fontWeight = FontWeight.Medium)
                    if (area.lastSharedDate > 0L) {
                        Text(
                            "Last shared: ${DateUtils.formatDate(area.lastSharedDate)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Text("Total Sales", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(DateUtils.formatCurrency(totalSales), fontWeight = FontWeight.Bold)
                }
                Column(Modifier.weight(1f)) {
                    Text("Harvest Share", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(DateUtils.formatCurrency(totalHarvestShare), fontWeight = FontWeight.SemiBold)
                }
                Column {
                    FilledTonalButton(
                        onClick = { onToggleShare(!area.shareGiven) },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            if (area.shareGiven) Icons.Default.Undo else Icons.Default.Check,
                            null, modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            if (area.shareGiven) "Undo" else "Share Given",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}
