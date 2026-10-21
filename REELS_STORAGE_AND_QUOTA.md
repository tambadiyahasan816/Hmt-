# Reels Local Storage, Quota, Expiry & Purge System
## HMT Android Application

This document specifies the complete local-only architecture, data rules, and quota management for the 10-day expiring reels system in HMT.

---

## 1. Core Business Rules

1. **Eligible Roles**:
   - Only **BUSINESS OWNER** and **WORKER** roles can upload reels.
   - **CUSTOMER** can NEVER upload reels (upload button hidden, upload rejected in ViewModel and Repository).

2. **Monthly Upload Quota (3 Reels / Month)**:
   - Max 3 reels per calendar month in **Asia/Kolkata** timezone.
   - Quota consumes ONLY upon successful reel creation.
   - Cancelled or failed uploads DO NOT consume quota.
   - Deleting an old reel DOES NOT restore the quota.
   - Quota persists synchronously in `LocalPreferences` on the device.

3. **Strict 10-Day Expiry**:
   - `expires_at = created_at + 10 days` (calculated at publish time).
   - Reels past `expires_at` are automatically filtered in the Android UI (feed, business profile, worker profile, owner dashboard, and search results).
   - If opened via deep link, expired reels display a prominent "Expired" status banner.

4. **Local Purge / Cleanup**:
   - `purgeExpiredReels(currentTime)` safely filters out expired reels from active state.
   - Admin panel provides a 1-tap action to purge expired reels.
   - Operates idempotently without errors.

---

## 2. Local Model Specification

```kotlin
data class Reel(
    val id: String,
    val businessId: String = "",
    val workerId: String = "",
    val uploaderId: String = "",
    val uploaderRole: String = "", // "OWNER" or "WORKER"
    val businessName: String,
    val businessCategory: String,
    val businessPhone: String,
    val businessWhatsapp: String,
    val videoUrl: String = "", // Local file / content URI if recorded/picked
    val title: String,
    val caption: String,
    val likes: Int = 0,
    val views: Int = 0,
    val createdAt: Long = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(2),
    val expiresAt: Long = createdAt + TimeUnit.DAYS.toMillis(10), // strictly 10 days
    val gradientColorIndex: Int = 0
)
```

---

## 3. Video Playback & Handling

- Video playback uses AndroidX Media3 (`ExoPlayer`) when a valid local video URI is provided.
- Attractive gradient background fallbacks with typography and category branding are used when rendering previews or reels without local media files.
