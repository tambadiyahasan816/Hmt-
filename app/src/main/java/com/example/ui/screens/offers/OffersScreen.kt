package com.example.ui.screens.offers

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.data.model.BusinessCategory
import com.example.data.model.HimmatnagarLocations
import com.example.data.model.Offer
import com.example.ui.components.CategoryIcon
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
    var selectedCategory by remember { mutableStateOf<BusinessCategory?>(null) }
    var selectedArea by remember { mutableStateOf<String?>(null) }
    var sortByHighestDiscount by remember { mutableStateOf(true) }

    val quickSearches = listOf("iPhone 18 Pro", "Kurti", "Thali", "Almonds", "Watch", "Ceramic Coating")

    val filteredOffers = remember(offers, searchQuery, selectedCategory, selectedArea, sortByHighestDiscount) {
        var result = offers.filter { offer ->
            val matchesSearch = searchQuery.isBlank() ||
                    offer.productName.contains(searchQuery, ignoreCase = true) ||
                    offer.businessName.contains(searchQuery, ignoreCase = true) ||
                    offer.category.displayName.contains(searchQuery, ignoreCase = true) ||
                    offer.couponCode.contains(searchQuery, ignoreCase = true)

            val matchesCategory = selectedCategory == null || offer.category == selectedCategory
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

                    // Search Input: "Customer types a product (e.g. iPhone 18 Pro)"
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                text = "Search any product (e.g. iPhone 18 Pro, Kurti)...",
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

        // Quick Tag Suggestions
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Text(
                    text = "Popular Searches in Himmatnagar:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(quickSearches) { tag ->
                        val isSelected = searchQuery.equals(tag, ignoreCase = true)
                        Card(
                            modifier = Modifier.clickable {
                                searchQuery = if (isSelected) "" else tag
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) BrandPrimaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                text = tag,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) BrandPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Sorting & Filter Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Highest discount vs Lowest price toggle
                FilterChip(
                    selected = sortByHighestDiscount,
                    onClick = { sortByHighestDiscount = !sortByHighestDiscount },
                    label = {
                        Text(
                            text = if (sortByHighestDiscount) "Sorted: Highest Discount First" else "Sorted: Lowest Price First",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = if (sortByHighestDiscount) Icons.Default.TrendingDown else Icons.Default.AttachMoney,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )

                Text(
                    text = "${filteredOffers.size} offers",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        // Area Filter Chips
        item {
            LazyRow(
                modifier = Modifier.padding(vertical = 4.dp),
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
                items(HimmatnagarLocations.AREAS.take(6)) { area ->
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

        // Offers Feed
        if (filteredOffers.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.LocalOffer,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No offers found for '$searchQuery'",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Try searching for iPhone, Kurti, Thali, or clear filters",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        } else {
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
}
