# HMT (Himmatnagar) — Application Audit Report

**Platform:** Native Android (Kotlin + Jetpack Compose)  
**Package Namespace:** `com.example`  
**Application ID:** `com.aistudio.sheherhimmatnagar.kznvxp`  
**Target SDK:** 36 (Android 15 / 16 Preview) | **Min SDK:** 24 (Android 7.0)

---

## 1. Executive Summary

HMT is a hyper-local community and marketplace application designed specifically for the city of Himmatnagar, Gujarat. It allows local shop owners to showcase their businesses and upload 10-day expiring video reels, helps local workers (plumbers, electricians, mechanics, painters, tutors) list their services for free direct calls, and gives citizens and customers an instant discovery engine for daily discounts, verified shops, and direct chat.

---

## 2. Fixed Bugs & Improvements Implemented

1. **Repository Validation Enforcement (`HimmatnagarRepository.kt`)**:
   - **Reels**: Blank title and blank caption are rejected. Input is strictly trimmed before storage.
   - **Workers**: Blank worker name, service type, mobile phone, or area are rejected. Input is trimmed.
   - **Chat Messages**: Blank or whitespace-only messages are blocked.
   - **Offers / Deals**:
     - Product name, category, and expiry date must not be blank.
     - `originalPrice` must be strictly positive (`> 0`).
     - `discountedPrice` must be strictly positive (`> 0`) and strictly less than `originalPrice`.
     - Coupon codes are automatically trimmed and capitalized (`.uppercase().trim()`).
   - **Authentication**: Whitespace trimmed on customer and business registration.

2. **Cascade Deletion on Business Removal**:
   - When a business is deleted via `deleteBusiness(businessId)`, all associated data is now cascaded and purged:
     - Active discount offers linked to `businessId`.
     - Active 10-day reels linked to `businessId`.
     - Follower associations (`followedBusinessIds`).
     - View counters (`businessViews`).
     - Customer-owner chat threads (`chatMessages`).

3. **Local State Persistence Across App Restarts**:
   - Integrated `LocalPreferences` (`SharedPreferences`) for synchronous session and state restoration upon app launch.
   - Current user profile (`UserProfile`), followed businesses (`followedBusinessIds`), and liked reels (`likedReelIds`) persist across process restarts.
   - Integrated Room Database (`HimmatnagarDatabase`) and `CacheDao` following the architecture guidelines.
   - App automatically navigates returning logged-in users straight to the main discovery tabs instead of forcing role selection on every launch.

4. **Google Sign-In & Authentication Safeguards**:
   - Removed artificial fake phone number fallback (`+91 98250 00000`). Real phone numbers are used when available, or email/user ID.
   - Added explicit, user-friendly exception messages when Firebase Google Sign-In or `google-services.json` is missing or unconfigured.

5. **Production Security & Role Isolation**:
   - `switchRoleForTesting` has been completely purged from production UI, ViewModel, and screen workflows. Admin roles cannot be assigned client-side.
   - Authentication identity is anchored strictly to the unique Firebase UID.
   - Owners can only edit their own shop profile and workers can only update their own skills/profile.
   - Added `updateUserPhone` capability allowing returning users to change their contact number safely.

6. **Google Sign-In with Form-Based Phone Collection**:
   - Replaced demo OTP login with role-specific "Continue with Google" placed directly at the bottom of customer, owner, and worker registration forms.
   - Separate duplicate Google login options removed from role selection and forms.
   - Phone numbers are never guessed or synthesized; they are collected directly from the user's form input and stored with `+91 ` prefix.
   - Google Account provides only Firebase UID, display name, and verified email. Google email is never saved as a phone number.
   - Duplicate prevention: re-logging in with the same Firebase UID identifies and loads the existing profile (CUSTOMER, OWNER, or WORKER) without creating duplicate accounts.
   - Owner's business phone and WhatsApp number entered during registration are clearly visible and clickable on the business profile for all customers.

