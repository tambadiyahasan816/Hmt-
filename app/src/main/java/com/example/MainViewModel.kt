package com.example

import androidx.lifecycle.ViewModel
import com.example.data.model.*
import com.example.data.repository.HimmatnagarRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ScreenState {
    ROLE_SELECTION,
    CUSTOMER_AUTH,
    OWNER_AUTH,
    WORKER_AUTH,
    MAIN_TABS,
    BUSINESS_PROFILE,
    CHAT,
    WORKER_DIRECTORY,
    ADMIN_PANEL
}

class MainViewModel(
    val repository: HimmatnagarRepository = HimmatnagarRepository()
) : ViewModel() {

    private val _currentScreen = MutableStateFlow(ScreenState.ROLE_SELECTION)
    val currentScreen: StateFlow<ScreenState> = _currentScreen.asStateFlow()

    // 0: Home, 1: Explore, 2: Reels, 3: Workers (Wrench icon), 4: Profile/Dashboard
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _selectedBusiness = MutableStateFlow<Business?>(null)
    val selectedBusiness: StateFlow<Business?> = _selectedBusiness.asStateFlow()

    private val _chatBusiness = MutableStateFlow<Business?>(null)
    val chatBusiness: StateFlow<Business?> = _chatBusiness.asStateFlow()

    val currentUser = repository.currentUser
    val businesses = repository.businesses
    val reels = repository.reels
    val offers = repository.offers
    val workers = repository.workers
    val followedIds = repository.followedBusinessIds
    val likedReelIds = repository.likedReelIds
    val chatMessages = repository.chatMessages
    val businessViews = repository.businessViews

    fun navigateTo(screen: ScreenState) {
        _currentScreen.value = screen
    }

    fun selectTab(tabIndex: Int) {
        _selectedTab.value = tabIndex
        _currentScreen.value = ScreenState.MAIN_TABS
    }

    fun openBusinessProfile(business: Business) {
        _selectedBusiness.value = business
        _currentScreen.value = ScreenState.BUSINESS_PROFILE
    }

    fun openBusinessProfileById(businessId: String) {
        val b = businesses.value.find { it.id == businessId }
        if (b != null) {
            openBusinessProfile(b)
        }
    }

    fun openChat(business: Business) {
        _chatBusiness.value = business
        _currentScreen.value = ScreenState.CHAT
    }

    fun openChatByBusinessId(businessId: String) {
        val b = businesses.value.find { it.id == businessId }
        if (b != null) {
            openChat(b)
        }
    }

    fun handleBack() {
        when (_currentScreen.value) {
            ScreenState.BUSINESS_PROFILE,
            ScreenState.CHAT,
            ScreenState.WORKER_DIRECTORY,
            ScreenState.ADMIN_PANEL -> {
                _currentScreen.value = ScreenState.MAIN_TABS
            }
            ScreenState.CUSTOMER_AUTH,
            ScreenState.OWNER_AUTH,
            ScreenState.WORKER_AUTH -> {
                _currentScreen.value = ScreenState.ROLE_SELECTION
            }
            ScreenState.MAIN_TABS -> {
                if (_selectedTab.value != 0) {
                    _selectedTab.value = 0
                }
            }
            ScreenState.ROLE_SELECTION -> {
                // Stay on landing
            }
        }
    }

    // Auth actions
    fun registerCustomer(name: String, phone: String) {
        repository.loginOrRegisterCustomer(name, phone)
        _selectedTab.value = 0 // Customer lands on discovery feed
        _currentScreen.value = ScreenState.MAIN_TABS
    }

    fun registerOwner(
        shopName: String,
        ownerName: String,
        phone: String,
        category: String,
        area: String,
        address: String,
        whatsapp: String
    ) {
        val newBusiness = repository.loginOrRegisterOwner(
            shopName, ownerName, phone, category, area, address, whatsapp
        )
        _selectedBusiness.value = newBusiness
        _selectedTab.value = 4 // Owner lands on Business Dashboard
        _currentScreen.value = ScreenState.MAIN_TABS
    }

    fun registerWorker(
        name: String,
        phone: String,
        serviceType: String,
        area: String,
        exp: String,
        photoUrl: String = ""
    ) {
        repository.loginOrRegisterWorker(name, phone, serviceType, area, exp, photoUrl)
        _selectedTab.value = 4 // Worker lands on Profile Tab to see their worker card (FIX 7)
        _currentScreen.value = ScreenState.MAIN_TABS
    }

    fun updateWorkerProfile(
        workerId: String,
        name: String,
        serviceType: String,
        phone: String,
        area: String,
        exp: String
    ) {
        repository.updateWorkerProfile(workerId, name, serviceType, phone, area, exp)
    }

    fun continueAsGuest() {
        repository.setCurrentUser(null)
        _selectedTab.value = 0
        _currentScreen.value = ScreenState.MAIN_TABS
    }

    fun switchRole(newRole: UserRole) {
        repository.switchRoleForTesting(newRole)
        when (newRole) {
            UserRole.OWNER -> _selectedTab.value = 4 // Dashboard
            UserRole.WORKER -> _selectedTab.value = 4 // Worker Profile
            else -> _selectedTab.value = 0 // Discovery
        }
        _currentScreen.value = ScreenState.MAIN_TABS
    }

    fun logout() {
        try {
            com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
        } catch (e: Exception) {
            // ignore
        }
        repository.setCurrentUser(null)
        _currentScreen.value = ScreenState.ROLE_SELECTION
    }

    // Social actions
    fun toggleFollow(businessId: String) {
        repository.toggleFollow(businessId)
    }

    fun toggleLikeReel(reelId: String) {
        repository.toggleLikeReel(reelId)
    }

    // Owner actions
    fun uploadReel(title: String, caption: String) {
        val user = currentUser.value ?: return
        if (user.role != UserRole.OWNER) return
        val businessId = businesses.value.find { it.ownerId == user.id }?.id ?: return
        repository.addReel(businessId, title, caption)
    }

    fun addOffer(
        businessId: String,
        productName: String,
        category: String,
        originalPrice: Double,
        discountedPrice: Double,
        couponCode: String,
        validUntil: String
    ) {
        repository.addOffer(businessId, productName, category, originalPrice, discountedPrice, couponCode, validUntil)
    }

    fun deleteOffer(offerId: String) {
        repository.deleteOffer(offerId)
    }

    fun deleteReel(reelId: String) {
        repository.deleteReel(reelId)
    }

    fun updateBusinessDetails(businessId: String, bio: String, phone: String, whatsapp: String, address: String) {
        repository.updateBusinessDetails(businessId, bio, phone, whatsapp, address)
        if (_selectedBusiness.value?.id == businessId) {
            _selectedBusiness.value = businesses.value.find { it.id == businessId }
        }
    }

    // Worker Directory actions
    fun addWorker(name: String, serviceType: String, phone: String, area: String, exp: String) {
        repository.addWorker(name, serviceType, phone, area, exp)
    }

    // Chat actions
    fun sendChatMessage(text: String, isFromOwner: Boolean) {
        val b = _chatBusiness.value ?: return
        repository.sendChatMessage(b.id, b.name, text, isFromOwner)
    }

    // Admin actions
    fun approveBusiness(businessId: String, approve: Boolean) {
        repository.approveBusiness(businessId, approve)
    }

    fun deleteBusiness(businessId: String) {
        repository.deleteBusiness(businessId)
    }

    fun purgeExpiredReels() {
        repository.purgeExpiredReels()
    }
}
