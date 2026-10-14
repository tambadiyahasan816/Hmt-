-- ====================================================================
-- PHASE 1A: PROFILES & DATA INTEGRATION MIGRATION (NON-DESTRUCTIVE)
-- ====================================================================

-- 1. Ensure profiles table has email, firebase_uid, avatar_url, updated_at
ALTER TABLE public.profiles ADD COLUMN IF NOT EXISTS email TEXT;
ALTER TABLE public.profiles ADD COLUMN IF NOT EXISTS firebase_uid TEXT;
ALTER TABLE public.profiles ADD COLUMN IF NOT EXISTS avatar_url TEXT;
ALTER TABLE public.profiles ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ DEFAULT NOW();

-- 2. Performance indexes
CREATE INDEX IF NOT EXISTS idx_profiles_firebase_uid ON public.profiles(firebase_uid);
CREATE INDEX IF NOT EXISTS idx_businesses_owner_id ON public.businesses(owner_id);
CREATE INDEX IF NOT EXISTS idx_offers_business_id ON public.offers(business_id);
CREATE INDEX IF NOT EXISTS idx_workers_profile_id ON public.workers(profile_id);