7. **Removal of Explore Tab & Dedicated Offers Screen**:
   - The Explore tab has been completely removed from bottom navigation (reduced to 4 clean tabs: Home, Reels, Workers, Profile/Dashboard).
   - "View all deals" from the Home deals carousel redirects to a dedicated `OffersScreen` with category filtering, coupon code copying, and direct shop navigation.
   - Smooth back-handling from `OffersScreen` restores the user's tab state seamlessly.

8. **UI States (Loading, Empty, Offline & Error Feedback)**:
   - Added `isOffline`, `isLoading`, and `errorMessage` state flows to `MainViewModel`.
   - Added global animated Offline / Error Banner with a "Retry" button.
   - Preserved all existing screens while ensuring empty states are handled gracefully (empty reels, empty worker search, empty shop search, empty chat).

9. **Unit & Robolectric Test Suite**:
   - Comprehensive test suite covering:
     1. Customer form entered phone is saved.
     2. Owner's business phone is displayed and accessible to customers.
     3. Worker's entered phone is saved.
     4. Google email is never saved as phone number.
     5. Same Firebase UID does not create duplicate user.
     6. Existing user login loads and preserves old data (reels, offers, follows).
     7. Explore tab is completely removed from navigation tabs and ScreenState.
     8. Logout transitions back to the initial login / role selection screen.
     9. Offer validation (positive prices, discount < original, no blank fields, uppercase coupon).
     10. Blank chat messages, reels, and workers are blocked.
     11. Business cascade deletion purges all linked reels, offers, chats, and follows.
     12. App name is verified as "HMT".

---

## 3. Remaining Bugs & Technical Limitations

1. **In-Memory Seed Data vs. Live Cloud Sync**:
   - The app currently seeds 8 authentic Himmatnagar businesses in-memory and caches state locally.
   - While the Supabase SQL schema exists in `/supabase/schema.sql`, live multi-user cloud synchronization requires connecting the Supabase Kotlin SDK or a REST client (Retrofit) with real authentication tokens.
2. **Reel Video Player**:
   - Current reels use animated gradient canvas backdrops with video thumbnails. Integration of AndroidX Media3 (ExoPlayer) with HTTP live streaming is required for actual uploaded `.mp4` video playback.
3. **SMS Gateway for Real OTP**:
   - Phone verification currently runs in demo mode (code `482910`). For production, Firebase Phone Auth or a specialized Indian SMS gateway (e.g. MSG91, Twilio) must be configured.
4. **Google Play Services Dependency for Google Sign-In**:
   - Google Sign-In requires the target device to have Google Play Services installed and the SHA-1 fingerprint registered in the Firebase Console.

---

## 4. Future Features Roadmap

1. **ExoPlayer & CameraX Reel Creator**:
   - Integrate `androidx.camera` and `androidx.media3` to record, trim, and upload real vertical video reels directly from within the app.
2. **Cloudflare Images / Supabase Storage Integration**:
   - Upload shop cover banners, product deal photos, and worker profile avatars to a secure cloud bucket.
3. **Push Notifications (FCM)**:
   - Alert customers when a followed store publishes a flash discount.
   - Notify shop owners immediately when a customer initiates a chat.
4. **Multi-Language (Gujarati ગુજરાતી & Hindi हिन्दी)**:
   - Full localized string resources for Gujarati-speaking traders and residents in Sabarkantha district.
5. **GPS Distance & Map Navigation**:
   - Real-time location calculation ("2.1 km away in Motipura") and sorting by distance from user's current GPS coordinates.
6. **UPI / QR Code Payment Integration**:
   - Direct "Pay with UPI" button on shop profiles and deal cards opening Google Pay, PhonePe, or Paytm.

---

## 5. Supabase Backend Setup Guide

The complete database schema with Row Level Security (RLS) is located at `/supabase/schema.sql`.

