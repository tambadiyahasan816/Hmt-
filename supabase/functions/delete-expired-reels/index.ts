// ====================================================================
// SUPABASE EDGE FUNCTION: delete-expired-reels
// Automatically deletes 10-day expired reels and their storage videos
// ====================================================================

import { serve } from "https://deno.land/std@0.168.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2.39.7";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
};

serve(async (req: Request) => {
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  try {
    const supabaseUrl = Deno.env.get("SUPABASE_URL")!;
    const supabaseServiceRoleKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!;

    // Create admin client with service_role to bypass RLS for maintenance
    const supabase = createClient(supabaseUrl, supabaseServiceRoleKey);

    const nowIso = new Date().toISOString();
    console.log(`[Auto-Delete] Checking expired reels before ${nowIso}...`);

    // 1. Fetch expired reels
    const { data: expiredReels, error: fetchError } = await supabase
      .from("reels")
      .select("id, video_url, title")
      .lt("expires_at", nowIso);

    if (fetchError) {
      throw fetchError;
    }

    if (!expiredReels || expiredReels.length === 0) {
      console.log("[Auto-Delete] No expired reels found.");
      return new Response(
        JSON.stringify({ message: "No expired reels found", deletedCount: 0 }),
        { headers: { ...corsHeaders, "Content-Type": "application/json" }, status: 200 }
      );
    }

    console.log(`[Auto-Delete] Found ${expiredReels.length} expired reels to purge.`);

    // 2. Delete storage files
    const filePathsToDelete: string[] = [];
    for (const reel of expiredReels) {
      if (reel.video_url && reel.video_url.includes("reels-videos/")) {
        const parts = reel.video_url.split("reels-videos/");
        if (parts[1]) {
          filePathsToDelete.push(parts[1]);
        }
      }
    }

    if (filePathsToDelete.length > 0) {
      const { error: storageError } = await supabase.storage
        .from("reels-videos")
        .remove(filePathsToDelete);

      if (storageError) {
        console.error("[Auto-Delete Storage Error]:", storageError);
      } else {
        console.log(`[Auto-Delete] Successfully removed ${filePathsToDelete.length} video files from storage.`);
      }
    }

    // 3. Delete reel records from database
    const expiredIds = expiredReels.map((r) => r.id);
    const { error: deleteError } = await supabase
      .from("reels")
      .delete()
      .in("id", expiredIds);

    if (deleteError) {
      throw deleteError;
    }

    console.log(`[Auto-Delete] Successfully purged ${expiredIds.length} expired reel rows.`);

    return new Response(
      JSON.stringify({
        success: true,
        purgedCount: expiredIds.length,
        filesPurged: filePathsToDelete.length,
        purgedAt: nowIso,
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
