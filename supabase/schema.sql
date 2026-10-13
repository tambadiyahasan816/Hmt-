-- ====================================================================
-- SHEHER HIMMATNAGAR — SUPABASE DATABASE SCHEMA WITH FULL RLS POLICIES
-- Platform for local shops, 10-day auto-deleting reels, workers & deals
-- ====================================================================

-- 1. EXTENSIONS
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_cron";

-- 2. ENUMS
-- FIX 2: Updated role = 'customer' | 'owner' | 'worker' | 'admin'
DO $$ BEGIN
    CREATE TYPE user_role AS ENUM ('customer', 'owner', 'worker', 'admin');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- 3. PROFILES TABLE
-- One phone number = one account. Role is checked on every protected action.
CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    phone TEXT NOT NULL UNIQUE,
    role user_role NOT NULL DEFAULT 'customer',
    name TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 4. BUSINESSES TABLE
-- Local shops & businesses in Himmatnagar, Gujarat
CREATE TABLE IF NOT EXISTS public.businesses (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    owner_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    category TEXT NOT NULL,
    phone TEXT NOT NULL,
    whatsapp TEXT NOT NULL,
    area TEXT NOT NULL, -- Motipura, Mahavirnagar, Station Road, Tower Chowk, etc.
    address TEXT NOT NULL,
    lat DOUBLE PRECISION NOT NULL DEFAULT 23.5977,
    lng DOUBLE PRECISION NOT NULL DEFAULT 72.9667,
    bio TEXT,
    image_url TEXT,
    cover_url TEXT,
    is_approved BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 5. BUSINESS POSTS TABLE
CREATE TABLE IF NOT EXISTS public.business_posts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES public.businesses(id) ON DELETE CASCADE,
    image_url TEXT NOT NULL,
    caption TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 6. REELS TABLE (CRITICAL AUTO-DELETE SCHEMA)
-- expires_at is automatically calculated as created_at + 10 days
-- Uploader can be BUSINESS OWNER or WORKER. CUSTOMER CAN NEVER UPLOAD.
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

-- Trigger to enforce strictly 10 days expiry on every reel insert
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

-- 6b. REEL UPLOAD USAGE LEDGER TABLE (MAX 3 REELS PER CALENDAR MONTH ASIA/KOLKATA)
-- Preserves upload counts even after expired reels are deleted
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

-- 6c. SERVER-SIDE TRANSACTION FUNCTION FOR REEL QUOTA & ATOMIC INSERT
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
    -- 1. Role validation: Customer rejected!
    IF p_uploader_role NOT IN ('owner', 'worker', 'admin') THEN
        RAISE EXCEPTION 'Unauthorized: Customers are not permitted to publish reels.';
    END IF;

    -- 2. Asia/Kolkata calendar month boundary
    v_year_month := TO_CHAR(NOW() AT TIME ZONE 'Asia/Kolkata', 'YYYY-MM');

    -- 3. Lock row in usage ledger for atomic transaction
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

    -- 5. Consume quota upon successful publish
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

-- 7. OFFERS TABLE (DISCOUNT FINDER)
CREATE TABLE IF NOT EXISTS public.offers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES public.businesses(id) ON DELETE CASCADE,
    product_name TEXT NOT NULL,
    original_price NUMERIC(10,2) NOT NULL,
    discounted_price NUMERIC(10,2) NOT NULL,
    discount_percent INT NOT NULL,
    coupon_code TEXT NOT NULL,
    valid_until TEXT NOT NULL,
    image_url TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 8. WORKERS TABLE (FIX 2: workers directory for plumbers, electricians, tutors, mechanics, etc.)
CREATE TABLE IF NOT EXISTS public.workers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    profile_id UUID REFERENCES public.profiles(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    service_type TEXT NOT NULL, -- Plumber, Electrician, Carpenter, Painter, Mechanic, Tutor, etc.
    phone TEXT NOT NULL,
    area TEXT NOT NULL,
    experience_years TEXT,
    photo_url TEXT,
    rating NUMERIC(2,1) DEFAULT 0.0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 9. FOLLOWS TABLE
CREATE TABLE IF NOT EXISTS public.follows (
    follower_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    business_id UUID NOT NULL REFERENCES public.businesses(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (follower_id, business_id)
);

-- 10. CHATS AND MESSAGES TABLE
CREATE TABLE IF NOT EXISTS public.chats (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    customer_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    business_id UUID NOT NULL REFERENCES public.businesses(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (customer_id, business_id)
);

CREATE TABLE IF NOT EXISTS public.messages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    chat_id UUID NOT NULL REFERENCES public.chats(id) ON DELETE CASCADE,
    sender_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    text TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ====================================================================
-- ROW LEVEL SECURITY (RLS) POLICIES
-- ====================================================================

ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.businesses ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.business_posts ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.reels ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.offers ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.workers ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.follows ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.chats ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.messages ENABLE ROW LEVEL SECURITY;

-- Helper function: check if user is admin
CREATE OR REPLACE FUNCTION is_admin()
RETURNS BOOLEAN AS $$
    SELECT EXISTS (
        SELECT 1 FROM public.profiles
        WHERE id = auth.uid() AND role = 'admin'
    );
$$ LANGUAGE sql SECURITY DEFINER;

-- PROFILES POLICIES
CREATE POLICY "Public profiles are viewable by everyone" ON public.profiles
    FOR SELECT USING (true);

CREATE POLICY "Users can update own profile" ON public.profiles
    FOR UPDATE USING (auth.uid() = id);

-- BUSINESSES POLICIES
CREATE POLICY "Approved businesses are viewable by everyone" ON public.businesses
    FOR SELECT USING (is_approved = true OR auth.uid() = owner_id OR is_admin());

CREATE POLICY "Business owners can insert business" ON public.businesses
    FOR INSERT WITH CHECK (
        EXISTS (
            SELECT 1 FROM public.profiles
            WHERE id = auth.uid() AND role IN ('owner', 'admin')
        )
    );

CREATE POLICY "Business owners can update own business" ON public.businesses
    FOR UPDATE USING (auth.uid() = owner_id OR is_admin());

CREATE POLICY "Admins or owners can delete business" ON public.businesses
    FOR DELETE USING (auth.uid() = owner_id OR is_admin());

-- BUSINESS POSTS POLICIES
CREATE POLICY "Posts viewable by everyone" ON public.business_posts
    FOR SELECT USING (true);

CREATE POLICY "Only shop owners can insert posts" ON public.business_posts
    FOR INSERT WITH CHECK (
        EXISTS (
            SELECT 1 FROM public.businesses
            WHERE id = business_id AND owner_id = auth.uid()
        )
    );

-- REELS POLICIES: BUSINESS OWNER & WORKER ONLY. CUSTOMERS REJECTED!
CREATE POLICY "Active reels viewable by everyone" ON public.reels
    FOR SELECT USING (expires_at > NOW() OR is_admin());

CREATE POLICY "Owners and workers can post reels" ON public.reels
    FOR INSERT WITH CHECK (
        (
            -- Business Owner post for their own shop
            EXISTS (
                SELECT 1 FROM public.businesses
                WHERE id = business_id AND owner_id = auth.uid()
            ) AND
            EXISTS (
                SELECT 1 FROM public.profiles
                WHERE id = auth.uid() AND role = 'owner'
            )
        ) OR (
            -- Worker post for their own listing
            EXISTS (
                SELECT 1 FROM public.workers
                WHERE (id = worker_id OR profile_id = auth.uid()) AND profile_id = auth.uid()
            ) AND
            EXISTS (
                SELECT 1 FROM public.profiles
                WHERE id = auth.uid() AND role = 'worker'
            )
        ) OR is_admin()
    );

CREATE POLICY "Owners and workers can delete own reels" ON public.reels
    FOR DELETE USING (
        (business_id IS NOT NULL AND EXISTS (
            SELECT 1 FROM public.businesses
            WHERE id = reels.business_id AND owner_id = auth.uid()
        )) OR
        (worker_id IS NOT NULL AND EXISTS (
            SELECT 1 FROM public.workers
            WHERE (id = reels.worker_id OR profile_id = auth.uid()) AND profile_id = auth.uid()
        )) OR
        uploader_id = auth.uid()::text OR
        is_admin()
    );

-- REEL UPLOAD USAGE LEDGER POLICIES
ALTER TABLE public.reel_upload_usage ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view own reel usage" ON public.reel_upload_usage
    FOR SELECT USING (uploader_id = auth.uid()::text OR is_admin());

-- STORAGE POLICIES FOR 'reels-videos' BUCKET
-- Private bucket: only authorized owners and workers can upload, users can stream active reels
INSERT INTO storage.buckets (id, name, public)
VALUES ('reels-videos', 'reels-videos', false)
ON CONFLICT (id) DO NOTHING;

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

CREATE POLICY "Service role and owners can delete expired reel videos"
ON storage.objects FOR DELETE
USING (
    bucket_id = 'reels-videos' AND (
        auth.role() = 'service_role' OR
        is_admin()
    )
);

-- OFFERS POLICIES (Only business owners can create offers)
CREATE POLICY "Offers viewable by everyone" ON public.offers
    FOR SELECT USING (true);

CREATE POLICY "Only business owners can create offers" ON public.offers
    FOR INSERT WITH CHECK (
        EXISTS (
            SELECT 1 FROM public.businesses
            WHERE id = business_id AND owner_id = auth.uid()
        )
    );

CREATE POLICY "Owners can delete own offers" ON public.offers
    FOR DELETE USING (
        EXISTS (
            SELECT 1 FROM public.businesses
            WHERE id = offers.business_id AND owner_id = auth.uid()
        ) OR is_admin()
    );

-- WORKERS POLICIES (Viewable by everyone, workers can insert/update own profile)
CREATE POLICY "Workers directory viewable by everyone" ON public.workers
    FOR SELECT USING (true);

CREATE POLICY "Workers can register and update profile" ON public.workers
    FOR INSERT WITH CHECK (true);

CREATE POLICY "Workers can update own profile" ON public.workers
    FOR UPDATE USING (profile_id = auth.uid() OR is_admin());

-- FOLLOWS POLICIES
CREATE POLICY "Follows viewable by everyone" ON public.follows
    FOR SELECT USING (true);

CREATE POLICY "Users can manage own follows" ON public.follows
    FOR ALL USING (auth.uid() = follower_id);

-- CHATS & MESSAGES POLICIES
CREATE POLICY "Participants can view chats" ON public.chats
    FOR SELECT USING (
        auth.uid() = customer_id OR
        EXISTS (
            SELECT 1 FROM public.businesses
            WHERE id = business_id AND owner_id = auth.uid()
        )
    );

CREATE POLICY "Customers can create chats" ON public.chats
    FOR INSERT WITH CHECK (auth.uid() = customer_id);

CREATE POLICY "Participants can view messages" ON public.messages
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM public.chats
            WHERE id = chat_id AND (customer_id = auth.uid() OR EXISTS (
                SELECT 1 FROM public.businesses
                WHERE id = business_id AND owner_id = auth.uid()
            ))
        )
    );

CREATE POLICY "Participants can insert messages" ON public.messages
    FOR INSERT WITH CHECK (
        auth.uid() = sender_id AND
        EXISTS (
            SELECT 1 FROM public.chats
            WHERE id = chat_id AND (customer_id = auth.uid() OR EXISTS (
                SELECT 1 FROM public.businesses
                WHERE id = business_id AND owner_id = auth.uid()
            ))
        )
    );
