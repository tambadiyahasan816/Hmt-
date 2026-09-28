package com.example.data.model

import androidx.compose.runtime.mutableStateListOf
import java.util.concurrent.TimeUnit

enum class UserRole {
    CUSTOMER,
    OWNER,
    WORKER,
    ADMIN
}

object BusinessCategories {
    val ALL = listOf(
        "Food & Dining",
        "Fashion & Wear",
        "Mobile & Gadgets",
        "Medical & Pharma",
        "Home & Services",
        "Grocery & Mart",
        "Jewelry & Gold",
        "Auto & Vehicles",
        "Electronics & Appliances",
        "Furniture & Home Decor",
        "Footwear",
        "Beauty & Salon",
        "Gym & Fitness",
        "Education & Coaching",
        "Books & Stationery",
        "Hardware & Paint",
        "Electrical & Sanitary Goods",
        "Travel & Transport",
        "Real Estate",
        "Printing & Xerox",
        "Courier & Logistics",
        "Photo Studio",
        "Toys & Gifts",
        "Dairy & Sweets",
        "Tiffin & Catering",
        "Wedding & Event Services",
        "Pet Shop",
        "Agriculture & Seeds",
        "Other"
    )
}

object WorkerServiceTypes {
    val ALL = listOf(
        "Plumber",
        "Electrician",
        "Carpenter",
        "Painter",
        "Mechanic",
        "Tutor",
        "AC/TV/Fridge Repair",
        "Mason",
        "Driver",
        "Tailor",
        "Cook",
        "Cleaner",
        "Beautician",
        "Other"
    )
}

object HimmatnagarLocations {
    private val _areas = mutableStateListOf(
        "Motipura",
        "Mahavirnagar",
        "Station Road",
        "Tower Chowk",
        "Polo Ground",
        "Ganesh Baug",
        "Vaktanagar",
        "Gayatri Nagar",
        "Sant Kabir Nagar",
        "Bhatvarna",
        "Devipujak Vas",
        "Chiloda",
        "RTO Circle",
        "Khedapa Road",
        "Idar Road",
        "Shamlaji Road",
        "New CB Patel Road",
        "Sardar Patel Circle",
        "Municipal Colony",
        "GIDC Area",
        "Khadpa",
        "Sabalagadh Road",
        "Dabhoda",
        "Bhoyan",
        "Panpur Patiya",
        "Other"
    )

    val AREAS: List<String> get() = _areas

    fun addCustomArea(newArea: String) {
        val trimmed = newArea.trim()
        if (trimmed.isNotBlank() && !_areas.any { it.equals(trimmed, ignoreCase = true) }) {
            val otherIdx = _areas.indexOf("Other")
            if (otherIdx >= 0) {
                _areas.add(otherIdx, trimmed)
            } else {
                _areas.add(trimmed)
            }
        }
    }

    // Central Himmatnagar Coordinates
    const val CENTER_LAT = 23.5977
    const val CENTER_LNG = 72.9667
}

data class UserProfile(
    val id: String,
    val phone: String,
    val role: UserRole,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

data class Business(
    val id: String,
    val ownerId: String,
    val name: String,
    val category: String,
    val phone: String,
    val whatsapp: String,
    val area: String,
    val address: String,
    val lat: Double,
    val lng: Double,
    val bio: String,
    val imageUrl: String = "",
    val coverUrl: String = "",
    val isApproved: Boolean = true,
    val followerCount: Int = 0,
    val rating: Float = 0f,
    val reviewCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

data class BusinessPost(
    val id: String,
    val businessId: String,
    val imageUrl: String,
    val caption: String,
    val likes: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

data class Reel(
    val id: String,
    val businessId: String,
    val businessName: String,
    val businessCategory: String,
    val businessPhone: String,
    val businessWhatsapp: String,
    val videoUrl: String = "",
    val title: String,
    val caption: String,
    val likes: Int = 0,
    val views: Int = 0,
    val createdAt: Long = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(2),
    val expiresAt: Long = createdAt + TimeUnit.DAYS.toMillis(10), // strictly 10 days
    val gradientColorIndex: Int = 0
) {
    fun getRemainingDays(currentTime: Long = System.currentTimeMillis()): Int {
        val diff = expiresAt - currentTime
        return if (diff > 0) {
            (diff / TimeUnit.DAYS.toMillis(1)).toInt().coerceAtLeast(1)
        } else {
            0
        }
    }

    fun isExpired(currentTime: Long = System.currentTimeMillis()): Boolean {
        return currentTime >= expiresAt
    }
}

data class Offer(
    val id: String,
    val businessId: String,
    val businessName: String,
    val area: String,
    val productName: String,
    val category: String,
    val originalPrice: Double,
    val discountedPrice: Double,
    val discountPercent: Int,
    val couponCode: String,
    val validUntil: String,
    val imageUrl: String = ""
)

data class Worker(
    val id: String,
    val profileId: String = "",
    val name: String,
    val serviceType: String,
    val phone: String,
    val whatsapp: String = "",
    val area: String,
    val experienceYears: String = "",
    val photoUrl: String = "",
    val rating: Float = 0f,
    val createdBy: String = "Community",
    val createdAt: Long = System.currentTimeMillis()
)

data class ChatMessage(
    val id: String,
    val chatId: String,
    val businessId: String,
    val businessName: String,
    val senderId: String,
    val senderName: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFromOwner: Boolean = false
)
