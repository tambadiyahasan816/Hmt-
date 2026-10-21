package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.Business
import com.example.data.model.ChatMessage
import com.example.data.model.Offer
import com.example.data.model.Reel
import com.example.data.model.UserProfile
import com.example.data.model.Worker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * Lightweight Supabase Data API sync for the Android app.
 * Firebase remains the login provider. The publishable key is safe for a mobile
 * client, while database access must still be protected by RLS policies.
 */
class SupabaseSync private constructor() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val isConfigured: Boolean
        get() = BuildConfig.SUPABASE_URL.isNotBlank() && BuildConfig.SUPABASE_PUBLISHABLE_KEY.isNotBlank()

    fun upsertProfile(user: UserProfile) = post("profiles", JSONObject().apply {
        put("id", user.id)
        put("firebase_uid", user.firebaseUid)
        put("name", user.name)
        put("phone", user.phone)
        put("email", user.email)
        put("role", user.role.name)
        put("created_at", iso(user.createdAt))
        put("updated_at", iso(user.updatedAt))
    })

    fun upsertBusiness(business: Business) = post("businesses", JSONObject().apply {
        put("id", business.id)
        put("owner_id", business.ownerId)
        put("name", business.name)
        put("category", business.category)
        put("phone", business.phone)
        put("whatsapp", business.whatsapp)
        put("area", business.area)
        put("address", business.address)
        put("latitude", business.lat)
        put("longitude", business.lng)
        put("bio", business.bio)
        put("image_url", business.imageUrl)
        put("cover_url", business.coverUrl)
        put("is_approved", business.isApproved)
        put("follower_count", business.followerCount)
        put("rating", business.rating)
        put("review_count", business.reviewCount)
        put("created_at", iso(business.createdAt))
    })

    fun upsertWorker(worker: Worker) = post("workers", JSONObject().apply {
        put("id", worker.id)
        put("profile_id", worker.profileId)
        put("name", worker.name)
        put("service_type", worker.serviceType)
        put("phone", worker.phone)
        put("email", worker.email)
        put("whatsapp", worker.whatsapp)
        put("area", worker.area)
        put("experience_years", worker.experienceYears)
        put("photo_url", worker.photoUrl)
        put("rating", worker.rating)
        put("created_by", worker.createdBy)
        put("created_at", iso(worker.createdAt))
    })

    fun upsertOffer(offer: Offer) = post("offers", JSONObject().apply {
        put("id", offer.id)
        put("business_id", offer.businessId)
        put("product_name", offer.productName)
        put("category", offer.category)
        put("original_price", offer.originalPrice)
        put("discounted_price", offer.discountedPrice)
        put("discount_percent", offer.discountPercent)
        put("coupon_code", offer.couponCode)
        put("valid_until", parseDate(offer.validUntil))
        put("image_url", offer.imageUrl)
    })

    fun upsertReel(reel: Reel) = post("reels", JSONObject().apply {
        put("id", reel.id)
        put("business_id", reel.businessId)
        put("worker_id", reel.workerId)
        put("uploader_id", reel.uploaderId)
        put("uploader_role", reel.uploaderRole)
        put("business_name", reel.businessName)
        put("business_category", reel.businessCategory)
        put("business_phone", reel.businessPhone)
        put("business_whatsapp", reel.businessWhatsapp)
        put("video_url", reel.videoUrl)
        put("r2_object_key", "")
        put("title", reel.title)
        put("caption", reel.caption)
        put("likes", reel.likes)
        put("views", reel.views)
        put("created_at", iso(reel.createdAt))
        put("expires_at", iso(reel.expiresAt))
    })

    fun upsertFollow(userId: String, businessId: String, active: Boolean) {
        if (active) {
            post("follows", JSONObject().apply {
                put("user_id", userId)
                put("business_id", businessId)
            })
        } else {
            delete("follows", "user_id=eq.$userId&business_id=eq.$businessId")
        }
    }

    fun upsertLike(userId: String, reelId: String, active: Boolean) {
        if (active) {
            post("reel_likes", JSONObject().apply {
                put("user_id", userId)
                put("reel_id", reelId)
            })
        } else {
            delete("reel_likes", "user_id=eq.$userId&reel_id=eq.$reelId")
        }
    }

    fun upsertChat(message: ChatMessage) = post("chat_messages", JSONObject().apply {
        put("id", message.id)
        put("chat_id", message.chatId)
        put("business_id", message.businessId)
        put("sender_id", message.senderId)
        put("sender_name", message.senderName)
        put("message", message.text)
        put("is_from_owner", message.isFromOwner)
        put("created_at", iso(message.timestamp))
    })

    fun deleteById(table: String, id: String) = delete(table, "id=eq.$id")

    private fun post(table: String, body: JSONObject) {
        if (!isConfigured) return
        scope.launch {
            request("POST", table, body.toString())
        }
    }

    private fun delete(table: String, query: String) {
        if (!isConfigured) return
        scope.launch { request("DELETE", "$table?$query", null) }
    }

    private fun request(method: String, path: String, body: String?) {
        try {
            val base = BuildConfig.SUPABASE_URL.trimEnd('/')
            val connection = URL("$base/rest/v1/$path").openConnection() as HttpURLConnection
            connection.requestMethod = method
            connection.connectTimeout = 15_000
            connection.readTimeout = 15_000
            connection.setRequestProperty("apikey", BuildConfig.SUPABASE_PUBLISHABLE_KEY)
            connection.setRequestProperty("Authorization", "Bearer ${BuildConfig.SUPABASE_PUBLISHABLE_KEY}")
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Prefer", "resolution=merge-duplicates,return=minimal")
            if (body != null) {
                connection.doOutput = true
                connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            }
            val code = connection.responseCode
            if (code !in 200..299) {
                val error = connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
                Log.w(TAG, "Supabase $method $path failed: HTTP $code $error")
            }
            connection.disconnect()
        } catch (e: Exception) {
            Log.w(TAG, "Supabase sync skipped: ${e.message}")
        }
    }

    private fun iso(time: Long): String = ISO_FORMAT.format(time)

    private fun parseDate(value: String): String? = try {
        SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).apply { isLenient = false }
            .parse(value.trim())?.let { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(it) }
    } catch (_: Exception) { null }

    companion object {
        private const val TAG = "SupabaseSync"
        private val ISO_FORMAT = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        @Volatile private var instance: SupabaseSync? = null
        fun getInstance(): SupabaseSync = instance ?: synchronized(this) {
            instance ?: SupabaseSync().also { instance = it }
        }
    }
}
