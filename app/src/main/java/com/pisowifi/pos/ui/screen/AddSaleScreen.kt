package com.pisowifi.pos.ui.screen

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pisowifi.pos.data.entity.SaleRecord
import com.pisowifi.pos.data.repository.PisoRepository
import com.pisowifi.pos.util.DateUtils
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSaleScreen(
    repository: PisoRepository,
    navController: NavController,
    preselectedPisowifiId: Int? = null
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val devices by repository.getAllPisowifi().collectAsState(initial = emptyList())

    var selectedDeviceId by remember { mutableStateOf(preselectedPisowifiId) }
    var amount by remember { mutableStateOf("") }
    var deviceExpanded by remember { mutableStateOf(false) }

    val cal = Calendar.getInstance()
    var selectedDateMillis by remember { mutableStateOf(cal.timeInMillis) }
    var dateLabel by remember { mutableStateOf(DateUtils.formatDate(cal.timeInMillis)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Record Sale") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (devices.isEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, null)
                        Spacer(Modifier.width(12.dp))
                        Text("Add a Pisowifi device first before recording sales.")
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = deviceExpanded,
                onExpandedChange = { deviceExpanded = !deviceExpanded }
            ) {
                OutlinedTextField(
                    value = devices.find { it.id == selectedDeviceId }?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Pisowifi Device") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = deviceExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    leadingIcon = { Icon(Icons.Default.Router, null) }
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
                onValueChange = {
                    if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) amount = it
                },
                label = { Text("Amount (₱)") },
                placeholder = { Text("0.00") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Payments, null) }
            )

            OutlinedTextField(
                value = dateLabel,
                onValueChange = {},
                readOnly = true,
                label = { Text("Date") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.CalendarToday, null) },
                trailingIcon = {
                    IconButton(onClick = {
                        val c = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                selectedDateMillis = DateUtils.dateToMillis(year, month, day)
                                dateLabel = DateUtils.formatDate(selectedDateMillis)
                            },
                            c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }) {
                        Icon(Icons.Default.DateRange, "Pick Date")
                    }
                }
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    scope.launch {
                        val sale = SaleRecord(
                            pisowifiId = selectedDeviceId ?: return@launch,
                            amount = amount.toDoubleOrNull() ?: return@launch,
                            date = selectedDateMillis
                        )
                        repository.addSale(sale)
                        navController.popBackStack()
                    }
                },
                enabled = selectedDeviceId != null && amount.toDoubleOrNull() ?: 0.0 > 0,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Record Sale")
            }
        }
    }
}
