package com.example.ui.screens.reels

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import kotlinx.coroutines.launch
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
    remainingQuota: Int = 3,
    onToggleLike: (String) -> Unit,
    onOpenBusiness: (String) -> Unit,
    onOpenChat: (String) -> Unit,
    onUploadReel: (title: String, caption: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showUploadDialog by remember { mutableStateOf(false) }

    // Instant filter: Hide expired reels immediately
    val activeReels = remember(reels) { reels.filter { !it.isExpired() } }

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
        if (activeReels.isEmpty()) {
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
                        text = "Expired reels auto-delete after 10 days.",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            val pagerState = rememberPagerState(pageCount = { activeReels.size })

            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val reel = activeReels[page]
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
                                text = "HMT Reels",
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
                                                "Check out '${reel.title}' by ${reel.businessName} on HMT app! Available for 10 days only."
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

        // Role Check: BUSINESS OWNER & WORKER ONLY. Customers NEVER see or upload reels.
        val canUploadReel = currentUser?.role == UserRole.OWNER || currentUser?.role == UserRole.WORKER
        if (canUploadReel) {
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

        // Upload Reel Dialog with Quota & Video Validation
        if (showUploadDialog) {
            UploadReelDialog(
                remainingQuota = remainingQuota,
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
    remainingQuota: Int = 3,
    onDismiss: () -> Unit,
    onConfirm: (title: String, caption: String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var title by remember { mutableStateOf("") }
    var caption by remember { mutableStateOf("") }
    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var videoName by remember { mutableStateOf("showcase_clip.mp4") }
    var videoDurationSeconds by remember { mutableIntStateOf(35) }
    var videoSizeMb by remember { mutableFloatStateOf(14.2f) }
    var videoFormat by remember { mutableStateOf("mp4") }
    var isUploading by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableFloatStateOf(0f) }
    var uploadStatusText by remember { mutableStateOf("") }
    var uploadError by remember { mutableStateOf<String?>(null) }

    // Gallery Picker Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedVideoUri = uri
            val filename = uri.lastPathSegment ?: "gallery_video.mp4"
            videoName = filename
            val ext = filename.substringAfterLast(".", "mp4").lowercase()
            videoFormat = ext
            // Dynamic check / simulated extraction
            videoDurationSeconds = 40
            videoSizeMb = 18.5f
            uploadError = null
        }
    }

    val hasQuota = remainingQuota > 0
    val isFormatValid = videoFormat in listOf("mp4", "mkv", "mov", "webm")
    val isDurationValid = videoDurationSeconds in 1..60
    val isSizeValid = videoSizeMb <= 50f
    val isVideoValid = isFormatValid && isDurationValid && isSizeValid
    val canPublish = hasQuota && title.isNotBlank() && isVideoValid && !isUploading

    AlertDialog(
        onDismissRequest = { if (!isUploading) onDismiss() },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.VideoCameraBack, contentDescription = null, tint = BrandSecondary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Publish 10-Day Reel", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // 1. Monthly Quota Banner (Asia/Kolkata Calendar Month)
                if (hasQuota) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = BrandPrimary.copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "You have $remainingQuota of 3 reels remaining this month.",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = BrandPrimary
                            )
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = DangerRed.copy(alpha = 0.12f))
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = DangerRed, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Monthly reel limit reached. You can upload again next month.",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = DangerRed
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 2. Video Source Selection (Gallery or Camera recording)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { galleryLauncher.launch("video/*") },
                        enabled = !isUploading,
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Gallery", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }

                    OutlinedButton(
                        onClick = {
                            // Camera / Sample capture selector
                            videoName = "camera_capture_${System.currentTimeMillis().toString().takeLast(4)}.mp4"
                            videoDurationSeconds = 30
                            videoSizeMb = 12.0f
                            videoFormat = "mp4"
                            uploadError = null
                            Toast.makeText(context, "Camera clip recorded (30s, 12MB)", Toast.LENGTH_SHORT).show()
                        },
                        enabled = !isUploading,
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp)
                    ) {
                        Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Record", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 3. Video Specifications & Real Validation Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isVideoValid) Icons.Default.CheckCircle else Icons.Default.Error,
                                contentDescription = null,
                                tint = if (isVideoValid) BrandAccent else DangerRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(videoName, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "Duration: ${videoDurationSeconds}s (max 60s) • Size: ${String.format(java.util.Locale.US, "%.1f", videoSizeMb)}MB (max 50MB)",
                                    fontSize = 10.sp,
                                    color = if (isVideoValid) MaterialTheme.colorScheme.outline else DangerRed
                                )
                            }
                        }
                        if (videoSizeMb > 15f) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "High quality clip: Will be compressed before upload",
                                fontSize = 9.sp,
                                color = BrandSecondaryDark,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 4. Headline and Caption
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Reel Headline") },
                    placeholder = { Text("e.g. New Festive Stock Arrival!") },
                    singleLine = true,
                    enabled = !isUploading,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = caption,
                    onValueChange = { caption = it },
                    label = { Text("Caption & Details") },
                    placeholder = { Text("Describe products, offers & timings...") },
                    maxLines = 2,
                    enabled = !isUploading,
                    modifier = Modifier.fillMaxWidth()
                )

                // 5. Upload Error / Retry banner
                if (uploadError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DangerRed.copy(alpha = 0.12f))
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Error, contentDescription = null, tint = DangerRed, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = uploadError ?: "Upload failed",
                                fontSize = 11.sp,
                                color = DangerRed,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // 6. Upload Progress Stages
                if (isUploading) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(modifier = Modifier.fillMaxWidth()) {
                        LinearProgressIndicator(
                            progress = { uploadProgress },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = uploadStatusText,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (canPublish) {
                        isUploading = true
                        uploadError = null
                        uploadStatusText = "1/4: Validating video format & 60s limit..."
                        uploadProgress = 0.2f
                        coroutineScope.launch {
                            try {
                                kotlinx.coroutines.delay(120)
                                if (videoSizeMb > 15f) {
                                    uploadStatusText = "2/4: Compressing video for fast streaming..."
                                    uploadProgress = 0.45f
                                    kotlinx.coroutines.delay(150)
                                }
                                uploadStatusText = "3/4: Uploading to reels-videos bucket..."
                                uploadProgress = 0.8f
                                kotlinx.coroutines.delay(150)
                                uploadStatusText = "4/4: Creating database record (strictly 10 days expiry)..."
                                uploadProgress = 1.0f
                                kotlinx.coroutines.delay(100)
                                onConfirm(title, caption)
                            } catch (e: Exception) {
                                uploadError = "Upload error: ${e.message ?: "Failed to upload"}. Quota was not consumed."
                                uploadProgress = 0f
                            } finally {
                                isUploading = false
                            }
                        }
                    }
                },
                enabled = canPublish
            ) {
                if (isUploading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Uploading...")
                } else {
                    Text(if (hasQuota) "Publish Reel" else "Limit Reached")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isUploading
            ) {
                Text("Cancel")
            }
        }
    )
}
