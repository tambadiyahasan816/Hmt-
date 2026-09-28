package com.example.ui.screens.workers

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HimmatnagarLocations
import com.example.data.model.Worker
import com.example.data.model.WorkerServiceTypes
import com.example.ui.components.WorkerCard
import com.example.ui.components.launchDialer
import com.example.ui.components.launchWhatsApp
import com.example.ui.theme.*

// FIX 5: Worker Directory Tab screen with search, service type filter & area filter
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerDirectoryScreen(
    workers: List<Worker>,
    onAddWorker: (name: String, serviceType: String, phone: String, area: String, exp: String) -> Unit,
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedServiceType by remember { mutableStateOf<String?>(null) }
    var selectedArea by remember { mutableStateOf<String?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    val filteredWorkers = remember(workers, searchQuery, selectedServiceType, selectedArea) {
        workers.filter { w ->
            val matchQuery = searchQuery.isBlank() ||
                    w.name.contains(searchQuery, ignoreCase = true) ||
                    w.serviceType.contains(searchQuery, ignoreCase = true) ||
                    w.area.contains(searchQuery, ignoreCase = true)

            val matchType = selectedServiceType == null || selectedServiceType == "All" ||
                    w.serviceType.contains(selectedServiceType!!, ignoreCase = true)

            val matchArea = selectedArea == null || selectedArea == "All" ||
                    w.area.equals(selectedArea, ignoreCase = true)

            matchQuery && matchType && matchArea
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Himmatnagar Worker Directory", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        Text("Plumbers, Electricians, Carpenters & Technicians", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    }
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            tint = BrandPrimary,
                            modifier = Modifier.padding(start = 16.dp, end = 8.dp).size(22.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = BrandPrimary,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                text = { Text("List a Worker", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Search Input
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by name, service or area (e.g. Plumber, Motipura)...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = BrandPrimary) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Service Type Chips
            item {
                Column {
                    Text(
                        text = "Filter by Profession / Service:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedServiceType == null,
                                onClick = { selectedServiceType = null },
                                label = { Text("All Services") }
                            )
                        }
                        items(WorkerServiceTypes.ALL.filter { it != "Other" }) { sType ->
                            FilterChip(
                                selected = selectedServiceType == sType,
                                onClick = {
                                    selectedServiceType = if (selectedServiceType == sType) null else sType
                                },
                                label = { Text(sType) }
                            )
                        }
                    }
                }
            }

            // Area Filter Chips
            item {
                Column {
                    Text(
                        text = "Filter by Himmatnagar Locality:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedArea == null,
                                onClick = { selectedArea = null },
                                label = { Text("All Areas") }
                            )
                        }
                        items(HimmatnagarLocations.AREAS) { area ->
                            FilterChip(
                                selected = selectedArea == area,
                                onClick = {
                                    selectedArea = if (selectedArea == area) null else area
                                },
                                label = { Text(area) }
                            )
                        }
                    }
                }
            }

            // Results count
            item {
                Text(
                    text = "${filteredWorkers.size} workers & service technicians available",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            if (filteredWorkers.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Engineering, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No workers found", fontWeight = FontWeight.Bold)
                            Text("Try clearing filters or search query to see all Himmatnagar workers.", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            } else {
                // Worker Cards with Call & WhatsApp Buttons
                items(filteredWorkers, key = { it.id }) { worker ->
                    WorkerCard(
                        worker = worker,
                        onCallClick = { launchDialer(context, worker.phone) },
                        onWhatsAppClick = { launchWhatsApp(context, worker.phone) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(70.dp))
            }
        }
    }

    if (showAddDialog) {
        AddWorkerDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, sType, phone, area, exp ->
                onAddWorker(name, sType, phone, area, exp)
                showAddDialog = false
                Toast.makeText(context, "Worker listed in Himmatnagar directory!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWorkerDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, serviceType: String, phone: String, area: String, exp: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var serviceType by remember { mutableStateOf(WorkerServiceTypes.ALL.first()) }
    var customServiceType by remember { mutableStateOf("") }
    var serviceExpanded by remember { mutableStateOf(false) }

    var phone by remember { mutableStateOf("") }
    var area by remember { mutableStateOf(HimmatnagarLocations.AREAS.first()) }
    var areaExpanded by remember { mutableStateOf(false) }
    var exp by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("List a Worker in Himmatnagar", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Help local technicians and workers get work across Himmatnagar.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.outline
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Worker Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = serviceExpanded,
                    onExpandedChange = { serviceExpanded = !serviceExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = serviceType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Service Type / Profession") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = serviceExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = serviceExpanded,
                        onDismissRequest = { serviceExpanded = false },
                        modifier = Modifier.heightIn(max = 240.dp)
                    ) {
                        WorkerServiceTypes.ALL.forEach { s ->
                            DropdownMenuItem(
                                text = { Text(s) },
                                onClick = {
                                    serviceType = s
                                    serviceExpanded = false
                                }
                            )
                        }
                    }
                }

                if (serviceType == "Other") {
                    OutlinedTextField(
                        value = customServiceType,
                        onValueChange = { customServiceType = it },
                        label = { Text("Specify Profession") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = phone,
                    onValueChange = { if (it.length <= 10) phone = it },
                    label = { Text("Contact Phone") },
                    prefix = { Text("+91 ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = areaExpanded,
                    onExpandedChange = { areaExpanded = !areaExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = area,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Himmatnagar Area") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = areaExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = areaExpanded,
                        onDismissRequest = { areaExpanded = false },
                        modifier = Modifier.heightIn(max = 240.dp)
                    ) {
                        HimmatnagarLocations.AREAS.forEach { a ->
                            DropdownMenuItem(
                                text = { Text(a) },
                                onClick = {
                                    area = a
                                    areaExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = exp,
                    onValueChange = { exp = it },
                    label = { Text("Experience (e.g. 5 Years Exp)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            val effectiveService = if (serviceType == "Other") customServiceType.trim() else serviceType
            Button(
                onClick = {
                    if (name.isNotBlank() && effectiveService.isNotBlank() && phone.length >= 10) {
                        onAdd(name, effectiveService, "+91 $phone", area, if (exp.isNotBlank()) exp else "Experienced")
                    }
                },
                enabled = name.isNotBlank() && effectiveService.isNotBlank() && phone.length >= 10
            ) {
                Text("Add Listing")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
