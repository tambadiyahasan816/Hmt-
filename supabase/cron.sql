-- ====================================================================
-- PG_CRON SCHEDULE FOR HOURLY EXPIRED REELS CLEANUP (STRICT 10-DAY EXPIRY)
-- Deletes BOTH video file in 'reels-videos' bucket and database row in 'reels'
-- ====================================================================

CREATE EXTENSION IF NOT EXISTS "pg_cron";
CREATE EXTENSION IF NOT EXISTS "pg_net";

-- 1. Database fallback cleanup function: deletes reels where expires_at <= NOW()
-- (Used as secondary safety net in case Edge Function network is unavailable)
CREATE OR REPLACE FUNCTION public.delete_expired_reels_db()
RETURNS INT AS $$
DECLARE
    deleted_count INT;
BEGIN
    DELETE FROM public.reels
    WHERE expires_at <= NOW();
    
    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    RETURN deleted_count;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 2. Primary hourly cron job: Invokes Supabase Edge Function 'delete-expired-reels'
-- Runs at minute 0 of every hour: '0 * * * *'
-- Deletes storage object from 'reels-videos' bucket AND database row idempotently.
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

