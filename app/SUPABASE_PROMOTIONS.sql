-- ============================================================================
-- Supabase Schema for Custom Admin Promotions
-- Table: public.promotions
-- ============================================================================

create table if not exists public.promotions (
    id text primary key,
    brand_name text not null default '',
    title text not null default '',
    description text not null default '',
    image_url text not null default '',
    target_url text not null default '',
    placement text not null check (placement in ('LOGIN', 'HOME', 'REELS', 'OFFERS')),
    start_at timestamptz not null default now(),
    end_at timestamptz not null default (now() + interval '7 days'),
    is_active boolean not null default true,
    is_approved boolean not null default true,
    priority integer not null default 1,
    views integer not null default 0,
    clicks integer not null default 0,
    created_by text not null default '',
    created_at timestamptz not null default now()
);

-- Index for querying active and approved promotions by placement and schedule
create index if not exists idx_promotions_placement_active on public.promotions (placement, is_active, is_approved, start_at, end_at);

-- Index for priority ordering
create index if not exists idx_promotions_priority on public.promotions (priority desc);

-- Enable Row Level Security
alter table public.promotions enable row level security;

-- Grant PostgREST Data API permissions to the anon role
grant select, insert, update, delete on public.promotions to anon;

-- Safe policy recreation to avoid duplicate-policy errors
drop policy if exists "hmt dev promotions access" on public.promotions;

create policy "hmt dev promotions access"
    on public.promotions
    for all
    to anon
    using (true)
    with check (true);
