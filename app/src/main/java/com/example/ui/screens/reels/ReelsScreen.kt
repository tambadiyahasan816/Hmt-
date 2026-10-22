package com.example.ui.screens.reels

import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
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
    reelsPromotion: com.example.data.model.Promotion? = null,
    onPromotionView: ((String) -> Unit)? = null,
    onPromotionClick: ((String) -> Unit)? = null,
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
        if (reelsPromotion != null) {
            var promoDismissed by remember(reelsPromotion.id) { mutableStateOf(false) }
            if (!promoDismissed) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 56.dp, start = 14.dp, end = 14.dp)
                ) {
                    com.example.ui.components.PromotionBannerCard(
                        promotion = reelsPromotion,
                        onView = { onPromotionView?.invoke(it) },
                        onClick = { onPromotionClick?.invoke(it) }
                    )
                    IconButton(
                        onClick = { promoDismissed = true },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .size(28.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
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
                    Toast.makeText(context, "Reel published locally! Set to auto-expire in 10 days.", Toast.LENGTH_LONG).show()
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
    var title by remember { mutableStateOf("") }
    var caption by remember { mutableStateOf("") }
    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var videoName by remember { mutableStateOf("") }
    var videoDurationSeconds by remember { mutableIntStateOf(0) }
    var videoSizeMb by remember { mutableFloatStateOf(0f) }
    var videoSizeBytes by remember { mutableLongStateOf(0L) }
    var videoFormat by remember { mutableStateOf("") }
    var uploadError by remember { mutableStateOf<String?>(null) }

    // Real Gallery Video Picker Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedVideoUri = uri
            uploadError = null
            try {
                // Read display name and file size via ContentResolver and OpenableColumns
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (cursor.moveToFirst()) {
                        if (nameIndex != -1) {
                            videoName = cursor.getString(nameIndex) ?: "video.mp4"
                        }
                        if (sizeIndex != -1) {
                            val bytes = cursor.getLong(sizeIndex)
                            videoSizeBytes = bytes
                            videoSizeMb = bytes / (1024f * 1024f)
                        }
                    }
                }
                if (videoName.isBlank()) {
                    videoName = uri.lastPathSegment ?: "video.mp4"
                }

                // Read real duration and mime type via MediaMetadataRetriever
                val retriever = MediaMetadataRetriever()
                try {
                    retriever.setDataSource(context, uri)
                    val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    val durationMs = durationStr?.toLongOrNull() ?: 0L
                    videoDurationSeconds = (durationMs / 1000).toInt()
                    val mime = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)
                    if (!mime.isNullOrBlank()) {
                        videoFormat = mime.substringAfterLast("/", "mp4").lowercase()
                    } else {
                        videoFormat = videoName.substringAfterLast(".", "mp4").lowercase()
                    }
                } finally {
                    try { retriever.release() } catch (_: Exception) {}
                }

                // Strictly validate 60s and 25MB limits
                if (videoDurationSeconds > 60) {
                    uploadError = "Video duration ($videoDurationSeconds s) exceeds the 60 seconds limit."
                } else if (videoSizeMb > 25.0f) {
                    uploadError = "Video size (${String.format(java.util.Locale.US, "%.1f", videoSizeMb)} MB) exceeds the 25MB limit."
                } else if (videoDurationSeconds <= 0 && videoSizeBytes == 0L) {
                    uploadError = "Could not parse video metadata from selected file."
                }
            } catch (e: Exception) {
                uploadError = "Error reading video file: ${e.message}"
            }
        }
    }

    val hasQuota = remainingQuota > 0
    val isFormatValid = videoFormat.isBlank() || videoFormat in listOf("mp4", "mkv", "mov", "webm", "3gp")
    val isDurationValid = videoDurationSeconds in 1..60
    val isSizeValid = videoSizeMb in 0.01f..25.0f
    val isVideoSelected = selectedVideoUri != null
    val isVideoValid = isVideoSelected && isFormatValid && isDurationValid && isSizeValid && uploadError == null
    val canPublish = hasQuota && title.isNotBlank() && isVideoValid

    AlertDialog(
        onDismissRequest = onDismiss,
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

                // 2. Video Selection (Real Video Picker)
                OutlinedButton(
                    onClick = { galleryLauncher.launch("video/*") },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isVideoSelected) "Change Video File" else "Choose Video (Max 60s, 25MB)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 3. Video Specifications & Validation Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isVideoValid) Icons.Default.CheckCircle else if (isVideoSelected) Icons.Default.Error else Icons.Default.Info,
                                contentDescription = null,
                                tint = if (isVideoValid) BrandAccent else if (isVideoSelected) DangerRed else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (isVideoSelected) videoName else "No video selected yet",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isVideoSelected) {
                                        "Duration: ${videoDurationSeconds}s (max 60s) • Size: ${String.format(java.util.Locale.US, "%.1f", videoSizeMb)}MB (max 25MB)"
                                    } else {
                                        "Select a video clip under 60 seconds and 25MB"
                                    },
                                    fontSize = 10.sp,
                                    color = if (isVideoValid || !isVideoSelected) MaterialTheme.colorScheme.outline else DangerRed
                                )
                            }
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
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = caption,
                    onValueChange = { caption = it },
                    label = { Text("Caption & Details") },
                    placeholder = { Text("Describe products, offers & timings...") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                // 5. Upload Error / Validation banner
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
                                text = uploadError ?: "Validation failed",
                                fontSize = 11.sp,
                                color = DangerRed,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (canPublish) {
                        onConfirm(
                            title.trim(),
                            caption.trim()
                        )
                    }
                },
                enabled = canPublish
            ) {
                Text(if (hasQuota) "Publish Reel" else "Limit Reached")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
