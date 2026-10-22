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
    OFFERS,
    ADMIN_PANEL,
    REEL_DETAIL
}

class MainViewModel(
    val repository: HimmatnagarRepository = HimmatnagarRepository.getInstance()
) : ViewModel() {

    private val _currentScreen = MutableStateFlow(
        if (repository.currentUser.value != null) ScreenState.MAIN_TABS else ScreenState.ROLE_SELECTION
    )
    val currentScreen: StateFlow<ScreenState> = _currentScreen.asStateFlow()

    // 0: Home, 1: Reels, 2: Workers, 3: Profile/Dashboard
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _selectedBusiness = MutableStateFlow<Business?>(null)
    val selectedBusiness: StateFlow<Business?> = _selectedBusiness.asStateFlow()

    private val _selectedReel = MutableStateFlow<Reel?>(null)
    val selectedReel: StateFlow<Reel?> = _selectedReel.asStateFlow()

    private val _chatBusiness = MutableStateFlow<Business?>(null)
    val chatBusiness: StateFlow<Business?> = _chatBusiness.asStateFlow()

    // UI Feedback States: Loading, Error, Offline
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    fun clearError() {
        _errorMessage.value = null
    }

    fun setOffline(offline: Boolean) {
        _isOffline.value = offline
    }

    fun retryConnection() {
        _isLoading.value = true
        _isOffline.value = false
        _errorMessage.value = null
        purgeExpiredReels()
        _isLoading.value = false
    }

    val currentUser = repository.currentUser
    val businesses = repository.businesses
    val reels = repository.reels
    val offers = repository.offers
    val workers = repository.workers
    val followedIds = repository.followedBusinessIds
    val likedReelIds = repository.likedReelIds
    val chatMessages = repository.chatMessages
    val businessViews = repository.businessViews
    val promotions = repository.promotions

    fun isCurrentUserAdmin(): Boolean = repository.isUserAdmin(currentUser.value)

    fun getActivePromotion(placement: PromotionPlacement): Promotion? {
        return repository.getActivePromotionsForPlacement(placement).firstOrNull()
    }

    fun getActivePromotions(placement: PromotionPlacement): List<Promotion> {
        return repository.getActivePromotionsForPlacement(placement)
    }

    fun navigateTo(screen: ScreenState) {
        if (screen == ScreenState.ADMIN_PANEL && !isCurrentUserAdmin()) {
            return
        }
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

    fun openOffersScreen() {
        _currentScreen.value = ScreenState.OFFERS
    }

    fun openReelDetail(reelId: String) {
        val r = repository.reels.value.find { it.id == reelId }
        _selectedReel.value = r
        _currentScreen.value = ScreenState.REEL_DETAIL
    }

    fun handleBack() {
        when (_currentScreen.value) {
            ScreenState.BUSINESS_PROFILE,
            ScreenState.CHAT,
            ScreenState.WORKER_DIRECTORY,
            ScreenState.OFFERS,
            ScreenState.ADMIN_PANEL,
            ScreenState.REEL_DETAIL -> {
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

    // Auth actions (local persistence)
    fun registerCustomerWithGoogle(
        firebaseUid: String,
        googleName: String,
        googleEmail: String,
        enteredPhone: String,
        enteredName: String,
        firebaseIdToken: String = "",
        avatarUrl: String? = null,
        onComplete: ((Result<Unit>) -> Unit)? = null
    ) {
        try {
            repository.loginOrRegisterCustomerWithGoogle(
                firebaseUid = firebaseUid,
                googleName = googleName,
                googleEmail = googleEmail,
                enteredPhone = enteredPhone,
                enteredName = enteredName
            )
            _selectedTab.value = 0
            _currentScreen.value = ScreenState.MAIN_TABS
            onComplete?.invoke(Result.success(Unit))
        } catch (e: Exception) {
            _errorMessage.value = e.message ?: "Registration failed"
            onComplete?.invoke(Result.failure(e))
        }
    }

    fun registerOwnerWithGoogle(
        firebaseUid: String,
        googleName: String,
        googleEmail: String,
        enteredOwnerName: String,
        enteredPhone: String,
        shopName: String,
        category: String,
        area: String,
        address: String,
        whatsapp: String,
        firebaseIdToken: String = "",
        avatarUrl: String? = null,
        onComplete: ((Result<Unit>) -> Unit)? = null
    ) {
        try {
            val (_, business) = repository.loginOrRegisterOwnerWithGoogle(
                firebaseUid = firebaseUid,
                googleName = googleName,
                googleEmail = googleEmail,
                enteredOwnerName = enteredOwnerName,
                enteredPhone = enteredPhone,
                shopName = shopName,
                category = category,
                area = area,
                address = address,
                whatsapp = whatsapp
            )
            if (business != null) {
                _selectedBusiness.value = business
            }
            _selectedTab.value = 3 // Owner lands on Dashboard
            _currentScreen.value = ScreenState.MAIN_TABS
            onComplete?.invoke(Result.success(Unit))
        } catch (e: Exception) {
            _errorMessage.value = e.message ?: "Registration failed"
            onComplete?.invoke(Result.failure(e))
        }
    }

    fun registerWorkerWithGoogle(
        firebaseUid: String,
        googleName: String,
        googleEmail: String,
        enteredName: String,
        enteredPhone: String,
        serviceType: String,
        area: String,
        exp: String,
        photoUrl: String = "",
        firebaseIdToken: String = "",
        onComplete: ((Result<Unit>) -> Unit)? = null
    ) {
        try {
            repository.loginOrRegisterWorkerWithGoogle(
                firebaseUid = firebaseUid,
                googleName = googleName,
                googleEmail = googleEmail,
                enteredName = enteredName,
                enteredPhone = enteredPhone,
                serviceType = serviceType,
                area = area,
                exp = exp,
                photoUrl = photoUrl
            )
            _selectedTab.value = 3 // Worker lands on My Profile
            _currentScreen.value = ScreenState.MAIN_TABS
            onComplete?.invoke(Result.success(Unit))
        } catch (e: Exception) {
            _errorMessage.value = e.message ?: "Registration failed"
            onComplete?.invoke(Result.failure(e))
        }
    }

    fun loginExistingUserWithGoogle(
        firebaseUid: String,
        googleEmail: String,
        googleName: String,
        firebaseIdToken: String = ""
    ): Boolean {
        val existing = repository.loginExistingUserWithGoogle(firebaseUid, googleEmail, googleName)
        if (existing != null) {
            when (existing.role) {
                UserRole.OWNER -> _selectedTab.value = 3
                UserRole.WORKER -> _selectedTab.value = 3
                else -> _selectedTab.value = 0
            }
            _currentScreen.value = ScreenState.MAIN_TABS
            return true
        }
        return false
    }

    fun updateUserPhone(newPhone: String) {
        val uid = currentUser.value?.firebaseUid ?: currentUser.value?.id ?: return
        repository.updateUserPhone(uid, newPhone)
    }

    fun registerCustomer(name: String, phone: String) {
        repository.loginOrRegisterCustomer(name, phone)
        _selectedTab.value = 0
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
        _selectedTab.value = 3
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
        _selectedTab.value = 3
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

    fun logout() {
        try {
            com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
        } catch (_: Exception) {
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

    // Reel actions (Owner and Worker with monthly quota)
    fun getRemainingReelQuota(uploaderId: String): Int {
        return repository.getRemainingMonthlyReelQuota(uploaderId)
    }

    fun uploadReel(
        title: String,
        caption: String,
        videoUriString: String = "",
        videoDurationSeconds: Int = 30,
        fileSizeBytes: Long = 5_000_000L
    ): ReelUploadResult {
        val user = currentUser.value ?: return ReelUploadResult.Error("Please log in to upload a reel.")
        if (user.role == UserRole.CUSTOMER) {
            return ReelUploadResult.Error("Unauthorized: Customers are not permitted to upload reels.")
        }
        return repository.publishReel(
            uploader = user,
            title = title,
            caption = caption,
            videoUriString = videoUriString,
            videoDurationSeconds = videoDurationSeconds,
            fileSizeBytes = fileSizeBytes
        )
    }

    suspend fun uploadReelRequest(
        request: ReelUploadRequest,
        onProgress: (Float) -> Unit = {}
    ): ReelUploadResult {
        onProgress(0.5f)
        val result = uploadReel(
            title = request.title,
            caption = request.caption,
            videoUriString = request.videoUri?.toString() ?: "",
            videoDurationSeconds = request.durationSeconds,
            fileSizeBytes = request.sizeBytes
        )
        onProgress(1.0f)
        return result
    }

    fun onReelViewed(reelId: String) = repository.recordReelView(reelId)

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

    // Promotion actions (Admin only)
    fun createOrUpdatePromotion(promotion: Promotion): Result<Promotion> {
        return repository.createOrUpdatePromotion(promotion, currentUser.value)
    }

    fun deletePromotion(promotionId: String): Result<Unit> {
        return repository.deletePromotion(promotionId, currentUser.value)
    }

    fun togglePromotionActive(promotionId: String, active: Boolean): Result<Unit> {
        return repository.togglePromotionActive(promotionId, active, currentUser.value)
    }

    fun togglePromotionApproved(promotionId: String, approved: Boolean): Result<Unit> {
        return repository.togglePromotionApproved(promotionId, approved, currentUser.value)
    }

    fun recordPromotionView(promotionId: String) {
        repository.recordPromotionView(promotionId)
    }

    fun recordPromotionClick(promotionId: String) {
        repository.recordPromotionClick(promotionId)
    }
}
