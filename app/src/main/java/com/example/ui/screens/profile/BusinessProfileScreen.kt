package com.example.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Business
import com.example.data.model.Offer
import com.example.data.model.Reel
import com.example.ui.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessProfileScreen(
    business: Business,
    offers: List<Offer>,
    reels: List<Reel>,
    isFollowed: Boolean,
    onToggleFollow: () -> Unit,
    onStartChat: () -> Unit,
    onBack: () -> Unit,
    onSelectOffer: (Offer) -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Posts, 1: Offers, 2: Reels

    val businessOffers = remember(offers, business.id) {
        offers.filter { it.businessId == business.id }
    }
    val businessReels = remember(reels, business.id) {
        reels.filter { it.businessId == business.id }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = business.name,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        // VERIFIED_BADGE_HIDDEN — will be used for paid plans later
                        if (business.isApproved) {
                            VerifiedBadge()
                        }
                    }
                },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header Banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(BrandPrimary, Color(0xFF2563EB), BrandSecondaryDark)
                            )
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.Black.copy(alpha = 0.3f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "📍 ${business.area}, Himmatnagar",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Profile Info & Instagram-style Header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    // Avatar overlapping banner
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = (-36).dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                                .border(3.dp, MaterialTheme.colorScheme.surface, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                CategoryIcon(category = business.category, size = 36)
                            }
                        }

                        // Stats counters (Posts, Followers, Reviews)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(20.dp),
                            modifier = Modifier.padding(bottom = 6.dp)
                        ) {
                            ProfileStatItem(count = "${businessOffers.size + businessReels.size + 4}", label = "Posts")
                            ProfileStatItem(count = "${business.followerCount}", label = "Followers")
                            ProfileStatItem(
                                count = if (business.rating > 0f) String.format("%.1f ★", business.rating) else "New",
                                label = "Rating"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height((-20).dp))

                    Text(
                        text = business.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = business.category,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = business.bio,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Address and Open in Maps
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                launchGoogleMaps(context, business.lat, business.lng, business.name)
                            }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = BrandPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = business.address,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "MAPS ↗",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action Buttons: Follow, In-App Chat, Call, WhatsApp
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onToggleFollow,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isFollowed) MaterialTheme.colorScheme.surfaceVariant else BrandPrimary,
                                contentColor = if (isFollowed) MaterialTheme.colorScheme.onSurfaceVariant else Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = if (isFollowed) "Following" else "Follow",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = onStartChat,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Chat", fontWeight = FontWeight.Bold)
                        }

                        FilledTonalIconButton(
                            onClick = { launchDialer(context, business.phone) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = "Call")
                        }

                        FilledTonalIconButton(
                            onClick = { launchWhatsApp(context, business.whatsapp) },
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = WhatsAppGreen.copy(alpha = 0.15f),
                                contentColor = WhatsAppGreen
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "WhatsApp")
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Profile Navigation Tabs
                    PrimaryTabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Grid") }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Offers (${businessOffers.size})") }
                        )
                        Tab(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            text = { Text("Reels (${businessReels.size})") }
                        )
                    }
                }
            }

            // Tab Content
            when (selectedTab) {
                0 -> {
                    // 3-Column Instagram Photo Grid
                    item {
                        Column(modifier = Modifier.padding(2.dp)) {
                            val itemsList = listOf(
                                "Showroom Front View", "New Arrivals Section", "Festive Display",
                                "Special Products", "Customer Showcase", "Store Entrance"
                            )
                            val chunked = itemsList.chunked(3)
                            chunked.forEach { rowItems ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    rowItems.forEach { label ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f)
                                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                                .padding(6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Icon(
                                                    imageVector = Icons.Default.Image,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.outline,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = label,
                                                    fontSize = 10.sp,
                                                    textAlign = TextAlign.Center,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 2
                                                )
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                            }
                        }
                    }
                }
                1 -> {
                    // Offers List
                    if (businessOffers.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No active discount offers right now.",
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    } else {
                        items(businessOffers) { offer ->
                            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                                OfferCard(
                                    offer = offer,
                                    onShopClick = {}
                                )
                            }
                        }
                    }
                }
                2 -> {
                    // Reels List with 10-day countdown
                    if (businessReels.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No active reels published by this shop.",
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    } else {
                        items(businessReels) { reel ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(60.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(BrandPrimaryLight),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = reel.title,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${reel.views} views • ${reel.likes} likes",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        ExpiryCountdownBadge(remainingDays = reel.getRemainingDays())
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun ProfileStatItem(count: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.outline
        )
    }
}
