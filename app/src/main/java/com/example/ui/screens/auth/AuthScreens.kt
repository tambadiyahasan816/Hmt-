package com.example.ui.screens.auth

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BusinessCategories
import com.example.data.model.HimmatnagarLocations
import com.example.data.model.WorkerServiceTypes
import com.example.ui.theme.*

// FIX 2: Role selection screen with 3 options: Business Owner, Worker, Customer
@Composable
fun RoleSelectionScreen(
    onSelectCustomer: () -> Unit,
    onSelectOwner: () -> Unit,
    onSelectWorker: () -> Unit,
    onContinueAsGuest: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        BrandPrimary,
                        Color(0xFF172554),
                        Color(0xFF0F172A)
                    )
                )
            )
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // App Brand Header
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White.copy(alpha = 0.12f))
                    .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(22.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Storefront,
                    contentDescription = null,
                    tint = BrandSecondary,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Sheher Himmatnagar",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Local Shops, Deals, Workers & Services",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Choose your role to get started:",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.9f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Option 1: Business Owner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectOwner() },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.15f)
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(
                        listOf(BrandSecondary, BrandSecondaryDark)
                    ),
                    width = 1.5.dp
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(BrandSecondary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Store,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "I'm a Business Owner",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Have a shop? Post reels, publish offers, manage dashboard & connect with customers",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            lineHeight = 15.sp
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = BrandSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Option 2: Worker (FIX 2)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectWorker() },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.15f)
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(
                        listOf(Color(0xFFF97316), Color(0xFFEA580C))
                    ),
                    width = 1.5.dp
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF97316)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "I'm a Worker",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "No shop? Plumbers, electricians, painters, mechanics, tutors get direct customer calls",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            lineHeight = 15.sp
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color(0xFFF97316),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Option 3: Customer
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectCustomer() },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.12f)
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(
                        listOf(BrandPrimaryLight, Color(0xFF60A5FA))
                    ),
                    width = 1.5.dp
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(BrandPrimaryLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "I'm a Customer",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Discover Himmatnagar shops, highest discounts, watch reels & find local workers",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            lineHeight = 15.sp
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = BrandPrimaryLight,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(
                onClick = onContinueAsGuest,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Explore as Guest",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerAuthScreen(
    onBack: () -> Unit,
    onSuccess: (name: String, phone: String) -> Unit
) {
    val context = LocalContext.current
    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }
    var otpInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Customer Registration") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Text(
                text = "Welcome to Himmatnagar!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Sign in to save favorite shops, claim coupon codes, chat with local owners and discover best deals.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(28.dp))

            if (!isOtpSent) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { if (it.length <= 10) phoneNumber = it },
                    label = { Text("Mobile Phone Number") },
                    prefix = { Text("+91 ") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (fullName.isBlank() || phoneNumber.length < 10) {
                            Toast.makeText(context, "Please enter your name and 10-digit mobile number", Toast.LENGTH_SHORT).show()
                        } else {
                            isOtpSent = true
                            otpInput = "482910"
                            Toast.makeText(context, "OTP sent to +91 $phoneNumber (Demo code: 482910)", Toast.LENGTH_LONG).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Get Verification OTP", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "OTP sent to +91 $phoneNumber",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Enter the 6-digit verification code below:",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = otpInput,
                    onValueChange = { if (it.length <= 6) otpInput = it },
                    label = { Text("Enter 6-Digit OTP") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (otpInput.length == 6) {
                            onSuccess(fullName, "+91 $phoneNumber")
                        } else {
                            Toast.makeText(context, "Please enter 6-digit OTP", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Verify & Continue", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = { isOtpSent = false },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Change Mobile Number")
                }
            }
        }
    }
}

