-- ====================================================================
-- SHEHER HIMMATNAGAR — SUPABASE DATABASE SCHEMA WITH FULL RLS POLICIES
-- Platform for local shops, 10-day auto-deleting reels, and deals finder
-- ====================================================================

-- 1. EXTENSIONS
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_cron";

-- 2. ENUMS
CREATE TYPE user_role AS ENUM ('customer', 'owner', 'admin');

-- 3. PROFILES TABLE
-- One phone number = one account. Stores role and display details.
CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    phone TEXT NOT NULL UNIQUE,
    role user_role NOT NULL DEFAULT 'customer',
    name TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 4. BUSINESSES TABLE
-- Verified local businesses in Himmatnagar, Gujarat
CREATE TABLE IF NOT EXISTS public.businesses (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    owner_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    category TEXT NOT NULL,
    phone TEXT NOT NULL,
    whatsapp TEXT NOT NULL,
    area TEXT NOT NULL, -- e.g., Motipura, Mahavirnagar, Station Road, Tower Chowk
    address TEXT NOT NULL,
    lat DOUBLE PRECISION NOT NULL DEFAULT 23.5977,
    lng DOUBLE PRECISION NOT NULL DEFAULT 72.9667,
    bio TEXT,
    image_url TEXT,
    cover_url TEXT,
    is_approved BOOLEAN NOT NULL DEFAULT false,
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
CREATE TABLE IF NOT EXISTS public.reels (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES public.businesses(id) ON DELETE CASCADE,
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

-- 8. WORKERS TABLE (LOCAL DIRECTORY)
CREATE TABLE IF NOT EXISTS public.workers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT NOT NULL,
    service_type TEXT NOT NULL, -- e.g., Plumber, Electrician, Tutor
    phone TEXT NOT NULL,
    area TEXT NOT NULL,
    experience TEXT,
    rating NUMERIC(2,1) DEFAULT 4.8,
    created_by UUID REFERENCES public.profiles(id) ON DELETE SET NULL,
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

CREATE POLICY "Users can update their own profile" ON public.profiles
    FOR UPDATE USING (auth.uid() = id);

CREATE POLICY "Users can insert their own profile" ON public.profiles
    FOR INSERT WITH CHECK (auth.uid() = id);

-- BUSINESSES POLICIES
CREATE POLICY "Approved businesses are viewable by everyone" ON public.businesses
    FOR SELECT USING (is_approved = true OR auth.uid() = owner_id OR is_admin());

CREATE POLICY "Owners can insert their own business" ON public.businesses
    FOR INSERT WITH CHECK (
        auth.uid() = owner_id AND
        EXISTS (SELECT 1 FROM public.profiles WHERE id = auth.uid() AND role IN ('owner', 'admin'))
    );

CREATE POLICY "Owners can update their own business" ON public.businesses
    FOR UPDATE USING (auth.uid() = owner_id OR is_admin());

CREATE POLICY "Owners and admins can delete business" ON public.businesses
    FOR DELETE USING (auth.uid() = owner_id OR is_admin());

-- BUSINESS POSTS POLICIES
CREATE POLICY "Posts viewable by everyone" ON public.business_posts
    FOR SELECT USING (true);

CREATE POLICY "Only business owner can insert posts" ON public.business_posts
    FOR INSERT WITH CHECK (
        EXISTS (SELECT 1 FROM public.businesses WHERE id = business_id AND owner_id = auth.uid()) OR is_admin()
    );

CREATE POLICY "Owner can delete their posts" ON public.business_posts
    FOR DELETE USING (
        EXISTS (SELECT 1 FROM public.businesses WHERE id = business_id AND owner_id = auth.uid()) OR is_admin()
    );

-- REELS POLICIES (AUTO-DELETE ENFORCED: Customers can NEVER insert reels)
CREATE POLICY "Non-expired reels are viewable by everyone" ON public.reels
    FOR SELECT USING (expires_at > NOW());

CREATE POLICY "Only business owners can post reels" ON public.reels
    FOR INSERT WITH CHECK (
        (EXISTS (SELECT 1 FROM public.businesses WHERE id = business_id AND owner_id = auth.uid()) AND
         EXISTS (SELECT 1 FROM public.profiles WHERE id = auth.uid() AND role IN ('owner', 'admin'))) OR is_admin()
    );

CREATE POLICY "Owners can delete their reels" ON public.reels
    FOR DELETE USING (
        EXISTS (SELECT 1 FROM public.businesses WHERE id = business_id AND owner_id = auth.uid()) OR is_admin()
    );

-- OFFERS POLICIES
CREATE POLICY "Offers viewable by everyone" ON public.offers
    FOR SELECT USING (true);

CREATE POLICY "Only owners can insert offers" ON public.offers
    FOR INSERT WITH CHECK (
        EXISTS (SELECT 1 FROM public.businesses WHERE id = business_id AND owner_id = auth.uid()) OR is_admin()
    );

CREATE POLICY "Owners can update and delete their offers" ON public.offers
    FOR ALL USING (
        EXISTS (SELECT 1 FROM public.businesses WHERE id = business_id AND owner_id = auth.uid()) OR is_admin()
    );

-- WORKERS DIRECTORY POLICIES
CREATE POLICY "Workers directory is viewable by everyone" ON public.workers
    FOR SELECT USING (true);

CREATE POLICY "Anyone authenticated can add a worker" ON public.workers
    FOR INSERT WITH CHECK (auth.uid() IS NOT NULL);

-- FOLLOWS POLICIES
CREATE POLICY "Anyone can view follows" ON public.follows
    FOR SELECT USING (true);

CREATE POLICY "Users can manage their own follows" ON public.follows
    FOR ALL USING (auth.uid() = follower_id);

-- CHATS & MESSAGES POLICIES
CREATE POLICY "Participants can view their chats" ON public.chats
    FOR SELECT USING (
        auth.uid() = customer_id OR
        EXISTS (SELECT 1 FROM public.businesses WHERE id = business_id AND owner_id = auth.uid()) OR
        is_admin()
    );

CREATE POLICY "Customers can initiate chats" ON public.chats
    FOR INSERT WITH CHECK (auth.uid() = customer_id);

CREATE POLICY "Chat participants can view messages" ON public.messages
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM public.chats c
            WHERE c.id = chat_id AND (
                c.customer_id = auth.uid() OR
                EXISTS (SELECT 1 FROM public.businesses b WHERE b.id = c.business_id AND b.owner_id = auth.uid())
            )
        ) OR is_admin()
    );

CREATE POLICY "Chat participants can send messages" ON public.messages
    FOR INSERT WITH CHECK (
        auth.uid() = sender_id AND
        EXISTS (
            SELECT 1 FROM public.chats c
            WHERE c.id = chat_id AND (
                c.customer_id = auth.uid() OR
                EXISTS (SELECT 1 FROM public.businesses b WHERE b.id = c.business_id AND b.owner_id = auth.uid())
            )
        )
    );
