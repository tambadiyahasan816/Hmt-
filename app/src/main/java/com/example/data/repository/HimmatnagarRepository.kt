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
                category = BusinessCategory.ELECTRONICS,
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
                category = BusinessCategory.FOOD,
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
                category = BusinessCategory.FASHION,
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
                category = BusinessCategory.FOOD,
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
                category = BusinessCategory.MEDICAL,
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
                category = BusinessCategory.GROCERY,
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
                category = BusinessCategory.JEWELRY,
                phone = "+91 98242 66778",
                whatsapp = "919824266778",
                area = "Station Road",
                address = "Sona Bazaar, Station Road, Himmatnagar",
                lat = 23.5955,
                lng = 72.9618,
                bio = "100% BIS Hallmarked 916 Gold jewellery, certified Solitaire rings, bridal necklace sets & custom craftsmanship.",
                followerCount = 2890,
                rating = 4.9f,
                reviewCount = 188
            ),
            Business(
                id = "b8",
                ownerId = "owner_8",
                name = "Krishna Auto Spa & Car Care",
                category = BusinessCategory.AUTO,
                phone = "+91 99099 22334",
                whatsapp = "919909922334",
                area = "Bypass Road",
                address = "Bypass Ring Road, Near HP Petrol Pump, Himmatnagar",
                lat = 23.5910,
                lng = 72.9750,
                bio = "High-pressure snow foam wash, interior dry cleaning, 9H ceramic coating & wheel alignment.",
                followerCount = 1120,
                rating = 4.7f,
                reviewCount = 92
            )
        )
        _businesses.value = sampleBusinesses

        val sampleOffers = listOf(
            Offer(
                id = "o1",
                businessId = "b1",
                businessName = "Mahavir Mobile & Gadgets",
                area = "Mahavirnagar",
                productName = "iPhone 18 Pro (256GB)",
                category = BusinessCategory.ELECTRONICS,
                originalPrice = 134900.0,
                discountedPrice = 119900.0,
                discountPercent = 12,
                couponCode = "HIMMATIPHONE",
                validUntil = "Oct 15, 2026"
            ),
            Offer(
                id = "o2",
                businessId = "b2",
                businessName = "Shreeji Kathiyawadi & Punjabi Dhaba",
                area = "Motipura",
                productName = "Unlimited Gujarati Kathiyawadi Thali",
                category = BusinessCategory.FOOD,
                originalPrice = 350.0,
                discountedPrice = 199.0,
                discountPercent = 43,
                couponCode = "DESITHALI",
                validUntil = "Oct 10, 2026"
            ),
            Offer(
                id = "o3",
                businessId = "b3",
                businessName = "Royal Heritage Fashion & Kurtis",
                area = "Station Road",
                productName = "Festive Cotton Embroidered Kurti Set",
                category = BusinessCategory.FASHION,
                originalPrice = 1499.0,
                discountedPrice = 699.0,
                discountPercent = 53,
                couponCode = "FESTIVE50",
                validUntil = "Oct 20, 2026"
            ),
            Offer(
                id = "o4",
                businessId = "b6",
                businessName = "Polo Green Mart & Dry Fruits",
                area = "Polo Ground",
                productName = "Premium California Almonds + Walnuts Combo (1kg)",
                category = BusinessCategory.GROCERY,
                originalPrice = 1500.0,
                discountedPrice = 950.0,
                discountPercent = 37,
                couponCode = "POLONUTS",
                validUntil = "Oct 12, 2026"
            ),
            Offer(
                id = "o5",
                businessId = "b1",
                businessName = "Mahavir Mobile & Gadgets",
                area = "Mahavirnagar",
                productName = "Smart Fitness Band Watch 9 AMOLED",
                category = BusinessCategory.ELECTRONICS,
                originalPrice = 3999.0,
                discountedPrice = 1499.0,
                discountPercent = 62,
                couponCode = "SMART62",
                validUntil = "Oct 08, 2026"
            ),
            Offer(
                id = "o6",
                businessId = "b5",
                businessName = "Apex 24/7 Pharmacy & Wellness",
                area = "Tower Chowk",
                productName = "Full Body Comprehensive Health Test Package",
                category = BusinessCategory.MEDICAL,
                originalPrice = 1999.0,
                discountedPrice = 799.0,
                discountPercent = 60,
                couponCode = "CARE60",
                validUntil = "Oct 30, 2026"
            ),
            Offer(
                id = "o7",
                businessId = "b4",
                businessName = "Radhe Krishna Sweets & Farsan",
                area = "Tower Chowk",
                productName = "Pure Desi Ghee Kaju Katli (500g Gift Box)",
                category = BusinessCategory.FOOD,
                originalPrice = 550.0,
                discountedPrice = 380.0,
                discountPercent = 31,
                couponCode = "SWEET30",
                validUntil = "Oct 05, 2026"
            ),
            Offer(
                id = "o8",
                businessId = "b8",
                businessName = "Krishna Auto Spa & Car Care",
                area = "Bypass Road",
                productName = "Complete Exterior Ceramic Foam Detailing",
                category = BusinessCategory.AUTO,
                originalPrice = 9000.0,
                discountedPrice = 4999.0,
                discountPercent = 44,
                couponCode = "AUTOSPA",
                validUntil = "Oct 18, 2026"
            )
        )
        _offers.value = sampleOffers

        val sampleReels = listOf(
            Reel(
                id = "r1",
                businessId = "b1",
                businessName = "Mahavir Mobile & Gadgets",
                businessCategory = "Mobile & Gadgets",
                businessPhone = "+91 98250 11223",
                businessWhatsapp = "919825011223",
                title = "New iPhone 18 Pro Unboxing & Festive Launch Event!",
                caption = "Now in stock at Mahavir Mobile Himmatnagar! Get flat ₹15,000 exchange bonus & free wireless adapter on every purchase today. Call us now!",
                likes = 340,
                views = 1840,
                createdAt = now - TimeUnit.DAYS.toMillis(1),
                expiresAt = (now - TimeUnit.DAYS.toMillis(1)) + TimeUnit.DAYS.toMillis(10),
                gradientColorIndex = 0
            ),
            Reel(
                id = "r2",
                businessId = "b2",
                businessName = "Shreeji Kathiyawadi & Punjabi Dhaba",
                businessCategory = "Food & Dining",
                businessPhone = "+91 98981 44556",
                businessWhatsapp = "919898144556",
                title = "Fresh Ringna No Olo & Hot Bajra Rotla Live Preparation",
                caption = "Experience the real smoke flavour of charcoal-roasted ringna no olo with organic white butter! Unlimited Thali ₹199 only this weekend.",
                likes = 890,
                views = 4320,
                createdAt = now - TimeUnit.DAYS.toMillis(3),
                expiresAt = (now - TimeUnit.DAYS.toMillis(3)) + TimeUnit.DAYS.toMillis(10),
                gradientColorIndex = 1
            ),
            Reel(
                id = "r3",
                businessId = "b3",
                businessName = "Royal Heritage Fashion & Kurtis",
                businessCategory = "Fashion & Wear",
                businessPhone = "+91 94270 55667",
                businessWhatsapp = "919427055667",
                title = "Bridal Chaniya Choli & Navratri Designer Collection",
                caption = "Handcrafted mirror work, pure silk and vibrant heritage colours straight from our workshop! Visit our Station Road showroom.",
                likes = 620,
                views = 2950,
                createdAt = now - TimeUnit.DAYS.toMillis(2),
                expiresAt = (now - TimeUnit.DAYS.toMillis(2)) + TimeUnit.DAYS.toMillis(10),
                gradientColorIndex = 2
            ),
            Reel(
                id = "r4",
                businessId = "b4",
                businessName = "Radhe Krishna Sweets & Farsan",
                businessCategory = "Food & Dining",
                businessPhone = "+91 98254 77889",
                businessWhatsapp = "919825477889",
                title = "Morning Hot Fafda Jalebi & Kadhi Making at Tower Chowk",
                caption = "Crispy fafda, piping hot saffron jalebi with spicy papaya sambharo. Himmatnagar's favourite breakfast since 1984!",
                likes = 1250,
                views = 6100,
                createdAt = now - TimeUnit.DAYS.toMillis(4),
                expiresAt = (now - TimeUnit.DAYS.toMillis(4)) + TimeUnit.DAYS.toMillis(10),
                gradientColorIndex = 3
            ),
            Reel(
                id = "r5",
                businessId = "b8",
                businessName = "Krishna Auto Spa & Car Care",
                businessCategory = "Auto & Vehicles",
                businessPhone = "+91 99099 22334",
                businessWhatsapp = "9909922334",
                title = "Extreme Snow Foam Wash & 9H Ceramic Gloss Demonstration",
                caption = "Watch the mirror finish shine! Protection against highway dust, UV fading and swirl marks. Book your slot via WhatsApp.",
                likes = 410,
                views = 1680,
                createdAt = now - TimeUnit.DAYS.toMillis(2),
                expiresAt = (now - TimeUnit.DAYS.toMillis(2)) + TimeUnit.DAYS.toMillis(10),
                gradientColorIndex = 4
            )
        )
        _reels.value = sampleReels

        val sampleWorkers = listOf(
            Worker(
                id = "w1",
                name = "Ramesh Solanki",
                serviceType = "Plumber",
                phone = "+91 98251 90812",
                area = "Motipura",
                experience = "12 Years Exp",
                rating = 4.9f
            ),
            Worker(
                id = "w2",
                name = "Jignesh Panchal",
                serviceType = "Electrician",
                phone = "+91 97230 41526",
                area = "Mahavirnagar",
                experience = "8 Years Exp",
                rating = 4.8f
            ),
            Worker(
                id = "w3",
                name = "Bharat Prajapati",
                serviceType = "AC Repair & Technician",
                phone = "+91 99042 11984",
                area = "Station Road",
                experience = "6 Years Exp",
                rating = 4.7f
            ),
            Worker(
                id = "w4",
                name = "Sunita Patel",
                serviceType = "Mathematics & Science Tutor",
                phone = "+91 94268 77213",
                area = "Polo Ground",
                experience = "7 Years Exp",
                rating = 4.9f
            ),
            Worker(
                id = "w5",
                name = "Dinesh Mistri",
                serviceType = "Carpenter & Wood Interior",
                phone = "+91 98980 33412",
                area = "Tower Chowk",
                experience = "15 Years Exp",
                rating = 4.8f
            ),
            Worker(
                id = "w6",
                name = "Mahesh Vankar",
                serviceType = "House Painter & Wall Polish",
                phone = "+91 98244 55112",
                area = "Bypass Road",
                experience = "10 Years Exp",
                rating = 4.6f
            )
        )
        _workers.value = sampleWorkers

        // Initial welcome chat message
        _chatMessages.value = listOf(
            ChatMessage(
                id = "m1",
                chatId = "chat_b1",
                businessId = "b1",
                businessName = "Mahavir Mobile & Gadgets",
                senderId = "b1",
                senderName = "Mahavir Mobile",
                text = "Welcome to Mahavir Mobile Himmatnagar! How can we assist you with smartphones or offers today?",
                timestamp = now - 3600000,
                isFromOwner = true
            )
        )
    }

    // Auto-Delete Feature Implementation
    // Check and remove reels where expires_at < now()
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
        category: BusinessCategory,
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
            isApproved = true // Automatically marked ready or pending review
        )
        _businesses.value = listOf(newBusiness) + _businesses.value
        return newBusiness
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

    // Business Owner Actions (Protected)
    fun addReel(
        businessId: String,
        title: String,
        caption: String,
        videoUrl: String = ""
    ) {
        val business = _businesses.value.find { it.id == businessId } ?: _businesses.value.first()
        val now = System.currentTimeMillis()
        val newReel = Reel(
            id = "r_${UUID.randomUUID().toString().take(8)}",
            businessId = business.id,
            businessName = business.name,
            businessCategory = business.category.displayName,
            businessPhone = business.phone,
            businessWhatsapp = business.whatsapp,
            videoUrl = videoUrl,
            title = title,
            caption = caption,
            likes = 1,
            views = 10,
            createdAt = now,
            expiresAt = now + TimeUnit.DAYS.toMillis(10), // strictly 10 days
            gradientColorIndex = (_reels.value.size % 5)
        )
        _reels.value = listOf(newReel) + _reels.value
    }

    fun deleteReel(reelId: String) {
        _reels.value = _reels.value.filter { it.id != reelId }
    }

    fun addOffer(
        businessId: String,
        productName: String,
        category: BusinessCategory,
        originalPrice: Double,
        discountedPrice: Double,
        couponCode: String,
        validUntil: String
    ) {
        val business = _businesses.value.find { it.id == businessId } ?: _businesses.value.first()
        val discountPercent = if (originalPrice > 0) {
            (((originalPrice - discountedPrice) / originalPrice) * 100).toInt().coerceIn(1, 99)
        } else 10

        val newOffer = Offer(
            id = "o_${UUID.randomUUID().toString().take(8)}",
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
            if (it.id == businessId) it.copy(bio = bio, phone = phone, whatsapp = whatsapp, address = address) else it
        }
    }

    // Worker Directory Actions
    fun addWorker(
        name: String,
        serviceType: String,
        phone: String,
        area: String,
        experience: String
    ) {
        val newWorker = Worker(
            id = "w_${UUID.randomUUID().toString().take(8)}",
            name = name,
            serviceType = serviceType,
            phone = phone,
            area = area,
            experience = experience,
            createdBy = _currentUser.value?.name ?: "Community Member"
        )
        _workers.value = listOf(newWorker) + _workers.value
    }

    // Chat Actions
    fun sendChatMessage(
        businessId: String,
        businessName: String,
        text: String,
        isFromOwner: Boolean
    ) {
        val user = _currentUser.value
        val newMsg = ChatMessage(
            id = "m_${UUID.randomUUID().toString().take(8)}",
            chatId = "chat_$businessId",
            businessId = businessId,
            businessName = businessName,
            senderId = user?.id ?: "guest",
            senderName = user?.name ?: if (isFromOwner) businessName else "Customer",
            text = text,
            timestamp = System.currentTimeMillis(),
            isFromOwner = isFromOwner
        )
        _chatMessages.value = _chatMessages.value + newMsg
    }

    // Admin Actions
    fun approveBusiness(businessId: String, approve: Boolean) {
        _businesses.value = _businesses.value.map {
            if (it.id == businessId) it.copy(isApproved = approve) else it
        }
    }

    fun deleteBusiness(businessId: String) {
        _businesses.value = _businesses.value.filter { it.id != businessId }
        _offers.value = _offers.value.filter { it.businessId != businessId }
        _reels.value = _reels.value.filter { it.businessId != businessId }
    }
}
