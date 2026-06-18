package com.pisowifi.pos.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pisowifi.pos.data.entity.Area
import com.pisowifi.pos.data.entity.PisowifiDevice
import com.pisowifi.pos.data.repository.PisoRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPisowifiScreen(
    repository: PisoRepository,
    navController: NavController,
    editDeviceId: Int? = null
) {
    val scope = rememberCoroutineScope()
    val areas by repository.getAllAreas().collectAsState(initial = emptyList())

    val isEditing = editDeviceId != null
    var name by remember { mutableStateOf("") }
    var selectedAreaId by remember { mutableStateOf<Int?>(null) }
    var capital by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(isEditing) }
    var areaExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(editDeviceId) {
        if (editDeviceId != null) {
            val device = repository.getPisowifiById(editDeviceId)
            if (device != null) {
                name = device.name
                selectedAreaId = device.areaId
                capital = if (device.capital > 0) device.capital.toString() else ""
            }
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Device" else "Add Device") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Device Name") },
                    placeholder = { Text("e.g. Vendo 1 - Daanbantayan") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Router, null) }
                )

                ExposedDropdownMenuBox(
                    expanded = areaExpanded,
                    onExpandedChange = { areaExpanded = !areaExpanded }
                ) {
                    OutlinedTextField(
                        value = areas.find { it.id == selectedAreaId }?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Area") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = areaExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        leadingIcon = { Icon(Icons.Default.LocationOn, null) }
                    )
                    ExposedDropdownMenu(
                        expanded = areaExpanded,
                        onDismissRequest = { areaExpanded = false }
                    ) {
                        areas.forEach { area ->
                            DropdownMenuItem(
                                text = { Text(area.name) },
                                onClick = {
                                    selectedAreaId = area.id
                                    areaExpanded = false
                                }
                            )
                        }
                        if (areas.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No areas yet. Go to Areas to add.", fontStyle = androidx.compose.ui.text.font.FontStyle.Italic) },
                                onClick = { areaExpanded = false }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = capital,
                    onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) capital = it },
                    label = { Text("Capital (₱)") },
                    placeholder = { Text("0.00") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.AccountBalance, null) }
                )

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = {
                        scope.launch {
                            val device = PisowifiDevice(
                                id = editDeviceId ?: 0,
                                name = name.trim(),
                                areaId = selectedAreaId ?: 0,
                                capital = capital.toDoubleOrNull() ?: 0.0
                            )
                            if (isEditing) repository.updatePisowifi(device)
                            else repository.addPisowifi(device)
                            navController.popBackStack()
                        }
                    },
                    enabled = name.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (isEditing) "Update Device" else "Add Device")
                }
            }
        }
    }
}
