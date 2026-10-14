package com.example.data.supabase

import com.example.BuildConfig

/**
 * Supabase client configuration loaded safely from BuildConfig / environment properties.
 *
 * Requirements:
 * - Public anon/public key only.
 * - Never use, print, or store any service_role key or secret key.
 * - Do not hardcode secrets in source files.
 */
object SupabaseConfig {

    private const val PLACEHOLDER_URL = "https://your-project-id.supabase.co"
    private const val PLACEHOLDER_KEY = "your-anon-public-key"

    val supabaseUrl: String
        get() = try {
            val raw = BuildConfig.SUPABASE_URL.trim().removeSurrounding("\"")
            if (raw.endsWith("/rest/v1/")) {
                raw.removeSuffix("rest/v1/")
            } else if (raw.endsWith("/rest/v1")) {
                raw.removeSuffix("rest/v1")
            } else {
                raw
            }
        } catch (e: Throwable) {
            ""
        }

    val supabaseAnonKey: String
        get() = try {
            BuildConfig.SUPABASE_ANON_KEY.trim().removeSurrounding("\"")
        } catch (e: Throwable) {
            ""
        }

    const val DEFAULT_SYNC_PROFILE_ENDPOINT = "https://enlwxchhfducvjebkkgu.supabase.co/functions/v1/sync-profile"

    val syncProfileUrl: String
        get() = try {
            val fromConfig = BuildConfig.SUPABASE_SYNC_PROFILE_URL.trim().removeSurrounding("\"")
            if (fromConfig.isNotBlank()) fromConfig else DEFAULT_SYNC_PROFILE_ENDPOINT
        } catch (e: Throwable) {
            DEFAULT_SYNC_PROFILE_ENDPOINT
        }

    /**
     * Checks whether the Supabase public client configuration is provided with real
     * non-placeholder credentials.
     */
    val isConfigured: Boolean
        get() {
            val url = supabaseUrl
            val key = supabaseAnonKey
            return url.isNotBlank() &&
                    url != PLACEHOLDER_URL &&
                    !url.contains("your-project-id") &&
                    url.startsWith("https://") &&
                    key.isNotBlank() &&
                    key != PLACEHOLDER_KEY &&
                    !key.contains("your-anon-public-key")
        }
}