// FIX 2: Worker Registration Flow
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerAuthScreen(
    onBack: () -> Unit,
    onSuccess: (name: String, phone: String, serviceType: String, area: String, exp: String, photoUrl: String) -> Unit
) {
    val context = LocalContext.current
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var selectedServiceType by remember { mutableStateOf(WorkerServiceTypes.ALL.first()) }
    var customServiceType by remember { mutableStateOf("") }
    var serviceExpanded by remember { mutableStateOf(false) }

    var selectedArea by remember { mutableStateOf(HimmatnagarLocations.AREAS.first()) }
    var customAreaInput by remember { mutableStateOf("") }
    var showCustomAreaDialog by remember { mutableStateOf(false) }
    var areaExpanded by remember { mutableStateOf(false) }

    var experienceYears by remember { mutableStateOf("") }
    var isOtpStep by remember { mutableStateOf(false) }
    var otpCode by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Worker Registration") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text(
                text = "Register as a Worker in Himmatnagar",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "For plumbers, electricians, carpenters, painters, tutors, mechanics and repair technicians. Receive direct customer calls with no commission.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (!isOtpStep) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { if (it.length <= 10) phone = it },
                    label = { Text("Mobile Phone Number (Calls & WhatsApp)") },
                    prefix = { Text("+91 ") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Service Type Dropdown
                ExposedDropdownMenuBox(
                    expanded = serviceExpanded,
                    onExpandedChange = { serviceExpanded = !serviceExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedServiceType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Service Type / Profession") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = serviceExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = serviceExpanded,
                        onDismissRequest = { serviceExpanded = false },
                        modifier = Modifier.heightIn(max = 280.dp)
                    ) {
                        WorkerServiceTypes.ALL.forEach { sType ->
                            DropdownMenuItem(
                                text = { Text(sType) },
                                onClick = {
                                    selectedServiceType = sType
                                    serviceExpanded = false
                                }
                            )
                        }
                    }
                }

                if (selectedServiceType == "Other") {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = customServiceType,
                        onValueChange = { customServiceType = it },
                        label = { Text("Specify Your Profession / Skill") },
                        placeholder = { Text("e.g. Sofa Cleaner, Curtain Maker") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Location Dropdown with "Add New Location" (FIX 3)
                ExposedDropdownMenuBox(
                    expanded = areaExpanded,
                    onExpandedChange = { areaExpanded = !areaExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedArea,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Himmatnagar Area / Ward") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = areaExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = areaExpanded,
                        onDismissRequest = { areaExpanded = false },
                        modifier = Modifier.heightIn(max = 280.dp)
                    ) {
                        HimmatnagarLocations.AREAS.forEach { areaName ->
                            DropdownMenuItem(
                                text = { Text(areaName) },
                                onClick = {
                                    selectedArea = areaName
                                    areaExpanded = false
                                }
                            )
                        }
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AddLocationAlt, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Add New Location...", fontWeight = FontWeight.Bold, color = BrandPrimary)
                                }
                            },
                            onClick = {
                                areaExpanded = false
                                showCustomAreaDialog = true
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = experienceYears,
                    onValueChange = { experienceYears = it },
                    label = { Text("Years of Experience (Optional)") },
                    placeholder = { Text("e.g. 5 Years Exp") },
                    leadingIcon = { Icon(Icons.Default.WorkHistory, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val effectiveService = if (selectedServiceType == "Other") customServiceType.trim() else selectedServiceType
                        if (fullName.isBlank() || phone.length < 10 || effectiveService.isBlank()) {
                            Toast.makeText(context, "Please enter your name, 10-digit phone and profession", Toast.LENGTH_SHORT).show()
                        } else {
                            isOtpStep = true
                            otpCode = "729410"
                            Toast.makeText(context, "OTP sent to +91 $phone (Demo code: 729410)", Toast.LENGTH_LONG).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Proceed to OTP Verification", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                // OTP Step
                OutlinedTextField(
                    value = otpCode,
                    onValueChange = { if (it.length <= 6) otpCode = it },
                    label = { Text("Enter 6-Digit Worker OTP") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (otpCode.length == 6) {
                            val finalService = if (selectedServiceType == "Other" && customServiceType.isNotBlank()) customServiceType.trim() else selectedServiceType
                            onSuccess(
                                fullName,
                                "+91 $phone",
                                finalService,
                                selectedArea,
                                experienceYears,
                                ""
                            )
                        } else {
                            Toast.makeText(context, "Please enter 6-digit OTP", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Verify & Create Worker Profile", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = { isOtpStep = false },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Back to Edit Details")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showCustomAreaDialog) {
        AlertDialog(
            onDismissRequest = { showCustomAreaDialog = false },
            title = { Text("Add New Location in Himmatnagar", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter locality, society or landmark name:")
                    OutlinedTextField(
                        value = customAreaInput,
                        onValueChange = { customAreaInput = it },
                        placeholder = { Text("e.g. Mehsana Highway, Shrinathji Nagar") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customAreaInput.isNotBlank()) {
                            HimmatnagarLocations.addCustomArea(customAreaInput)
                            selectedArea = customAreaInput.trim()
                            showCustomAreaDialog = false
                            customAreaInput = ""
                            Toast.makeText(context, "New location added to Himmatnagar list!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = customAreaInput.isNotBlank()
                ) {
                    Text("Add Location")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomAreaDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// FIX 1 & FIX 3: Business Owner Registration with full categories & expanded locations
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessOwnerAuthScreen(
    onBack: () -> Unit,
    onSuccess: (
        shopName: String,
        ownerName: String,
        phone: String,
        category: String,
        area: String,
        address: String,
        whatsapp: String
    ) -> Unit
) {
    val context = LocalContext.current
    var shopName by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var whatsapp by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(BusinessCategories.ALL.first()) }
    var customCategoryInput by remember { mutableStateOf("") }
    var categoryExpanded by remember { mutableStateOf(false) }

    var selectedArea by remember { mutableStateOf(HimmatnagarLocations.AREAS.first()) }
    var customAreaInput by remember { mutableStateOf("") }
    var showCustomAreaDialog by remember { mutableStateOf(false) }
    var areaExpanded by remember { mutableStateOf(false) }

    var address by remember { mutableStateOf("") }
    var isGpsPinned by remember { mutableStateOf(false) }
    var isOtpStep by remember { mutableStateOf(false) }
    var otpCode by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Business Owner Registration") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text(
                text = "Register Your Himmatnagar Business",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Reach thousands of local customers, share 10-day video reels and publish hot discount offers.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (!isOtpStep) {
                OutlinedTextField(
                    value = shopName,
                    onValueChange = { shopName = it },
                    label = { Text("Shop / Business Name") },
                    leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = ownerName,
                    onValueChange = { ownerName = it },
                    label = { Text("Owner Full Name") },
                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { if (it.length <= 10) phone = it },
                    label = { Text("Calling Phone Number") },
                    prefix = { Text("+91 ") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = whatsapp,
                    onValueChange = { if (it.length <= 10) whatsapp = it },
                    label = { Text("WhatsApp Business Number") },
                    prefix = { Text("+91 ") },
                    leadingIcon = { Icon(Icons.Default.Send, contentDescription = null, tint = WhatsAppGreen) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // FIX 1: Business Category Dropdown with full categories list + "Other"
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Business Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false },
                        modifier = Modifier.heightIn(max = 280.dp)
                    ) {
                        BusinessCategories.ALL.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    selectedCategory = category
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                // If "Other" selected, show custom text field (FIX 1)
                if (selectedCategory == "Other") {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = customCategoryInput,
                        onValueChange = { customCategoryInput = it },
                        label = { Text("Type Your Business Category Name") },
                        placeholder = { Text("e.g. Antique Store, Organic Nursery") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // FIX 3: Expanded Himmatnagar Area Dropdown with "Add New Location"
                ExposedDropdownMenuBox(
                    expanded = areaExpanded,
                    onExpandedChange = { areaExpanded = !areaExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedArea,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Himmatnagar Area / Ward") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = areaExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = areaExpanded,
                        onDismissRequest = { areaExpanded = false },
                        modifier = Modifier.heightIn(max = 280.dp)
                    ) {
                        HimmatnagarLocations.AREAS.forEach { areaName ->
                            DropdownMenuItem(
                                text = { Text(areaName) },
                                onClick = {
                                    selectedArea = areaName
                                    areaExpanded = false
                                }
                            )
                        }
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AddLocationAlt, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Add New Location...", fontWeight = FontWeight.Bold, color = BrandPrimary)
                                }
                            },
                            onClick = {
                                areaExpanded = false
                                showCustomAreaDialog = true
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Full Shop Address (Street, Landmark)") },
                    leadingIcon = { Icon(Icons.Default.Place, contentDescription = null) },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // GPS Location Pin Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.GpsFixed,
                                contentDescription = null,
                                tint = if (isGpsPinned) BrandAccent else MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "GPS Location Pin",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = if (isGpsPinned) "Lat 23.5977, Lng 72.9667 (Himmatnagar Central)" else "Pin not set",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }

                        TextButton(onClick = {
                            isGpsPinned = true
                            Toast.makeText(context, "Location pinned accurately in Himmatnagar", Toast.LENGTH_SHORT).show()
                        }) {
                            Text(if (isGpsPinned) "Pinned" else "Set Pin")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val effectiveCategory = if (selectedCategory == "Other") customCategoryInput.trim() else selectedCategory
                        if (shopName.isBlank() || ownerName.isBlank() || phone.length < 10 || effectiveCategory.isBlank()) {
                            Toast.makeText(context, "Please complete all mandatory fields", Toast.LENGTH_SHORT).show()
                        } else {
                            isOtpStep = true
                            otpCode = "918234"
                            Toast.makeText(context, "OTP sent to verify shop owner (Demo code: 918234)", Toast.LENGTH_LONG).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Proceed to Phone OTP Verification", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                // OTP Step
                OutlinedTextField(
                    value = otpCode,
                    onValueChange = { if (it.length <= 6) otpCode = it },
                    label = { Text("Enter 6-Digit Owner OTP") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (otpCode.length == 6) {
                            val finalWhatsapp = if (whatsapp.isNotBlank()) "+91 $whatsapp" else "+91 $phone"
                            val finalAddress = if (address.isNotBlank()) address else "$selectedArea, Himmatnagar"
                            val finalCategory = if (selectedCategory == "Other" && customCategoryInput.isNotBlank()) customCategoryInput.trim() else selectedCategory
                            onSuccess(
                                shopName,
                                ownerName,
                                "+91 $phone",
                                finalCategory,
                                selectedArea,
                                finalAddress,
                                finalWhatsapp
                            )
                        } else {
                            Toast.makeText(context, "Please enter 6-digit OTP", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Verify & Submit Business Profile", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = { isOtpStep = false },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Back to Edit Details")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showCustomAreaDialog) {
        AlertDialog(
            onDismissRequest = { showCustomAreaDialog = false },
            title = { Text("Add New Location in Himmatnagar", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter area, society or landmark name:")
                    OutlinedTextField(
                        value = customAreaInput,
                        onValueChange = { customAreaInput = it },
                        placeholder = { Text("e.g. Mahavir Nagar Ext., Shrinathji Society") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customAreaInput.isNotBlank()) {
                            HimmatnagarLocations.addCustomArea(customAreaInput)
                            selectedArea = customAreaInput.trim()
                            showCustomAreaDialog = false
                            customAreaInput = ""
                            Toast.makeText(context, "New location added to Himmatnagar list!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = customAreaInput.isNotBlank()
                ) {
                    Text("Add Location")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomAreaDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
