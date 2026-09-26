package com.example.ui.screens.workers

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HimmatnagarLocations
import com.example.data.model.Worker
import com.example.ui.components.launchDialer
import com.example.ui.components.launchWhatsApp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerDirectoryScreen(
    workers: List<Worker>,
    onAddWorker: (name: String, serviceType: String, phone: String, area: String, exp: String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedServiceType by remember { mutableStateOf<String?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    val serviceTypes = listOf("All", "Plumber", "Electrician", "AC Repair & Technician", "Mathematics & Science Tutor", "Carpenter & Wood Interior", "House Painter")

    val filteredWorkers = remember(workers, searchQuery, selectedServiceType) {
        workers.filter { w ->
            val matchQuery = searchQuery.isBlank() ||
                    w.name.contains(searchQuery, ignoreCase = true) ||
                    w.serviceType.contains(searchQuery, ignoreCase = true) ||
                    w.area.contains(searchQuery, ignoreCase = true)

            val matchType = selectedServiceType == null || selectedServiceType == "All" ||
                    w.serviceType.contains(selectedServiceType!!, ignoreCase = true)

            matchQuery && matchType
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Himmatnagar Worker Directory", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        Text("Plumbers, Electricians, Tutors & Mechanics", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(serviceTypes) { sType ->
                        val isSelected = (selectedServiceType == null && sType == "All") || selectedServiceType == sType
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedServiceType = if (sType == "All") null else sType
                            },
                            label = { Text(sType) }
                        )
                    }
                }
            }

            // Results count
            item {
                Text(
                    text = "${filteredWorkers.size} service providers available in Himmatnagar",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            // Worker Cards
            items(filteredWorkers, key = { it.id }) { worker ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Worker Glyph
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(BrandPrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Engineering,
                                contentDescription = null,
                                tint = BrandPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = worker.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = worker.serviceType,
                                style = MaterialTheme.typography.bodySmall,
                                color = BrandPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "📍 ${worker.area}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "• ${worker.experience}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }

                        // Call & WhatsApp Buttons
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilledTonalIconButton(
                                onClick = { launchDialer(context, worker.phone) },
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = "Call", modifier = Modifier.size(18.dp))
                            }

                            FilledTonalIconButton(
                                onClick = { launchWhatsApp(context, worker.phone) },
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = WhatsAppGreen.copy(alpha = 0.15f),
                                    contentColor = WhatsAppGreen
                                ),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = "WhatsApp", modifier = Modifier.size(18.dp))
                            }
                        }
                    }
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
    var serviceType by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var area by remember { mutableStateOf(HimmatnagarLocations.AREAS.first()) }
    var exp by remember { mutableStateOf("") }
    var areaExpanded by remember { mutableStateOf(false) }

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

                OutlinedTextField(
                    value = serviceType,
                    onValueChange = { serviceType = it },
                    label = { Text("Service Type (e.g. Plumber, Tutor)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

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
                        onDismissRequest = { areaExpanded = false }
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
            Button(
                onClick = {
                    if (name.isNotBlank() && serviceType.isNotBlank() && phone.length >= 10) {
                        onAdd(name, serviceType, "+91 $phone", area, if (exp.isNotBlank()) exp else "Experienced")
                    }
                },
                enabled = name.isNotBlank() && serviceType.isNotBlank() && phone.length >= 10
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
