package com.example.ui.screens.profile

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Chat
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.ExpiryCountdownBadge
import com.example.ui.components.VerifiedBadge
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileDashboardScreen(
    currentUser: UserProfile?,
    businesses: List<Business>,
    reels: List<Reel>,
    offers: List<Offer>,
    followedIds: Set<String>,
    onSelectBusiness: (Business) -> Unit,
    onStartChat: (Business) -> Unit,
    onAddOffer: (businessId: String, name: String, category: BusinessCategory, orig: Double, disc: Double, code: String, valid: String) -> Unit,
    onDeleteOffer: (String) -> Unit,
    onDeleteReel: (String) -> Unit,
    onUploadReel: (title: String, caption: String) -> Unit,
    onUpdateBusiness: (businessId: String, bio: String, phone: String, whatsapp: String, address: String) -> Unit,
    onSwitchRole: (UserRole) -> Unit,
    onOpenAdminPanel: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showCreateOfferDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showUploadReelDialog by remember { mutableStateOf(false) }

    val userRole = currentUser?.role ?: UserRole.CUSTOMER
    val isOwner = userRole == UserRole.OWNER
    val isAdmin = userRole == UserRole.ADMIN

    // Find the owner's business
    val ownerBusiness = if (isOwner) {
        businesses.find { it.ownerId == currentUser?.id } ?: businesses.firstOrNull()
    } else null

    val ownerReels = remember(reels, ownerBusiness) {
        if (ownerBusiness != null) reels.filter { it.businessId == ownerBusiness.id } else emptyList()
    }

    val ownerOffers = remember(offers, ownerBusiness) {
        if (ownerBusiness != null) offers.filter { it.businessId == ownerBusiness.id } else emptyList()
    }

    val followedBusinesses = remember(businesses, followedIds) {
        businesses.filter { followedIds.contains(it.id) }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // User Profile Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BrandPrimary)
                    .padding(horizontal = 16.dp, vertical = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isOwner) Icons.Default.Store else if (isAdmin) Icons.Default.AdminPanelSettings else Icons.Default.Person,
                            contentDescription = null,
                            tint = BrandSecondary,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = currentUser?.name ?: "Guest User",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            if (isOwner) VerifiedBadge()
                        }

                        Text(
                            text = currentUser?.phone ?: "Not registered",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    when (userRole) {
                                        UserRole.OWNER -> BrandSecondary
                                        UserRole.ADMIN -> DangerRed
                                        UserRole.CUSTOMER -> BrandPrimaryLight
                                    }
                                )
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = when (userRole) {
                                    UserRole.OWNER -> "BUSINESS OWNER"
                                    UserRole.ADMIN -> "SYSTEM ADMIN"
                                    UserRole.CUSTOMER -> "CUSTOMER / CITIZEN"
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (userRole == UserRole.OWNER) Color.Black else Color.White
                            )
                        }
                    }

                    if (isAdmin) {
                        Button(
                            onClick = onOpenAdminPanel,
                            colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Admin Panel", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // ================= OWNER DASHBOARD CONTENT =================
        if (isOwner && ownerBusiness != null) {
            // Stats Row
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Shop Analytics & Reach",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DashboardMetricCard(
                            title = "Followers",
                            value = "${ownerBusiness.followerCount}",
                            icon = Icons.Default.Group,
                            modifier = Modifier.weight(1f)
                        )
                        DashboardMetricCard(
                            title = "Reel Views",
                            value = "${ownerReels.sumOf { it.views }}",
                            icon = Icons.Default.Visibility,
                            modifier = Modifier.weight(1f)
                        )
                        DashboardMetricCard(
                            title = "Active Deals",
                            value = "${ownerOffers.size}",
                            icon = Icons.Default.LocalOffer,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Manage Business Profile Bar
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
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
                        Column {
                            Text(text = ownerBusiness.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = "📍 ${ownerBusiness.area}, Himmatnagar", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                        }
                        Button(
                            onClick = { showEditProfileDialog = true },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Edit Shop Info", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Manage 10-Day Reels Section
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Your Published Reels (${ownerReels.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "All reels auto-delete after 10 days strictly",
                                fontSize = 11.sp,
                                color = BrandSecondaryDark
                            )
                        }

                        Button(
                            onClick = { showUploadReelDialog = true },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("+ Post Reel", fontSize = 12.sp)
                        }
                    }
                }
            }

            if (ownerReels.isEmpty()) {
                item {
                    Text(
                        text = "You haven't posted any reels yet. Tap '+ Post Reel' to reach customers!",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            } else {
                items(ownerReels) { reel ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = reel.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(text = "${reel.views} views • ${reel.likes} likes", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                Spacer(modifier = Modifier.height(4.dp))
                                ExpiryCountdownBadge(remainingDays = reel.getRemainingDays())
                            }

                            IconButton(onClick = { onDeleteReel(reel.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = DangerRed)
                            }
                        }
                    }
                }
            }

            // Manage Offers Section
            item {
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Your Discount Offers (${ownerOffers.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Button(
                            onClick = { showCreateOfferDialog = true },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("+ Add Offer", fontSize = 12.sp)
                        }
                    }
                }
            }

            if (ownerOffers.isEmpty()) {
                item {
                    Text(
                        text = "No offers posted yet. Add a discount coupon to appear on the Offers Finder!",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            } else {
                items(ownerOffers) { offer ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "${offer.discountPercent}% OFF", fontWeight = FontWeight.Bold, color = BrandSecondaryDark)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = offer.productName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Text(
                                    text = "₹${offer.discountedPrice.toInt()} (Reg: ₹${offer.originalPrice.toInt()}) • Code: ${offer.couponCode}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }

                            IconButton(onClick = { onDeleteOffer(offer.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = DangerRed)
                            }
                        }
                    }
                }
            }
        }

        // ================= CUSTOMER DASHBOARD CONTENT =================
        if (!isOwner) {
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Followed Himmatnagar Shops (${followedBusinesses.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (followedBusinesses.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Storefront, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("You haven't followed any shops yet.", fontWeight = FontWeight.Bold)
                            Text("Explore Himmatnagar markets and tap 'Follow' to get updates.", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            } else {
                items(followedBusinesses) { shop ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .clickable { onSelectBusiness(shop) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = shop.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(text = "${shop.category.displayName} • ${shop.area}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            }
                            IconButton(onClick = { onStartChat(shop) }) {
                                Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Chat", tint = BrandPrimary)
                            }
                        }
                    }
                }
            }
        }

        // ================= ROLE SWITCHER & SETTINGS =================
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Account & Testing Switcher:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Seamlessly preview how Customer vs Business Owner sees the app.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.outline
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onSwitchRole(UserRole.CUSTOMER) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Customer View", fontSize = 11.sp)
                    }

                    Button(
                        onClick = { onSwitchRole(UserRole.OWNER) },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandSecondary, contentColor = Color.Black),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Owner Dashboard", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { onSwitchRole(UserRole.ADMIN) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Admin", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Switch Role / Logout")
                }
            }
        }
    }

    // Dialog: Create Offer
    if (showCreateOfferDialog && ownerBusiness != null) {
        CreateOfferDialog(
            onDismiss = { showCreateOfferDialog = false },
            onCreate = { pName, cat, orig, disc, code, valid ->
                onAddOffer(ownerBusiness.id, pName, cat, orig, disc, code, valid)
                showCreateOfferDialog = false
                Toast.makeText(context, "Discount offer posted to Himmatnagar deals feed!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Dialog: Edit Shop Profile
    if (showEditProfileDialog && ownerBusiness != null) {
        EditBusinessDialog(
            currentBusiness = ownerBusiness,
            onDismiss = { showEditProfileDialog = false },
            onSave = { bio, phone, whatsapp, addr ->
                onUpdateBusiness(ownerBusiness.id, bio, phone, whatsapp, addr)
                showEditProfileDialog = false
                Toast.makeText(context, "Shop details updated successfully!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Dialog: Post Reel
    if (showUploadReelDialog) {
        com.example.ui.screens.reels.UploadReelDialog(
            onDismiss = { showUploadReelDialog = false },
            onConfirm = { t, c ->
                onUploadReel(t, c)
                showUploadReelDialog = false
                Toast.makeText(context, "Reel posted! Set to auto-expire in 10 days.", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun DashboardMetricCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            Text(text = title, fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateOfferDialog(
    onDismiss: () -> Unit,
    onCreate: (productName: String, category: BusinessCategory, original: Double, discounted: Double, code: String, valid: String) -> Unit
) {
    var productName by remember { mutableStateOf("") }
    var originalPrice by remember { mutableStateOf("") }
    var discountedPrice by remember { mutableStateOf("") }
    var couponCode by remember { mutableStateOf("") }
    var validUntil by remember { mutableStateOf("Oct 31, 2026") }
    var category by remember { mutableStateOf(BusinessCategory.ELECTRONICS) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Discount Offer", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = productName,
                    onValueChange = { productName = it },
                    label = { Text("Product / Service Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = originalPrice,
                        onValueChange = { originalPrice = it },
                        label = { Text("Original ₹") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = discountedPrice,
                        onValueChange = { discountedPrice = it },
                        label = { Text("Deal ₹") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = couponCode,
                    onValueChange = { couponCode = it.uppercase() },
                    label = { Text("Coupon Code (e.g. FESTIVE20)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = validUntil,
                    onValueChange = { validUntil = it },
                    label = { Text("Valid Until Date") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val orig = originalPrice.toDoubleOrNull() ?: 100.0
                    val disc = discountedPrice.toDoubleOrNull() ?: 80.0
                    val code = if (couponCode.isNotBlank()) couponCode else "SAVE10"
                    if (productName.isNotBlank()) {
                        onCreate(productName, category, orig, disc, code, validUntil)
                    }
                },
                enabled = productName.isNotBlank()
            ) {
                Text("Publish Offer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun EditBusinessDialog(
    currentBusiness: Business,
    onDismiss: () -> Unit,
    onSave: (bio: String, phone: String, whatsapp: String, address: String) -> Unit
) {
    var bio by remember { mutableStateOf(currentBusiness.bio) }
    var phone by remember { mutableStateOf(currentBusiness.phone) }
    var whatsapp by remember { mutableStateOf(currentBusiness.whatsapp) }
    var address by remember { mutableStateOf(currentBusiness.address) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Business Details", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Business Bio") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Calling Phone") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = whatsapp,
                    onValueChange = { whatsapp = it },
                    label = { Text("WhatsApp Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Full Address") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(bio, phone, whatsapp, address) }) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
