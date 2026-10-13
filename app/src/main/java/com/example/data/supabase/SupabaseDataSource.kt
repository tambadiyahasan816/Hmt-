package com.example.data.supabase

import com.example.data.model.Business
import com.example.data.model.Offer
import com.example.data.model.Reel
import com.example.data.model.Worker
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Supabase client and data-source layer for read-only access to public Himmatnagar data.
 *
 * Characteristics:
 * 1. Safe public configuration: Uses anonymous/public key only via apiKey and Authorization headers.
 * 2. Never bypasses Row Level Security (RLS).
 * 3. Does not replace Firebase Google Login (auth remains strictly Firebase).
 * 4. Provides methods to read public businesses, public workers, active reels, and public offers.
 */
class SupabaseDataSource(
    private val baseUrl: String = SupabaseConfig.supabaseUrl,
    private val anonKey: String = SupabaseConfig.supabaseAnonKey
) {

    private val isReady: Boolean
        get() = SupabaseConfig.isConfigured && baseUrl.isNotBlank() && anonKey.isNotBlank()

    private val restService: SupabaseRestService? by lazy {
        if (!isReady) null else createService(baseUrl, anonKey)
    }

    private fun createService(url: String, key: String): SupabaseRestService {
        val normalizedUrl = if (url.endsWith("/")) url else "$url/"

        val headerInterceptor = Interceptor { chain ->
            val original = chain.request()
            val request = original.newBuilder()
                .header("apikey", key)
                .header("Authorization", "Bearer $key")
                .header("Accept", "application/json")
                .build()
            chain.proceed(request)
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(headerInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(normalizedUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        return retrofit.create(SupabaseRestService::class.java)
    }

    suspend fun fetchPublicBusinesses(): Result<List<Business>> = runCatching {
        val service = restService ?: throw IllegalStateException("Supabase client is not configured")
        val dtoList = service.getBusinesses()
        dtoList.map { dto ->
            Business(
                id = dto.id,
                ownerId = dto.ownerId,
                name = dto.name,
                category = dto.category,
                phone = dto.phone,
                whatsapp = dto.whatsapp,
                area = dto.area,
                address = dto.address,
                lat = dto.lat,
                lng = dto.lng,
                bio = dto.bio ?: "",
                imageUrl = dto.imageUrl ?: "",
                coverUrl = dto.coverUrl ?: "",
                isApproved = dto.isApproved
            )
        }
    }

    suspend fun fetchPublicWorkers(): Result<List<Worker>> = runCatching {
        val service = restService ?: throw IllegalStateException("Supabase client is not configured")
        val dtoList = service.getWorkers()
        dtoList.map { dto ->
            Worker(
                id = dto.id,
                profileId = dto.profileId ?: "",
                name = dto.name,
                serviceType = dto.serviceType,
                phone = dto.phone,
                area = dto.area,
                experienceYears = dto.experienceYears ?: "",
                photoUrl = dto.photoUrl ?: "",
                rating = (dto.rating ?: 0.0).toFloat()
            )
        }
    }

    suspend fun fetchActiveReels(): Result<List<Reel>> = runCatching {
        val service = restService ?: throw IllegalStateException("Supabase client is not configured")
        val dtoList = service.getActiveReels()
        val now = System.currentTimeMillis()
        dtoList.mapNotNull { dto ->
            val createdAtMillis = parseIsoTimestamp(dto.createdAt) ?: (now - TimeUnit.DAYS.toMillis(1))
            val expiresAtMillis = parseIsoTimestamp(dto.expiresAt) ?: (createdAtMillis + TimeUnit.DAYS.toMillis(10))

            // RLS and client filter: active reels only
            if (expiresAtMillis <= now) return@mapNotNull null

            Reel(
                id = dto.id,
                businessId = dto.businessId ?: "",
                workerId = dto.workerId ?: "",
                uploaderId = dto.uploaderId,
                uploaderRole = dto.uploaderRole,
                businessName = dto.title,
                businessCategory = "Himmatnagar",
                businessPhone = "",
                businessWhatsapp = "",
                storageBucket = dto.storageBucket,
                storagePath = dto.storagePath,
                videoUrl = dto.videoUrl,
                title = dto.title,
                caption = dto.caption ?: "",
                likes = dto.likes,
                views = dto.views,
                createdAt = createdAtMillis,
                expiresAt = expiresAtMillis
            )
        }
    }

    suspend fun fetchPublicOffers(): Result<List<Offer>> = runCatching {
        val service = restService ?: throw IllegalStateException("Supabase client is not configured")
        val dtoList = service.getOffers()
        dtoList.map { dto ->
            Offer(
                id = dto.id,
                businessId = dto.businessId,
                businessName = "",
                area = "",
                productName = dto.productName,
                category = "",
                originalPrice = dto.originalPrice,
                discountedPrice = dto.discountedPrice,
                discountPercent = dto.discountPercent,
                couponCode = dto.couponCode,
                validUntil = dto.validUntil,
                imageUrl = dto.imageUrl ?: ""
            )
        }
    }

    private fun parseIsoTimestamp(isoString: String?): Long? {
        if (isoString.isNullOrBlank()) return null
        return try {
            val format = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
            format.timeZone = java.util.TimeZone.getTimeZone("UTC")
            val clean = if (isoString.contains(".")) isoString.substringBefore(".") else isoString.substringBefore("+").substringBefore("Z")
            format.parse(clean)?.time
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Calls the deployed Supabase Edge Function to sync the Firebase user profile:
     * - Endpoint: https://enlwxchhfducvjebkkgu.supabase.co/functions/v1/sync-profile
     * - Headers:
     *     Authorization: Bearer <Firebase ID token>
     *     Content-Type: application/json
     *     apikey: <anonKey> (if configured)
     * - Body:
     *     role, name, phone, email, avatar_url
     *
     * Never sends or exposes any service-role key.
     */
    suspend fun syncProfileWithEdgeFunction(
        firebaseIdToken: String,
        role: String,
        name: String,
        phone: String,
        email: String,
        avatarUrl: String? = null,
        endpointUrl: String = SupabaseConfig.syncProfileUrl
    ): Result<SyncProfileResponse> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        runCatching {
            if (firebaseIdToken.isBlank()) {
                throw IllegalArgumentException("Firebase ID token is blank or invalid")
            }

            val targetUrl = if (endpointUrl.isNotBlank()) endpointUrl else SupabaseConfig.DEFAULT_SYNC_PROFILE_ENDPOINT

            val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
            val requestAdapter = moshi.adapter(SyncProfileRequest::class.java)
            val responseAdapter = moshi.adapter(SyncProfileResponse::class.java)

            val payload = SyncProfileRequest(
                role = role,
                name = name,
                phone = phone,
                email = email,
                avatarUrl = if (avatarUrl.isNullOrBlank()) null else avatarUrl
            )
            val jsonBody = requestAdapter.toJson(payload)

            val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
            val requestBody = jsonBody.toRequestBody(mediaType)

            val requestBuilder = okhttp3.Request.Builder()
                .url(targetUrl)
                .header("Authorization", "Bearer $firebaseIdToken")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .post(requestBody)

            if (anonKey.isNotBlank()) {
                requestBuilder.header("apikey", anonKey)
            }

            val client = OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build()

            val response = client.newCall(requestBuilder.build()).execute()
            val responseBodyString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val errorMsg = try {
                    val parsed = responseAdapter.fromJson(responseBodyString)
                    parsed?.error ?: parsed?.message ?: "HTTP ${response.code}: ${response.message}"
                } catch (_: Exception) {
                    "HTTP ${response.code}: ${responseBodyString.ifBlank { response.message }}"
                }
                throw IllegalStateException(errorMsg)
            }

            val parsedResponse = try {
                responseAdapter.fromJson(responseBodyString)
            } catch (e: Exception) {
                SyncProfileResponse(success = true, message = responseBodyString)
            } ?: SyncProfileResponse(success = true)

            if (!parsedResponse.success && !parsedResponse.error.isNullOrBlank()) {
                throw IllegalStateException(parsedResponse.error)
            }

            parsedResponse
        }
    }
}
