package com.example.ui.screens.explore

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Business
import com.example.data.model.BusinessCategory
import com.example.data.model.HimmatnagarLocations
import com.example.ui.components.BusinessCard
import com.example.ui.components.CategoryIcon
import com.example.ui.components.launchGoogleMaps
import com.example.ui.theme.*

@Composable
fun ExploreScreen(
    businesses: List<Business>,
    followedIds: Set<String>,
    onFollowClick: (String) -> Unit,
    onSelectBusiness: (Business) -> Unit,
    onOpenWorkerDirectory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedArea by remember { mutableStateOf<String?>(null) }
    var selectedCategory by remember { mutableStateOf<BusinessCategory?>(null) }
    var isMapView by remember { mutableStateOf(false) }
    var activePinnedShop by remember { mutableStateOf<Business?>(null) }

    val filteredBusinesses = remember(businesses, selectedArea, selectedCategory) {
        businesses.filter { b ->
            val matchArea = selectedArea == null || b.area.equals(selectedArea, ignoreCase = true)
            val matchCat = selectedCategory == null || b.category == selectedCategory
            matchArea && matchCat
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Explore Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BrandPrimary)
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Explore Himmatnagar",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "Filter by locality, market area & map pins",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }

                    // View Mode Toggle (Map vs List)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(2.dp)
                    ) {
                        IconButton(
                            onClick = { isMapView = false },
                            modifier = Modifier
                                .size(34.dp)
                                .background(
                                    if (!isMapView) Color.White else Color.Transparent,
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.ViewList,
                                contentDescription = "List",
                                tint = if (!isMapView) BrandPrimary else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = { isMapView = true },
                            modifier = Modifier
                                .size(34.dp)
                                .background(
                                    if (isMapView) Color.White else Color.Transparent,
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = "Map",
                                tint = if (isMapView) BrandPrimary else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Area Filter Chips
        Column(modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)) {
            Text(
                text = "Filter by Himmatnagar Locality:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
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

        // Worker Directory Quick Callout Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clickable { onOpenWorkerDirectory() },
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(BrandPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Himmatnagar Worker Directory",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Find local Plumbers, Electricians, Tutors & Mechanics",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = BrandPrimary
                )
            }
        }

        if (isMapView) {
            // Map View Simulation
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 72.dp)
            ) {
                // Interactive Canvas Map Grid
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFE2E8F0))
                ) {
                    val w = size.width
                    val h = size.height

                    // Main Highway (NH-8)
                    drawLine(
                        color = Color(0xFFCBD5E1),
                        start = Offset(0f, h * 0.4f),
                        end = Offset(w, h * 0.45f),
                        strokeWidth = 14f
                    )
                    // Station Road
                    drawLine(
                        color = Color(0xFFCBD5E1),
                        start = Offset(w * 0.3f, 0f),
                        end = Offset(w * 0.35f, h),
                        strokeWidth = 10f
                    )
                    // Bypass Road
                    drawLine(
                        color = Color(0xFFCBD5E1),
                        start = Offset(w * 0.6f, 0f),
                        end = Offset(w * 0.65f, h),
                        strokeWidth = 8f
                    )
                    // Tower Chowk Junction
                    drawCircle(
                        color = Color(0xFF94A3B8),
                        radius = 24f,
                        center = Offset(w * 0.35f, h * 0.42f)
                    )
                }

                // Place pins for businesses
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    filteredBusinesses.take(5).forEachIndexed { index, shop ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = (index * 20).dp),
                            horizontalArrangement = if (index % 2 == 0) Arrangement.Start else Arrangement.End
                        ) {
                            Card(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { activePinnedShop = shop },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (activePinnedShop?.id == shop.id) BrandPrimary else Color.White
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = if (activePinnedShop?.id == shop.id) BrandSecondary else DangerRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = shop.name.take(18) + if (shop.name.length > 18) ".." else "",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (activePinnedShop?.id == shop.id) Color.White else Color.Black
                                    )
                                }
                            }
                        }
                    }
                }

                // Bottom preview sheet for selected pin
                activePinnedShop?.let { shop ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = shop.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(
                                    onClick = { activePinnedShop = null },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(16.dp))
                                }
                            }
                            Text(
                                text = "📍 ${shop.area}, Himmatnagar • ${shop.category.displayName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onSelectBusiness(shop) },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Open Shop Profile")
                                }
                                OutlinedButton(
                                    onClick = { launchGoogleMaps(context, shop.lat, shop.lng, shop.name) },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Directions")
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // List View
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 72.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = "Businesses in ${selectedArea ?: "All Himmatnagar"} (${filteredBusinesses.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                items(filteredBusinesses, key = { it.id }) { business ->
                    BusinessCard(
                        business = business,
                        isFollowed = followedIds.contains(business.id),
                        onFollowClick = { onFollowClick(business.id) },
                        onClick = { onSelectBusiness(business) }
                    )
                }
            }
        }
    }
}
