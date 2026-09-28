package com.example.ui.screens.admin

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.data.model.Business
import com.example.data.model.Offer
import com.example.data.model.Reel
import com.example.ui.components.ExpiryCountdownBadge
import com.example.ui.components.VerifiedBadge
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    businesses: List<Business>,
    reels: List<Reel>,
    offers: List<Offer>,
    onApproveBusiness: (String, Boolean) -> Unit,
    onDeleteBusiness: (String) -> Unit,
    onDeleteReel: (String) -> Unit,
    onDeleteOffer: (String) -> Unit,
    onPurgeExpiredReels: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Businesses, 1: Reels, 2: Offers

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Sheher Himmatnagar Admin", fontWeight = FontWeight.Bold, fontSize = 17.sp)
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
                        Text("Moderation & Verification Portal", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
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
                    AdminStatCard(title = "Active Reels", value = "${reels.size}", modifier = Modifier.weight(1f))
                    AdminStatCard(title = "Live Deals", value = "${offers.size}", modifier = Modifier.weight(1f))
                }
            }

            // Scheduled Auto-Delete Trigger Action
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
                            Text("pg_cron Auto-Delete Trigger", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Trigger Supabase Edge Function to purge reels expired > 10 days", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        }
                        Button(
                            onClick = {
                                onPurgeExpiredReels()
                                Toast.makeText(context, "Purge executed: All expired reels and videos removed!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                        ) {
                            Text("Run Cron", fontSize = 11.sp)
                        }
                    }
                }
            }

            // Tabs for moderation
            item {
                PrimaryTabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Businesses (${businesses.size})") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Reels (${reels.size})") }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Offers (${offers.size})") }
                    )
                }
            }

            when (selectedTab) {
                0 -> {
                    // Businesses approval & moderation
                    items(businesses, key = { it.id }) { shop ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                                            containerColor = if (shop.isApproved) DangerRed.copy(alpha = 0.8f) else BrandAccent
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
                    items(reels, key = { it.id }) { reel ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Abusive Content", tint = DangerRed)
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
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
            }
        }
    }
}

@Composable
fun AdminStatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = BrandPrimary)
            Text(title, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
        }
    }
}
