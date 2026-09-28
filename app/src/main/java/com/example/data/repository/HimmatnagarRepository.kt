package com.example.data.repository

import com.example.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import java.util.concurrent.TimeUnit

class HimmatnagarRepository {

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

    init {
        seedInitialData()
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
    }

    fun loginOrRegisterCustomer(name: String, phone: String) {
        val user = UserProfile(
            id = "user_${UUID.randomUUID().toString().take(8)}",
            phone = phone,
            role = UserRole.CUSTOMER,
            name = name
        )
        _currentUser.value = user
    }

    fun loginOrRegisterOwner(
        shopName: String,
        ownerName: String,
        phone: String,
        category: String,
        area: String,
        address: String,
        whatsapp: String
    ): Business {
        val ownerId = "owner_${UUID.randomUUID().toString().take(8)}"
        val user = UserProfile(
            id = ownerId,
            phone = phone,
            role = UserRole.OWNER,
            name = ownerName
        )
        _currentUser.value = user

        val newBusiness = Business(
            id = "b_${UUID.randomUUID().toString().take(8)}",
            ownerId = ownerId,
            name = shopName,
            category = category,
            phone = phone,
            whatsapp = whatsapp,
            area = area,
            address = address,
            lat = HimmatnagarLocations.CENTER_LAT + (Math.random() - 0.5) * 0.01,
            lng = HimmatnagarLocations.CENTER_LNG + (Math.random() - 0.5) * 0.01,
            bio = "Official store in $area, Himmatnagar. Welcome customers for best deals and authentic products.",
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
    ): Worker {
        val workerId = "worker_${UUID.randomUUID().toString().take(8)}"
        val user = UserProfile(
            id = workerId,
            phone = phone,
            role = UserRole.WORKER,
            name = name
        )
        _currentUser.value = user

        val newWorker = Worker(
            id = workerId,
            profileId = workerId,
            name = name,
            serviceType = serviceType,
            phone = phone,
            whatsapp = phone,
            area = area,
            experienceYears = if (exp.isNotBlank()) exp else "Experienced",
            photoUrl = photoUrl,
            rating = 0f,
            createdBy = "Registered Worker"
        )
        _workers.value = listOf(newWorker) + _workers.value
        return newWorker
    }

    fun switchRoleForTesting(newRole: UserRole) {
        val current = _currentUser.value
        if (current == null) {
            _currentUser.value = UserProfile(
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
            _currentUser.value = current.copy(role = newRole)
        }
    }

    // Follow / Unfollow
    fun toggleFollow(businessId: String) {
        val current = _followedBusinessIds.value
        if (current.contains(businessId)) {
            _followedBusinessIds.value = current - businessId
            _businesses.value = _businesses.value.map {
                if (it.id == businessId) it.copy(followerCount = (it.followerCount - 1).coerceAtLeast(0)) else it
            }
        } else {
            _followedBusinessIds.value = current + businessId
            _businesses.value = _businesses.value.map {
                if (it.id == businessId) it.copy(followerCount = it.followerCount + 1) else it
            }
        }
    }

    // Like Reel
    fun toggleLikeReel(reelId: String) {
        val current = _likedReelIds.value
        if (current.contains(reelId)) {
            _likedReelIds.value = current - reelId
            _reels.value = _reels.value.map {
                if (it.id == reelId) it.copy(likes = (it.likes - 1).coerceAtLeast(0)) else it
            }
        } else {
            _likedReelIds.value = current + reelId
            _reels.value = _reels.value.map {
                if (it.id == reelId) it.copy(likes = it.likes + 1) else it
            }
        }
    }

    // Business Owner Actions
    fun addReel(businessId: String, title: String, caption: String) {
        val business = _businesses.value.find { it.id == businessId } ?: return
        val now = System.currentTimeMillis()
        val newReel = Reel(
            id = "reel_${UUID.randomUUID().toString().take(8)}",
            businessId = business.id,
            businessName = business.name,
            businessCategory = business.category,
            businessPhone = business.phone,
            businessWhatsapp = business.whatsapp,
            title = title,
            caption = caption,
            likes = 0,
            views = 0,
            createdAt = now,
            expiresAt = now + TimeUnit.DAYS.toMillis(10), // Strictly 10 days
            gradientColorIndex = (0..3).random()
        )
        _reels.value = listOf(newReel) + _reels.value
    }

    fun deleteReel(reelId: String) {
        _reels.value = _reels.value.filter { it.id != reelId }
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
        val business = _businesses.value.find { it.id == businessId } ?: return
        val discountPercent = if (originalPrice > 0) {
            (((originalPrice - discountedPrice) / originalPrice) * 100).toInt().coerceIn(1, 99)
        } else 10

        val newOffer = Offer(
            id = "off_${UUID.randomUUID().toString().take(8)}",
            businessId = business.id,
            businessName = business.name,
            area = business.area,
            productName = productName,
            category = category,
            originalPrice = originalPrice,
            discountedPrice = discountedPrice,
            discountPercent = discountPercent,
            couponCode = couponCode.uppercase().trim(),
            validUntil = validUntil
        )
        _offers.value = listOf(newOffer) + _offers.value
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
        _businesses.value = _businesses.value.map {
            if (it.id == businessId) {
                it.copy(
                    bio = bio,
                    phone = phone,
                    whatsapp = whatsapp,
                    address = address
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
        _workers.value = _workers.value.map {
            if (it.id == workerId || it.profileId == workerId) {
                it.copy(
                    name = name,
                    serviceType = serviceType,
                    phone = phone,
                    area = area,
                    experienceYears = experienceYears
                )
            } else it
        }
        _currentUser.value = _currentUser.value?.copy(name = name, phone = phone)
    }

    // Worker Directory Actions
    fun addWorker(
        name: String,
        serviceType: String,
        phone: String,
        area: String,
        exp: String
    ) {
        val newWorker = Worker(
            id = "w_${UUID.randomUUID().toString().take(8)}",
            profileId = "",
            name = name,
            serviceType = serviceType,
            phone = phone,
            whatsapp = phone,
            area = area,
            experienceYears = exp,
            rating = 0f,
            createdBy = "Community"
        )
        _workers.value = listOf(newWorker) + _workers.value
    }

    // Chat Actions
    fun sendChatMessage(businessId: String, businessName: String, text: String, isFromOwner: Boolean) {
        val user = _currentUser.value
        val senderId = user?.id ?: "guest"
        val senderName = user?.name ?: if (isFromOwner) businessName else "Customer"

        val newMsg = ChatMessage(
            id = "msg_${UUID.randomUUID().toString().take(8)}",
            chatId = "chat_$businessId",
            businessId = businessId,
            businessName = businessName,
            senderId = senderId,
            senderName = senderName,
            text = text,
            timestamp = System.currentTimeMillis(),
            isFromOwner = isFromOwner
        )
        _chatMessages.value = _chatMessages.value + newMsg
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
    }
}