### Setup Steps:
1. Create a project at [supabase.com](https://supabase.com).
2. Go to **SQL Editor** -> Click **New Query**.
3. Paste the contents of `/supabase/schema.sql` and execute.
   - Sets up `profiles`, `businesses`, `business_posts`, `reels`, `offers`, `workers`, `follows`, `chats`, and `messages`.
   - Enforces automatic 10-day reel expiry trigger (`set_reel_expiry`).
4. (Optional) Run `/supabase/seed.sql` to populate sample Himmatnagar data.
5. Deploy the auto-delete cron job from `/supabase/cron.sql`:
   ```sql
   SELECT cron.schedule(
       'delete-expired-reels-daily',
       '0 0 * * *',
       $$ SELECT public.delete_expired_reels(); $$
   );
   ```

### ⚠️ CRITICAL SECURITY RULE:
- **NEVER include the Supabase `service_role` key in the Android APK or source code.**
- The `service_role` key completely bypasses Row Level Security (RLS). If embedded in an APK, anyone can reverse-engineer it and delete or alter the entire database.
- The Android app must ONLY use the public `anon` key (`NEXT_PUBLIC_SUPABASE_ANON_KEY` / `SUPABASE_ANON_KEY`). All authorization is enforced by Supabase RLS using the user's JWT token.

---

## 6. Google Sign-In Setup Guide

The app implements modern Android Credential Manager and Firebase Auth in `com.example.auth.GoogleAuthHelper`.

### Setup Steps:
1. Go to [Firebase Console](https://console.firebase.google.com).
2. Create or select your project and click **Add Android app**.
3. Set the package name to:
   ```
   com.aistudio.sheherhimmatnagar.kznvxp
   ```
4. Generate your SHA-1 and SHA-256 fingerprints:
   ```bash
   keytool -list -v -keystore ./debug.keystore -alias androiddebugkey -storepass android -keypass android
   ```
5. Enter the SHA-1 in Firebase Project Settings under your Android app.
6. Navigate to **Authentication** -> **Sign-in method** -> Enable **Google**.
7. In the Google provider setup, copy the **Web SDK Client ID**.
8. Download the updated `google-services.json` file and place it in the `app/` folder of this project.
9. Build and run the app. Google Sign-In will authenticate through Android Credential Manager seamlessly.

---

## 7. Google Play Store Release Checklist

Before submitting the app to the Google Play Console:

- [x] **App Name**: Set to "HMT" (≤ 30 characters, compliant with Play Store metadata policy).
- [x] **Unique Application ID**: Configured as `com.aistudio.sheherhimmatnagar.kznvxp` in `app/build.gradle.kts`.
- [x] **Adaptive Launcher Icon**: Custom icon configured in `res/mipmap-*/ic_launcher.xml` and manifest.
- [x] **Permissions (Least Privilege)**: Declared only required permissions in `AndroidManifest.xml` (`INTERNET`, `VIBRATE`, `ACCESS_NETWORK_STATE`). No broad storage permissions.
- [x] **Testing Switcher Stripped**: Role testing switcher wrapped in `BuildConfig.DEBUG` to ensure no privilege escalation in release APKs.
- [x] **Signing Config**: Keystore configuration configured for release builds.
- [ ] **Release Signing Keystore**: Ensure upload key (`my-upload-key.jks`) is created and environment variables `KEYSTORE_PATH`, `STORE_PASSWORD`, `KEY_PASSWORD` are configured in CI/CD.
- [ ] **Build Release App Bundle (AAB)**:
  ```bash
  gradle bundleRelease
  ```
  The generated AAB is located at `app/build/outputs/bundle/release/app-release.aab`.
- [ ] **Privacy Policy URL**: Host a privacy policy disclosing camera, network, and contact usage.
- [ ] **Content Rating**: Complete Google Play Content Rating Questionnaire (Rated 3+ / Everyone).
