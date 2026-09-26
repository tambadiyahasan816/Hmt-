package com.example.data.model

import java.util.concurrent.TimeUnit

enum class UserRole {
    CUSTOMER,
    OWNER,
    ADMIN
}

enum class BusinessCategory(val displayName: String, val iconName: String) {
    FOOD("Food & Dining", "restaurant"),
    FASHION("Fashion & Wear", "checkroom"),
    ELECTRONICS("Mobile & Gadgets", "smartphone"),
    MEDICAL("Medical & Pharma", "local_hospital"),
    SERVICES("Home & Services", "build"),
    GROCERY("Grocery & Mart", "shopping_cart"),
    JEWELRY("Jewelry & Gold", "diamond"),
    AUTO("Auto & Vehicles", "directions_car")
}

object HimmatnagarLocations {
    val AREAS = listOf(
        "Motipura",
        "Mahavirnagar",
        "Station Road",
        "Tower Chowk",
        "Polo Ground",
        "Nyay Mandir",
        "Himat High School Road",
        "Bypass Road",
        "Sahakari Jin Road",
        "Civil Hospital Area"
    )

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
    val category: BusinessCategory,
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
    val followerCount: Int = 120,
    val rating: Float = 4.7f,
    val reviewCount: Int = 38,
    val createdAt: Long = System.currentTimeMillis()
)

data class BusinessPost(
    val id: String,
    val businessId: String,
    val imageUrl: String,
    val caption: String,
    val likes: Int = 42,
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
    val likes: Int = 85,
    val views: Int = 420,
    val createdAt: Long = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(2), // default 2 days ago
    val expiresAt: Long = createdAt + TimeUnit.DAYS.toMillis(10), // 10 days strictly
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
    val category: BusinessCategory,
    val originalPrice: Double,
    val discountedPrice: Double,
    val discountPercent: Int,
    val couponCode: String,
    val validUntil: String,
    val imageUrl: String = ""
)

data class Worker(
    val id: String,
    val name: String,
    val serviceType: String,
    val phone: String,
    val area: String,
    val experience: String,
    val rating: Float = 4.8f,
    val createdBy: String = "Community"
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
