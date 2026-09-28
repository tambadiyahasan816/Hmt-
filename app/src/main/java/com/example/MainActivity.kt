package com.example

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.UserRole
import com.example.ui.screens.admin.AdminPanelScreen
import com.example.ui.screens.auth.BusinessOwnerAuthScreen
import com.example.ui.screens.auth.CustomerAuthScreen
import com.example.ui.screens.auth.RoleSelectionScreen
import com.example.ui.screens.auth.WorkerAuthScreen
import com.example.ui.screens.chat.ChatScreen
import com.example.ui.screens.explore.ExploreScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.profile.BusinessProfileScreen
import com.example.ui.screens.profile.ProfileDashboardScreen
import com.example.ui.screens.reels.ReelsScreen
import com.example.ui.screens.workers.WorkerDirectoryScreen
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                SheherHimmatnagarApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun SheherHimmatnagarApp(viewModel: MainViewModel) {
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
    val chatBusiness by viewModel.chatBusiness.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()

    // Handle back button on sub-screens
    BackHandler(enabled = currentScreen != ScreenState.ROLE_SELECTION) {
        viewModel.handleBack()
    }

    when (currentScreen) {
        ScreenState.ROLE_SELECTION -> {
            RoleSelectionScreen(
                onSelectCustomer = { viewModel.navigateTo(ScreenState.CUSTOMER_AUTH) },
                onSelectOwner = { viewModel.navigateTo(ScreenState.OWNER_AUTH) },
                onSelectWorker = { viewModel.navigateTo(ScreenState.WORKER_AUTH) },
                onContinueAsGuest = { viewModel.continueAsGuest() }
            )
        }

        ScreenState.CUSTOMER_AUTH -> {
            CustomerAuthScreen(
                onBack = { viewModel.handleBack() },
                onSuccess = { name, phone ->
                    viewModel.registerCustomer(name, phone)
                }
            )
        }

        ScreenState.OWNER_AUTH -> {
            BusinessOwnerAuthScreen(
                onBack = { viewModel.handleBack() },
                onSuccess = { shopName, ownerName, phone, cat, area, addr, wa ->
                    viewModel.registerOwner(shopName, ownerName, phone, cat, area, addr, wa)
                }
            )
        }

        ScreenState.WORKER_AUTH -> {
            WorkerAuthScreen(
                onBack = { viewModel.handleBack() },
                onSuccess = { name, phone, sType, area, exp, photoUrl ->
                    viewModel.registerWorker(name, phone, sType, area, exp, photoUrl)
                }
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
                        // Open offer in explore deals
                        viewModel.selectTab(1)
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
            AdminPanelScreen(
                businesses = businesses,
                reels = reels,
                offers = offers,
                onApproveBusiness = { bId, approve -> viewModel.approveBusiness(bId, approve) },
                onDeleteBusiness = { bId -> viewModel.deleteBusiness(bId) },
                onDeleteReel = { rId -> viewModel.deleteReel(rId) },
                onDeleteOffer = { oId -> viewModel.deleteOffer(oId) },
                onPurgeExpiredReels = { viewModel.purgeExpiredReels() },
                onBack = { viewModel.handleBack() }
            )
        }

        ScreenState.MAIN_TABS -> {
            Scaffold(
                bottomBar = {
                    NavigationBar(
                        containerColor = if (selectedTab == 2) Color.Black else MaterialTheme.colorScheme.surface,
                        contentColor = if (selectedTab == 2) Color.White else MaterialTheme.colorScheme.onSurface,
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
                            colors = getNavColors(selectedTab == 2)
                        )

                        // Tab 1: Explore (with Offers / Deals on top)
                        NavigationBarItem(
                            selected = selectedTab == 1,
                            onClick = { viewModel.selectTab(1) },
                            icon = {
                                Icon(
                                    imageVector = if (selectedTab == 1) Icons.Filled.Explore else Icons.Outlined.Explore,
                                    contentDescription = "Explore"
                                )
                            },
                            label = { Text("Explore", fontSize = 11.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                            colors = getNavColors(selectedTab == 2)
                        )

                        // Tab 2: Reels
                        NavigationBarItem(
                            selected = selectedTab == 2,
                            onClick = { viewModel.selectTab(2) },
                            icon = {
                                Icon(
                                    imageVector = if (selectedTab == 2) Icons.Filled.PlayCircleFilled else Icons.Outlined.PlayCircleOutline,
                                    contentDescription = "Reels"
                                )
                            },
                            label = { Text("Reels", fontSize = 11.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) },
                            colors = getNavColors(selectedTab == 2)
                        )

                        // FIX 5: Tab 3: Workers with WRENCH ICON
                        NavigationBarItem(
                            selected = selectedTab == 3,
                            onClick = { viewModel.selectTab(3) },
                            icon = {
                                Icon(
                                    imageVector = if (selectedTab == 3) Icons.Filled.Build else Icons.Outlined.Build,
                                    contentDescription = "Workers"
                                )
                            },
                            label = { Text("Workers", fontSize = 11.sp, fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal) },
                            colors = getNavColors(selectedTab == 2)
                        )

                        // Tab 4: Profile / Dashboard
                        NavigationBarItem(
                            selected = selectedTab == 4,
                            onClick = { viewModel.selectTab(4) },
                            icon = {
                                Icon(
                                    imageVector = if (selectedTab == 4) Icons.Filled.Person else Icons.Outlined.Person,
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
                                    fontWeight = if (selectedTab == 4) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = getNavColors(selectedTab == 2)
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
                            onSelectOffer = { viewModel.selectTab(1) },
                            onNavigateToExplore = { viewModel.selectTab(1) }
                        )

                        1 -> ExploreScreen(
                            businesses = businesses,
                            offers = offers,
                            followedIds = followedIds,
                            onFollowClick = { viewModel.toggleFollow(it) },
                            onSelectBusiness = { viewModel.openBusinessProfile(it) },
                            onOpenWorkerDirectory = { viewModel.selectTab(3) }
                        )

                        2 -> ReelsScreen(
                            reels = reels,
                            currentUser = currentUser,
                            likedReelIds = likedReelIds,
                            onToggleLike = { viewModel.toggleLikeReel(it) },
                            onOpenBusiness = { viewModel.openBusinessProfileById(it) },
                            onOpenChat = { viewModel.openChatByBusinessId(it) },
                            onUploadReel = { title, caption -> viewModel.uploadReel(title, caption) }
                        )

                        3 -> WorkerDirectoryScreen(
                            workers = workers,
                            onAddWorker = { name, sType, phone, area, exp ->
                                viewModel.addWorker(name, sType, phone, area, exp)
                            },
                            onBack = null
                        )

                        4 -> ProfileDashboardScreen(
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
                            onSwitchRole = { viewModel.switchRole(it) },
                            onOpenAdminPanel = { viewModel.navigateTo(ScreenState.ADMIN_PANEL) },
                            onLogout = { viewModel.logout() }
                        )
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
            selectedIconColor = BrandSecondary,
            selectedTextColor = BrandSecondary,
            unselectedIconColor = Color.Gray,
            unselectedTextColor = Color.Gray,
            indicatorColor = Color.White.copy(alpha = 0.12f)
        )
    } else {
        NavigationBarItemDefaults.colors(
            selectedIconColor = BrandPrimary,
            selectedTextColor = BrandPrimary,
            unselectedIconColor = MaterialTheme.colorScheme.outline,
            unselectedTextColor = MaterialTheme.colorScheme.outline,
            indicatorColor = BrandPrimaryContainer
        )
    }
}
