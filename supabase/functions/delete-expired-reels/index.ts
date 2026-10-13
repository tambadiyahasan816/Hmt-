// ====================================================================
// SUPABASE EDGE FUNCTION: delete-expired-reels
// Automatically deletes 10-day expired reels and their storage videos
// Runs at least hourly via pg_cron. Safe, idempotent & transaction-ready.
// ====================================================================

import { serve } from "https://deno.land/std@0.168.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2.39.7";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
};

interface ExpiredReel {
  id: string;
  storage_bucket: string | null;
  storage_path: string | null;
  video_url: string;
  title: string;
  expires_at: string;
}

serve(async (req: Request) => {
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  try {
    const supabaseUrl = Deno.env.get("SUPABASE_URL");
    const supabaseServiceRoleKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY");

    if (!supabaseUrl || !supabaseServiceRoleKey) {
      throw new Error("SUPABASE_URL and SUPABASE_SERVICE_ROLE_KEY environment variables are required.");
    }

    // Create admin client with service_role to bypass RLS for server maintenance
    const supabase = createClient(supabaseUrl, supabaseServiceRoleKey);

    const nowIso = new Date().toISOString();
    console.log(`[Auto-Delete Hourly Job] Checking expired reels before ${nowIso}...`);

    // 1. Fetch expired reels in batches of 100
    const { data: expiredReels, error: fetchError } = await supabase
      .from("reels")
      .select("id, storage_bucket, storage_path, video_url, title, expires_at")
      .lte("expires_at", nowIso)
      .limit(100);

    if (fetchError) {
      console.error("[Auto-Delete Fetch Error]:", fetchError);
      throw fetchError;
    }

    if (!expiredReels || expiredReels.length === 0) {
      console.log("[Auto-Delete] No expired reels found. Cleanup completed.");
      return new Response(
        JSON.stringify({ message: "No expired reels found", deletedCount: 0, deletedFilesCount: 0 }),
        { headers: { ...corsHeaders, "Content-Type": "application/json" }, status: 200 }
      );
    }

    console.log(`[Auto-Delete] Found ${expiredReels.length} expired reels to purge.`);

    let filesPurged = 0;
    const successfullyCleanedReelIds: string[] = [];
    const failedReelIds: string[] = [];

    // Group files by bucket to remove in batch
    const bucketFilesMap = new Map<string, { path: string; reelId: string }[]>();

    for (const reel of expiredReels as ExpiredReel[]) {
      const bucket = reel.storage_bucket || "reels-videos";
      let path = reel.storage_path;

      // Fallback if storage_path was missing: extract from video_url
      if (!path && reel.video_url && reel.video_url.includes("reels-videos/")) {
        const parts = reel.video_url.split("reels-videos/");
        if (parts[1]) {
          path = parts[1];
        }
      }

      if (path) {
        if (!bucketFilesMap.has(bucket)) {
          bucketFilesMap.set(bucket, []);
        }
        bucketFilesMap.get(bucket)!.push({ path, reelId: reel.id });
      } else {
        // No file associated or path empty; safe to delete DB record directly
        successfullyCleanedReelIds.push(reel.id);
      }
    }

    // 2. Delete storage files per bucket
    for (const [bucket, items] of bucketFilesMap.entries()) {
      const pathsToDelete = items.map((i) => i.path);
      console.log(`[Auto-Delete] Removing ${pathsToDelete.length} files from bucket '${bucket}'...`);

      const { data: removedData, error: storageError } = await supabase.storage
        .from(bucket)
        .remove(pathsToDelete);

      if (storageError) {
        console.error(`[Auto-Delete Storage Error in bucket ${bucket}]:`, storageError);
        // Do NOT delete DB rows if storage removal fails! Mark for retry next hourly run.
        items.forEach((i) => failedReelIds.push(i.reelId));
      } else {
        // Storage removal succeeded (or files were already deleted/404 - idempotent)
        filesPurged += pathsToDelete.length;
        items.forEach((i) => successfullyCleanedReelIds.push(i.reelId));
        console.log(`[Auto-Delete] Successfully purged ${pathsToDelete.length} storage files from '${bucket}'.`);
      }
    }

    // 3. Delete reel records from database only for verified storage removals
    let deletedDbRows = 0;
    if (successfullyCleanedReelIds.length > 0) {
      const { error: deleteError } = await supabase
        .from("reels")
        .delete()
        .in("id", successfullyCleanedReelIds);

      if (deleteError) {
        console.error("[Auto-Delete DB Delete Error]:", deleteError);
        throw deleteError;
      }
      deletedDbRows = successfullyCleanedReelIds.length;
      console.log(`[Auto-Delete] Successfully deleted ${deletedDbRows} expired reel rows from database.`);
    }

    if (failedReelIds.length > 0) {
      console.warn(`[Auto-Delete Warning] ${failedReelIds.length} reels failed storage deletion and will be retried next run.`);
    }

    return new Response(
      JSON.stringify({
        success: true,
        purgedCount: deletedDbRows,
        filesPurged: filesPurged,
        failedCount: failedReelIds.length,
        purgedAt: nowIso,
        idempotent: true
      }),
      { headers: { ...corsHeaders, "Content-Type": "application/json" }, status: 200 }
    );
  } catch (error: any) {
    console.error("[Auto-Delete Error]:", error);
    return new Response(
      JSON.stringify({ error: error.message || "Failed to purge expired reels" }),
      { headers: { ...corsHeaders, "Content-Type": "application/json" }, status: 500 }
    );
  }
});
