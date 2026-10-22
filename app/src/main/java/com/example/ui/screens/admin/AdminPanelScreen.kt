package com.example.ui.screens.admin

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Business
import com.example.data.model.Offer
import com.example.data.model.Promotion
import com.example.data.model.PromotionPlacement
import com.example.data.model.Reel
import com.example.ui.components.ExpiryCountdownBadge
import com.example.ui.components.PromotionBannerCard
import com.example.ui.components.VerifiedBadge
import com.example.ui.theme.*
import java.util.UUID
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    businesses: List<Business>,
    reels: List<Reel>,
    offers: List<Offer>,
    promotions: List<Promotion> = emptyList(),
    onApproveBusiness: (String, Boolean) -> Unit,
    onDeleteBusiness: (String) -> Unit,
    onDeleteReel: (String) -> Unit,
    onDeleteOffer: (String) -> Unit,
    onPurgeExpiredReels: () -> Unit,
    onCreateOrUpdatePromotion: ((Promotion) -> Unit)? = null,
    onDeletePromotion: ((String) -> Unit)? = null,
    onTogglePromotionActive: ((String, Boolean) -> Unit)? = null,
    onTogglePromotionApproved: ((String, Boolean) -> Unit)? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Businesses, 1: Reels, 2: Offers, 3: Promotions

    var showPromotionEditorDialog by remember { mutableStateOf(false) }
    var promotionToEdit by remember { mutableStateOf<Promotion?>(null) }
    var previewPromotion by remember { mutableStateOf<Promotion?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("HMT Admin", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(DangerRed)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("ADMIN", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                        Text("Moderation & Promotions Portal", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Metrics Overview
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AdminStatCard(title = "Shops", value = "${businesses.size}", modifier = Modifier.weight(1f))
                    AdminStatCard(title = "Reels", value = "${reels.size}", modifier = Modifier.weight(1f))
                    AdminStatCard(title = "Deals", value = "${offers.size}", modifier = Modifier.weight(1f))
                    AdminStatCard(title = "Promos", value = "${promotions.size}", modifier = Modifier.weight(1f))
                }
            }

            // Tabs for moderation
            item {
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    edgePadding = 0.dp,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Businesses (${businesses.size})", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Reels (${reels.size})", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Offers (${offers.size})", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        text = { Text("Promotions (${promotions.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            when (selectedTab) {
                0 -> {
                    // Businesses approval & moderation
                    items(businesses, key = { it.id }) { shop ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(shop.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            if (shop.isApproved) VerifiedBadge()
                                        }
                                        Text("${shop.category} • ${shop.area}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                        Text("Owner Phone: ${shop.phone}", fontSize = 11.sp, color = BrandPrimary)
                                    }

                                    IconButton(onClick = { onDeleteBusiness(shop.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = DangerRed)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { onApproveBusiness(shop.id, !shop.isApproved) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (shop.isApproved) DangerRed.copy(alpha = 0.8f) else BrandTeal
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(if (shop.isApproved) "Revoke Badge" else "Approve Business", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // Reels Moderation
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
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
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Local Expired Reels Filter", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Refreshes local list and hides reels older than 10 days.", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                }
                                Button(
                                    onClick = {
                                        onPurgeExpiredReels()
                                        Toast.makeText(context, "Local cache refreshed.", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                                ) {
                                    Text("Refresh", fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    items(reels, key = { it.id }) { reel ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(reel.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("By: ${reel.businessName}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    ExpiryCountdownBadge(remainingDays = reel.getRemainingDays())
                                }

                                IconButton(onClick = { onDeleteReel(reel.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Content", tint = DangerRed)
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // Offers Moderation
                    items(offers, key = { it.id }) { offer ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(offer.productName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("${offer.discountPercent}% OFF • Shop: ${offer.businessName}", fontSize = 11.sp, color = BrandSecondaryDark)
                                    Text("Code: ${offer.couponCode} • Valid: ${offer.validUntil}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                }

                                IconButton(onClick = { onDeleteOffer(offer.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Offer", tint = DangerRed)
                                }
                            }
                        }
                    }
                }
                3 -> {
                    // ========================================================
                    // Tab 3: Custom Admin Promotion Manager
                    // ========================================================
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Custom Promotions Portal",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            text = "Direct advertiser campaigns (Offline billing / Non-AdMob)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            promotionToEdit = null
                                            showPromotionEditorDialog = true
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("New Promo", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    if (promotions.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Campaign, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(48.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("No promotions yet", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)
                                    Text("Tap 'New Promo' above to create one.", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                                }
                            }
                        }
                    } else {
                        items(promotions, key = { it.id }) { promo ->
                            AdminPromotionCard(
                                promotion = promo,
                                onEdit = {
                                    promotionToEdit = promo
                                    showPromotionEditorDialog = true
                                },
                                onPreview = {
                                    previewPromotion = promo
                                },
                                onToggleActive = { active ->
                                    onTogglePromotionActive?.invoke(promo.id, active)
                                },
                                onToggleApprove = { approved ->
                                    onTogglePromotionApproved?.invoke(promo.id, approved)
                                },
                                onDelete = {
                                    onDeletePromotion?.invoke(promo.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Promotion Create / Edit Dialog
    if (showPromotionEditorDialog) {
        PromotionEditorDialog(
            initialPromotion = promotionToEdit,
            onDismiss = { showPromotionEditorDialog = false },
            onSave = { savedPromo ->
                onCreateOrUpdatePromotion?.invoke(savedPromo)
                showPromotionEditorDialog = false
                Toast.makeText(context, "Promotion saved successfully", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Promotion Live Preview Dialog
    if (previewPromotion != null) {
        Dialog(
            onDismissRequest = { previewPromotion = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Promotion Preview (${previewPromotion?.placement?.name})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { previewPromotion = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    previewPromotion?.let { p ->
                        PromotionBannerCard(
                            promotion = p,
                            onView = {},
                            onClick = {}
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { previewPromotion = null },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Done Previewing")
                    }
                }
            }
        }
    }
}

@Composable
fun AdminStatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
            Text(title, fontSize = 10.sp, color = MaterialTheme.colorScheme.outline, maxLines = 1)
        }
    }
}

@Composable
private fun AdminPromotionCard(
    promotion: Promotion,
    onEdit: () -> Unit,
    onPreview: () -> Unit,
    onToggleActive: (Boolean) -> Unit,
    onToggleApprove: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Placement + Status badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = promotion.placement.name,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        color = if (promotion.isActive) SuccessGreen.copy(alpha = 0.15f) else DangerRed.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (promotion.isActive) "ACTIVE" else "PAUSED",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (promotion.isActive) SuccessGreen else DangerRed,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (promotion.isApproved) {
                        Surface(
                            color = BrandTeal.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "APPROVED",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = BrandTeal,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onPreview, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Visibility, contentDescription = "Preview", tint = BrandPrimary, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = BrandPrimary, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = DangerRed, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = promotion.brandName.ifBlank { "Unbranded" },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = promotion.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (promotion.description.isNotBlank()) {
                Text(
                    text = promotion.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Metrics row: Views, Clicks, CTR, Priority
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "👁 ${promotion.views} views",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "👆 ${promotion.clicks} clicks",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                val ctr = if (promotion.views > 0) (promotion.clicks.toFloat() / promotion.views * 100) else 0f
                Text(
                    text = String.format("CTR: %.1f%%", ctr),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "Priority: ${promotion.priority}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Controls: Toggle Active / Approved & Offline Payment Note
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = BrandAmber, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Offline Invoicing",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(
                        onClick = { onToggleActive(!promotion.isActive) },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text(if (promotion.isActive) "Pause" else "Activate", fontSize = 11.sp)
                    }

                    FilledTonalButton(
                        onClick = { onToggleApprove(!promotion.isApproved) },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text(if (promotion.isApproved) "Revoke" else "Approve", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun PromotionEditorDialog(
    initialPromotion: Promotion?,
    onDismiss: () -> Unit,
    onSave: (Promotion) -> Unit
) {
    var brandName by remember { mutableStateOf(initialPromotion?.brandName ?: "") }
    var title by remember { mutableStateOf(initialPromotion?.title ?: "") }
    var description by remember { mutableStateOf(initialPromotion?.description ?: "") }
    var imageUrl by remember { mutableStateOf(initialPromotion?.imageUrl ?: "") }
    var targetUrl by remember { mutableStateOf(initialPromotion?.targetUrl ?: "") }
    var placement by remember { mutableStateOf(initialPromotion?.placement ?: PromotionPlacement.HOME) }
    var priority by remember { mutableIntStateOf(initialPromotion?.priority ?: 5) }
    var durationDays by remember { mutableIntStateOf(7) }
    var isActive by remember { mutableStateOf(initialPromotion?.isActive ?: true) }
    var isApproved by remember { mutableStateOf(initialPromotion?.isApproved ?: true) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (initialPromotion == null) "Create Promotion" else "Edit Promotion",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = brandName,
                    onValueChange = { brandName = it },
                    label = { Text("Brand / Advertiser Name") },
                    placeholder = { Text("e.g. Mahavir Mobile, City Council") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Promotion Headline *") },
                    placeholder = { Text("e.g. Festival Mega Sale on Station Road") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Promotion Description") },
                    placeholder = { Text("Describe the offer, location, or invitation...") },
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("Banner Image URL (Optional)") },
                    placeholder = { Text("https://example.com/banner.jpg") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = targetUrl,
                    onValueChange = { targetUrl = it },
                    label = { Text("Target Web URL (Optional)") },
                    placeholder = { Text("https://mybrand.com") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Placement Selection
                Text(
                    text = "Placement Screen:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PromotionPlacement.values().forEach { p ->
                        FilterChip(
                            selected = placement == p,
                            onClick = { placement = p },
                            label = { Text(p.name, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Priority & Duration Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Priority: $priority", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Slider(
                            value = priority.toFloat(),
                            onValueChange = { priority = it.toInt() },
                            valueRange = 1f..10f,
                            steps = 8
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Duration: $durationDays days", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Slider(
                            value = durationDays.toFloat(),
                            onValueChange = { durationDays = it.toInt() },
                            valueRange = 1f..30f,
                            steps = 28
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Active and Approved Toggles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isActive, onCheckedChange = { isActive = it })
                        Text("Active (Published)", fontSize = 13.sp)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isApproved, onCheckedChange = { isApproved = it })
                        Text("Approved", fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Live Preview within Dialog
                Text(
                    text = "Live Preview (Before Activation):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(6.dp))

                val previewObj = remember(brandName, title, description, imageUrl, targetUrl, placement, priority, isActive, isApproved) {
                    Promotion(
                        id = initialPromotion?.id ?: "preview",
                        brandName = brandName.ifBlank { "Brand Name" },
                        title = title.ifBlank { "Headline Preview" },
                        description = description.ifBlank { "Your promo description will appear here on user phones." },
                        imageUrl = imageUrl.trim(),
                        targetUrl = targetUrl.trim(),
                        placement = placement,
                        priority = priority,
                        isActive = isActive,
                        isApproved = isApproved
                    )
                }

                PromotionBannerCard(
                    promotion = previewObj,
                    onView = {},
                    onClick = {}
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val now = System.currentTimeMillis()
                        val promo = Promotion(
                            id = initialPromotion?.id ?: "promo_${UUID.randomUUID().toString().take(8)}",
                            brandName = brandName.trim(),
                            title = title.trim().ifBlank { "Featured Promotion" },
                            description = description.trim(),
                            imageUrl = imageUrl.trim(),
                            targetUrl = targetUrl.trim(),
                            placement = placement,
                            startAt = initialPromotion?.startAt ?: now,
                            endAt = (initialPromotion?.startAt ?: now) + TimeUnit.DAYS.toMillis(durationDays.toLong()),
                            isActive = isActive,
                            isApproved = isApproved,
                            priority = priority,
                            views = initialPromotion?.views ?: 0,
                            clicks = initialPromotion?.clicks ?: 0,
                            createdBy = initialPromotion?.createdBy ?: "Admin",
                            createdAt = initialPromotion?.createdAt ?: now
                        )
                        onSave(promo)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Save & Activate Promotion", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
