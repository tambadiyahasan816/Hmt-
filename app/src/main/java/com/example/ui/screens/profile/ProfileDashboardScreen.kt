package com.example.ui.screens.profile

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
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
import com.example.ui.components.WorkerCard
import com.example.ui.components.launchDialer
import com.example.ui.components.launchWhatsApp
import com.example.ui.theme.*

// FIX 6 & FIX 7: Redesigned Owner Dashboard & Worker Profile View
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileDashboardScreen(
    currentUser: UserProfile?,
    businesses: List<Business>,
    reels: List<Reel>,
    offers: List<Offer>,
    workers: List<Worker> = emptyList(),
    businessViews: Map<String, Int> = emptyMap(),
    followedIds: Set<String>,
    onSelectBusiness: (Business) -> Unit,
    onStartChat: (Business) -> Unit,
    onAddOffer: (businessId: String, name: String, category: String, orig: Double, disc: Double, code: String, valid: String) -> Unit,
    onDeleteOffer: (String) -> Unit,
    onDeleteReel: (String) -> Unit,
    onUploadReel: (title: String, caption: String) -> Unit,
    onUpdateBusiness: (businessId: String, bio: String, phone: String, whatsapp: String, address: String) -> Unit,
    onUpdateWorker: (workerId: String, name: String, serviceType: String, phone: String, area: String, exp: String) -> Unit = { _, _, _, _, _, _ -> },
    onSwitchRole: (UserRole) -> Unit,
    onOpenAdminPanel: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showCreateOfferDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showUploadReelDialog by remember { mutableStateOf(false) }
    var showEditWorkerDialog by remember { mutableStateOf(false) }

    val userRole = currentUser?.role ?: UserRole.CUSTOMER
    val isOwner = userRole == UserRole.OWNER
    val isWorker = userRole == UserRole.WORKER
    val isAdmin = userRole == UserRole.ADMIN

    // Find the owner's business
    val ownerBusiness = if (isOwner) {
        businesses.find { it.ownerId == currentUser?.id } ?: businesses.firstOrNull()
    } else null

    // Find worker profile if worker
    val workerProfile = if (isWorker) {
        workers.find { it.profileId == currentUser?.id || it.phone == currentUser?.phone }
            ?: workers.firstOrNull()
            ?: Worker(
                id = currentUser?.id ?: "w_default",
                profileId = currentUser?.id ?: "w_default",
                name = currentUser?.name ?: "Worker",
                serviceType = "Technician",
                phone = currentUser?.phone ?: "+91 98250 11223",
                whatsapp = currentUser?.phone ?: "+91 98250 11223",
                area = "Himmatnagar",
                experienceYears = "Experienced"
            )
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
        // ================= TOP BRAND / USER HEADER =================
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BrandPrimary)
                    .padding(horizontal = 16.dp, vertical = 18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when {
                                isOwner -> Icons.Default.Store
                                isWorker -> Icons.Default.Engineering
                                isAdmin -> Icons.Default.AdminPanelSettings
                                else -> Icons.Default.Person
                            },
                            contentDescription = null,
                            tint = when {
                                isOwner -> BrandSecondary
                                isWorker -> Color(0xFFF97316)
                                else -> Color.White
                            },
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentUser?.name ?: "Guest User",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Text(
                            text = currentUser?.phone ?: "Guest Mode",
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
                                        UserRole.WORKER -> Color(0xFFF97316)
                                        UserRole.ADMIN -> DangerRed
                                        UserRole.CUSTOMER -> BrandPrimaryLight
                                    }
                                )
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = when (userRole) {
                                    UserRole.OWNER -> "BUSINESS OWNER"
                                    UserRole.WORKER -> "REGISTERED WORKER"
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
                            Text("Admin", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // ================= FIX 6: REDESIGNED OWNER DASHBOARD =================
        if (isOwner && ownerBusiness != null) {
            // 1. Shop name, category, area, status ("Live" with green dot if approved, "Under Review" if pending)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = ownerBusiness.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${ownerBusiness.category} • ${ownerBusiness.area}, Himmatnagar",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }

                            // Status Badge
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (ownerBusiness.isApproved) SuccessGreen.copy(alpha = 0.12f) else Color(0xFFFEF3C7),
                                border = BorderStroke(
                                    1.dp,
                                    if (ownerBusiness.isApproved) SuccessGreen.copy(alpha = 0.4f) else Color(0xFFF59E0B)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (ownerBusiness.isApproved) SuccessGreen else Color(0xFFD97706))
                                    )
                                    Text(
                                        text = if (ownerBusiness.isApproved) "Live" else "Under Review",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (ownerBusiness.isApproved) SuccessGreen else Color(0xFFB45309)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Quick Stats: Profile Views, Followers, Active Reels, Active Offers — REAL NUMBERS FROM DB (0 if none)
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "Real-Time Shop Statistics",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val viewsCount = businessViews[ownerBusiness.id] ?: 0
                    val followersCount = ownerBusiness.followerCount // Real follower count, 0 if new account
                    val activeReelsCount = ownerReels.count { !it.isExpired() }
                    val activeOffersCount = ownerOffers.size

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DashboardMetricCard(
                            title = "Profile Views",
                            value = "$viewsCount",
                            icon = Icons.Default.Visibility,
                            modifier = Modifier.weight(1f)
                        )
                        DashboardMetricCard(
                            title = "Followers",
                            value = "$followersCount",
                            icon = Icons.Default.Group,
                            modifier = Modifier.weight(1f)
                        )
                        DashboardMetricCard(
                            title = "Active Reels",
                            value = "$activeReelsCount",
                            icon = Icons.Default.PlayCircle,
                            modifier = Modifier.weight(1f)
                        )
                        DashboardMetricCard(
                            title = "Active Offers",
                            value = "$activeOffersCount",
                            icon = Icons.Default.LocalOffer,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 3. 4 Action Buttons: "Post a Reel", "Create Offer", "Edit Shop Profile", "View Public Profile"
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Quick Actions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showUploadReelDialog = true },
                            modifier = Modifier.weight(1f).height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(Icons.Default.VideoCameraBack, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Post a Reel", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { showCreateOfferDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandSecondary, contentColor = Color.Black),
                            modifier = Modifier.weight(1f).height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(Icons.Default.LocalOffer, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Create Offer", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showEditProfileDialog = true },
                            modifier = Modifier.weight(1f).height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit Shop Profile", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = { onSelectBusiness(ownerBusiness) },
                            modifier = Modifier.weight(1f).height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("View Public Profile", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // 4. Active Reels section with countdown badges and a Delete button on each
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Active Reels (${ownerReels.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Reels auto-delete after 10 days with visible countdown",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }

            if (ownerReels.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.PlayCircle, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("No reels yet", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Post 60s video updates of your products to get local discovery.", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            } else {
                items(ownerReels, key = { it.id }) { reel ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = reel.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(
                                    text = "${reel.views} views • ${reel.likes} likes",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                ExpiryCountdownBadge(remainingDays = reel.getRemainingDays())
                            }

                            IconButton(onClick = { onDeleteReel(reel.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Reel", tint = DangerRed)
                            }
                        }
                    }
                }
            }

            // 5. Active Offers section with a Delete button on each
            item {
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 6.dp)) {
                    Text(
                        text = "Active Offers (${ownerOffers.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (ownerOffers.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.LocalOffer, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("No offers posted yet", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Publish a discount code to feature on the Explore Deals finder.", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            } else {
                items(ownerOffers, key = { it.id }) { offer ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${offer.discountPercent}% OFF",
                                        fontWeight = FontWeight.Bold,
                                        color = BrandSecondaryDark
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = offer.productName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Text(
                                    text = "₹${offer.discountedPrice.toInt()} (Reg: ₹${offer.originalPrice.toInt()}) • Code: ${offer.couponCode}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }

                            IconButton(onClick = { onDeleteOffer(offer.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Offer", tint = DangerRed)
                            }
                        }
                    }
                }
            }
        }

        // ================= FIX 7: WORKER PROFILE VIEW AFTER LOGIN =================
        if (isWorker && workerProfile != null) {
            // Worker card
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Your Worker Listing",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    WorkerCard(
                        worker = workerProfile,
                        onCallClick = { launchDialer(context, workerProfile.phone) },
                        onWhatsAppClick = { launchWhatsApp(context, workerProfile.phone) }
                    )
                }
            }

            // Edit Profile Button
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Button(
                        onClick = { showEditWorkerDialog = true },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Edit Profile", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Explanation card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, BrandPrimary.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = BrandPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Himmatnagar Worker Directory Status",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = BrandPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "As a registered worker in Himmatnagar, your profile is listed in the Workers directory. Customers can call or WhatsApp you directly for work.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }

        // ================= CUSTOMER VIEW =================
        if (!isOwner && !isWorker) {
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
                                Text(text = "${shop.category} • ${shop.area}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
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
                    text = "Account & Role Testing Switcher:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Instantly switch views to preview the Customer, Business Owner or Worker experience.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.outline
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(
                        onClick = { onSwitchRole(UserRole.CUSTOMER) },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Text("Customer", fontSize = 11.sp)
                    }

                    Button(
                        onClick = { onSwitchRole(UserRole.OWNER) },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandSecondary, contentColor = Color.Black),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Text("Owner", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { onSwitchRole(UserRole.WORKER) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF97316), contentColor = Color.White),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Text("Worker", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { onSwitchRole(UserRole.ADMIN) },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Text("Admin", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

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
            defaultCategory = ownerBusiness.category,
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

    // Dialog: Edit Worker Profile
    if (showEditWorkerDialog && workerProfile != null) {
        EditWorkerDialog(
            currentWorker = workerProfile,
            onDismiss = { showEditWorkerDialog = false },
            onSave = { name, sType, phone, area, exp ->
                onUpdateWorker(workerProfile.id, name, sType, phone, area, exp)
                showEditWorkerDialog = false
                Toast.makeText(context, "Worker profile updated!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun DashboardMetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            Text(text = title, fontSize = 9.sp, color = MaterialTheme.colorScheme.outline, maxLines = 1)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateOfferDialog(
    defaultCategory: String,
    onDismiss: () -> Unit,
    onCreate: (productName: String, category: String, original: Double, discounted: Double, code: String, valid: String) -> Unit
) {
    var productName by remember { mutableStateOf("") }
    var originalPrice by remember { mutableStateOf("") }
    var discountedPrice by remember { mutableStateOf("") }
    var couponCode by remember { mutableStateOf("") }
    var validUntil by remember { mutableStateOf("31 Oct 2026") }
    var category by remember { mutableStateOf(defaultCategory) }

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
                    label = { Text("Coupon Code (e.g. HIMMAT25)") },
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

@Composable
fun EditWorkerDialog(
    currentWorker: Worker,
    onDismiss: () -> Unit,
    onSave: (name: String, serviceType: String, phone: String, area: String, exp: String) -> Unit
) {
    var name by remember { mutableStateOf(currentWorker.name) }
    var serviceType by remember { mutableStateOf(currentWorker.serviceType) }
    var phone by remember { mutableStateOf(currentWorker.phone) }
    var area by remember { mutableStateOf(currentWorker.area) }
    var exp by remember { mutableStateOf(currentWorker.experienceYears) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Worker Profile", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = serviceType,
                    onValueChange = { serviceType = it },
                    label = { Text("Service Type / Profession") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Contact Phone") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = area,
                    onValueChange = { area = it },
                    label = { Text("Himmatnagar Area") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = exp,
                    onValueChange = { exp = it },
                    label = { Text("Experience") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(name, serviceType, phone, area, exp) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
