# HMT (Himmatnagar)

Native Android mobile application built with **Kotlin** and **Jetpack Compose** (Material 3).

HMT is a hyper-local business discovery and community platform for Himmatnagar, Gujarat. It features verified local shops, 10-day expiring promotional reels, daily flash deals/coupons, a local worker directory (plumbers, electricians, mechanics, painters, tutors), and direct customer-to-shop-owner chat.

---

## Tech Stack & Architecture

- **Language:** Kotlin 2.x
- **UI Framework:** Jetpack Compose (Material Design 3)
- **Architecture:** MVVM (Model-View-ViewModel) + Repository Pattern
- **Reactive State:** Kotlin Coroutines & `StateFlow` (`collectAsStateWithLifecycle`)
- **Local Persistence:** Room Database (`HimmatnagarDatabase`) + `SharedPreferences` (`LocalPreferences`)
- **Authentication:** Android Credential Manager + Firebase Auth (Google Sign-In with Form-Entered Phone)
- **Image Loading:** Coil (`coil-compose`)
- **Testing:** JUnit 4 + Robolectric (JVM-based CUJ testing)
- **Build System:** Gradle (Kotlin DSL - `.gradle.kts`) with Version Catalog (`libs.versions.toml`)

---

## App Screens & Features

1. **Role Selection & Registration (`AuthScreens`)**:
   - Roles: **Business Owner**, **Local Worker**, **Customer**, or **Guest mode**.
   - Form-based registration collecting full name, mobile number, shop details, or trade service.
   - One-tap "Continue with Google" securely links verified Google display name, email, and Firebase UID.
   - Entered phone number is saved directly from user input (no fake numbers, no phone from Google).
   - Duplicate account prevention: returning users with the same Google account automatically load their existing role and profile.

2. **Home Feed (`HomeScreen`)**:
   - City header banner, category filter pills, hot deals carousel with "View all" redirection, and local businesses feed.

3. **Hot Deals & Offers (`OffersScreen`)**:
   - Dedicated deals showcase displaying all active discounts in Himmatnagar with category filter, discount percentage badges, coupon code copying, and direct shop navigation.

4. **10-Day Expiring Reels (`ReelsScreen`)**:
   - Full-screen vertical swipe reels feed with visual countdown expiry badges.
   - Like, view count, direct chat with the owner, and WhatsApp contact.
   - Verified shop owners can upload new promotional reels.

5. **Local Workers Directory (`WorkerDirectoryScreen`)**:
   - Community-curated directory of electricians, plumbers, carpenters, painters, tutors, mechanics, etc.
   - Direct call and WhatsApp buttons with zero middleman fees.

6. **Shop Profile (`BusinessProfileScreen`)**:
   - Complete store catalog, active discount coupon codes, verified badge, GPS coordinates, explicit owner phone & WhatsApp contact cards, and direct chat.

7. **Direct Messaging (`ChatScreen`)**:
   - Direct real-time chat between customers and local shop owners.

8. **User Dashboard (`ProfileDashboardScreen`)**:
   - Customer view: Saved favorite shops, liked reels, claimed coupons, and phone number update dialog.
   - Owner dashboard: Live metrics (views, followers, active offers), deal creator with price validation, reel uploader, store editor.
   - Worker profile: View and edit trade details and contact information.

9. **Admin Panel (`AdminPanelScreen`)**:
   - Moderate listings, approve new shops, delete inactive stores, and purge expired reels.

---

## Getting Started & Building the App

### Prerequisites
- Android Studio Ladybug (2024.2+) or newer
- JDK 17 or JDK 21
- Android SDK 36 (or 34+)

### Build with Gradle
To compile and assemble the debug APK:
```bash
gradle assembleDebug
```
Output location:
`app/build/outputs/apk/debug/app-debug.apk`

### Running Unit & Robolectric Tests
```bash
gradle :app:testDebugUnitTest
```

### Generating a Release Bundle (AAB)
```bash
gradle bundleRelease
```
Output location:
`app/build/outputs/bundle/release/app-release.aab`

---

## Local-Only Data & Persistence Architecture

The application operates as a standalone, privacy-first, local-only Android app:
- **Local Persistence:** Room Database (`HimmatnagarDatabase`) and synchronous `LocalPreferences` (`SharedPreferences`) handle device storage.
- **Profiles & Registrations:** Customer, Business Owner, and Worker registrations are saved locally on the device.
- **Reels & Media:** Promotional video reels are stored and played locally from device URIs with automatic 10-day local expiry tracking.
- **Authentication:** Firebase Google Sign-In authenticates users securely without requiring external database sync.

For the complete technical audit and release checklist, refer to [APP_AUDIT.md](./APP_AUDIT.md).

