# Sheher Himmatnagar (શહેર હિંમતનગર)

Local business discovery platform, Instagram-style reels feed with 10-day auto-expiry, and deals finder for the city of Himmatnagar, Gujarat.

---

## 1. Supabase Backend Setup

### A. Run SQL Migration
1. Open your Supabase Dashboard -> **SQL Editor**.
2. Copy and execute `/supabase/schema.sql`. This sets up:
   - `profiles` table with Phone OTP role mapping (`customer`, `owner`, `admin`)
   - `businesses` table with GPS coordinates and approval flags
   - `reels` table with automated 10-day expiry trigger (`expires_at = created_at + interval '10 days'`)
   - `offers` discount finder table
   - `workers` community directory
   - `chats` and `messages` real-time messaging
   - Complete Row Level Security (RLS) policies protecting tables.
3. (Optional) Run `/supabase/seed.sql` to populate with 8 authentic Himmatnagar shops, active deals, and workers.

### B. Deploy 10-Day Auto-Delete Edge Function
Run in your terminal using Supabase CLI:
```bash
supabase functions deploy delete-expired-reels --no-verify-jwt
```

### C. Schedule Daily Auto-Delete Cron Job
In your Supabase SQL Editor:
```sql
-- Execute content from /supabase/cron.sql
SELECT cron.schedule(
    'delete-expired-reels-daily',
    '0 0 * * *',
    $$ SELECT public.delete_expired_reels(); $$
);
```

---

## 2. Web Deployment (Next.js / Vercel)

1. Clone or export repository.
2. Configure `.env.local`:
   ```env
   NEXT_PUBLIC_SUPABASE_URL=https://your-project.supabase.co
   NEXT_PUBLIC_SUPABASE_ANON_KEY=your-anon-key
   ```
3. Deploy to Vercel:
   ```bash
   vercel --prod
   ```

---

## 3. Capacitor & Android APK Build

1. Build web assets:
   ```bash
   npm run build
   npx cap sync android
   ```
2. Open Android Studio:
   ```bash
   npx cap open android
   ```
3. Or build directly with Gradle:
   ```bash
   gradle assembleRelease
   ```

---

## 4. Publishing to Google Play Store

1. **Keystore Signing**:
   Generate an upload keystore:
   ```bash
   keytool -genkey -v -keystore himmatnagar-release.jks -alias himmatnagar -keyalg RSA -keysize 2048 -validity 10000
   ```
2. **Build Android App Bundle (AAB)**:
   ```bash
   gradle bundleRelease
   ```
   The `.aab` output will be at `app/build/outputs/bundle/release/app-release.aab`.
3. **Google Play Console Setup**:
   - Create app under "Sheher Himmatnagar"
   - Category: "Shopping" or "Local & Lifestyle"
   - Content Rating: Everyone
   - Privacy Policy URL: host privacy policy detailing camera/phone permissions.
   - Upload `app-release.aab` under Production / Internal Testing track.
