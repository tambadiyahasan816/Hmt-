package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.PromotionPlacement
import com.example.data.model.UserRole
import com.example.data.repository.HimmatnagarRepository
import com.example.ui.screens.admin.AdminPanelScreen
import com.example.ui.screens.auth.BusinessOwnerAuthScreen
import com.example.ui.screens.auth.CustomerAuthScreen
import com.example.ui.screens.auth.RoleSelectionScreen
import com.example.ui.screens.auth.WorkerAuthScreen
import com.example.ui.screens.chat.ChatScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.offers.OffersScreen
import com.example.ui.screens.profile.BusinessProfileScreen
import com.example.ui.screens.profile.ProfileDashboardScreen
import com.example.ui.screens.reels.ReelDetailScreen
import com.example.ui.screens.reels.ReelsScreen
import com.example.ui.screens.workers.WorkerDirectoryScreen
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        HimmatnagarRepository.init(applicationContext)
        enableEdgeToEdge()
        val viewModel: MainViewModel by viewModels()
        handleReelDeepLink(intent, viewModel)
        setContent {
            MyApplicationTheme {
                HMTApp(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val viewModel: MainViewModel by viewModels()
        handleReelDeepLink(intent, viewModel)
    }

    private fun handleReelDeepLink(intent: Intent?, viewModel: MainViewModel) {
        val data = intent?.data
        val reelId = data?.getQueryParameter("id") ?: intent?.getStringExtra("reel_id")
        if (!reelId.isNullOrBlank()) {
            viewModel.openReelDetail(reelId)
        }
    }
}

@Composable
fun HMTApp(viewModel: MainViewModel) {
    val context = LocalContext.current
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val businesses by viewModel.businesses.collectAsStateWithLifecycle()
    val reels by viewModel.reels.collectAsStateWithLifecycle()
    val offers by viewModel.offers.collectAsStateWithLifecycle()
    val workers by viewModel.workers.collectAsStateWithLifecycle()
    val followedIds by viewModel.followedIds.collectAsStateWithLifecycle()
    val likedReelIds by viewModel.likedReelIds.collectAsStateWithLifecycle()
    val businessViews by viewModel.businessViews.collectAsStateWithLifecycle()
    val selectedBusiness by viewModel.selectedBusiness.collectAsStateWithLifecycle()
    val selectedReel by viewModel.selectedReel.collectAsStateWithLifecycle()
    val chatBusiness by viewModel.chatBusiness.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isOffline by viewModel.isOffline.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val promotions by viewModel.promotions.collectAsStateWithLifecycle()

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Handle back button on sub-screens
    BackHandler(enabled = currentScreen != ScreenState.ROLE_SELECTION) {
        focusManager.clearFocus()
        keyboardController?.hide()
        viewModel.handleBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("loading_progress_bar"),
                color = BrandPrimary
            )
        }
        if (isOffline) {
            Surface(
                color = BrandAmber,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CloudOff, contentDescription = null, tint = Color(0xFF102A43), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Offline Mode: Showing saved data", color = Color(0xFF102A43), fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                    TextButton(onClick = { viewModel.retryConnection() }) {
                        Text("Retry", color = Color(0xFF102A43), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
        errorMessage?.let { error ->
            Surface(
                color = DangerRed,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(error, color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    TextButton(onClick = { viewModel.retryConnection() }) {
                        Text("Retry", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    IconButton(onClick = { viewModel.clearError() }, modifier = Modifier.size(20.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (currentScreen) {
        ScreenState.ROLE_SELECTION -> {
            RoleSelectionScreen(
                onSelectCustomer = { viewModel.navigateTo(ScreenState.CUSTOMER_AUTH) },
                onSelectOwner = { viewModel.navigateTo(ScreenState.OWNER_AUTH) },
                onSelectWorker = { viewModel.navigateTo(ScreenState.WORKER_AUTH) },
                onContinueAsGuest = { viewModel.continueAsGuest() },
                onGoogleSignInSuccess = { uid, gName, gEmail, idToken ->
                    val loggedIn = viewModel.loginExistingUserWithGoogle(uid, gEmail, gName, idToken)
                    if (loggedIn) {
                        android.widget.Toast.makeText(context, "Welcome back, $gName!", android.widget.Toast.LENGTH_SHORT).show()
                    } else {
                        android.widget.Toast.makeText(
                            context,
                            "No existing account found. Please register as a Business Owner, Worker, or Customer above.",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    }
                },
                loginPromotion = viewModel.getActivePromotion(PromotionPlacement.LOGIN),
                onPromotionView = { viewModel.recordPromotionView(it) },
                onPromotionClick = { viewModel.recordPromotionClick(it) }
            )
        }

        ScreenState.CUSTOMER_AUTH -> {
            CustomerAuthScreen(
                onBack = { viewModel.handleBack() },
                onSuccess = { uid, gName, gEmail, phone, name, idToken, avatarUrl ->
                    try {
                        viewModel.registerCustomerWithGoogle(
                            firebaseUid = uid,
                            googleName = gName,
                            googleEmail = gEmail,
                            enteredPhone = phone,
                            enteredName = name,
                            firebaseIdToken = idToken,
                            avatarUrl = avatarUrl,
                            onComplete = { res ->
                                if (res.isFailure) {
                                    val err = res.exceptionOrNull()?.localizedMessage ?: "Failed to sync profile"
                                    android.widget.Toast.makeText(context, err, android.widget.Toast.LENGTH_LONG).show()
                                }
                            }
                        )
                    } catch (e: Exception) {
                        android.widget.Toast.makeText(context, e.localizedMessage ?: "Authentication failed", android.widget.Toast.LENGTH_LONG).show()
                    }
                }
            )
        }

        ScreenState.OWNER_AUTH -> {
            BusinessOwnerAuthScreen(
                onBack = { viewModel.handleBack() },
                onSuccess = { uid, gName, gEmail, ownerName, phone, shopName, cat, area, addr, wa, idToken, avatarUrl ->
                    try {
                        viewModel.registerOwnerWithGoogle(
                            firebaseUid = uid,
                            googleName = gName,
                            googleEmail = gEmail,
                            enteredOwnerName = ownerName,
                            enteredPhone = phone,
                            shopName = shopName,
                            category = cat,
                            area = area,
                            address = addr,
                            whatsapp = wa,
                            firebaseIdToken = idToken,
                            avatarUrl = avatarUrl,
                            onComplete = { res ->
                                if (res.isFailure) {
                                    val err = res.exceptionOrNull()?.localizedMessage ?: "Failed to sync profile"
                                    android.widget.Toast.makeText(context, err, android.widget.Toast.LENGTH_LONG).show()
                                }
                            }
                        )
                    } catch (e: Exception) {
                        android.widget.Toast.makeText(context, e.localizedMessage ?: "Authentication failed", android.widget.Toast.LENGTH_LONG).show()
                    }
                }
            )
        }

        ScreenState.WORKER_AUTH -> {
            WorkerAuthScreen(
                onBack = { viewModel.handleBack() },
                onSuccess = { uid, gName, gEmail, name, phone, sType, area, exp, photoUrl, idToken ->
                    try {
                        viewModel.registerWorkerWithGoogle(
                            firebaseUid = uid,
                            googleName = gName,
                            googleEmail = gEmail,
                            enteredName = name,
                            enteredPhone = phone,
                            serviceType = sType,
                            area = area,
                            exp = exp,
                            photoUrl = photoUrl,
                            firebaseIdToken = idToken,
                            onComplete = { res ->
                                if (res.isFailure) {
                                    val err = res.exceptionOrNull()?.localizedMessage ?: "Failed to sync profile"
                                    android.widget.Toast.makeText(context, err, android.widget.Toast.LENGTH_LONG).show()
                                }
                            }
                        )
                    } catch (e: Exception) {
                        android.widget.Toast.makeText(context, e.localizedMessage ?: "Authentication failed", android.widget.Toast.LENGTH_LONG).show()
                    }
                }
            )
        }

        ScreenState.OFFERS -> {
            OffersScreen(
                offers = offers,
                onBack = { viewModel.handleBack() },
                onSelectOffer = { offer ->
                    viewModel.openBusinessProfileById(offer.businessId)
                },
                offersPromotion = viewModel.getActivePromotion(PromotionPlacement.OFFERS),
                onPromotionView = { viewModel.recordPromotionView(it) },
                onPromotionClick = { viewModel.recordPromotionClick(it) }
            )
        }

        ScreenState.BUSINESS_PROFILE -> {
            selectedBusiness?.let { business ->
                BusinessProfileScreen(
                    business = business,
                    offers = offers,
                    reels = reels,
                    isFollowed = followedIds.contains(business.id),
                    onToggleFollow = { viewModel.toggleFollow(business.id) },
                    onStartChat = { viewModel.openChat(business) },
                    onBack = { viewModel.handleBack() },
                    onSelectOffer = {
                        viewModel.openOffersScreen()
                    }
                )
            } ?: run {
                viewModel.handleBack()
            }
        }

        ScreenState.CHAT -> {
            chatBusiness?.let { business ->
                ChatScreen(
                    business = business,
                    messages = chatMessages,
                    currentUser = currentUser,
                    onSendMessage = { text, isFromOwner ->
                        viewModel.sendChatMessage(text, isFromOwner)
                    },
                    onBack = { viewModel.handleBack() }
                )
            } ?: run {
                viewModel.handleBack()
            }
        }

        ScreenState.WORKER_DIRECTORY -> {
            WorkerDirectoryScreen(
                workers = workers,
                onAddWorker = { name, sType, phone, area, exp ->
                    viewModel.addWorker(name, sType, phone, area, exp)
                },
                onBack = { viewModel.handleBack() }
            )
        }

        ScreenState.ADMIN_PANEL -> {
            if (!viewModel.isCurrentUserAdmin()) {
                viewModel.handleBack()
            } else {
                AdminPanelScreen(
                    businesses = businesses,
                    reels = reels,
                    offers = offers,
                    promotions = promotions,
                    onApproveBusiness = { bId, approve -> viewModel.approveBusiness(bId, approve) },
                    onDeleteBusiness = { bId -> viewModel.deleteBusiness(bId) },
                    onDeleteReel = { rId -> viewModel.deleteReel(rId) },
                    onDeleteOffer = { oId -> viewModel.deleteOffer(oId) },
                    onPurgeExpiredReels = { viewModel.purgeExpiredReels() },
                    onCreateOrUpdatePromotion = { viewModel.createOrUpdatePromotion(it) },
                    onDeletePromotion = { viewModel.deletePromotion(it) },
                    onTogglePromotionActive = { id, act -> viewModel.togglePromotionActive(id, act) },
                    onTogglePromotionApproved = { id, app -> viewModel.togglePromotionApproved(id, app) },
                    onBack = { viewModel.handleBack() }
                )
            }
        }

        ScreenState.REEL_DETAIL -> {
            ReelDetailScreen(
                reel = selectedReel,
                likedReelIds = likedReelIds,
                onToggleLike = { viewModel.toggleLikeReel(it) },
                onOpenBusiness = { viewModel.openBusinessProfileById(it) },
                onOpenChat = { viewModel.openChatByBusinessId(it) },
                onBack = { viewModel.handleBack() }
            )
        }

        ScreenState.MAIN_TABS -> {
            Scaffold(
                bottomBar = {
                    NavigationBar(
                        containerColor = if (selectedTab == 1) Color.Black else MaterialTheme.colorScheme.surface,
                        contentColor = if (selectedTab == 1) Color.White else MaterialTheme.colorScheme.onSurface,
                        tonalElevation = 6.dp,
                        modifier = Modifier.testTag("bottom_nav_bar")
                    ) {
                        // Tab 0: Home
                        NavigationBarItem(
                            selected = selectedTab == 0,
                            onClick = { viewModel.selectTab(0) },
                            icon = {
                                Icon(
                                    imageVector = if (selectedTab == 0) Icons.Filled.Home else Icons.Outlined.Home,
                                    contentDescription = "Home"
                                )
                            },
                            label = { Text("Home", fontSize = 11.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                            colors = getNavColors(selectedTab == 1)
                        )

                        // Tab 1: Reels
                        NavigationBarItem(
                            selected = selectedTab == 1,
                            onClick = { viewModel.selectTab(1) },
                            icon = {
                                Icon(
                                    imageVector = if (selectedTab == 1) Icons.Filled.PlayCircleFilled else Icons.Outlined.PlayCircleOutline,
                                    contentDescription = "Reels"
                                )
                            },
                            label = { Text("Reels", fontSize = 11.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                            colors = getNavColors(selectedTab == 1)
                        )

                        // Tab 2: Workers
                        NavigationBarItem(
                            selected = selectedTab == 2,
                            onClick = { viewModel.selectTab(2) },
                            icon = {
                                Icon(
                                    imageVector = if (selectedTab == 2) Icons.Filled.Build else Icons.Outlined.Build,
                                    contentDescription = "Workers"
                                )
                            },
                            label = { Text("Workers", fontSize = 11.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) },
                            colors = getNavColors(selectedTab == 1)
                        )

                        // Tab 3: Profile / Dashboard
                        NavigationBarItem(
                            selected = selectedTab == 3,
                            onClick = { viewModel.selectTab(3) },
                            icon = {
                                Icon(
                                    imageVector = if (selectedTab == 3) Icons.Filled.Person else Icons.Outlined.Person,
                                    contentDescription = "Profile"
                                )
                            },
                            label = {
                                Text(
                                    when (currentUser?.role) {
                                        UserRole.OWNER -> "Dashboard"
                                        UserRole.WORKER -> "My Profile"
                                        else -> "Profile"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = getNavColors(selectedTab == 1)
                        )
                    }
                }
            ) { padding ->
                Box(modifier = Modifier.padding(padding)) {
                    when (selectedTab) {
                        0 -> HomeScreen(
                            businesses = businesses,
                            offers = offers,
                            followedIds = followedIds,
                            onFollowClick = { viewModel.toggleFollow(it) },
                            onSelectBusiness = { viewModel.openBusinessProfile(it) },
                            onSelectOffer = { viewModel.openOffersScreen() },
                            onNavigateToOffers = { viewModel.openOffersScreen() },
                            homePromotion = viewModel.getActivePromotion(PromotionPlacement.HOME),
                            onPromotionView = { viewModel.recordPromotionView(it) },
                            onPromotionClick = { viewModel.recordPromotionClick(it) }
                        )

                        1 -> ReelsScreen(
                            reels = reels,
                            currentUser = currentUser,
                            likedReelIds = likedReelIds,
                            remainingQuota = currentUser?.let { viewModel.getRemainingReelQuota(it.firebaseUid.ifBlank { it.id }) } ?: 0,
                            onToggleLike = { viewModel.toggleLikeReel(it) },
                            onOpenBusiness = { viewModel.openBusinessProfileById(it) },
                            onOpenChat = { viewModel.openChatByBusinessId(it) },
                            onUploadReel = { title, caption -> viewModel.uploadReel(title, caption) },
                            reelsPromotion = viewModel.getActivePromotion(PromotionPlacement.REELS),
                            onPromotionView = { viewModel.recordPromotionView(it) },
                            onPromotionClick = { viewModel.recordPromotionClick(it) }
                        )

                        2 -> WorkerDirectoryScreen(
                            workers = workers,
                            onAddWorker = { name, sType, phone, area, exp ->
                                viewModel.addWorker(name, sType, phone, area, exp)
                            },
                            onBack = null
                        )

                        3 -> ProfileDashboardScreen(
                            currentUser = currentUser,
                            businesses = businesses,
                            reels = reels,
                            offers = offers,
                            workers = workers,
                            businessViews = businessViews,
                            followedIds = followedIds,
                            onSelectBusiness = { viewModel.openBusinessProfile(it) },
                            onStartChat = { viewModel.openChat(it) },
                            onAddOffer = { bId, pName, cat, orig, disc, code, valid ->
                                viewModel.addOffer(bId, pName, cat, orig, disc, code, valid)
                            },
                            onDeleteOffer = { viewModel.deleteOffer(it) },
                            onDeleteReel = { viewModel.deleteReel(it) },
                            onUploadReel = { t, c -> viewModel.uploadReel(t, c) },
                            onUpdateBusiness = { bId, bio, ph, wa, addr ->
                                viewModel.updateBusinessDetails(bId, bio, ph, wa, addr)
                            },
                            onUpdateWorker = { wId, name, sType, ph, area, exp ->
                                viewModel.updateWorkerProfile(wId, name, sType, ph, area, exp)
                            },
                            onUpdatePhone = { viewModel.updateUserPhone(it) },
                            remainingQuota = currentUser?.let { viewModel.getRemainingReelQuota(it.firebaseUid.ifBlank { it.id }) } ?: 0,
                            onOpenAdminPanel = { viewModel.navigateTo(ScreenState.ADMIN_PANEL) },
                            onLogout = { viewModel.logout() }
                        )
                    }
                }
            }
        }
            }
        }
    }
}

@Composable
private fun getNavColors(isDark: Boolean): NavigationBarItemColors {
    return if (isDark) {
        NavigationBarItemDefaults.colors(
            selectedIconColor = BrandAmber,
            selectedTextColor = BrandAmber,
            unselectedIconColor = Color.LightGray,
            unselectedTextColor = Color.LightGray,
            indicatorColor = Color.White.copy(alpha = 0.12f)
        )
    } else {
        NavigationBarItemDefaults.colors(
            selectedIconColor = BrandTeal,
            selectedTextColor = BrandTeal,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
            indicatorColor = BrandTeal.copy(alpha = 0.14f)
        )
    }
}
