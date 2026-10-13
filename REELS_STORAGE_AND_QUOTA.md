# Reels Storage, Quota, Expiry & Server Cleanup System
## HMT Android Application

This document specifies the complete production architecture, database schema, security policies, storage setup, edge function deployment, and cron automation for the 10-day expiring reels and monthly uploader quota system.

---

## 1. Core Business Rules

1. **Eligible Roles**:
   - Only **BUSINESS OWNER** and **WORKER** roles can upload reels.
   - **CUSTOMER** can NEVER upload reels (upload button hidden, API rejected, RLS denied).

2. **Monthly Upload Quota (3 Reels / Month)**:
   - Max 3 reels per calendar month in **Asia/Kolkata** timezone.
   - Quota consumes ONLY upon successful storage upload and database record creation.
   - Cancelled or failed uploads DO NOT consume quota.
   - Deleting an old reel DOES NOT restore the quota.
   - Quota persists in the `reel_upload_usage` ledger table even after expired reels are deleted.

3. **Strict 10-Day Expiry**:
   - `expires_at = created_at + 10 days` (calculated strictly from server-side timestamp).
   - Phone/device clock is never trusted for expiry.
   - Reels past `expires_at` are immediately filtered in the Android UI (feed, business profile, worker profile, owner dashboard, search results, and deep-link screen).

4. **Server-Side Cleanup (Hourly)**:
   - Runs at minute 0 of every hour (`0 * * * *`).
   - Queries reels where `expires_at <= NOW()`.
   - Deletes video object from the private `reels-videos` Supabase storage bucket using exact `storage_path`.
   - Deletes database row only after storage file removal succeeds.
   - Idempotent and safe against network retries.

---

## 2. Exact Database Migration

Execute this SQL migration in your Supabase SQL Editor:

```sql
-- ====================================================================
-- 1. REELS TABLE DEFINITION WITH EXACT STORAGE PATHS & TIMESTAMPS
-- ====================================================================
CREATE TABLE IF NOT EXISTS public.reels (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID REFERENCES public.businesses(id) ON DELETE CASCADE,
    worker_id UUID REFERENCES public.workers(id) ON DELETE CASCADE,
    uploader_id TEXT NOT NULL,
    uploader_role TEXT NOT NULL CHECK (uploader_role IN ('owner', 'worker', 'admin')),
    storage_bucket TEXT NOT NULL DEFAULT 'reels-videos',
    storage_path TEXT NOT NULL,
    video_url TEXT NOT NULL,
    thumbnail_url TEXT,
    title TEXT NOT NULL,
    caption TEXT,
    views INT NOT NULL DEFAULT 0,
    likes INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMPTZ NOT NULL DEFAULT (NOW() + INTERVAL '10 days')
);

-- Trigger ensuring strictly 10 days expiry on every reel insert
CREATE OR REPLACE FUNCTION set_reel_expiry()
RETURNS TRIGGER AS $$
BEGIN
    NEW.expires_at := NEW.created_at + INTERVAL '10 days';
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_set_reel_expiry ON public.reels;
CREATE TRIGGER trigger_set_reel_expiry
BEFORE INSERT ON public.reels
FOR EACH ROW
EXECUTE FUNCTION set_reel_expiry();

-- ====================================================================
-- 2. REEL UPLOAD USAGE LEDGER TABLE (ASIA/KOLKATA MONTH BOUNDARY)
-- ====================================================================
CREATE TABLE IF NOT EXISTS public.reel_upload_usage (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    uploader_id TEXT NOT NULL,
    year_month TEXT NOT NULL, -- Asia/Kolkata boundary format: 'YYYY-MM'
    upload_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT unique_uploader_year_month UNIQUE (uploader_id, year_month)
);

CREATE INDEX IF NOT EXISTS idx_reels_expires_at ON public.reels(expires_at);
CREATE INDEX IF NOT EXISTS idx_reels_uploader ON public.reels(uploader_id, created_at);
CREATE INDEX IF NOT EXISTS idx_reel_usage_uploader_month ON public.reel_upload_usage(uploader_id, year_month);

-- ====================================================================
-- 3. TRANSACTIONAL STORED PROCEDURE FOR ATOMIC QUOTA ENFORCEMENT
-- ====================================================================
CREATE OR REPLACE FUNCTION public.publish_reel_with_quota(
    p_uploader_id TEXT,
    p_uploader_role TEXT,
    p_title TEXT,
    p_caption TEXT,
    p_storage_bucket TEXT,
    p_storage_path TEXT,
    p_video_url TEXT,
    p_business_id UUID DEFAULT NULL,
    p_worker_id UUID DEFAULT NULL
)
RETURNS JSONB AS $$
DECLARE
    v_year_month TEXT;
    v_current_count INT := 0;
    v_new_reel_id UUID;
    v_created_at TIMESTAMPTZ := NOW();
    v_expires_at TIMESTAMPTZ := NOW() + INTERVAL '10 days';
BEGIN
    -- 1. Strictly forbid customer uploads
    IF p_uploader_role NOT IN ('owner', 'worker', 'admin') THEN
        RAISE EXCEPTION 'Unauthorized: Customers are not permitted to publish reels.';
    END IF;

    -- 2. Compute Asia/Kolkata calendar month boundary ('YYYY-MM')
    v_year_month := TO_CHAR(NOW() AT TIME ZONE 'Asia/Kolkata', 'YYYY-MM');

    -- 3. Lock usage row for atomic transaction
    INSERT INTO public.reel_upload_usage (uploader_id, year_month, upload_count)
    VALUES (p_uploader_id, v_year_month, 0)
    ON CONFLICT (uploader_id, year_month) DO NOTHING;

    SELECT upload_count INTO v_current_count
    FROM public.reel_upload_usage
    WHERE uploader_id = p_uploader_id AND year_month = v_year_month
    FOR UPDATE;

    IF v_current_count >= 3 THEN
        RAISE EXCEPTION 'Monthly reel limit reached. You can upload again next month.';
    END IF;

    -- 4. Insert reel record with exact storage paths and 10-day expiry
    INSERT INTO public.reels (
        business_id, worker_id, uploader_id, uploader_role,
        storage_bucket, storage_path, video_url,
        title, caption, created_at, expires_at
    ) VALUES (
        p_business_id, p_worker_id, p_uploader_id, p_uploader_role,
        COALESCE(p_storage_bucket, 'reels-videos'), p_storage_path, p_video_url,
        p_title, p_caption, v_created_at, v_expires_at
    ) RETURNING id INTO v_new_reel_id;

    -- 5. Increment usage count
    UPDATE public.reel_upload_usage
    SET upload_count = upload_count + 1, updated_at = NOW()
    WHERE uploader_id = p_uploader_id AND year_month = v_year_month;

    RETURN jsonb_build_object(
        'success', true,
        'reel_id', v_new_reel_id,
        'remaining_quota', 3 - (v_current_count + 1),
        'created_at', v_created_at,
        'expires_at', v_expires_at
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;
```

