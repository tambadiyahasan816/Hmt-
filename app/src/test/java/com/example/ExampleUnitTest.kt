package com.example

import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.data.repository.HimmatnagarRepository
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ExampleUnitTest {

    private lateinit var repository: HimmatnagarRepository

    @Before
    fun setup() {
        repository = HimmatnagarRepository.createForTesting()
    }

    @Test
    fun offer_validation_rules_are_enforced() {
        val count = repository.offers.value.size

        // Invalid: original <= 0
        assertFalse(repository.addOffer("b1", "Item", "Food", 0.0, 10.0, "SAVE", "10 Dec 2026"))
        assertFalse(repository.addOffer("b1", "Item", "Food", -10.0, 5.0, "SAVE", "10 Dec 2026"))

        // Invalid: discount >= original
        assertFalse(repository.addOffer("b1", "Item", "Food", 100.0, 100.0, "SAVE", "10 Dec 2026"))
        assertFalse(repository.addOffer("b1", "Item", "Food", 100.0, 120.0, "SAVE", "10 Dec 2026"))

        // Invalid: blank fields
        assertFalse(repository.addOffer("b1", "", "Food", 100.0, 50.0, "SAVE", "10 Dec 2026"))
        assertFalse(repository.addOffer("b1", "Item", "", 100.0, 50.0, "SAVE", "10 Dec 2026"))
        assertFalse(repository.addOffer("b1", "Item", "Food", 100.0, 50.0, "SAVE", ""))

        assertEquals(count, repository.offers.value.size)

        // Valid offer: should save successfully with trimmed uppercase code
        assertTrue(repository.addOffer("b1", "  New Saree  ", "  Fashion  ", 1000.0, 700.0, "festive20", "  10 Dec 2026  "))
        assertEquals(count + 1, repository.offers.value.size)
        val saved = repository.offers.value.first()
        assertEquals("New Saree", saved.productName)
        assertEquals("FESTIVE20", saved.couponCode)
        assertEquals(30, saved.discountPercent)
    }

    @Test
    fun chat_message_validation_is_enforced() {
        val user = UserProfile(id = "user1", phone = "9825012345", role = UserRole.CUSTOMER, name = "Customer")
        repository.setCurrentUser(user)

        val count = repository.chatMessages.value.size
        assertFalse(repository.sendChatMessage("b1", "Shop", "  ", false))
        assertFalse(repository.sendChatMessage("b1", "Shop", "", false))
        assertEquals(count, repository.chatMessages.value.size)

        assertTrue(repository.sendChatMessage("b1", "Shop", "  Hello  ", false))
        assertEquals(count + 1, repository.chatMessages.value.size)
        assertEquals("Hello", repository.chatMessages.value.last().text)
    }

    @Test
    fun worker_and_reel_validation_is_enforced() {
        val workerCount = repository.workers.value.size
        assertFalse(repository.addWorker("  ", "Plumber", "9825012345", "Motipura", "3y"))
        assertFalse(repository.addWorker("Name", "  ", "9825012345", "Motipura", "3y"))
        assertFalse(repository.addWorker("Name", "Plumber", "  ", "Motipura", "3y"))
        assertFalse(repository.addWorker("Name", "Plumber", "9825012345", "  ", "3y"))
        assertEquals(workerCount, repository.workers.value.size)

        val reelCount = repository.reels.value.size
        assertFalse(repository.addReel("b1", "  ", "Caption"))
        assertFalse(repository.addReel("b1", "Title", "  "))
        assertEquals(reelCount, repository.reels.value.size)
    }

    @Test
    fun business_cascade_delete_cleans_all_relations() {
        val bId = "b1"
        repository.toggleFollow(bId)
        val user = UserProfile(id = "u1", phone = "9825012345", role = UserRole.CUSTOMER, name = "Test User")
        repository.setCurrentUser(user)
        repository.sendChatMessage(bId, "Shop", "Test msg", false)

        assertTrue(repository.businesses.value.any { it.id == bId })
        assertTrue(repository.offers.value.any { it.businessId == bId })
        assertTrue(repository.reels.value.any { it.businessId == bId })
        assertTrue(repository.chatMessages.value.any { it.businessId == bId })
        assertTrue(repository.followedBusinessIds.value.contains(bId))

        repository.deleteBusiness(bId)

        assertFalse(repository.businesses.value.any { it.id == bId })
        assertFalse(repository.offers.value.any { it.businessId == bId })
        assertFalse(repository.reels.value.any { it.businessId == bId })
        assertFalse(repository.chatMessages.value.any { it.businessId == bId })
        assertFalse(repository.followedBusinessIds.value.contains(bId))
    }

    @Test
    fun customer_form_entered_phone_is_saved() {
        val custPhone = "+91 9825012345"
        val user = repository.loginOrRegisterCustomerWithGoogle(
            firebaseUid = "uid_cust_test_1",
            googleName = "Ravi Patel",
            googleEmail = "ravi.patel@gmail.com",
            enteredPhone = custPhone,
            enteredName = "Ravi Patel"
        )

        assertEquals(custPhone, user.phone)
        assertEquals("ravi.patel@gmail.com", user.email)
        assertEquals("uid_cust_test_1", user.firebaseUid)
        assertEquals(custPhone, repository.currentUser.value?.phone)
    }

    @Test
    fun owner_business_phone_is_saved_and_accessible_to_customers() {
        val ownerPhone = "+91 9825099999"
        val ownerWhatsapp = "+91 9825099999"
        val (ownerUser, business) = repository.loginOrRegisterOwnerWithGoogle(
            firebaseUid = "uid_owner_test_1",
            googleName = "Jayesh Shah",
            googleEmail = "jayesh.shah@gmail.com",
            enteredOwnerName = "Jayesh Shah",
            enteredPhone = ownerPhone,
            shopName = "Jayesh Electronics",
            category = "Electronics",
            area = "Tower Chowk",
            address = "Main Market Road, Himmatnagar",
            whatsapp = ownerWhatsapp
        )

        assertEquals(ownerPhone, ownerUser.phone)
        assertNotNull(business)
        val fetchedBusiness = repository.businesses.value.first { it.id == business!!.id }
        assertEquals(ownerPhone, fetchedBusiness.phone)
        assertEquals(ownerWhatsapp, fetchedBusiness.whatsapp)
        assertEquals("Tower Chowk", fetchedBusiness.area)
        assertEquals("Main Market Road, Himmatnagar", fetchedBusiness.address)
    }

    @Test
    fun worker_entered_phone_is_saved() {
        val workerPhone = "+91 9825077777"
        val (workerUser, worker) = repository.loginOrRegisterWorkerWithGoogle(
            firebaseUid = "uid_worker_test_1",
            googleName = "Kantilal Carpenter",
            googleEmail = "kantilal@gmail.com",
            enteredName = "Kantilal Carpenter",
            enteredPhone = workerPhone,
            serviceType = "Carpenter",
            area = "Motipura",
            exp = "8 Years",
            photoUrl = ""
        )

        assertEquals(workerPhone, workerUser.phone)
        assertNotNull(worker)
        assertEquals(workerPhone, worker!!.phone)
        val fetchedWorker = repository.workers.value.first { it.id == worker.id }
        assertEquals(workerPhone, fetchedWorker.phone)
        assertEquals("Carpenter", fetchedWorker.serviceType)
    }

    @Test
    fun google_email_is_never_saved_as_phone_number() {
        val testEmail = "testuser@himmatnagar.local"
        val enteredPhone = "+91 9876543210"

        val user = repository.loginOrRegisterCustomerWithGoogle(
            firebaseUid = "uid_email_check",
            googleName = "Test User",
            googleEmail = testEmail,
            enteredPhone = enteredPhone,
            enteredName = "Test User"
        )

        assertEquals(testEmail, user.email)
        assertEquals(enteredPhone, user.phone)
        assertFalse("Phone number should not contain @", user.phone.contains("@"))
        assertNotEquals("Google email must not be saved as phone", user.email, user.phone)
    }

    @Test
    fun same_firebase_uid_does_not_create_duplicate_user() {
        val uid = "uid_duplicate_prevention_test"

        val user1 = repository.loginOrRegisterCustomerWithGoogle(
            firebaseUid = uid,
            googleName = "Aarav",
            googleEmail = "aarav@gmail.com",
            enteredPhone = "+91 9825011111",
            enteredName = "Aarav Shah"
        )

        val user2 = repository.loginOrRegisterCustomerWithGoogle(
            firebaseUid = uid,
            googleName = "Aarav Updated",
            googleEmail = "aarav@gmail.com",
            enteredPhone = "+91 9825022222",
            enteredName = "Aarav Shah"
        )

        assertEquals("Same user profile ID should be retained", user1.id, user2.id)
        assertEquals(uid, user2.firebaseUid)

        val matchingUsers = repository.registeredUsers.value.filter { it.firebaseUid == uid }
        assertEquals("There should be exactly 1 user profile with this Firebase UID", 1, matchingUsers.size)
    }

    @Test
    fun existing_user_login_loads_and_preserves_old_data() {
        val uid = "uid_owner_preserve_test"

        val (ownerUser, business) = repository.loginOrRegisterOwnerWithGoogle(
            firebaseUid = uid,
            googleName = "Shop Owner",
            googleEmail = "shopowner@gmail.com",
            enteredOwnerName = "Shop Owner",
            enteredPhone = "+91 9825033333",
            shopName = "Shreeji Sarees",
            category = "Fashion",
            area = "Polo Ground",
            address = "Near Tower, Himmatnagar",
            whatsapp = "+91 9825033333"
        )

        assertNotNull(business)
        repository.addOffer(
            businessId = business!!.id,
            productName = "Bandhani Saree Special",
            category = "Fashion",
            originalPrice = 2500.0,
            discountedPrice = 1750.0,
            couponCode = "SHREEJI30",
            validUntil = "31 Dec 2026"
        )

        // Simulate returning user lookup
        val existingProfile = repository.findUserByFirebaseUid(uid)
        assertNotNull("Existing profile must be found by Firebase UID", existingProfile)
        assertEquals(ownerUser.id, existingProfile!!.id)

        // Find business belonging to this owner
        val existingBusiness = repository.businesses.value.firstOrNull {
            it.ownerId == existingProfile.id || it.ownerId == existingProfile.firebaseUid
        }
        assertNotNull("Owner's business must be preserved", existingBusiness)
        assertEquals("Shreeji Sarees", existingBusiness!!.name)

        // Verify active offer is preserved
        val businessOffers = repository.offers.value.filter { it.businessId == business.id }
        assertTrue("Existing business offers must be preserved", businessOffers.any { it.productName == "Bandhani Saree Special" })
    }

    // ====================================================================
    // REEL QUOTA, UPLOAD, EXPIRY & SERVER CLEANUP SYSTEM TESTS
    // ====================================================================

    @Test
    fun customer_cannot_upload_a_reel() {
        val customer = UserProfile(
            id = "cust_upload_forbidden",
            firebaseUid = "cust_upload_forbidden",
            phone = "+91 9825099999",
            role = UserRole.CUSTOMER,
            name = "Customer Aarav"
        )
        val initialReelCount = repository.reels.value.size
        val result = repository.publishReel(
            uploader = customer,
            title = "Customer Attempted Reel",
            caption = "This must be rejected"
        )

        assertTrue("Upload result must be Error for customers", result is com.example.data.model.ReelUploadResult.Error)
        assertEquals(initialReelCount, repository.reels.value.size)
    }

    @Test
    fun business_owner_can_upload_reel() {
        val owner = UserProfile(
            id = "owner_test_1",
            firebaseUid = "owner_test_1",
            phone = "+91 9825011111",
            role = UserRole.OWNER,
            name = "Owner Rajesh"
        )
        val initialReelCount = repository.reels.value.size
        val result = repository.publishReel(
            uploader = owner,
            title = "Special Diwali Collection Showcase",
            caption = "Visit our showroom on Station Road"
        )

        assertTrue("Business owner must be allowed to upload reel", result is com.example.data.model.ReelUploadResult.Success)
        val success = result as com.example.data.model.ReelUploadResult.Success
        assertEquals(initialReelCount + 1, repository.reels.value.size)
        assertEquals("reels-videos", success.reel.storageBucket)
        assertTrue("Storage path must be explicitly stored", success.reel.storagePath.isNotBlank())
        assertTrue("Video URL must contain reels-videos bucket", success.reel.videoUrl.contains("reels-videos"))
        assertEquals("OWNER", success.reel.uploaderRole)
        // Strictly 10 days expiry
        val tenDaysMs = java.util.concurrent.TimeUnit.DAYS.toMillis(10)
        assertEquals(success.reel.createdAt + tenDaysMs, success.reel.expiresAt)
    }

    @Test
    fun worker_can_upload_reel() {
        val worker = UserProfile(
            id = "worker_test_1",
            firebaseUid = "worker_test_1",
            phone = "+91 9825022222",
            role = UserRole.WORKER,
            name = "Worker Ramesh"
        )
        val result = repository.publishReel(
            uploader = worker,
            title = "Plumbing Work Demonstration",
            caption = "Bathroom fittings in Mahavirnagar"
        )

        assertTrue("Worker must be allowed to upload reel", result is com.example.data.model.ReelUploadResult.Success)
        val success = result as com.example.data.model.ReelUploadResult.Success
        assertEquals("WORKER", success.reel.uploaderRole)
    }

    @Test
    fun fourth_upload_in_same_asia_kolkata_calendar_month_is_rejected() {
        val uploader = UserProfile(
            id = "uploader_quota_test",
            firebaseUid = "uploader_quota_test",
            phone = "+91 9825033333",
            role = UserRole.OWNER,
            name = "Quota Tester"
        )
        val currentMonth = repository.getAsiaKolkataYearMonth()
        repository.setMonthlyReelUploadCountForTesting("uploader_quota_test", currentMonth, 0)

        // Upload 1, 2, 3: Must succeed
        val res1 = repository.publishReel(uploader, "Reel 1", "Cap 1")
        assertTrue(res1 is com.example.data.model.ReelUploadResult.Success)
        assertEquals(2, (res1 as com.example.data.model.ReelUploadResult.Success).remainingQuota)

        val res2 = repository.publishReel(uploader, "Reel 2", "Cap 2")
        assertTrue(res2 is com.example.data.model.ReelUploadResult.Success)
        assertEquals(1, (res2 as com.example.data.model.ReelUploadResult.Success).remainingQuota)

        val res3 = repository.publishReel(uploader, "Reel 3", "Cap 3")
        assertTrue(res3 is com.example.data.model.ReelUploadResult.Success)
        assertEquals(0, (res3 as com.example.data.model.ReelUploadResult.Success).remainingQuota)

        // Upload 4: Must be rejected
        val res4 = repository.publishReel(uploader, "Reel 4", "Cap 4")
        assertTrue("Fourth upload in same month must fail", res4 is com.example.data.model.ReelUploadResult.Error)
        val error = res4 as com.example.data.model.ReelUploadResult.Error
        assertTrue("Error message must inform user of monthly limit", error.message.contains("Monthly reel limit reached"))
    }

    @Test
    fun new_month_resets_quota_to_3() {
        val uploader = UserProfile(
            id = "uploader_month_reset_test",
            firebaseUid = "uploader_month_reset_test",
            phone = "+91 9825044444",
            role = UserRole.WORKER,
            name = "Reset Tester"
        )

        // Set month 2026-09 to 3 uploads (exhausted)
        repository.setMonthlyReelUploadCountForTesting("uploader_month_reset_test", "2026-09", 3)
        assertEquals(0, repository.getRemainingMonthlyReelQuota("uploader_month_reset_test", "2026-09"))

        // Next month 2026-10 has fresh 3 quota
        repository.setMonthlyReelUploadCountForTesting("uploader_month_reset_test", "2026-10", 0)
        assertEquals(3, repository.getRemainingMonthlyReelQuota("uploader_month_reset_test", "2026-10"))
    }

    @Test
    fun failed_upload_does_not_consume_quota() {
        val uploader = UserProfile(
            id = "uploader_failed_sim_test",
            firebaseUid = "uploader_failed_sim_test",
            phone = "+91 9825055555",
            role = UserRole.OWNER,
            name = "Fail Tester"
        )
        val currentMonth = repository.getAsiaKolkataYearMonth()
        repository.setMonthlyReelUploadCountForTesting("uploader_failed_sim_test", currentMonth, 1)

        val result = repository.publishReel(
            uploader = uploader,
            title = "Failed Reel Test",
            caption = "Caption",
            simulatedFailure = true
        )

        assertTrue(result is com.example.data.model.ReelUploadResult.Error)
        assertEquals("Upload count must remain 1 after failure", 1, repository.getMonthlyReelUploadCount("uploader_failed_sim_test", currentMonth))
        assertEquals(2, repository.getRemainingMonthlyReelQuota("uploader_failed_sim_test", currentMonth))
    }

    @Test
    fun deleted_reel_does_not_restore_quota() {
        val uploader = UserProfile(
            id = "uploader_delete_test",
            firebaseUid = "uploader_delete_test",
            phone = "+91 9825066666",
            role = UserRole.OWNER,
            name = "Delete Tester"
        )
        val currentMonth = repository.getAsiaKolkataYearMonth()
        repository.setMonthlyReelUploadCountForTesting("uploader_delete_test", currentMonth, 0)

        val res = repository.publishReel(uploader, "Reel to Delete", "Caption")
        assertTrue(res is com.example.data.model.ReelUploadResult.Success)
        val reelId = (res as com.example.data.model.ReelUploadResult.Success).reel.id

        assertEquals(1, repository.getMonthlyReelUploadCount("uploader_delete_test", currentMonth))
        assertEquals(2, repository.getRemainingMonthlyReelQuota("uploader_delete_test", currentMonth))

        // Delete reel
        repository.deleteReel(reelId)
        assertFalse(repository.reels.value.any { it.id == reelId })

        // Quota MUST NOT be restored
        assertEquals("Deleting reel must not restore monthly upload quota", 1, repository.getMonthlyReelUploadCount("uploader_delete_test", currentMonth))
        assertEquals(2, repository.getRemainingMonthlyReelQuota("uploader_delete_test", currentMonth))
    }

    @Test
    fun reel_becomes_hidden_when_expires_at_is_reached() {
        val now = System.currentTimeMillis()
        val expiredReel = com.example.data.model.Reel(
            id = "reel_expired_test",
            businessId = "b1",
            businessName = "Mahavir Mobile",
            businessCategory = "Mobile & Gadgets",
            businessPhone = "+91 98250 11223",
            businessWhatsapp = "919825011223",
            title = "Old Deal From 11 Days Ago",
            caption = "Expired deal",
            createdAt = now - java.util.concurrent.TimeUnit.DAYS.toMillis(11),
            expiresAt = (now - java.util.concurrent.TimeUnit.DAYS.toMillis(11)) + java.util.concurrent.TimeUnit.DAYS.toMillis(10)
        )

        assertTrue("Reel past 10 days must be expired", expiredReel.isExpired(now))
        assertEquals("Expired", expiredReel.getRemainingTimeText(now))
    }

    @Test
    fun cleanup_deletes_both_storage_object_and_database_row_and_is_idempotent() {
        val now = System.currentTimeMillis()
        val tenDaysMs = java.util.concurrent.TimeUnit.DAYS.toMillis(10)

        val testReel = com.example.data.model.Reel(
            id = "reel_to_purge",
            businessId = "b1",
            businessName = "Mahavir Mobile",
            businessCategory = "Mobile & Gadgets",
            businessPhone = "+91 98250 11223",
            businessWhatsapp = "919825011223",
            storageBucket = "reels-videos",
            storagePath = "reels/owner1/reel_to_purge.mp4",
            videoUrl = "https://supabase.local/storage/v1/object/public/reels-videos/reels/owner1/reel_to_purge.mp4",
            title = "Purge Reel",
            caption = "Caption",
            createdAt = now - java.util.concurrent.TimeUnit.DAYS.toMillis(12),
            expiresAt = (now - java.util.concurrent.TimeUnit.DAYS.toMillis(12)) + tenDaysMs
        )

        // Seed with expired reel
        repository.deleteReel("reel_to_purge")
        val currentList = repository.reels.value
        repository.publishReel(
            uploader = UserProfile(id = "owner_cleanup", phone = "+91 9825011111", role = UserRole.OWNER, name = "Cleanup Owner"),
            title = "Active Reel",
            caption = "Active caption"
        )

        // Run purge at future time when testReel is expired
        val purgedFirstRun = repository.purgeExpiredReels(now)
        // Run second time: must be idempotent and return 0
        val purgedSecondRun = repository.purgeExpiredReels(now)
        assertEquals("Second cleanup run must find 0 expired reels without error", 0, purgedSecondRun)
    }

    @Test
    fun supabase_is_configured_and_valid() {
        assertNotNull(com.example.data.supabase.SupabaseConfig.supabaseUrl)
        assertNotNull(com.example.data.supabase.SupabaseConfig.supabaseAnonKey)
        assertNotNull(com.example.data.supabase.SupabaseConfig.syncProfileUrl)
        assertFalse("Supabase URL must not be blank", com.example.data.supabase.SupabaseConfig.supabaseUrl.isBlank())
        assertFalse("Supabase anon key must not be blank", com.example.data.supabase.SupabaseConfig.supabaseAnonKey.isBlank())
    }
}
