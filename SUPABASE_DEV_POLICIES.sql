-- HMT development bridge
-- Firebase remains the login provider in the Android app.
-- IMPORTANT: These anon policies are for development/testing only.
-- Before public launch, replace them with a verified Firebase-token backend/Edge Function.

-- The tables were already created with RLS enabled. Grant Data API access:
grant select, insert, update, delete on public.profiles to anon;
grant select, insert, update, delete on public.businesses to anon;
grant select, insert, update, delete on public.workers to anon;
grant select, insert, update, delete on public.offers to anon;
grant select, insert, update, delete on public.reels to anon;
grant select, insert, update, delete on public.follows to anon;
grant select, insert, update, delete on public.reel_likes to anon;
grant select, insert, update, delete on public.chat_messages to anon;

create policy "hmt dev profiles" on public.profiles for all to anon using (true) with check (true);
create policy "hmt dev businesses" on public.businesses for all to anon using (true) with check (true);
create policy "hmt dev workers" on public.workers for all to anon using (true) with check (true);
create policy "hmt dev offers" on public.offers for all to anon using (true) with check (true);
create policy "hmt dev reels" on public.reels for all to anon using (true) with check (true);
create policy "hmt dev follows" on public.follows for all to anon using (true) with check (true);
create policy "hmt dev reel likes" on public.reel_likes for all to anon using (true) with check (true);
create policy "hmt dev chat messages" on public.chat_messages for all to anon using (true) with check (true);
