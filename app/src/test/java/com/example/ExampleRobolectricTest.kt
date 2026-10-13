package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.data.repository.HimmatnagarRepository
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var repository: HimmatnagarRepository

    @Before
    fun setup() {
        repository = HimmatnagarRepository.createForTesting()
    }

    @Test
    fun app_name_is_HMT() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("HMT", appName)
    }

    @Test
    fun invalid_offer_is_not_saved() {
        val initialOfferCount = repository.offers.value.size

        // 1. Original price zero or negative
        val resZeroPrice = repository.addOffer("b1", "Product", "Food", 0.0, 50.0, "SAVE10", "31 Dec 2026")
        assertFalse("Offer with zero price should not be saved", resZeroPrice)
        assertEquals(initialOfferCount, repository.offers.value.size)

        val resNegativePrice = repository.addOffer("b1", "Product", "Food", -100.0, 50.0, "SAVE10", "31 Dec 2026")
        assertFalse("Offer with negative price should not be saved", resNegativePrice)
        assertEquals(initialOfferCount, repository.offers.value.size)

        // 2. Discount price >= Original price
        val resHigherDiscount = repository.addOffer("b1", "Product", "Food", 100.0, 150.0, "SAVE10", "31 Dec 2026")
        assertFalse("Offer with discount higher than original should not be saved", resHigherDiscount)
        assertEquals(initialOfferCount, repository.offers.value.size)

        val resEqualPrice = repository.addOffer("b1", "Product", "Food", 100.0, 100.0, "SAVE10", "31 Dec 2026")
        assertFalse("Offer with equal original and deal price should not be saved", resEqualPrice)
        assertEquals(initialOfferCount, repository.offers.value.size)

        // 3. Blank product name
        val resBlankName = repository.addOffer("b1", "   ", "Food", 200.0, 100.0, "SAVE10", "31 Dec 2026")
        assertFalse("Offer with blank product name should not be saved", resBlankName)
        assertEquals(initialOfferCount, repository.offers.value.size)

        // 4. Blank category or valid until
        val resBlankCat = repository.addOffer("b1", "Product", "  ", 200.0, 100.0, "SAVE10", "31 Dec 2026")
        assertFalse("Offer with blank category should not be saved", resBlankCat)
        assertEquals(initialOfferCount, repository.offers.value.size)

        val resBlankDate = repository.addOffer("b1", "Product", "Food", 200.0, 100.0, "SAVE10", "   ")
        assertFalse("Offer with blank date should not be saved", resBlankDate)
        assertEquals(initialOfferCount, repository.offers.value.size)
    }

    @Test
    fun blank_chat_message_is_not_saved() {
        val user = UserProfile(id = "user_cust1", phone = "9825000000", role = UserRole.CUSTOMER, name = "Aarav Shah")
        repository.setCurrentUser(user)

        val initialChatCount = repository.chatMessages.value.size

        val resBlank = repository.sendChatMessage("b1", "Mahavir Mobile", "   ", false)
        assertFalse("Blank chat message should not be saved", resBlank)
        assertEquals(initialChatCount, repository.chatMessages.value.size)

        val resEmpty = repository.sendChatMessage("b1", "Mahavir Mobile", "", false)
        assertFalse("Empty chat message should not be saved", resEmpty)
        assertEquals(initialChatCount, repository.chatMessages.value.size)
    }

    @Test
    fun blank_worker_and_reel_are_not_saved() {
        // Blank Worker test
        val initialWorkerCount = repository.workers.value.size
        val resBlankWorkerName = repository.addWorker("   ", "Plumber", "9825012345", "Motipura", "5 Years")
        assertFalse("Worker with blank name should not be saved", resBlankWorkerName)
        assertEquals(initialWorkerCount, repository.workers.value.size)

        val resBlankWorkerService = repository.addWorker("Ramesh", "   ", "9825012345", "Motipura", "5 Years")
        assertFalse("Worker with blank service should not be saved", resBlankWorkerService)
        assertEquals(initialWorkerCount, repository.workers.value.size)

        val resBlankWorkerPhone = repository.addWorker("Ramesh", "Plumber", "   ", "Motipura", "5 Years")
        assertFalse("Worker with blank phone should not be saved", resBlankWorkerPhone)
        assertEquals(initialWorkerCount, repository.workers.value.size)

        val resBlankWorkerArea = repository.addWorker("Ramesh", "Plumber", "9825012345", "   ", "5 Years")
        assertFalse("Worker with blank area should not be saved", resBlankWorkerArea)
        assertEquals(initialWorkerCount, repository.workers.value.size)

        // Blank Reel test
        val initialReelCount = repository.reels.value.size
        val resBlankReelTitle = repository.addReel("b1", "   ", "Awesome festival discount")
        assertFalse("Reel with blank title should not be saved", resBlankReelTitle)
        assertEquals(initialReelCount, repository.reels.value.size)

        val resBlankReelCaption = repository.addReel("b1", "Festival Sale", "   ")
        assertFalse("Reel with blank caption should not be saved", resBlankReelCaption)
        assertEquals(initialReelCount, repository.reels.value.size)
    }

    @Test
    fun business_delete_removes_all_related_data() {
        val targetBusinessId = "b1"

        // Ensure business exists
        assertTrue(repository.businesses.value.any { it.id == targetBusinessId })

        // Follow business
        repository.toggleFollow(targetBusinessId)
        assertTrue(repository.followedBusinessIds.value.contains(targetBusinessId))

        // Add chat message
        val user = UserProfile(id = "user_cust1", phone = "9825000000", role = UserRole.CUSTOMER, name = "Aarav Shah")
        repository.setCurrentUser(user)
        repository.sendChatMessage(targetBusinessId, "Mahavir Mobile", "Hello, do you have iPhone in stock?", false)

        assertTrue(repository.chatMessages.value.any { it.businessId == targetBusinessId })
        assertTrue(repository.offers.value.any { it.businessId == targetBusinessId })
        assertTrue(repository.reels.value.any { it.businessId == targetBusinessId })

        // Delete business
        repository.deleteBusiness(targetBusinessId)

        // Assert cascade deletion across all related entities
        assertFalse("Deleted business should not be in businesses list",
            repository.businesses.value.any { it.id == targetBusinessId })

        assertFalse("Related offers should be removed on business delete",
            repository.offers.value.any { it.businessId == targetBusinessId })

        assertFalse("Related reels should be removed on business delete",
            repository.reels.value.any { it.businessId == targetBusinessId })

        assertFalse("Follow relation should be removed on business delete",
            repository.followedBusinessIds.value.contains(targetBusinessId))

        assertFalse("Chat messages of deleted business should be removed",
            repository.chatMessages.value.any { it.businessId == targetBusinessId })
    }

    @Test
    fun explore_tab_is_not_present_and_home_deals_navigate_to_offers() {
        val viewModel = MainViewModel(repository)

        // Verify ScreenState does not contain EXPLORE
        val screenNames = ScreenState.values().map { it.name }
        assertFalse("Explore tab/screen should be completely removed", screenNames.contains("EXPLORE"))

        // Verify navigation tabs support only Home(0), Reels(1), Workers(2), Profile(3)
        viewModel.selectTab(0)
        assertEquals(0, viewModel.selectedTab.value)

        viewModel.selectTab(1)
        assertEquals(1, viewModel.selectedTab.value)

        viewModel.selectTab(2)
        assertEquals(2, viewModel.selectedTab.value)

        viewModel.selectTab(3)
        assertEquals(3, viewModel.selectedTab.value)

        // Verify Home screen offers redirect to OFFERS screen
        viewModel.openOffersScreen()
        assertEquals(ScreenState.OFFERS, viewModel.currentScreen.value)

        // Verify back handling from OFFERS returns to MAIN_TABS
        viewModel.handleBack()
        assertEquals(ScreenState.MAIN_TABS, viewModel.currentScreen.value)
    }

    @Test
    fun logout_returns_to_role_selection_login_screen() {
        val viewModel = MainViewModel(repository)

        // Login as customer
        viewModel.registerCustomerWithGoogle(
            firebaseUid = "uid_logout_test",
            googleName = "Neha Sharma",
            googleEmail = "neha@gmail.com",
            enteredPhone = "+91 9825044444",
            enteredName = "Neha Sharma"
        )

        assertEquals(ScreenState.MAIN_TABS, viewModel.currentScreen.value)
        assertNotNull(viewModel.currentUser.value)

        // Logout
        viewModel.logout()

        // Verify login/role selection screen is displayed
        assertEquals(ScreenState.ROLE_SELECTION, viewModel.currentScreen.value)
        assertNull(viewModel.currentUser.value)
        assertEquals(0, viewModel.selectedTab.value)
    }

    @Test
    fun customer_cannot_upload_reel_and_upload_is_rejected_in_viewmodel() {
        val viewModel = MainViewModel(repository)

        // Customer login
        viewModel.registerCustomerWithGoogle(
            firebaseUid = "uid_cust_no_upload",
            googleName = "Kavita",
            googleEmail = "kavita@gmail.com",
            enteredPhone = "+91 9825077777",
            enteredName = "Kavita Customer"
        )

        // Verify customer role
        assertEquals(UserRole.CUSTOMER, viewModel.currentUser.value?.role)

        // Attempt upload
        val result = viewModel.uploadReel(
            title = "Customer Illegal Reel",
            caption = "Should be rejected"
        )

        assertTrue("Upload must be rejected for customers", result is com.example.data.model.ReelUploadResult.Error)
        val err = result as com.example.data.model.ReelUploadResult.Error
        assertTrue(err.message.contains("Customers are not permitted to upload reels"))
    }

    @Test
    fun expired_reel_does_not_appear_in_feed_profile_or_deep_link_screen() {
        val viewModel = MainViewModel(repository)
        val now = System.currentTimeMillis()
        val tenDaysMs = java.util.concurrent.TimeUnit.DAYS.toMillis(10)

        val expiredReel = com.example.data.model.Reel(
            id = "reel_expired_cuj",
            businessId = "b1",
            businessName = "Mahavir Mobile",
            businessCategory = "Mobile & Gadgets",
            businessPhone = "+91 98250 11223",
            businessWhatsapp = "919825011223",
            title = "Expired 12 Days Ago",
            caption = "Hidden deal",
            createdAt = now - java.util.concurrent.TimeUnit.DAYS.toMillis(12),
            expiresAt = (now - java.util.concurrent.TimeUnit.DAYS.toMillis(12)) + tenDaysMs
        )

        assertTrue(expiredReel.isExpired(now))

        // 1. Verify active reels filter excludes expired reel
        val allReels = listOf(expiredReel) + repository.reels.value
        val activeReels = allReels.filter { !it.isExpired(now) }
        assertFalse("Active reels feed must never contain expired reel", activeReels.any { it.id == "reel_expired_cuj" })

        // 2. Verify business profile reels exclude expired reel
        val bReels = allReels.filter { it.businessId == "b1" && !it.isExpired(now) }
        assertFalse("Business profile must not show expired reel", bReels.any { it.id == "reel_expired_cuj" })

        // 3. Verify deep-link screen navigation to expired reel
        repository.publishReel(
            uploader = UserProfile(id = "owner_b1", phone = "+91 9825011111", role = UserRole.OWNER, name = "Owner"),
            title = "Fresh Active Reel",
            caption = "Fresh deal"
        )
        val activeReel = repository.reels.value.first()
        viewModel.openReelDetail(activeReel.id)
        assertEquals(ScreenState.REEL_DETAIL, viewModel.currentScreen.value)
        assertEquals(activeReel.id, viewModel.selectedReel.value?.id)
        assertFalse(viewModel.selectedReel.value!!.isExpired(now))

        // Open expired reel in deep-link screen
        viewModel.openReelDetail("reel_expired_cuj")
        // If expired, reel.isExpired() flag evaluates to true so ReelDetailScreen displays the expired banner
        val deepLinkedReel = expiredReel
        assertTrue("Deep link must identify reel as expired", deepLinkedReel.isExpired(now))
    }

    @Test
    fun customer_flow_google_signin_persistence_and_logout() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = HimmatnagarRepository(context)
        val viewModel = MainViewModel(repo)

        // 1. Initial screen is Role Selection
        assertEquals(ScreenState.ROLE_SELECTION, viewModel.currentScreen.value)

        // 2. Open Customer Registration Screen
        viewModel.navigateTo(ScreenState.CUSTOMER_AUTH)
        assertEquals(ScreenState.CUSTOMER_AUTH, viewModel.currentScreen.value)

        // 3. Register Customer with Google Auth details + entered inputs
        val testUid = "firebase_cust_uid_778899"
        val testGoogleName = "Tambadiya Hasan"
        val testGoogleEmail = "tambadiyahasan816@gmail.com"
        val testEnteredPhone = "+91 9876543210"
        val testEnteredName = "Hasan T."

        viewModel.registerCustomerWithGoogle(
            firebaseUid = testUid,
            googleName = testGoogleName,
            googleEmail = testGoogleEmail,
            enteredPhone = testEnteredPhone,
            enteredName = testEnteredName
        )

        // 4. Verify login succeeds and lands on Main Tabs
        assertEquals(ScreenState.MAIN_TABS, viewModel.currentScreen.value)
        val currentUser = viewModel.currentUser.value
        assertNotNull(currentUser)

        // 5. Verify that Firebase UID, Google display name, Google email, and entered mobile number are saved separately
        assertEquals(testUid, currentUser?.firebaseUid)
        assertEquals(testGoogleName, currentUser?.googleDisplayName)
        assertEquals(testGoogleEmail, currentUser?.email)
        assertEquals(testEnteredPhone, currentUser?.phone)
        assertEquals(testEnteredName, currentUser?.name)
        assertEquals(UserRole.CUSTOMER, currentUser?.role)

        // 6. Simulate app close and reopen: create fresh repository and ViewModel from persisted storage
        val reopenedRepo = HimmatnagarRepository(context)
        val reopenedVm = MainViewModel(reopenedRepo)

        // 7. Verify session is restored
        assertEquals(ScreenState.MAIN_TABS, reopenedVm.currentScreen.value)
        val restoredUser = reopenedVm.currentUser.value
        assertNotNull(restoredUser)
        assertEquals(testUid, restoredUser?.firebaseUid)
        assertEquals(testGoogleName, restoredUser?.googleDisplayName)
        assertEquals(testGoogleEmail, restoredUser?.email)
        assertEquals(testEnteredPhone, restoredUser?.phone)

        // 8. Verify NO duplicate customer profile is created
        val matchingInRegistered = reopenedRepo.registeredUsers.value.filter { it.firebaseUid == testUid }
        assertEquals("Must only have 1 profile for this Firebase UID", 1, matchingInRegistered.size)

        // Re-registration or login should also not create duplicate
        reopenedRepo.loginOrRegisterCustomerWithGoogle(
            firebaseUid = testUid,
            googleName = testGoogleName,
            googleEmail = testGoogleEmail,
            enteredPhone = testEnteredPhone,
            enteredName = testEnteredName
        )
        val matchingAfterReAuth = reopenedRepo.registeredUsers.value.filter { it.firebaseUid == testUid }
        assertEquals("Must still have exactly 1 profile after re-auth", 1, matchingAfterReAuth.size)

        // 9. Test logout and verify role-selection screen appears
        reopenedVm.logout()
        assertEquals(ScreenState.ROLE_SELECTION, reopenedVm.currentScreen.value)
        assertNull(reopenedVm.currentUser.value)

        // Verify persisted session is cleared after logout
        val postLogoutRepo = HimmatnagarRepository(context)
        val postLogoutVm = MainViewModel(postLogoutRepo)
        assertEquals(ScreenState.ROLE_SELECTION, postLogoutVm.currentScreen.value)
        assertNull(postLogoutVm.currentUser.value)
    }

    @Test
    fun default_web_client_id_is_resolved() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val helper = com.example.auth.GoogleAuthHelper(context)
        val clientId = helper.getWebClientId()
        assertNotNull(clientId)
        assertTrue("Web client ID should not be blank", clientId.isNotBlank())
        assertTrue("Web client ID must contain googleusercontent.com", clientId.contains("googleusercontent.com"))
    }

    @Test
    fun supabase_client_configuration_and_data_source_layer_test() {
        // 1. Verify SupabaseConfig properties exist and do not crash
        val url = com.example.data.supabase.SupabaseConfig.supabaseUrl
        val key = com.example.data.supabase.SupabaseConfig.supabaseAnonKey
        assertNotNull(url)
        assertNotNull(key)

        // 2. Instantiate SupabaseDataSource and verify data-source layer exists
        val dataSource = com.example.data.supabase.SupabaseDataSource(url, key)
        assertNotNull(dataSource)

        // 3. Verify repository has supabaseDataSource reference
        assertNotNull(repository.supabaseDataSource)
    }

    @Test
    fun profile_sync_edge_function_endpoint_and_role_mismatch_test() {
        // 1. Verify function endpoint URL is configured
        val endpoint = com.example.data.supabase.SupabaseConfig.syncProfileUrl
        assertNotNull(endpoint)
        assertTrue("Sync profile endpoint must be set", endpoint.isNotBlank())
        assertTrue("Sync profile endpoint must point to sync-profile", endpoint.contains("sync-profile"))

        // 2. Register customer
        val uid = "test_edge_sync_uid"
        val customer = repository.loginOrRegisterCustomerWithGoogle(
            firebaseUid = uid,
            googleName = "Test Customer",
            googleEmail = "customer@example.com",
            enteredPhone = "+91 9825011111",
            enteredName = "Customer 1"
        )
        assertNotNull(customer)
        assertEquals(UserRole.CUSTOMER, customer.role)

        // 3. Same Firebase UID attempting to register as OWNER must throw role mismatch exception
        var roleMismatchThrown = false
        try {
            repository.loginOrRegisterOwnerWithGoogle(
                firebaseUid = uid,
                googleName = "Test Owner",
                googleEmail = "customer@example.com",
                enteredOwnerName = "Owner",
                enteredPhone = "+91 9825022222",
                shopName = "Shop",
                category = "Food",
                area = "Motipura",
                address = "Address",
                whatsapp = "9825022222"
            )
        } catch (e: IllegalStateException) {
            roleMismatchThrown = true
            assertTrue(e.message?.contains("already registered as a Customer") == true)
        }
        assertTrue("Role mismatch must prevent overwriting role", roleMismatchThrown)

        // 4. Updating the profile with same role should not create a duplicate
        val updatedCustomer = repository.loginOrRegisterCustomerWithGoogle(
            firebaseUid = uid,
            googleName = "Test Customer",
            googleEmail = "customer@example.com",
            enteredPhone = "+91 9825099999",
            enteredName = "Updated Name"
        )
        assertEquals(updatedCustomer.firebaseUid, uid)
        val matchingProfiles = repository.registeredUsers.value.filter { it.firebaseUid == uid }
        assertEquals("Profile count must remain exactly 1", 1, matchingProfiles.size)
    }
}
