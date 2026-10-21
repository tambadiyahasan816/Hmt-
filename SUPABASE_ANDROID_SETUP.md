# HMT Supabase setup

Firebase Google login remains unchanged. This project sends app records to Supabase using the Supabase Data API. R2 is intentionally not included yet.

## 1. Add the two build values

In the Android Studio project root, open `gradle.properties` and add:

```properties
SUPABASE_URL=https://YOUR_PROJECT_REF.supabase.co
SUPABASE_PUBLISHABLE_KEY=sb_publishable_YOUR_KEY
```

Use the URL and **Publishable key** from Supabase Connect. Do not use `sb_secret_...` or `service_role` in the Android app.

If AI Studio regenerates `gradle.properties`, add these two lines again before building.

## 2. Run the SQL policies once

Open Supabase **SQL Editor → New query**, paste the contents of `SUPABASE_DEV_POLICIES.sql`, and press Run. The app uses Firebase Auth rather than Supabase Auth, so this is a development bridge that allows the mobile client to write through the Data API.

This bridge is suitable for development/testing only. Before a public launch, replace it with a server/Edge Function that verifies the Firebase ID token and writes with server-side credentials. Never ship a Supabase secret/service-role key in the APK.

## 3. Build

```bash
gradle assembleDebug
```

When the two Gradle properties are empty, the app still builds and continues using local Room/SharedPreferences. When they are present, these actions sync in the background:

- Firebase-registered profiles
- business owners and businesses
- workers
- offers
- reel metadata (title, caption, owner, dates, likes and views)
- follows and reel likes
- chat messages
- deletes and profile edits

The actual video file is deliberately not uploaded yet. `video_url` remains empty/local until the Cloudflare R2 step is added.

## 4. Upload permissions

The existing app rule remains active: customers cannot upload reels. Only `OWNER`, `WORKER`, and the existing admin path can publish reel metadata. Monthly quota, 60-second limit, 25 MB limit, and 10-day expiry remain unchanged.
