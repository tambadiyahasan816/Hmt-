package com.example.data.supabase

import retrofit2.Response
import retrofit2.http.*

/**
 * PostgREST HTTP interface for queries and mutations against Supabase tables.
 *
 * Honors Row Level Security (RLS) automatically:
 * - Approved businesses: SELECT USING (is_approved = true OR auth.uid() = owner_id OR is_admin())
 * - Public workers: SELECT USING (true)
 * - Active reels: SELECT USING (expires_at > NOW() OR is_admin())
 * - Public offers: SELECT USING (true)
 */
interface SupabaseRestService {

    // Businesses
    @GET("rest/v1/businesses")
    suspend fun getBusinesses(
        @Query("select") select: String = "*",
        @Query("is_approved") isApproved: String = "eq.true",
        @Query("order") order: String = "created_at.desc"
    ): List<SupabaseBusinessDto>

    @Headers("Prefer: return=representation")
    @POST("rest/v1/businesses")
    suspend fun createBusiness(
        @Body request: CreateBusinessRequest
    ): List<SupabaseBusinessDto>

    @Headers("Prefer: return=representation")
    @PATCH("rest/v1/businesses")
    suspend fun updateBusiness(
        @Query("id") idFilter: String,
        @Body request: UpdateBusinessRequest
    ): List<SupabaseBusinessDto>

    @DELETE("rest/v1/businesses")
    suspend fun deleteBusiness(
        @Query("id") idFilter: String
    ): Response<Unit>

    // Workers
    @GET("rest/v1/workers")
    suspend fun getWorkers(
        @Query("select") select: String = "*",
        @Query("order") order: String = "created_at.desc"
    ): List<SupabaseWorkerDto>

    @Headers("Prefer: return=representation")
    @POST("rest/v1/workers")
    suspend fun createWorker(
        @Body request: CreateWorkerRequest
    ): List<SupabaseWorkerDto>

    @Headers("Prefer: return=representation")
    @PATCH("rest/v1/workers")
    suspend fun updateWorker(
        @Query("id") idFilter: String,
        @Body request: UpdateWorkerRequest
    ): List<SupabaseWorkerDto>

    // Offers
    @GET("rest/v1/offers")
    suspend fun getOffers(
        @Query("select") select: String = "*",
        @Query("order") order: String = "created_at.desc"
    ): List<SupabaseOfferDto>

    @Headers("Prefer: return=representation")
    @POST("rest/v1/offers")
    suspend fun createOffer(
        @Body request: CreateOfferRequest
    ): List<SupabaseOfferDto>

    @DELETE("rest/v1/offers")
    suspend fun deleteOffer(
        @Query("id") idFilter: String
    ): Response<Unit>

    // Reels (read-only query preserved)
    @GET("rest/v1/reels")
    suspend fun getActiveReels(
        @Query("select") select: String = "*",
        @Query("order") order: String = "created_at.desc"
    ): List<SupabaseReelDto>
}
