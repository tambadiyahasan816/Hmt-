package com.example.data.supabase

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * PostgREST HTTP interface for read-only queries against Supabase tables.
 *
 * Honors Row Level Security (RLS) automatically:
 * - Approved businesses: SELECT USING (is_approved = true OR auth.uid() = owner_id OR is_admin())
 * - Public workers: SELECT USING (true)
 * - Active reels: SELECT USING (expires_at > NOW() OR is_admin())
 * - Public offers: SELECT USING (true)
 */
interface SupabaseRestService {

    @GET("rest/v1/businesses")
    suspend fun getBusinesses(
        @Query("select") select: String = "*",
        @Query("is_approved") isApproved: String = "eq.true",
        @Query("order") order: String = "created_at.desc"
    ): List<SupabaseBusinessDto>

    @GET("rest/v1/workers")
    suspend fun getWorkers(
        @Query("select") select: String = "*",
        @Query("order") order: String = "created_at.desc"
    ): List<SupabaseWorkerDto>

    @GET("rest/v1/reels")
    suspend fun getActiveReels(
        @Query("select") select: String = "*",
        @Query("order") order: String = "created_at.desc"
    ): List<SupabaseReelDto>

    @GET("rest/v1/offers")
    suspend fun getOffers(
        @Query("select") select: String = "*",
        @Query("order") order: String = "created_at.desc"
    ): List<SupabaseOfferDto>
}
