# HMT (Himmatnagar)

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

## 2. Android App Connection

The native Android app (`com.aistudio.sheherhimmatnagar.kznvxp`) connects to Supabase using:
- **Supabase URL:** `https://your-project.supabase.co`
- **Supabase Anon Key:** Your public anonymous key

> ⚠️ **CRITICAL SECURITY NOTE:**
> NEVER embed the `service_role` key into the Android app or APK. The `service_role` key bypasses all Row Level Security (RLS) policies. Only use the public `anon` key.

---

## 3. Auto-Delete Expiry Strategy

Reels in the app are promotional announcements intended to stay fresh. The `reels` table has an automated 10-day expiry mechanism:
- When a reel is inserted, a PostgreSQL trigger sets `expires_at = created_at + INTERVAL '10 days'`.
- The pg_cron job in `cron.sql` periodically calls `public.delete_expired_reels()`.
- Client-side, expired reels are filtered out by `Offer.isExpired()` and `Reel.isExpired()`.
