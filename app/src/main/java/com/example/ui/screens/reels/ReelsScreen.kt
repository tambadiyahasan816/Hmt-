package com.example.ui.screens.reels

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Business
import com.example.data.model.Reel
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.ui.components.ExpiryCountdownBadge
import com.example.ui.components.VerifiedBadge
import com.example.ui.components.launchDialer
import com.example.ui.components.launchWhatsApp
import com.example.ui.theme.*

@Composable
fun ReelsScreen(
    reels: List<Reel>,
    currentUser: UserProfile?,
    likedReelIds: Set<String>,
    onToggleLike: (String) -> Unit,
    onOpenBusiness: (String) -> Unit,
    onOpenChat: (String) -> Unit,
    onUploadReel: (title: String, caption: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showUploadDialog by remember { mutableStateOf(false) }

    // Color gradient presets for reel video backgrounds
    val gradientPresets = listOf(
        listOf(Color(0xFF0F172A), Color(0xFF1E3A8A), Color(0xFF0284C7)),
        listOf(Color(0xFF1E1B4B), Color(0xFF4C1D95), Color(0xFF831843)),
        listOf(Color(0xFF064E3B), Color(0xFF047857), Color(0xFF0F766E)),
        listOf(Color(0xFF7C2D12), Color(0xFFC2410C), Color(0xFFB45309)),
        listOf(Color(0xFF18181B), Color(0xFF27272A), Color(0xFF3F3F46))
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (reels.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.VideoLibrary,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(60.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No active reels available right now.",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Expired reels auto-delete every 10 days.",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            val pagerState = rememberPagerState(pageCount = { reels.size })

            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val reel = reels[page]
                val gradient = gradientPresets[reel.gradientColorIndex % gradientPresets.size]
                val isLiked = likedReelIds.contains(reel.id)

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(gradient))
                ) {
                    // Center animated video representation
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(140.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f))
                            .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircle,
                            contentDescription = "Playing Reel",
                            tint = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(64.dp)
                        )
                    }

                    // Top Bar Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Sheher Reels",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(BrandSecondary)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "10-DAY FEED",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }

                        // Mandatory Expiry Countdown Badge
                        ExpiryCountdownBadge(
                            remainingDays = reel.getRemainingDays()
                        )
                    }

                    // Bottom Overlay: Business info, Title, Caption, Action Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.75f),
                                        Color.Black.copy(alpha = 0.95f)
                                    )
                                )
                            )
                            .navigationBarsPadding()
                            .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 72.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // Left Column: Business info & description
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 12.dp)
                        ) {
                            // Business Name with Tap-Through
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onOpenBusiness(reel.businessId) }
                                    .padding(vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(BrandPrimaryLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Storefront,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = reel.businessName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        VerifiedBadge()
                                    }
                                    Text(
                                        text = "${reel.businessCategory} • Himmatnagar",
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = reel.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = reel.caption,
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.85f),
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 16.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Overlaid Contact Buttons
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { launchDialer(context, reel.businessPhone) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White.copy(alpha = 0.2f)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Call Shop", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { launchWhatsApp(context, reel.businessWhatsapp) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = WhatsAppGreen
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("WhatsApp", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Right Column: Vertical Action Bar (Like, Chat, Share)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Like
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                IconButton(
                                    onClick = { onToggleLike(reel.id) },
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(Color.White.copy(alpha = 0.15f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Like",
                                        tint = if (isLiked) DangerRed else Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Text(
                                    text = "${reel.likes}",
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Chat / Inquire
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                IconButton(
                                    onClick = { onOpenChat(reel.businessId) },
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(Color.White.copy(alpha = 0.15f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ChatBubbleOutline,
                                        contentDescription = "Inquire",
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Text(
                                    text = "Chat",
                                    fontSize = 11.sp,
                                    color = Color.White
                                )
                            }

                            // Share
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                IconButton(
                                    onClick = {
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(
                                                Intent.EXTRA_TEXT,
                                                "Check out '${reel.title}' by ${reel.businessName} on Sheher Himmatnagar app! Available for 10 days only."
                                            )
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Share Reel"))
                                    },
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(Color.White.copy(alpha = 0.15f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Share",
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Text(
                                    text = "Share",
                                    fontSize = 11.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // Business Owner ONLY: Upload Reel FAB
        // "Customers can NEVER post videos or create business listings."
        val isBusinessOwner = currentUser?.role == UserRole.OWNER || currentUser?.role == UserRole.ADMIN
        if (isBusinessOwner) {
            FloatingActionButton(
                onClick = { showUploadDialog = true },
                containerColor = BrandSecondary,
                contentColor = Color.Black,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 86.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.VideoCall, contentDescription = "Post Reel")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Post Reel",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Upload Reel Dialog
        if (showUploadDialog) {
            UploadReelDialog(
                onDismiss = { showUploadDialog = false },
                onConfirm = { title, caption ->
                    onUploadReel(title, caption)
                    showUploadDialog = false
                    Toast.makeText(context, "Reel published! Set to auto-expire in 10 days.", Toast.LENGTH_LONG).show()
                }
            )
        }
    }
}

@Composable
fun UploadReelDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, caption: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var caption by remember { mutableStateOf("") }
    var isVideoSelected by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.VideoCameraBack, contentDescription = null, tint = BrandSecondary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Publish 10-Day Shop Reel", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
        },
        text = {
            Column {
                Text(
                    text = "Post a video up to 60 seconds. This reel will automatically delete after 10 days.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Video File Picker Simulation
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = BrandAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("video_clip_himmatnagar.mp4 (42s)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Ready to upload to Supabase Storage", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Reel Headline") },
                    placeholder = { Text("e.g. New Festive Stock Arrival!") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = caption,
                    onValueChange = { caption = it },
                    label = { Text("Caption & Offer Details") },
                    placeholder = { Text("Describe products, pricing & timings...") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(BrandSecondary.copy(alpha = 0.15f))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.HourglassTop, contentDescription = null, tint = BrandSecondaryDark, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Auto-Deletion: expires in 10 days strictly",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandSecondaryDark
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title, caption)
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("Publish Reel")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
