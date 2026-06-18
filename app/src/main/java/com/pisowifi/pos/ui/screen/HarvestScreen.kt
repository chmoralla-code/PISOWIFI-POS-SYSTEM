package com.pisowifi.pos.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pisowifi.pos.data.dao.PisowifiHarvestSummary
import com.pisowifi.pos.data.entity.HarvestRecord
import com.pisowifi.pos.data.repository.PisoRepository
import com.pisowifi.pos.util.DateUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HarvestScreen(repository: PisoRepository, navController: NavController) {
    val scope = rememberCoroutineScope()
    val harvests by repository.getHarvestsWithDetails().collectAsState(initial = emptyList())
    val devices by repository.getAllPisowifi().collectAsState(initial = emptyList())

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }

    val pendingHarvests = harvests.filter { !it.shareGiven }
    val completedHarvests = harvests.filter { it.shareGiven }
    val displayList = if (selectedTab == 0) pendingHarvests else completedHarvests

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Harvest Records") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            if (devices.isNotEmpty()) {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, "Add Harvest")
                }
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Pending (${pendingHarvests.size})") },
                    icon = { Icon(Icons.Default.HourglassEmpty, null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Completed (${completedHarvests.size})") },
                    icon = { Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp)) }
                )
            }

            if (harvests.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Inventory2, null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                        Spacer(Modifier.height(8.dp))
                        Text("No harvest records", style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Tap + to record a harvest", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(displayList, key = { it.id }) { harvest ->
                        HarvestCard(
                            harvest = harvest,
                            onToggleShare = {
                                scope.launch {
                                    repository.updateShareGiven(harvest.id, !harvest.shareGiven)
                                }
                            },
                            onDelete = {
                                scope.launch {
                                    repository.deleteHarvest(harvest.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddHarvestDialog(
            devices = devices,
            onDismiss = { showAddDialog = false },
            onConfirm = { deviceId, amount, shareAmt ->
                scope.launch {
                    repository.addHarvest(
                        HarvestRecord(
                            pisowifiId = deviceId,
                            totalSales = amount,
                            shareAmount = shareAmt,
                            harvestDate = System.currentTimeMillis()
                        )
                    )
                }
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun HarvestCard(
    harvest: PisowifiHarvestSummary,
    onToggleShare: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (harvest.shareGiven) MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (harvest.shareGiven) Icons.Default.CheckCircle else Icons.Default.HourglassEmpty,
                    null,
                    tint = if (harvest.shareGiven) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(harvest.deviceName, fontWeight = FontWeight.Medium)
                    Text(
                        DateUtils.formatDate(harvest.harvestDate),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (harvest.areaName != null) {
                    Text(harvest.areaName, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Text("Total Sales", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(DateUtils.formatCurrency(harvest.totalSales), fontWeight = FontWeight.Bold)
                }
                Column(Modifier.weight(1f)) {
                    Text("Share Amount", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(DateUtils.formatCurrency(harvest.shareAmount),
                        fontWeight = FontWeight.SemiBold)
                }
                Column {
                    FilledTonalButton(
                        onClick = onToggleShare,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            if (harvest.shareGiven) Icons.Default.Undo else Icons.Default.Check,
                            null, modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(if (harvest.shareGiven) "Undo" else "Share Given", style = MaterialTheme.typography.labelSmall)
                    }
                    Spacer(Modifier.height(4.dp))
                    IconButton(onClick = { showDeleteConfirm = true },
                        modifier = Modifier.size(32.dp).align(Alignment.End)) {
                        Icon(Icons.Default.Delete, "Delete", modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Harvest") },
            text = { Text("Delete this harvest record from ${harvest.deviceName}?") },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteConfirm = false }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddHarvestDialog(
    devices: List<com.pisowifi.pos.data.entity.PisowifiDevice>,
    onDismiss: () -> Unit,
    onConfirm: (Int, Double, Double) -> Unit
) {
    var selectedDeviceId by remember { mutableIntStateOf(-1) }
    var amount by remember { mutableStateOf("") }
    var shareAmount by remember { mutableStateOf("") }
    var deviceExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Harvest") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ExposedDropdownMenuBox(
                    expanded = deviceExpanded,
                    onExpandedChange = { deviceExpanded = !deviceExpanded }
                ) {
                    OutlinedTextField(
                        value = devices.find { it.id == selectedDeviceId }?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Device") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = deviceExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = deviceExpanded,
                        onDismissRequest = { deviceExpanded = false }
                    ) {
                        devices.forEach { device ->
                            DropdownMenuItem(
                                text = { Text(device.name) },
                                onClick = {
                                    selectedDeviceId = device.id
                                    deviceExpanded = false
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = amount,
                    onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) amount = it },
                    label = { Text("Total Sales (₱)") },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = shareAmount,
                    onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) shareAmount = it },
                    label = { Text("Share Amount (₱)") },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(selectedDeviceId, amount.toDoubleOrNull() ?: 0.0, shareAmount.toDoubleOrNull() ?: 0.0)
                },
                enabled = selectedDeviceId > 0 && (amount.toDoubleOrNull() ?: 0.0) > 0
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
