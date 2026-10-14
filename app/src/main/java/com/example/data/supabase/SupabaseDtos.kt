package com.example.data.supabase

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data transfer objects matching Supabase PostgreSQL tables defined in supabase/schema.sql.
 *
 * All fields are deserialized safely with Moshi.
 */

@JsonClass(generateAdapter = true)
data class SupabaseBusinessDto(
    @Json(name = "id") val id: String,
    @Json(name = "owner_id") val ownerId: String = "",
    @Json(name = "name") val name: String = "",
    @Json(name = "category") val category: String = "",
    @Json(name = "phone") val phone: String = "",
    @Json(name = "whatsapp") val whatsapp: String = "",
    @Json(name = "area") val area: String = "",
    @Json(name = "address") val address: String = "",
    @Json(name = "lat") val lat: Double = 23.5977,
    @Json(name = "lng") val lng: Double = 72.9667,
    @Json(name = "bio") val bio: String? = null,
    @Json(name = "image_url") val imageUrl: String? = null,
    @Json(name = "cover_url") val coverUrl: String? = null,
    @Json(name = "is_approved") val isApproved: Boolean = true,
    @Json(name = "created_at") val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class SupabaseWorkerDto(
    @Json(name = "id") val id: String,
    @Json(name = "profile_id") val profileId: String? = null,
    @Json(name = "name") val name: String = "",
    @Json(name = "service_type") val serviceType: String = "",
    @Json(name = "phone") val phone: String = "",
    @Json(name = "area") val area: String = "",
    @Json(name = "experience_years") val experienceYears: String? = null,
    @Json(name = "photo_url") val photoUrl: String? = null,
    @Json(name = "rating") val rating: Double? = 0.0,
    @Json(name = "created_at") val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class SupabaseReelDto(
    @Json(name = "id") val id: String,
    @Json(name = "business_id") val businessId: String? = null,
    @Json(name = "worker_id") val workerId: String? = null,
    @Json(name = "uploader_id") val uploaderId: String = "",
    @Json(name = "uploader_role") val uploaderRole: String = "owner",
    @Json(name = "storage_bucket") val storageBucket: String = "reels-videos",
    @Json(name = "storage_path") val storagePath: String = "",
    @Json(name = "video_url") val videoUrl: String = "",
    @Json(name = "thumbnail_url") val thumbnailUrl: String? = null,
    @Json(name = "title") val title: String = "",
    @Json(name = "caption") val caption: String? = null,
    @Json(name = "views") val views: Int = 0,
    @Json(name = "likes") val likes: Int = 0,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "expires_at") val expiresAt: String? = null
)

@JsonClass(generateAdapter = true)
data class SupabaseOfferDto(
    @Json(name = "id") val id: String,
    @Json(name = "business_id") val businessId: String,
    @Json(name = "product_name") val productName: String = "",
    @Json(name = "original_price") val originalPrice: Double = 0.0,
    @Json(name = "discounted_price") val discountedPrice: Double = 0.0,
    @Json(name = "discount_percent") val discountPercent: Int = 0,
    @Json(name = "coupon_code") val couponCode: String = "",
    @Json(name = "valid_until") val validUntil: String = "",
    @Json(name = "image_url") val imageUrl: String? = null,
    @Json(name = "created_at") val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class SyncProfileRequest(
    @Json(name = "role") val role: String,
    @Json(name = "name") val name: String,
    @Json(name = "phone") val phone: String,
    @Json(name = "email") val email: String,
    @Json(name = "avatar_url") val avatarUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class SyncProfileResponse(
    @Json(name = "success") val success: Boolean = true,
    @Json(name = "user_id") val userId: String? = null,
    @Json(name = "role") val role: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "phone") val phone: String? = null,
    @Json(name = "email") val email: String? = null,
    @Json(name = "avatar_url") val avatarUrl: String? = null,
    @Json(name = "message") val message: String? = null,
    @Json(name = "error") val error: String? = null
)

@JsonClass(generateAdapter = true)
data class CreateBusinessRequest(
    @Json(name = "owner_id") val ownerId: String,
    @Json(name = "name") val name: String,
    @Json(name = "category") val category: String,
    @Json(name = "phone") val phone: String,
    @Json(name = "whatsapp") val whatsapp: String,
    @Json(name = "area") val area: String,
    @Json(name = "address") val address: String,
    @Json(name = "lat") val lat: Double = 23.5977,
    @Json(name = "lng") val lng: Double = 72.9667,
    @Json(name = "bio") val bio: String? = null,
    @Json(name = "image_url") val imageUrl: String? = null,
    @Json(name = "cover_url") val coverUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class UpdateBusinessRequest(
    @Json(name = "bio") val bio: String? = null,
    @Json(name = "phone") val phone: String? = null,
    @Json(name = "whatsapp") val whatsapp: String? = null,
    @Json(name = "address") val address: String? = null
)

@JsonClass(generateAdapter = true)
data class CreateWorkerRequest(
    @Json(name = "profile_id") val profileId: String? = null,
    @Json(name = "name") val name: String,
    @Json(name = "service_type") val serviceType: String,
    @Json(name = "phone") val phone: String,
    @Json(name = "area") val area: String,
    @Json(name = "experience_years") val experienceYears: String? = null,
    @Json(name = "photo_url") val photoUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class UpdateWorkerRequest(
    @Json(name = "name") val name: String? = null,
    @Json(name = "service_type") val serviceType: String? = null,
    @Json(name = "phone") val phone: String? = null,
    @Json(name = "area") val area: String? = null,
    @Json(name = "experience_years") val experienceYears: String? = null
)

@JsonClass(generateAdapter = true)
data class CreateOfferRequest(
    @Json(name = "business_id") val businessId: String,
    @Json(name = "product_name") val productName: String,
    @Json(name = "original_price") val originalPrice: Double,
    @Json(name = "discounted_price") val discountedPrice: Double,
    @Json(name = "discount_percent") val discountPercent: Int,
    @Json(name = "coupon_code") val couponCode: String,
    @Json(name = "valid_until") val validUntil: String,
    @Json(name = "image_url") val imageUrl: String? = null
)


