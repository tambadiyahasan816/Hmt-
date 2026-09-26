-- ====================================================================
-- PG_CRON SCHEDULE FOR AUTO-DELETING EXPIRED REELS (AFTER 10 DAYS)
-- Sheher Himmatnagar Platform
-- ====================================================================

-- Enable pg_cron and pg_net extensions
CREATE EXTENSION IF NOT EXISTS "pg_cron";
CREATE EXTENSION IF NOT EXISTS "pg_net";

-- 1. Direct database cleanup function: deletes reels where expires_at < now()
CREATE OR REPLACE FUNCTION public.delete_expired_reels()
RETURNS INT AS $$
DECLARE
    deleted_count INT;
BEGIN
    DELETE FROM public.reels
    WHERE expires_at < NOW();
    
    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    RETURN deleted_count;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 2. Schedule pg_cron job to execute daily at midnight (00:00 UTC)
SELECT cron.schedule(
    'delete-expired-reels-daily',
    '0 0 * * *',
    $$ SELECT public.delete_expired_reels(); $$
);

-- 3. (Optional) Alternatively, invoke Supabase Edge Function to clean up Storage files as well:
-- SELECT cron.schedule(
--     'invoke-delete-expired-reels-function',
--     '0 0 * * *',
--     $$
--     SELECT net.http_post(
--         url := 'https://<PROJECT-REF>.supabase.co/functions/v1/delete-expired-reels',
--         headers := jsonb_build_object(
--             'Content-Type', 'application/json',
--             'Authorization', 'Bearer ' || current_setting('app.settings.service_role_key')
--         ),
--         body := '{}'::jsonb
--     );
--     $$
-- );