---

## 3. Storage Bucket Setup & RLS Policies

Create the dedicated private storage bucket and configure Row Level Security:

```sql
-- 1. Create dedicated private bucket
INSERT INTO storage.buckets (id, name, public)
VALUES ('reels-videos', 'reels-videos', false)
ON CONFLICT (id) DO NOTHING;

-- 2. Storage RLS Policies
CREATE POLICY "Active reel videos can be read"
ON storage.objects FOR SELECT
USING (
    bucket_id = 'reels-videos' AND (
        EXISTS (
            SELECT 1 FROM public.reels
            WHERE reels.storage_path = storage.objects.name
            AND reels.expires_at > NOW()
        ) OR
        auth.role() = 'service_role' OR
        is_admin()
    )
);

CREATE POLICY "Owners and workers can upload reel videos"
ON storage.objects FOR INSERT
WITH CHECK (
    bucket_id = 'reels-videos' AND (
        auth.role() = 'service_role' OR
        EXISTS (
            SELECT 1 FROM public.profiles
            WHERE id = auth.uid() AND role IN ('owner', 'worker')
        )
    )
);

CREATE POLICY "Service role can delete expired reel videos"
ON storage.objects FOR DELETE
USING (
    bucket_id = 'reels-videos' AND (
        auth.role() = 'service_role' OR
        is_admin()
    )
);
```

---

## 4. Edge Function Deployment

The edge function `delete-expired-reels` safely deletes storage files and database rows in batches:

1. Install Supabase CLI:
   ```bash
   npm install -g supabase
   ```
2. Link your project:
   ```bash
   supabase link --project-ref <your-supabase-project-id>
   ```
3. Deploy the Edge Function:
   ```bash
   supabase functions deploy delete-expired-reels --no-verify-jwt
   ```
4. Configure required secrets in Supabase Dashboard (Settings -> Edge Functions):
   - `SUPABASE_URL`: `https://<your-project-id>.supabase.co`
   - `SUPABASE_SERVICE_ROLE_KEY`: Service role secret (kept secure on server)

---

## 5. Hourly pg_cron Job Setup

Schedule the cleanup to execute hourly:

```sql
CREATE EXTENSION IF NOT EXISTS "pg_cron";
CREATE EXTENSION IF NOT EXISTS "pg_net";

SELECT cron.schedule(
    'delete-expired-reels-hourly',
    '0 * * * *',
    $$
    SELECT net.http_post(
        url := current_setting('app.settings.supabase_url', true) || '/functions/v1/delete-expired-reels',
        headers := jsonb_build_object(
            'Content-Type', 'application/json',
            'Authorization', 'Bearer ' || current_setting('app.settings.service_role_key', true)
        ),
        body := '{}'::jsonb
    );
    $$
);
```

---

## 6. Security Warnings & Billing Considerations

> [!CAUTION]
> **NEVER SHIP THE SERVICE-ROLE KEY IN THE ANDROID APK**:
> The `service_role` key bypasses all Row Level Security. It MUST ONLY be configured inside the Supabase Edge Function environment variables. The Android client only uses the public `anon` key or Firebase Auth tokens.

> [!WARNING]
> **STORAGE BILLING PLAN**:
> Video uploads consume storage and network egress. Ensure your Supabase organization is on an appropriate tier (e.g., Supabase Pro) to handle monthly video quotas without service disruption.

---

## 7. Testing Instructions

Run unit and Robolectric tests:
```bash
gradle :app:testDebugUnitTest
```
Tests verify:
- Customer upload rejection
- Owner & Worker upload success
- 4th upload rejection in same Asia/Kolkata month
- Quota reset to 3 upon new month
- No quota consumed on upload failure or cancellation
- Deleted reels do not restore quota
- Expiry after strictly 10 days
- Expired reel hidden from feed, business profile, worker profile, search, and deep-link
- Idempotent cleanup execution
