package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Business
import com.example.data.model.Offer
import com.example.data.model.Worker
import com.example.ui.theme.*

// FIX 4: VERIFIED_BADGE_HIDDEN — will be used for paid plans later
// The green "VERIFIED" badge is removed/hidden from ALL business cards and profiles everywhere in the app.
// Do not delete the feature from code — just hide it from UI.
@Composable
fun VerifiedBadge(modifier: Modifier = Modifier) {
    // VERIFIED_BADGE_HIDDEN — will be used for paid plans later
    // Purposefully rendering nothing so the badge is hidden across the application.
}

@Composable
fun ExpiryCountdownBadge(
    remainingDays: Int,
    modifier: Modifier = Modifier
) {
    val isUrgent = remainingDays <= 2
    val bgColor = if (isUrgent) DangerRed else Color(0xFF1E293B)
    val textColor = if (isUrgent) Color.White else Color(0xFFFBBF24)

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor.copy(alpha = 0.92f))
            .border(1.dp, textColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = if (isUrgent) Icons.Default.Warning else Icons.Default.Timer,
            contentDescription = "Countdown Expiry",
            tint = textColor,
            modifier = Modifier.size(13.dp)
        )
        Text(
            text = if (remainingDays == 1) "Expires in 1 day" else "Expires in $remainingDays days",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
fun CategoryIcon(
    category: String,
    size: Int = 20,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    val icon = when {
        category.contains("Food", ignoreCase = true) || category.contains("Dining", ignoreCase = true) || category.contains("Tiffin", ignoreCase = true) -> Icons.Default.Restaurant
        category.contains("Dairy", ignoreCase = true) || category.contains("Sweets", ignoreCase = true) -> Icons.Default.Cake
        category.contains("Fashion", ignoreCase = true) || category.contains("Wear", ignoreCase = true) || category.contains("Footwear", ignoreCase = true) -> Icons.Default.ShoppingBag
        category.contains("Mobile", ignoreCase = true) || category.contains("Gadgets", ignoreCase = true) -> Icons.Default.Smartphone
        category.contains("Electronics", ignoreCase = true) || category.contains("Appliances", ignoreCase = true) -> Icons.Default.Tv
        category.contains("Medical", ignoreCase = true) || category.contains("Pharma", ignoreCase = true) -> Icons.Default.LocalHospital
        category.contains("Grocery", ignoreCase = true) || category.contains("Mart", ignoreCase = true) -> Icons.Default.ShoppingCart
        category.contains("Jewelry", ignoreCase = true) || category.contains("Gold", ignoreCase = true) -> Icons.Default.Diamond
        category.contains("Auto", ignoreCase = true) || category.contains("Vehicles", ignoreCase = true) -> Icons.Default.DirectionsCar
        category.contains("Furniture", ignoreCase = true) || category.contains("Home", ignoreCase = true) -> Icons.Default.Home
        category.contains("Beauty", ignoreCase = true) || category.contains("Salon", ignoreCase = true) -> Icons.Default.Face
        category.contains("Gym", ignoreCase = true) || category.contains("Fitness", ignoreCase = true) -> Icons.Default.FitnessCenter
        category.contains("Education", ignoreCase = true) || category.contains("Books", ignoreCase = true) -> Icons.Default.MenuBook
        category.contains("Hardware", ignoreCase = true) || category.contains("Electrical", ignoreCase = true) -> Icons.Default.Hardware
        category.contains("Travel", ignoreCase = true) || category.contains("Courier", ignoreCase = true) -> Icons.Default.LocalShipping
        category.contains("Real Estate", ignoreCase = true) -> Icons.Default.Apartment
        category.contains("Photo", ignoreCase = true) -> Icons.Default.CameraAlt
        category.contains("Toys", ignoreCase = true) || category.contains("Gifts", ignoreCase = true) -> Icons.Default.CardGiftcard
        category.contains("Wedding", ignoreCase = true) -> Icons.Default.Celebration
        category.contains("Pet", ignoreCase = true) -> Icons.Default.Pets
        category.contains("Agriculture", ignoreCase = true) -> Icons.Default.Grass
        else -> Icons.Default.Storefront
    }
    Icon(
        imageVector = icon,
        contentDescription = category,
        tint = tint,
        modifier = Modifier.size(size.dp)
    )
}

@Composable
fun BusinessCard(
    business: Business,
    isFollowed: Boolean,
    onFollowClick: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shop Avatar Icon with Category Tint
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    CategoryIcon(
                        category = business.category,
                        size = 24,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = business.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        // VERIFIED_BADGE_HIDDEN — will be used for paid plans later
                        if (business.isApproved) {
                            VerifiedBadge()
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = business.category,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = business.area,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = business.bio,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action Row: Rating / New Badge, Followers, Call, WhatsApp, Open Profile
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (business.rating > 0f) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = GoldenStar,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = String.format("%.1f", business.rating),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (business.reviewCount > 0) {
                            Text(
                                text = "(${business.reviewCount})",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    } else {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "New",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${business.followerCount} followers",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilledTonalIconButton(
                        onClick = { launchDialer(context, business.phone) },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Call",
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    FilledTonalIconButton(
                        onClick = { launchWhatsApp(context, business.whatsapp) },
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = WhatsAppGreen.copy(alpha = 0.15f),
                            contentColor = WhatsAppGreen
                        ),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "WhatsApp",
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Button(
                        onClick = onClick,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text(text = "View", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

// FIX 2: Workers get a simple profile card: name, service, area, experience, tap-to-call button, WhatsApp button.
@Composable
fun WorkerCard(
    worker: Worker,
    onCallClick: () -> Unit,
    onWhatsAppClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
                    if (worker.experienceYears.isNotBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "• ${worker.experienceYears}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilledTonalIconButton(
                    onClick = onCallClick,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Call",
                        modifier = Modifier.size(18.dp)
                    )
                }

                FilledTonalIconButton(
                    onClick = onWhatsAppClick,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = WhatsAppGreen.copy(alpha = 0.15f),
                        contentColor = WhatsAppGreen
                    ),
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "WhatsApp",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun OfferCard(
    offer: Offer,
    onShopClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = offer.productName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Sold by ${offer.businessName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable { onShopClick() }
                    )
                    Text(
                        text = "📍 ${offer.area}, Himmatnagar",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                // Discount Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(BrandSecondaryDark, BrandSecondary)
                            )
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${offer.discountPercent}% OFF",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Pricing Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "₹${offer.discountedPrice.toInt()}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = BrandPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "₹${offer.originalPrice.toInt()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                        textDecoration = TextDecoration.LineThrough
                    )
                }

                Text(
                    text = "Valid till ${offer.validUntil}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Coupon Code Banner + Copy Action
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        RoundedCornerShape(10.dp)
                    )
                    .clickable {
                        clipboardManager.setText(AnnotatedString(offer.couponCode))
                        Toast
                            .makeText(
                                context,
                                "Coupon '${offer.couponCode}' copied!",
                                Toast.LENGTH_SHORT
                            )
                            .show()
                    }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ConfirmationNumber,
                        contentDescription = "Coupon",
                        tint = BrandSecondaryDark,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "CODE: ${offer.couponCode}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Code",
                        tint = BrandPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "COPY",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = BrandPrimary
                    )
                }
            }
        }
    }
}

// Intent Launcher Utilities for Real Device Integration
fun launchDialer(context: Context, phone: String) {
    try {
        val cleanNumber = phone.replace(" ", "").replace("-", "")
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleanNumber"))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Could not open dialer for $phone", Toast.LENGTH_SHORT).show()
    }
}

fun launchWhatsApp(context: Context, phoneOrWhatsapp: String) {
    try {
        val cleanNumber = phoneOrWhatsapp.replace("+", "").replace(" ", "").replace("-", "")
        val url = "https://api.whatsapp.com/send?phone=$cleanNumber&text=Hello,%20I%20found%20you%20on%20Sheher%20Himmatnagar%20App!"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "WhatsApp not installed or could not be launched", Toast.LENGTH_SHORT).show()
    }
}

fun launchGoogleMaps(context: Context, lat: Double, lng: Double, shopName: String) {
    try {
        val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng($shopName)")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    } catch (e: Exception) {
        val webUri = Uri.parse("https://maps.google.com/?q=$lat,$lng")
        val webIntent = Intent(Intent.ACTION_VIEW, webUri)
        context.startActivity(webIntent)
    }
}
