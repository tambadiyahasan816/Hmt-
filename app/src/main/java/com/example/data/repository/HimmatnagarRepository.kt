package com.example.data.repository

import android.content.Context
import com.example.data.local.LocalPreferences
import com.example.data.model.*
import com.example.data.supabase.SupabaseDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import java.util.concurrent.TimeUnit

class HimmatnagarRepository(
    context: Context? = null,
    val supabaseDataSource: SupabaseDataSource = SupabaseDataSource()
) {

    private var localPrefs: LocalPreferences? = context?.let { LocalPreferences(it) }

    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    private val _businesses = MutableStateFlow<List<Business>>(emptyList())
    val businesses: StateFlow<List<Business>> = _businesses.asStateFlow()

    private val _reels = MutableStateFlow<List<Reel>>(emptyList())
    val reels: StateFlow<List<Reel>> = _reels.asStateFlow()

    private val _offers = MutableStateFlow<List<Offer>>(emptyList())
    val offers: StateFlow<List<Offer>> = _offers.asStateFlow()

    private val _workers = MutableStateFlow<List<Worker>>(emptyList())
    val workers: StateFlow<List<Worker>> = _workers.asStateFlow()

    private val _followedBusinessIds = MutableStateFlow<Set<String>>(emptySet())
    val followedBusinessIds: StateFlow<Set<String>> = _followedBusinessIds.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _likedReelIds = MutableStateFlow<Set<String>>(emptySet())
    val likedReelIds: StateFlow<Set<String>> = _likedReelIds.asStateFlow()

    private val _businessViews = MutableStateFlow<Map<String, Int>>(emptyMap())
    val businessViews: StateFlow<Map<String, Int>> = _businessViews.asStateFlow()

    private val _registeredUsers = MutableStateFlow<List<UserProfile>>(emptyList())
    val registeredUsers: StateFlow<List<UserProfile>> = _registeredUsers.asStateFlow()

    // Monthly Reel Quota Tracker Ledger (Asia/Kolkata timezone, max 3 per month)
    private val _reelUploadUsage = mutableMapOf<String, Int>()

    init {
        seedInitialData()
        restorePersistedState()
    }

    private fun restorePersistedState() {
        val loadedUsers = localPrefs?.loadRegisteredUsers() ?: emptyList()
        if (loadedUsers.isNotEmpty()) {
            _registeredUsers.value = loadedUsers
        }

        localPrefs?.loadCurrentUser()?.let { user ->
            _currentUser.value = user
            if (!_registeredUsers.value.any { it.firebaseUid == user.firebaseUid || it.id == user.id }) {
                _registeredUsers.value = _registeredUsers.value + user
            }
        }
        val followed = localPrefs?.loadFollowedBusinesses() ?: emptySet()
        if (followed.isNotEmpty()) {
            _followedBusinessIds.value = followed
        }
        val liked = localPrefs?.loadLikedReels() ?: emptySet()
        if (liked.isNotEmpty()) {
            _likedReelIds.value = liked
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: HimmatnagarRepository? = null

        fun getInstance(context: Context? = null): HimmatnagarRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: HimmatnagarRepository(context?.applicationContext).also {
                    INSTANCE = it
                }
            }
        }

        fun init(context: Context): HimmatnagarRepository {
            val repo = getInstance(context)
            if (repo.localPrefs == null) {
                repo.localPrefs = LocalPreferences(context.applicationContext)
                repo.restorePersistedState()
            }
            return repo
        }

        fun createForTesting(): HimmatnagarRepository {
            return HimmatnagarRepository(null)
        }
    }

    private fun seedInitialData() {
        val now = System.currentTimeMillis()

        val sampleBusinesses = listOf(
            Business(
                id = "b1",
                ownerId = "owner_1",
                name = "Mahavir Mobile & Gadgets",
                category = "Mobile & Gadgets",
                phone = "+91 98250 11223",
                whatsapp = "919825011223",
                area = "Mahavirnagar",
                address = "Shop 4, City Centre, Near Mahavirnagar Circle, Himmatnagar",
                lat = 23.5991,
                lng = 72.9642,
                bio = "Authorised dealer for Apple, OnePlus, Samsung & Vivo. Genuine accessories, exchange deals and express screen repair.",
                followerCount = 1850,
                rating = 4.9f,
                reviewCount = 142
            ),
            Business(
                id = "b2",
                ownerId = "owner_2",
                name = "Shreeji Kathiyawadi & Punjabi Dhaba",
                category = "Food & Dining",
                phone = "+91 98981 44556",
                whatsapp = "919898144556",
                area = "Motipura",
                address = "NH-8 Highway, Opp. RTO Checkpost, Motipura, Himmatnagar",
                lat = 23.5934,
                lng = 72.9712,
                bio = "Famous authentic Gujarati Thali, Ringna No Olo, Bajra Roti with White Butter & Sizzling Paneer dishes. AC Family Hall.",
                followerCount = 3200,
                rating = 4.8f,
                reviewCount = 310
            ),
            Business(
                id = "b3",
                ownerId = "owner_3",
                name = "Royal Heritage Fashion & Kurtis",
                category = "Fashion & Wear",
                phone = "+91 94270 55667",
                whatsapp = "919427055667",
                area = "Station Road",
                address = "Station Road, Opp. S.T. Bus Depot, Himmatnagar",
                lat = 23.5960,
                lng = 72.9610,
                bio = "Designer Chaniya Cholis, Festive Sarees, Wedding Sherwanis & Trendy Daily Wear Kurtis. Exclusive festive arrivals!",
                followerCount = 2410,
                rating = 4.7f,
                reviewCount = 89
            ),
            Business(
                id = "b4",
                ownerId = "owner_4",
                name = "Radhe Krishna Sweets & Farsan",
                category = "Dairy & Sweets",
                phone = "+91 98254 77889",
                whatsapp = "919825477889",
                area = "Tower Chowk",
                address = "Clock Tower Circle, Main Bazaar, Himmatnagar",
                lat = 23.5982,
                lng = 72.9675,
                bio = "Himmatnagar's landmark sweet shop since 1984. Morning fresh Fafda Jalebi, Kaju Katli & pure desi ghee delicacies.",
                followerCount = 4120,
                rating = 4.9f,
                reviewCount = 520
            ),
            Business(
                id = "b5",
                ownerId = "owner_5",
                name = "Apex 24/7 Pharmacy & Wellness",
                category = "Medical & Pharma",
                phone = "+91 97260 88990",
                whatsapp = "919726088990",
                area = "Tower Chowk",
                address = "Near Civil Hospital Road, Tower Chowk, Himmatnagar",
                lat = 23.5979,
                lng = 72.9690,
                bio = "Open 24 Hours. All prescription medicines, surgical items, baby care, orthopedic aids & home delivery across Himmatnagar.",
                followerCount = 980,
                rating = 4.8f,
                reviewCount = 64
            ),
            Business(
                id = "b6",
                ownerId = "owner_6",
                name = "Polo Green Mart & Dry Fruits",
                category = "Grocery & Mart",
                phone = "+91 94285 33221",
                whatsapp = "919428533221",
                area = "Polo Ground",
                address = "Polo Ground Shopping Arcade, Himmatnagar",
                lat = 23.6015,
                lng = 72.9630,
                bio = "Direct farm produce, organic spices, cold-pressed oils, premium California almonds, walnuts & gourmet groceries.",
                followerCount = 1340,
                rating = 4.6f,
                reviewCount = 76
            ),
            Business(
                id = "b7",
                ownerId = "owner_7",
                name = "Shree Ambica Jewellers",
                category = "Jewelry & Gold",
                phone = "+91 98242 66778",
                whatsapp = "919824266778",
                area = "Station Road",
                address = "Sona Bazaar, Station Road, Himmatnagar",
                lat = 23.5955,
                lng = 72.9618,
                bio = "916 Hallmark Gold jewellery, certified Diamond solitaires, silver gift items & bridal collections since 1978.",
                followerCount = 2890,
                rating = 4.9f,
                reviewCount = 188
            ),
            Business(
                id = "b8",
                ownerId = "owner_8",
                name = "Himmat Auto Care & Garage",
                category = "Auto & Vehicles",
                phone = "+91 99790 12345",
                whatsapp = "919979012345",
                area = "GIDC Area",
                address = "Near GIDC Entrance, Idar Road, Himmatnagar",
                lat = 23.6050,
                lng = 72.9750,
                bio = "Multi-brand car computerized service, wheel alignment, ceramic coating, cashless insurance accidental repairs & AC gas filling.",
                followerCount = 760,
                rating = 4.7f,
                reviewCount = 42
            )
        )
        _businesses.value = sampleBusinesses

        // Seed views
        _businessViews.value = mapOf(
            "b1" to 1420,
            "b2" to 3100,
            "b3" to 890,
            "b4" to 2200,
            "b5" to 430,
            "b6" to 670,
            "b7" to 1540,
            "b8" to 390
        )

        val sampleOffers = listOf(
            Offer(
                id = "off1",
                businessId = "b1",
                businessName = "Mahavir Mobile & Gadgets",
                area = "Mahavirnagar",
                productName = "Apple iPhone 16 / 15 Pro 128GB",
                category = "Mobile & Gadgets",
                originalPrice = 79900.0,
                discountedPrice = 69999.0,
                discountPercent = 12,
                couponCode = "HIMMATIPHONE",
                validUntil = "30 Oct 2026"
            ),
            Offer(
                id = "off2",
                businessId = "b2",
                businessName = "Shreeji Kathiyawadi & Punjabi Dhaba",
                area = "Motipura",
                productName = "Special Unlimited Kathiyawadi Thali with Sweet",
                category = "Food & Dining",
                originalPrice = 280.0,
                discountedPrice = 199.0,
                discountPercent = 28,
                couponCode = "SHREEJI28",
                validUntil = "15 Nov 2026"
            ),
            Offer(
                id = "off3",
                businessId = "b3",
                businessName = "Royal Heritage Fashion & Kurtis",
                area = "Station Road",
                productName = "Festive Pure Cotton & Rayon Designer Kurti Combo (Set of 2)",
                category = "Fashion & Wear",
                originalPrice = 2200.0,
                discountedPrice = 1299.0,
                discountPercent = 40,
                couponCode = "FESTIVE40",
                validUntil = "25 Oct 2026"
            ),
            Offer(
                id = "off4",
                businessId = "b6",
                businessName = "Polo Green Mart & Dry Fruits",
                area = "Polo Ground",
                productName = "Premium Jumbo California Almonds (1kg Vacuum Pack)",
                category = "Grocery & Mart",
                originalPrice = 1100.0,
                discountedPrice = 799.0,
                discountPercent = 27,
                couponCode = "ALMOND27",
                validUntil = "10 Nov 2026"
            ),
            Offer(
                id = "off5",
                businessId = "b8",
                businessName = "Himmat Auto Care & Garage",
                area = "GIDC Area",
                productName = "Complete Car Deep Foam Wash & Interior Dry Cleaning",
                category = "Auto & Vehicles",
                originalPrice = 1500.0,
                discountedPrice = 899.0,
                discountPercent = 40,
                couponCode = "AUTOSPA",
                validUntil = "31 Oct 2026"
            ),
            Offer(
                id = "off6",
                businessId = "b4",
                businessName = "Radhe Krishna Sweets & Farsan",
                area = "Tower Chowk",
                productName = "Pure Desi Ghee Kaju Katli (1 Kg Pack)",
                category = "Dairy & Sweets",
                originalPrice = 1000.0,
                discountedPrice = 799.0,
                discountPercent = 20,
                couponCode = "SWEET20",
                validUntil = "05 Nov 2026"
            )
        )
        _offers.value = sampleOffers

        // Seed 10-day expiring reels with visible countdown badge
        val sampleReels = listOf(
            Reel(
                id = "reel1",
                businessId = "b1",
                businessName = "Mahavir Mobile & Gadgets",
                businessCategory = "Mobile & Gadgets",
                businessPhone = "+91 98250 11223",
                businessWhatsapp = "919825011223",
                title = "New Arrival: iPhone 16 Pro Deep Purple in Himmatnagar!",
                caption = "Unboxing first batch of festival stock! Special exchange bonus up to ₹8,000 for Himmatnagar residents. Visit Mahavirnagar today.",
                likes = 342,
                views = 2800,
                createdAt = now - TimeUnit.DAYS.toMillis(2),
                expiresAt = (now - TimeUnit.DAYS.toMillis(2)) + TimeUnit.DAYS.toMillis(10), // 8 days remaining
                gradientColorIndex = 0
            ),
            Reel(
                id = "reel2",
                businessId = "b2",
                businessName = "Shreeji Kathiyawadi & Punjabi Dhaba",
                businessCategory = "Food & Dining",
                businessPhone = "+91 98981 44556",
                businessWhatsapp = "919898144556",
                title = "Live Sizzling Paneer Angara & Clay Pot Bajra Roti",
                caption = "Fresh evening cooking in clay tandoor! Served hot with desi white makhan and pure garlic chutney. Family AC hall ready.",
                likes = 612,
                views = 5400,
                createdAt = now - TimeUnit.DAYS.toMillis(4),
                expiresAt = (now - TimeUnit.DAYS.toMillis(4)) + TimeUnit.DAYS.toMillis(10), // 6 days remaining
                gradientColorIndex = 1
            ),
            Reel(
                id = "reel3",
                businessId = "b3",
                businessName = "Royal Heritage Fashion & Kurtis",
                businessCategory = "Fashion & Wear",
                businessPhone = "+91 94270 55667",
                businessWhatsapp = "919427055667",
                title = "Handcrafted Kutchi Embroidery & Mirror Work Chaniya Cholis",
                caption = "Festive collection preview! Sizes XS to 3XL available. Pure cotton ghagra with heavy flared dupatta. Order via WhatsApp.",
                likes = 890,
                views = 7200,
                createdAt = now - TimeUnit.DAYS.toMillis(1),
                expiresAt = (now - TimeUnit.DAYS.toMillis(1)) + TimeUnit.DAYS.toMillis(10), // 9 days remaining
                gradientColorIndex = 2
            ),
            Reel(
                id = "reel4",
                businessId = "b4",
                businessName = "Radhe Krishna Sweets & Farsan",
                businessCategory = "Dairy & Sweets",
                businessPhone = "+91 98254 77889",
                businessWhatsapp = "919825477889",
                title = "Crispy Golden Jalebi in Pure Amul Desi Ghee",
                caption = "Every morning 7 AM sharp! Authentic Himmatnagar taste with raw papaya sambharo and spicy fried green chillies.",
                likes = 450,
                views = 3100,
                createdAt = now - TimeUnit.DAYS.toMillis(7),
                expiresAt = (now - TimeUnit.DAYS.toMillis(7)) + TimeUnit.DAYS.toMillis(10), // 3 days remaining
                gradientColorIndex = 3
            )
        )
        _reels.value = sampleReels

        // Workers Directory
        val sampleWorkers = listOf(
            Worker(
                id = "w1",
                profileId = "profile_w1",
                name = "Ramesh Solanki",
                serviceType = "Plumber",
                phone = "+91 98791 22334",
                whatsapp = "919879122334",
                area = "Motipura",
                experienceYears = "8 Years Exp",
                rating = 4.8f,
                createdBy = "Registered Worker"
            ),
            Worker(
                id = "w2",
                profileId = "profile_w2",
                name = "Kanti Panchal",
                serviceType = "Electrician",
                phone = "+91 97250 88441",
                whatsapp = "919725088441",
                area = "Mahavirnagar",
                experienceYears = "12 Years Exp",
                rating = 4.9f,
                createdBy = "Registered Worker"
            ),
            Worker(
                id = "w3",
                profileId = "profile_w3",
                name = "Bharat Prajapati",
                serviceType = "AC/TV/Fridge Repair",
                phone = "+91 99042 11984",
                whatsapp = "919904211984",
                area = "Station Road",
                experienceYears = "6 Years Exp",
                rating = 4.7f,
                createdBy = "Registered Worker"
            ),
            Worker(
                id = "w4",
                profileId = "profile_w4",
                name = "Sunita Patel",
                serviceType = "Tutor",
                phone = "+91 94268 77213",
                whatsapp = "919426877213",
                area = "Polo Ground",
                experienceYears = "7 Years Exp",
                rating = 4.9f,
                createdBy = "Registered Worker"
            ),
            Worker(
                id = "w5",
                profileId = "profile_w5",
                name = "Dinesh Mistri",
                serviceType = "Carpenter",
                phone = "+91 98980 33412",
                whatsapp = "919898033412",
                area = "Tower Chowk",
                experienceYears = "15 Years Exp",
                rating = 4.8f,
                createdBy = "Registered Worker"
            ),
            Worker(
                id = "w6",
                profileId = "profile_w6",
                name = "Mahesh Vankar",
                serviceType = "Painter",
                phone = "+91 98244 55112",
                whatsapp = "919824455112",
                area = "GIDC Area",
                experienceYears = "10 Years Exp",
                rating = 4.6f,
                createdBy = "Registered Worker"
            )
        )
        _workers.value = sampleWorkers
    }

    // Auto-Delete Feature Implementation: purge expired reels where expires_at < now()
    fun purgeExpiredReels() {
        val now = System.currentTimeMillis()
        _reels.value = _reels.value.filter { !it.isExpired(now) }
    }

    // Auth & User Role Methods
    fun setCurrentUser(user: UserProfile?) {
        _currentUser.value = user
        localPrefs?.saveCurrentUser(user)
    }

    fun findUserByFirebaseUid(firebaseUid: String): UserProfile? {
        val uid = firebaseUid.trim()
        if (uid.isBlank()) return null
        return _registeredUsers.value.find { it.firebaseUid == uid || it.id == uid }
            ?: _currentUser.value?.takeIf { it.firebaseUid == uid || it.id == uid }
    }

    fun updateUserPhone(firebaseUid: String, newPhone: String): UserProfile? {
        val trimmedPhone = newPhone.trim()
        if (trimmedPhone.isBlank()) return null
        val existing = findUserByFirebaseUid(firebaseUid) ?: return null
        val updated = existing.copy(
            phone = trimmedPhone,
            updatedAt = System.currentTimeMillis()
        )
        _registeredUsers.value = _registeredUsers.value.map {
            if (it.firebaseUid == firebaseUid || it.id == firebaseUid) updated else it
        }
        if (_currentUser.value?.firebaseUid == firebaseUid || _currentUser.value?.id == firebaseUid) {
            _currentUser.value = updated
            localPrefs?.saveCurrentUser(updated)
        }
        localPrefs?.saveRegisteredUsers(_registeredUsers.value)
        return updated
    }

    fun loginExistingUserWithGoogle(
        firebaseUid: String,
        googleEmail: String = "",
        googleName: String = ""
    ): UserProfile? {
        val cleanUid = firebaseUid.trim()
        val existing = findUserByFirebaseUid(cleanUid) ?: return null
        val updated = existing.copy(
            email = if (googleEmail.isNotBlank()) googleEmail.trim() else existing.email,
            name = if (existing.name.isBlank() && googleName.isNotBlank()) googleName.trim() else existing.name,
            googleDisplayName = if (googleName.isNotBlank()) googleName.trim() else existing.googleDisplayName,
            updatedAt = System.currentTimeMillis()
        )
        _registeredUsers.value = _registeredUsers.value.map {
            if (it.firebaseUid == cleanUid || it.id == cleanUid) updated else it
        }
        _currentUser.value = updated
        localPrefs?.saveCurrentUser(updated)
        localPrefs?.saveRegisteredUsers(_registeredUsers.value)
        return updated
    }

    fun loginOrRegisterCustomerWithGoogle(
        firebaseUid: String,
        googleName: String,
        googleEmail: String,
        enteredPhone: String,
        enteredName: String
    ): UserProfile {
        val cleanUid = firebaseUid.trim()
        val cleanGoogleName = googleName.trim()
        val cleanGoogleEmail = googleEmail.trim()
        val cleanPhone = enteredPhone.trim()
        val cleanName = if (enteredName.isNotBlank()) enteredName.trim() else cleanGoogleName

        val existing = findUserByFirebaseUid(cleanUid)
        if (existing != null) {
            if (existing.role != UserRole.CUSTOMER) {
                throw IllegalStateException("This Google account is already registered as a ${existing.role.name.lowercase().replaceFirstChar { it.uppercase() }}. Please sign in using your existing role.")
            }
            val updated = existing.copy(
                phone = if (cleanPhone.isNotBlank()) cleanPhone else existing.phone,
                name = if (cleanName.isNotBlank() && existing.name.isBlank()) cleanName else existing.name,
                email = if (cleanGoogleEmail.isNotBlank()) cleanGoogleEmail else existing.email,
                googleDisplayName = if (cleanGoogleName.isNotBlank()) cleanGoogleName else existing.googleDisplayName,
                updatedAt = System.currentTimeMillis()
            )
            _registeredUsers.value = _registeredUsers.value.map {
                if (it.firebaseUid == cleanUid || it.id == cleanUid) updated else it
            }
            _currentUser.value = updated
            localPrefs?.saveCurrentUser(updated)
            localPrefs?.saveRegisteredUsers(_registeredUsers.value)
            return updated
        }

        val newUser = UserProfile(
            id = cleanUid,
            firebaseUid = cleanUid,
            phone = cleanPhone,
            email = cleanGoogleEmail,
            googleDisplayName = cleanGoogleName,
            role = UserRole.CUSTOMER,
            name = if (cleanName.isNotBlank()) cleanName else "Customer"
        )
        _registeredUsers.value = _registeredUsers.value + newUser
        _currentUser.value = newUser
        localPrefs?.saveCurrentUser(newUser)
        localPrefs?.saveRegisteredUsers(_registeredUsers.value)
        return newUser
    }

    fun loginOrRegisterOwnerWithGoogle(
        firebaseUid: String,
        googleName: String,
        googleEmail: String,
        enteredOwnerName: String,
        enteredPhone: String,
        shopName: String,
        category: String,
        area: String,
        address: String,
        whatsapp: String
    ): Pair<UserProfile, Business?> {
        val cleanUid = firebaseUid.trim()
        val cleanGoogleName = googleName.trim()
        val cleanGoogleEmail = googleEmail.trim()
        val cleanOwnerName = if (enteredOwnerName.isNotBlank()) enteredOwnerName.trim() else cleanGoogleName
        val cleanPhone = enteredPhone.trim()
        val cleanShop = shopName.trim()
        val cleanCat = category.trim()
        val cleanArea = area.trim()
        val cleanAddr = address.trim()
        val cleanWa = if (whatsapp.isNotBlank()) whatsapp.trim() else cleanPhone

        val existing = findUserByFirebaseUid(cleanUid)
        if (existing != null) {
            if (existing.role != UserRole.OWNER) {
                throw IllegalStateException("This Google account is already registered as a ${existing.role.name.lowercase().replaceFirstChar { it.uppercase() }}. Please sign in using your existing role.")
            }
            val updatedUser = existing.copy(
                phone = if (cleanPhone.isNotBlank()) cleanPhone else existing.phone,
                email = if (cleanGoogleEmail.isNotBlank()) cleanGoogleEmail else existing.email,
                updatedAt = System.currentTimeMillis()
            )
            _registeredUsers.value = _registeredUsers.value.map {
                if (it.firebaseUid == cleanUid || it.id == cleanUid) updatedUser else it
            }
            _currentUser.value = updatedUser
            localPrefs?.saveCurrentUser(updatedUser)
            localPrefs?.saveRegisteredUsers(_registeredUsers.value)
            val existingBusiness = _businesses.value.find { it.ownerId == cleanUid || it.ownerId == existing.id }
            return Pair(updatedUser, existingBusiness)
        }

        val newUser = UserProfile(
            id = cleanUid,
            firebaseUid = cleanUid,
            phone = cleanPhone,
            email = cleanGoogleEmail,
            role = UserRole.OWNER,
            name = if (cleanOwnerName.isNotBlank()) cleanOwnerName else "Business Owner"
        )
        _registeredUsers.value = _registeredUsers.value + newUser
        _currentUser.value = newUser
        localPrefs?.saveCurrentUser(newUser)
        localPrefs?.saveRegisteredUsers(_registeredUsers.value)

        val newBusiness = Business(
            id = "b_${cleanUid.take(8)}",
            ownerId = cleanUid,
            name = cleanShop,
            category = cleanCat,
            phone = cleanPhone,
            whatsapp = cleanWa,
            area = cleanArea,
            address = if (cleanAddr.isNotBlank()) cleanAddr else "$cleanArea, Himmatnagar",
            lat = HimmatnagarLocations.CENTER_LAT + (Math.random() - 0.5) * 0.01,
            lng = HimmatnagarLocations.CENTER_LNG + (Math.random() - 0.5) * 0.01,
            bio = "Official store in $cleanArea, Himmatnagar. Welcome customers for best deals and authentic products.",
            isApproved = true,
            followerCount = 0,
            rating = 0f,
            reviewCount = 0
        )
        _businesses.value = listOf(newBusiness) + _businesses.value
        _businessViews.value = _businessViews.value + (newBusiness.id to 0)
        return Pair(newUser, newBusiness)
    }

    fun loginOrRegisterWorkerWithGoogle(
        firebaseUid: String,
        googleName: String,
        googleEmail: String,
        enteredName: String,
        enteredPhone: String,
        serviceType: String,
        area: String,
        exp: String,
        photoUrl: String = ""
    ): Pair<UserProfile, Worker?> {
        val cleanUid = firebaseUid.trim()
        val cleanGoogleName = googleName.trim()
        val cleanGoogleEmail = googleEmail.trim()
        val cleanWorkerName = if (enteredName.isNotBlank()) enteredName.trim() else cleanGoogleName
        val cleanPhone = enteredPhone.trim()
        val cleanService = serviceType.trim()
        val cleanArea = area.trim()
        val cleanExp = exp.trim()

        val existing = findUserByFirebaseUid(cleanUid)
        if (existing != null) {
            if (existing.role != UserRole.WORKER) {
                throw IllegalStateException("This Google account is already registered as a ${existing.role.name.lowercase().replaceFirstChar { it.uppercase() }}. Please sign in using your existing role.")
            }
            val updatedUser = existing.copy(
                phone = if (cleanPhone.isNotBlank()) cleanPhone else existing.phone,
                email = if (cleanGoogleEmail.isNotBlank()) cleanGoogleEmail else existing.email,
                updatedAt = System.currentTimeMillis()
            )
            _registeredUsers.value = _registeredUsers.value.map {
                if (it.firebaseUid == cleanUid || it.id == cleanUid) updatedUser else it
            }
            _currentUser.value = updatedUser
            localPrefs?.saveCurrentUser(updatedUser)
            localPrefs?.saveRegisteredUsers(_registeredUsers.value)
            val existingWorker = _workers.value.find { it.profileId == cleanUid || it.id == cleanUid || it.profileId == existing.id }
            return Pair(updatedUser, existingWorker)
        }

        val newUser = UserProfile(
            id = cleanUid,
            firebaseUid = cleanUid,
            phone = cleanPhone,
            email = cleanGoogleEmail,
            role = UserRole.WORKER,
            name = if (cleanWorkerName.isNotBlank()) cleanWorkerName else "Worker"
        )
        _registeredUsers.value = _registeredUsers.value + newUser
        _currentUser.value = newUser
        localPrefs?.saveCurrentUser(newUser)
        localPrefs?.saveRegisteredUsers(_registeredUsers.value)

        val newWorker = Worker(
            id = "w_${cleanUid.take(8)}",
            profileId = cleanUid,
            name = cleanWorkerName,
            serviceType = cleanService,
            phone = cleanPhone,
            email = cleanGoogleEmail,
            whatsapp = cleanPhone,
            area = cleanArea,
            experienceYears = if (cleanExp.isNotBlank()) cleanExp else "Experienced",
            photoUrl = photoUrl.trim(),
            rating = 0f,
            createdBy = "Registered Worker"
        )
        _workers.value = listOf(newWorker) + _workers.value
        return Pair(newUser, newWorker)
    }

    fun loginOrRegisterCustomer(name: String, phone: String, email: String = "") {
        val trimmedName = name.trim()
        val trimmedPhone = phone.trim()
        val trimmedEmail = email.trim()
        if (trimmedName.isBlank() || (trimmedPhone.isBlank() && trimmedEmail.isBlank())) return

        val user = UserProfile(
            id = "user_${UUID.randomUUID().toString().take(8)}",
            phone = trimmedPhone,
            email = trimmedEmail,
            role = UserRole.CUSTOMER,
            name = trimmedName
        )
        _registeredUsers.value = _registeredUsers.value + user
        _currentUser.value = user
        localPrefs?.saveCurrentUser(user)
        localPrefs?.saveRegisteredUsers(_registeredUsers.value)
    }

    fun loginOrRegisterOwner(
        shopName: String,
        ownerName: String,
        phone: String,
        category: String,
        area: String,
        address: String,
        whatsapp: String
    ): Business? {
        val trimmedShop = shopName.trim()
        val trimmedOwner = ownerName.trim()
        val trimmedPhone = phone.trim()
        val trimmedCat = category.trim()
        val trimmedArea = area.trim()
        val trimmedAddr = address.trim()
        val trimmedWa = whatsapp.trim()

        if (trimmedShop.isBlank() || trimmedOwner.isBlank() || trimmedPhone.isBlank() || trimmedCat.isBlank() || trimmedArea.isBlank()) {
            return null
        }

        val ownerId = "owner_${UUID.randomUUID().toString().take(8)}"
        val user = UserProfile(
            id = ownerId,
            phone = trimmedPhone,
            role = UserRole.OWNER,
            name = trimmedOwner
        )
        _registeredUsers.value = _registeredUsers.value + user
        _currentUser.value = user
        localPrefs?.saveCurrentUser(user)
        localPrefs?.saveRegisteredUsers(_registeredUsers.value)

        val newBusiness = Business(
            id = "b_${UUID.randomUUID().toString().take(8)}",
            ownerId = ownerId,
            name = trimmedShop,
            category = trimmedCat,
            phone = trimmedPhone,
            whatsapp = if (trimmedWa.isNotBlank()) trimmedWa else trimmedPhone,
            area = trimmedArea,
            address = if (trimmedAddr.isNotBlank()) trimmedAddr else "$trimmedArea, Himmatnagar",
            lat = HimmatnagarLocations.CENTER_LAT + (Math.random() - 0.5) * 0.01,
            lng = HimmatnagarLocations.CENTER_LNG + (Math.random() - 0.5) * 0.01,
            bio = "Official store in $trimmedArea, Himmatnagar. Welcome customers for best deals and authentic products.",
            isApproved = true,
            followerCount = 0,
            rating = 0f,
            reviewCount = 0
        )
        _businesses.value = listOf(newBusiness) + _businesses.value
        _businessViews.value = _businessViews.value + (newBusiness.id to 0)
        return newBusiness
    }

    fun loginOrRegisterWorker(
        name: String,
        phone: String,
        serviceType: String,
        area: String,
        exp: String,
        photoUrl: String = ""
    ): Worker? {
        val trimmedName = name.trim()
        val trimmedPhone = phone.trim()
        val trimmedService = serviceType.trim()
        val trimmedArea = area.trim()
        val trimmedExp = exp.trim()

        if (trimmedName.isBlank() || trimmedPhone.isBlank() || trimmedService.isBlank() || trimmedArea.isBlank()) {
            return null
        }

        val workerId = "worker_${UUID.randomUUID().toString().take(8)}"
        val user = UserProfile(
            id = workerId,
            phone = trimmedPhone,
            role = UserRole.WORKER,
            name = trimmedName
        )
        _registeredUsers.value = _registeredUsers.value + user
        _currentUser.value = user
        localPrefs?.saveCurrentUser(user)
        localPrefs?.saveRegisteredUsers(_registeredUsers.value)

        val newWorker = Worker(
            id = workerId,
            profileId = workerId,
            name = trimmedName,
            serviceType = trimmedService,
            phone = trimmedPhone,
            whatsapp = trimmedPhone,
            area = trimmedArea,
            experienceYears = if (trimmedExp.isNotBlank()) trimmedExp else "Experienced",
            photoUrl = photoUrl.trim(),
            rating = 0f,
            createdBy = "Registered Worker"
        )
        _workers.value = listOf(newWorker) + _workers.value
        return newWorker
    }

    fun switchRoleForTesting(newRole: UserRole) {
        val current = _currentUser.value
        val updated = if (current == null) {
            UserProfile(
                id = "demo_user",
                phone = "+91 98250 00000",
                role = newRole,
                name = when (newRole) {
                    UserRole.CUSTOMER -> "Aarav Shah (Customer)"
                    UserRole.OWNER -> "Rajesh Patel (Owner)"
                    UserRole.WORKER -> "Ramesh Solanki (Worker)"
                    UserRole.ADMIN -> "Admin Himmatnagar"
                }
            )
        } else {
            current.copy(role = newRole)
        }
        _currentUser.value = updated
        localPrefs?.saveCurrentUser(updated)
    }

    // Follow / Unfollow
    fun toggleFollow(businessId: String) {
        val current = _followedBusinessIds.value
        if (current.contains(businessId)) {
            val updated = current - businessId
            _followedBusinessIds.value = updated
            localPrefs?.saveFollowedBusinesses(updated)
            _businesses.value = _businesses.value.map {
                if (it.id == businessId) it.copy(followerCount = (it.followerCount - 1).coerceAtLeast(0)) else it
            }
        } else {
            val updated = current + businessId
            _followedBusinessIds.value = updated
            localPrefs?.saveFollowedBusinesses(updated)
            _businesses.value = _businesses.value.map {
                if (it.id == businessId) it.copy(followerCount = it.followerCount + 1) else it
            }
        }
    }

    // Like Reel
    fun toggleLikeReel(reelId: String) {
        val current = _likedReelIds.value
        if (current.contains(reelId)) {
            val updated = current - reelId
            _likedReelIds.value = updated
            localPrefs?.saveLikedReels(updated)
            _reels.value = _reels.value.map {
                if (it.id == reelId) it.copy(likes = (it.likes - 1).coerceAtLeast(0)) else it
            }
        } else {
            val updated = current + reelId
            _likedReelIds.value = updated
            localPrefs?.saveLikedReels(updated)
            _reels.value = _reels.value.map {
                if (it.id == reelId) it.copy(likes = it.likes + 1) else it
            }
        }
    }

    // ====================================================================
    // REEL QUOTA & UPLOAD SYSTEM (Asia/Kolkata calendar month, Max 3 reels)
    // ====================================================================

    fun getAsiaKolkataYearMonth(timestamp: Long = System.currentTimeMillis()): String {
        return localPrefs?.getAsiaKolkataYearMonth(timestamp) ?: run {
            val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("Asia/Kolkata"))
            cal.timeInMillis = timestamp
            val year = cal.get(java.util.Calendar.YEAR)
            val month = cal.get(java.util.Calendar.MONTH) + 1
            String.format(java.util.Locale.US, "%04d-%02d", year, month)
        }
    }

    fun getMonthlyReelUploadCount(uploaderId: String, yearMonth: String = getAsiaKolkataYearMonth()): Int {
        val key = "$uploaderId:$yearMonth"
        return _reelUploadUsage[key] ?: localPrefs?.getMonthlyReelUploadCount(uploaderId, yearMonth) ?: 0
    }

    fun getRemainingMonthlyReelQuota(uploaderId: String, yearMonth: String = getAsiaKolkataYearMonth()): Int {
        return (3 - getMonthlyReelUploadCount(uploaderId, yearMonth)).coerceAtLeast(0)
    }

    fun setMonthlyReelUploadCountForTesting(uploaderId: String, yearMonth: String, count: Int) {
        val key = "$uploaderId:$yearMonth"
        _reelUploadUsage[key] = count
        localPrefs?.saveMonthlyReelUploadCount(uploaderId, yearMonth, count)
    }

    private fun incrementMonthlyReelUploadCount(uploaderId: String, yearMonth: String = getAsiaKolkataYearMonth()): Int {
        val current = getMonthlyReelUploadCount(uploaderId, yearMonth)
        val next = current + 1
        val key = "$uploaderId:$yearMonth"
        _reelUploadUsage[key] = next
        localPrefs?.saveMonthlyReelUploadCount(uploaderId, yearMonth, next)
        return next
    }

    fun publishReel(
        uploader: UserProfile,
        title: String,
        caption: String,
        videoUriString: String = "",
        videoDurationSeconds: Int = 30,
        fileSizeBytes: Long = 5_000_000L,
        simulatedFailure: Boolean = false
    ): ReelUploadResult {
        // 1. Role verification: ONLY Business Owner and Worker allowed. Customer REJECTED!
        if (uploader.role == UserRole.CUSTOMER) {
            return ReelUploadResult.Error("Unauthorized: Customers are not permitted to upload reels.")
        }
        if (uploader.role != UserRole.OWNER && uploader.role != UserRole.WORKER && uploader.role != UserRole.ADMIN) {
            return ReelUploadResult.Error("Unauthorized role for uploading reels: ${uploader.role}")
        }

        // 2. Input validation
        val trimmedTitle = title.trim()
        val trimmedCaption = caption.trim()
        if (trimmedTitle.isBlank() || trimmedCaption.isBlank()) {
            return ReelUploadResult.Error("Title and caption cannot be blank.")
        }

        // 3. Video constraints: max 60s, max 50MB
        if (videoDurationSeconds > 60) {
            return ReelUploadResult.Error("Video duration exceeds the maximum limit of 60 seconds.")
        }
        if (fileSizeBytes > 50 * 1024 * 1024) {
            return ReelUploadResult.Error("Video file size exceeds the 50MB limit.")
        }

        // 4. Monthly quota check (Asia/Kolkata calendar month, max 3)
        val uploaderId = uploader.firebaseUid.ifBlank { uploader.id }
        val currentMonthKey = getAsiaKolkataYearMonth()
        val currentUploadCount = getMonthlyReelUploadCount(uploaderId, currentMonthKey)
        if (currentUploadCount >= 3) {
            return ReelUploadResult.Error("Monthly reel limit reached. You can upload again next month.")
        }

        // 5. Simulated failure handling: must not consume quota!
        if (simulatedFailure) {
            return ReelUploadResult.Error("Upload failed: Storage upload encountered an error. Quota was not consumed.")
        }

        // 6. Calculate server timestamps: created_at and strictly created_at + 10 days
        val serverCreatedAt = System.currentTimeMillis()
        val serverExpiresAt = serverCreatedAt + TimeUnit.DAYS.toMillis(10)

        // 7. Resolve business/worker identity and exact storage paths
        val reelId = "reel_${UUID.randomUUID().toString().take(8)}"
        val storageBucket = "reels-videos"
        val storagePath = "reels/$uploaderId/$reelId.mp4"
        val videoUrl = "https://supabase.sheherhimmatnagar.local/storage/v1/object/public/$storageBucket/$storagePath"

        var bId = ""
        var wId = ""
        var bName = "Himmatnagar Trader"
        var bCat = "Local Business"
        var bPhone = uploader.phone
        var bWa = uploader.phone

        if (uploader.role == UserRole.OWNER) {
            val business = _businesses.value.find { it.ownerId == uploader.id || it.ownerId == uploader.firebaseUid }
                ?: _businesses.value.firstOrNull()
            if (business != null) {
                bId = business.id
                bName = business.name
                bCat = business.category
                bPhone = business.phone
                bWa = business.whatsapp
            }
        } else if (uploader.role == UserRole.WORKER) {
            val worker = _workers.value.find { it.profileId == uploader.id || it.profileId == uploader.firebaseUid }
            wId = worker?.id ?: uploaderId
            bName = worker?.name ?: uploader.name
            bCat = worker?.serviceType ?: "Local Services"
            bPhone = worker?.phone ?: uploader.phone
            bWa = worker?.whatsapp ?: uploader.phone
        } else {
            bId = "b1"
            bName = "HMT Admin"
        }

        val newReel = Reel(
            id = reelId,
            businessId = bId,
            workerId = wId,
            uploaderId = uploaderId,
            uploaderRole = uploader.role.name,
            businessName = bName,
            businessCategory = bCat,
            businessPhone = bPhone,
            businessWhatsapp = bWa,
            storageBucket = storageBucket,
            storagePath = storagePath,
            videoUrl = videoUrl,
            title = trimmedTitle,
            caption = trimmedCaption,
            likes = 0,
            views = 0,
            createdAt = serverCreatedAt,
            expiresAt = serverExpiresAt,
            gradientColorIndex = (0..3).random()
        )

        // Add to active reels
        _reels.value = listOf(newReel) + _reels.value

        // 8. Consume monthly quota only upon successful publish
        incrementMonthlyReelUploadCount(uploaderId, currentMonthKey)
        val remaining = getRemainingMonthlyReelQuota(uploaderId, currentMonthKey)

        return ReelUploadResult.Success(newReel, remaining)
    }

    // Business Owner Actions
    fun addReel(businessId: String, title: String, caption: String): Boolean {
        val trimmedTitle = title.trim()
        val trimmedCaption = caption.trim()
        if (trimmedTitle.isBlank() || trimmedCaption.isBlank()) return false

        val business = _businesses.value.find { it.id == businessId } ?: return false
        val current = _currentUser.value
        // If current user is CUSTOMER, reject!
        if (current != null && current.role == UserRole.CUSTOMER) {
            return false
        }
        val uploader = current ?: UserProfile(
            id = business.ownerId,
            firebaseUid = business.ownerId,
            phone = business.phone,
            role = UserRole.OWNER,
            name = business.name
        )
        val result = publishReel(uploader, trimmedTitle, trimmedCaption)
        return result is ReelUploadResult.Success
    }

    fun deleteReel(reelId: String) {
        // Deleting an old reel does NOT restore the monthly quota!
        _reels.value = _reels.value.filter { it.id != reelId }
    }

    fun purgeExpiredReels(currentTime: Long = System.currentTimeMillis()): Int {
        val beforeCount = _reels.value.size
        _reels.value = _reels.value.filter { !it.isExpired(currentTime) }
        return beforeCount - _reels.value.size
    }

    fun getActiveReels(currentTime: Long = System.currentTimeMillis()): List<Reel> {
        return _reels.value.filter { !it.isExpired(currentTime) }
    }

    fun addOffer(
        businessId: String,
        productName: String,
        category: String,
        originalPrice: Double,
        discountedPrice: Double,
        couponCode: String,
        validUntil: String
    ): Boolean {
        val trimmedProduct = productName.trim()
        val trimmedCategory = category.trim()
        val trimmedValidUntil = validUntil.trim()
        val trimmedCode = (if (couponCode.isBlank()) "SAVE10" else couponCode.trim()).uppercase()

        if (trimmedProduct.isBlank() || trimmedCategory.isBlank() || trimmedValidUntil.isBlank()) return false
        if (originalPrice <= 0.0 || discountedPrice <= 0.0 || discountedPrice >= originalPrice) return false

        val business = _businesses.value.find { it.id == businessId } ?: return false
        val discountPercent = (((originalPrice - discountedPrice) / originalPrice) * 100).toInt().coerceIn(1, 99)

        val newOffer = Offer(
            id = "off_${UUID.randomUUID().toString().take(8)}",
            businessId = business.id,
            businessName = business.name,
            area = business.area,
            productName = trimmedProduct,
            category = trimmedCategory,
            originalPrice = originalPrice,
            discountedPrice = discountedPrice,
            discountPercent = discountPercent,
            couponCode = trimmedCode,
            validUntil = trimmedValidUntil
        )
        _offers.value = listOf(newOffer) + _offers.value
        return true
    }

    fun deleteOffer(offerId: String) {
        _offers.value = _offers.value.filter { it.id != offerId }
    }

    fun updateBusinessDetails(
        businessId: String,
        bio: String,
        phone: String,
        whatsapp: String,
        address: String
    ) {
        val trimmedBio = bio.trim()
        val trimmedPhone = phone.trim()
        val trimmedWa = whatsapp.trim()
        val trimmedAddr = address.trim()

        _businesses.value = _businesses.value.map {
            if (it.id == businessId) {
                it.copy(
                    bio = trimmedBio,
                    phone = trimmedPhone,
                    whatsapp = trimmedWa,
                    address = trimmedAddr
                )
            } else it
        }
    }

    fun updateWorkerProfile(
        workerId: String,
        name: String,
        serviceType: String,
        phone: String,
        area: String,
        experienceYears: String
    ) {
        val trimmedName = name.trim()
        val trimmedService = serviceType.trim()
        val trimmedPhone = phone.trim()
        val trimmedArea = area.trim()
        val trimmedExp = experienceYears.trim()

        if (trimmedName.isBlank() || trimmedService.isBlank() || trimmedPhone.isBlank() || trimmedArea.isBlank()) {
            return
        }

        _workers.value = _workers.value.map {
            if (it.id == workerId || it.profileId == workerId) {
                it.copy(
                    name = trimmedName,
                    serviceType = trimmedService,
                    phone = trimmedPhone,
                    area = trimmedArea,
                    experienceYears = trimmedExp
                )
            } else it
        }
        _currentUser.value = _currentUser.value?.copy(name = trimmedName, phone = trimmedPhone).also {
            localPrefs?.saveCurrentUser(it)
        }
    }

    // Worker Directory Actions
    fun addWorker(
        name: String,
        serviceType: String,
        phone: String,
        area: String,
        exp: String
    ): Boolean {
        val trimmedName = name.trim()
        val trimmedService = serviceType.trim()
        val trimmedPhone = phone.trim()
        val trimmedArea = area.trim()
        val trimmedExp = exp.trim()

        if (trimmedName.isBlank() || trimmedService.isBlank() || trimmedPhone.isBlank() || trimmedArea.isBlank()) {
            return false
        }

        val newWorker = Worker(
            id = "w_${UUID.randomUUID().toString().take(8)}",
            profileId = "",
            name = trimmedName,
            serviceType = trimmedService,
            phone = trimmedPhone,
            whatsapp = trimmedPhone,
            area = trimmedArea,
            experienceYears = if (trimmedExp.isNotBlank()) trimmedExp else "Experienced",
            rating = 0f,
            createdBy = "Community"
        )
        _workers.value = listOf(newWorker) + _workers.value
        return true
    }

    // Chat Actions
    fun sendChatMessage(businessId: String, businessName: String, text: String, isFromOwner: Boolean): Boolean {
        val trimmedText = text.trim()
        if (trimmedText.isBlank()) return false

        val user = _currentUser.value ?: return false
        val customerId = if (isFromOwner) {
            _chatMessages.value.lastOrNull { it.businessId == businessId && !it.isFromOwner }?.customerId ?: return false
        } else user.id

        val newMsg = ChatMessage(
            id = "msg_${UUID.randomUUID().toString().take(8)}",
            chatId = "chat_${businessId}_$customerId",
            businessId = businessId,
            businessName = businessName,
            senderId = user.id,
            senderName = user.name,
            text = trimmedText,
            timestamp = System.currentTimeMillis(),
            isFromOwner = isFromOwner,
            customerId = customerId
        )
        _chatMessages.value = _chatMessages.value + newMsg
        return true
    }

    // Admin Actions
    fun approveBusiness(businessId: String, isApproved: Boolean) {
        _businesses.value = _businesses.value.map {
            if (it.id == businessId) it.copy(isApproved = isApproved) else it
        }
    }

    fun deleteBusiness(businessId: String) {
        _businesses.value = _businesses.value.filter { it.id != businessId }
        _offers.value = _offers.value.filter { it.businessId != businessId }
        _reels.value = _reels.value.filter { it.businessId != businessId }
        val updatedFollows = _followedBusinessIds.value - businessId
        _followedBusinessIds.value = updatedFollows
        localPrefs?.saveFollowedBusinesses(updatedFollows)
        _businessViews.value = _businessViews.value - businessId
        _chatMessages.value = _chatMessages.value.filter { it.businessId != businessId }
    }

    // =========================================================================
    // Supabase Public Data-Source Layer (Read-Only)
    // =========================================================================

    suspend fun refreshFromSupabase(): Result<Unit> = runCatching {
        // 1. Fetch public businesses
        val businessesResult = supabaseDataSource.fetchPublicBusinesses()
        businessesResult.getOrNull()?.let { remoteBusinesses ->
            if (remoteBusinesses.isNotEmpty()) {
                val existingMap = _businesses.value.associateBy { it.id }
                val merged = remoteBusinesses.map { rb ->
                    val local = existingMap[rb.id]
                    if (local != null) {
                        rb.copy(
                            followerCount = local.followerCount,
                            rating = local.rating,
                            reviewCount = local.reviewCount
                        )
                    } else rb
                }
                _businesses.value = merged
            }
        }

        // 2. Fetch public workers
        val workersResult = supabaseDataSource.fetchPublicWorkers()
        workersResult.getOrNull()?.let { remoteWorkers ->
            if (remoteWorkers.isNotEmpty()) {
                _workers.value = remoteWorkers
            }
        }

        // 3. Fetch active reels
        val reelsResult = supabaseDataSource.fetchActiveReels()
        reelsResult.getOrNull()?.let { remoteReels ->
            if (remoteReels.isNotEmpty()) {
                val activeOnly = remoteReels.filter { !it.isExpired() }
                _reels.value = activeOnly
            }
        }

        // 4. Fetch public offers
        val offersResult = supabaseDataSource.fetchPublicOffers()
        offersResult.getOrNull()?.let { remoteOffers ->
            if (remoteOffers.isNotEmpty()) {
                _offers.value = remoteOffers
            }
        }
    }

    suspend fun syncProfileWithEdgeFunction(
        firebaseIdToken: String,
        role: String,
        name: String,
        phone: String,
        email: String,
        avatarUrl: String? = null
    ): Result<com.example.data.supabase.SyncProfileResponse> {
        return supabaseDataSource.syncProfileWithEdgeFunction(
            firebaseIdToken = firebaseIdToken,
            role = role,
            name = name,
            phone = phone,
            email = email,
            avatarUrl = avatarUrl
        )
    }
}
