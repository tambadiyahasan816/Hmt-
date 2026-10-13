package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.UserProfile
import com.example.data.model.UserRole

/**
 * LocalPreferences handles instant synchronous persistence for:
 * - Current logged in user (Customer, Business Owner, Worker, Admin)
 * - Followed business IDs
 * - Liked reel IDs
 *
 * Ensures data is preserved across application restarts.
 */
class LocalPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("sheher_himmatnagar_prefs", Context.MODE_PRIVATE)

    fun saveCurrentUser(user: UserProfile?) {
        prefs.edit().apply {
            if (user == null) {
                remove("user_id")
                remove("user_firebase_uid")
                remove("user_name")
                remove("user_phone")
                remove("user_email")
                remove("user_google_display_name")
                remove("user_role")
                remove("user_created_at")
                remove("user_updated_at")
            } else {
                putString("user_id", user.id)
                putString("user_firebase_uid", user.firebaseUid)
                putString("user_name", user.name)
                putString("user_phone", user.phone)
                putString("user_email", user.email)
                putString("user_google_display_name", user.googleDisplayName)
                putString("user_role", user.role.name)
                putLong("user_created_at", user.createdAt)
                putLong("user_updated_at", user.updatedAt)
            }
            apply()
        }
    }

    fun loadCurrentUser(): UserProfile? {
        val id = prefs.getString("user_id", null) ?: return null
        val firebaseUid = prefs.getString("user_firebase_uid", id) ?: id
        val name = prefs.getString("user_name", "") ?: ""
        val phone = prefs.getString("user_phone", "") ?: ""
        val email = prefs.getString("user_email", "") ?: ""
        val googleDisplayName = prefs.getString("user_google_display_name", "") ?: ""
        val roleStr = prefs.getString("user_role", UserRole.CUSTOMER.name) ?: UserRole.CUSTOMER.name
        val role = try {
            UserRole.valueOf(roleStr)
        } catch (e: Exception) {
            UserRole.CUSTOMER
        }
        val createdAt = prefs.getLong("user_created_at", System.currentTimeMillis())
        val updatedAt = prefs.getLong("user_updated_at", createdAt)
        return UserProfile(
            id = id,
            firebaseUid = firebaseUid,
            name = name,
            phone = phone,
            email = email,
            googleDisplayName = googleDisplayName,
            role = role,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    fun saveRegisteredUsers(users: List<UserProfile>) {
        val encoded = users.joinToString(";;;") { u ->
            "${u.id}:::${u.firebaseUid}:::${u.name}:::${u.phone}:::${u.email}:::${u.role.name}:::${u.createdAt}:::${u.updatedAt}:::${u.googleDisplayName}"
        }
        prefs.edit().putString("all_registered_users", encoded).apply()
    }

    fun loadRegisteredUsers(): List<UserProfile> {
        val raw = prefs.getString("all_registered_users", null) ?: return emptyList()
        if (raw.isBlank()) return emptyList()
        return raw.split(";;;").mapNotNull { entry ->
            val parts = entry.split(":::")
            if (parts.size >= 8) {
                val role = try { UserRole.valueOf(parts[5]) } catch (e: Exception) { UserRole.CUSTOMER }
                val gDisplayName = if (parts.size >= 9) parts[8] else ""
                UserProfile(
                    id = parts[0],
                    firebaseUid = parts[1],
                    name = parts[2],
                    phone = parts[3],
                    email = parts[4],
                    googleDisplayName = gDisplayName,
                    role = role,
                    createdAt = parts[6].toLongOrNull() ?: System.currentTimeMillis(),
                    updatedAt = parts[7].toLongOrNull() ?: System.currentTimeMillis()
                )
            } else null
        }
    }

    fun saveFollowedBusinesses(ids: Set<String>) {
        prefs.edit().putStringSet("followed_business_ids", ids).apply()
    }

    fun loadFollowedBusinesses(): Set<String> {
        return prefs.getStringSet("followed_business_ids", emptySet())?.toSet() ?: emptySet()
    }

    fun saveLikedReels(ids: Set<String>) {
        prefs.edit().putStringSet("liked_reel_ids", ids).apply()
    }

    fun loadLikedReels(): Set<String> {
        return prefs.getStringSet("liked_reel_ids", emptySet())?.toSet() ?: emptySet()
    }

    // Monthly Reel Quota Tracker (Max 3 per calendar month in Asia/Kolkata)
    fun getAsiaKolkataYearMonth(timestamp: Long = System.currentTimeMillis()): String {
        val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("Asia/Kolkata"))
        cal.timeInMillis = timestamp
        val year = cal.get(java.util.Calendar.YEAR)
        val month = cal.get(java.util.Calendar.MONTH) + 1
        return String.format(java.util.Locale.US, "%04d-%02d", year, month)
    }

    fun getMonthlyReelUploadCount(uploaderId: String, yearMonth: String = getAsiaKolkataYearMonth()): Int {
        val key = "reel_quota_${uploaderId}_$yearMonth"
        return prefs.getInt(key, 0)
    }

    fun incrementMonthlyReelUploadCount(uploaderId: String, yearMonth: String = getAsiaKolkataYearMonth()): Int {
        val current = getMonthlyReelUploadCount(uploaderId, yearMonth)
        val newCount = current + 1
        val key = "reel_quota_${uploaderId}_$yearMonth"
        prefs.edit().putInt(key, newCount).apply()
        return newCount
    }

    fun saveMonthlyReelUploadCount(uploaderId: String, yearMonth: String, count: Int) {
        val key = "reel_quota_${uploaderId}_$yearMonth"
        prefs.edit().putInt(key, count).apply()
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }
}
