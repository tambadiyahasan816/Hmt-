package com.example.ui.screens.offers

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Business
import com.example.data.model.BusinessCategories
import com.example.data.model.HimmatnagarLocations
import com.example.data.model.Offer
import com.example.ui.components.OfferCard
import com.example.ui.theme.*

@Composable
fun OffersScreen(
    offers: List<Offer>,
    businesses: List<Business>,
    onSelectBusiness: (Business) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedArea by remember { mutableStateOf<String?>(null) }
    var sortByHighestDiscount by remember { mutableStateOf(true) }

    val filteredOffers = remember(offers, searchQuery, selectedCategory, selectedArea, sortByHighestDiscount) {
        var result = offers.filter { offer ->
            val matchesSearch = searchQuery.isBlank() ||
                    offer.productName.contains(searchQuery, ignoreCase = true) ||
                    offer.businessName.contains(searchQuery, ignoreCase = true) ||
                    offer.category.contains(searchQuery, ignoreCase = true) ||
                    offer.couponCode.contains(searchQuery, ignoreCase = true)

            val matchesCategory = selectedCategory == null || offer.category.equals(selectedCategory, ignoreCase = true)
            val matchesArea = selectedArea == null || offer.area.equals(selectedArea, ignoreCase = true)

            matchesSearch && matchesCategory && matchesArea
        }

        result = if (sortByHighestDiscount) {
            result.sortedByDescending { it.discountPercent }
        } else {
            result.sortedBy { it.discountedPrice }
        }

        result
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BrandPrimary)
                    .padding(horizontal = 16.dp, vertical = 18.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Deals & Discount Finder",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = "Compare local shop prices in Himmatnagar",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(BrandSecondary)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "BEST DEALS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                text = "Search any product (e.g. iPhone, Kurti, Thali)...",
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = BrandPrimary
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.Gray)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = BrandSecondary,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Category Filter Chips
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(
                    text = "Filter by Category:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { selectedCategory = null },
                            label = { Text("All Categories") }
                        )
                    }
                    items(BusinessCategories.ALL.filter { it != "Other" }) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = if (selectedCategory == cat) null else cat },
                            label = { Text(cat) }
                        )
                    }
                }
            }
        }

        // Area Filter Chips
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                Text(
                    text = "Filter by Locality:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                            onClick = { selectedArea = if (selectedArea == area) null else area },
                            label = { Text(area) }
                        )
                    }
                }
            }
        }

        // Count
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                Text(
                    text = "${filteredOffers.size} active discount offers in Himmatnagar",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        // Offers list
        items(filteredOffers, key = { it.id }) { offer ->
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                OfferCard(
                    offer = offer,
                    onShopClick = {
                        val shop = businesses.find { it.id == offer.businessId }
                        if (shop != null) onSelectBusiness(shop)
                    }
                )
            }
        }
    }
}
