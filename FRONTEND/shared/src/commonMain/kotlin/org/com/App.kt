package org.com

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.com.auth.AuthManager
import org.com.auth.AuthState
import org.com.model.*
import org.com.network.RoomApi
import org.com.ui.OwnerDashboardScreen
import org.com.ui.PostRoom
import org.com.ui.ProfileScreen
import com.mohamedrejeb.calf.io.readByteArray
import org.com.ui.PropertyDetailsScreen
import org.com.ui.BookingScreen
import org.com.ui.ChatScreen
import org.com.ui.DalaliDashboardScreen
import com.mohamedrejeb.calf.core.LocalPlatformContext
import org.com.i18n.RoomifyLocalization
import org.com.network.RoomifyApi
import org.com.ui.AnalyticsScreen
import org.com.ui.SplashScreen
import org.com.ui.TenantScreen
import org.com.ui.DiscoveryDashboard
import org.com.ui.MessagesScreen
import org.com.ui.MyBookingsScreen
import org.com.ui.FavouriteScreen
import org.com.ui.MapContent
import org.com.ui.AdminDashboardScreen
import org.com.ui.auth.LoginScreen
import org.com.ui.auth.RegisterScreen
import org.com.viewmodel.MapViewModel
import org.com.viewmodel.PostRoomViewModel

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.CompositionLocalProvider
// KamelConfig imports removed to fix build issues after Kamel upgrade. 
// The default configuration will be used automatically.

@Composable
fun App() {
    
    val roomifyTypography = Typography(
        displayLarge = TextStyle(fontWeight = FontWeight.Black, fontSize = 34.sp),
        displayMedium = TextStyle(fontWeight = FontWeight.Black, fontSize = 28.sp),
        displaySmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp),
        headlineLarge = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 22.sp),
        headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp),
        headlineSmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp),
        titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp),
        titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
        titleSmall = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
        bodyLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 16.sp),
        bodyMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp),
        bodySmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp),
        labelLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp),
        labelMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 12.sp),
        labelSmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 11.sp)
    )

    // ============================================================
    // SPLASH
    // ============================================================

    var splashFinished by remember {
        mutableStateOf(false)
    }

    if (!splashFinished) {
        SplashScreen(
            onLoadingComplete = {
                println("Roomify: Splash finished.")
                splashFinished = true
            }
        )
        return
    }

    // ============================================================
    // AUTH MANAGER
    // ============================================================

    val authManager = remember {
        AuthManager()
    }

    val authState by authManager.authState.collectAsState()

    // ============================================================
    // RESTORE SESSION
    // ============================================================

    LaunchedEffect(Unit) {
        println("Roomify: Restoring authentication session...")
        authManager.restoreSession()
    }

    // ============================================================
    // COROUTINE SCOPE
    // ============================================================

    val scope = remember {
        CoroutineScope(
            SupervisorJob() +
                    Dispatchers.Main
        )
    }

    // ============================================================
    // ROOM API
    // ============================================================

    val roomApi = remember {
        RoomApi()
    }

    val bookingApi = remember {
        org.com.network.BookingApi()
    }

    // ============================================================
    // MAP VIEW MODEL
    // ============================================================

    val viewModel = remember(roomApi, scope) {
        MapViewModel(
            roomApi = roomApi,
            scope = scope
        )
    }

    var ownerBookings by remember { mutableStateOf<List<Booking>>(emptyList()) }
    var tenantBookings by remember { mutableStateOf<List<Booking>>(emptyList()) }
    var ownerConversations by remember { mutableStateOf<List<Conversation>>(emptyList()) }
    var tenantConversations by remember { mutableStateOf<List<Conversation>>(emptyList()) }
    var tenantFavorites by remember { mutableStateOf<List<Room>>(emptyList()) }
    
    var furnitureList by remember { mutableStateOf(emptyList<Furniture>()) }
    var pendingFurniture by remember { mutableStateOf<Furniture?>(null) }
    var shopList by remember { mutableStateOf(emptyList<Shop>()) }
    var pendingShop by remember { mutableStateOf<Shop?>(null) }
    var showFantasticBubble by remember { mutableStateOf(false) }
    var activeConversation by remember { mutableStateOf<Conversation?>(null) }

    fun loadOwnerBookings() {
        val user = (authState as? AuthState.Authenticated)?.user
        if (user != null && (user.role.equals("OWNER", ignoreCase = true) || user.role.equals("DALALI", ignoreCase = true))) {
            scope.launch {
                ownerBookings = bookingApi.getOwnerBookings(user.id)
            }
        }
    }

    fun loadTenantBookings() {
        val user = (authState as? AuthState.Authenticated)?.user
        if (user != null && user.role.equals("TENANT", ignoreCase = true)) {
            scope.launch {
                tenantBookings = bookingApi.getUserBookings(user.id)
                val favResponse = RoomifyApi.getUserFavorites(user.id)
                if (favResponse.success) {
                    tenantFavorites = favResponse.data ?: emptyList()
                }
            }
        }
    }

    fun loadOwnerConversations() {
        val user = (authState as? AuthState.Authenticated)?.user
        if (user != null) {
            scope.launch {
                val response = RoomifyApi.getConversations(user.id)
                if (response.success) {
                    ownerConversations = response.data ?: emptyList()
                }
            }
        }
    }

    fun loadTenantConversations() {
        val user = (authState as? AuthState.Authenticated)?.user
        if (user != null) {
            scope.launch {
                val response = RoomifyApi.getConversations(user.id)
                if (response.success) {
                    tenantConversations = response.data ?: emptyList()
                }
            }
        }
    }


    val postRoomViewModel = remember(roomApi, authManager, scope) {
        PostRoomViewModel(
            roomApi = roomApi,
            authManager = authManager,
            scope = scope
        )
    }

    // ============================================================
    // LOAD ROOMS
    // ============================================================

    LaunchedEffect(Unit) {
        println("Roomify: Loading rooms...")
        viewModel.loadRooms()
        
        // Load real furniture data
        scope.launch {
            val response = RoomifyApi.getAllFurniture()
            if (response.success && response.data != null) {
                furnitureList = response.data!!
            }
        }

        // Load shops
        scope.launch {
            val response = RoomifyApi.getAllShops()
            if (response.success && response.data != null) {
                shopList = response.data!!
            }
        }
    }

    // ============================================================
    // POST ROOM STATE
    // ============================================================

    val postRoomState by postRoomViewModel.uiState.collectAsState()

    // ============================================================
    // NAVIGATION
    // ============================================================

    var currentRoute by remember {
        mutableStateOf("map")
    }

    var pendingRoom by remember {
        mutableStateOf<Room?>(null)
    }

    // Track where to go after login
    var postLoginDestination by remember {
        mutableStateOf<String?>(null)
    }

    var routingDestination by remember {
        mutableStateOf<Room?>(null)
    }

    val recentlyViewedRooms = remember { mutableStateListOf<Room>() }

    var discoveryQuery by remember { mutableStateOf<String?>(null) }

    var showSpacePlanner by remember {
        mutableStateOf(false)
    }

    var showAIRoomArranger by remember {
        mutableStateOf(false)
    }

    val isLoggedIn = authState is AuthState.Authenticated

    val uriHandler = LocalUriHandler.current

    val platformContext = LocalPlatformContext.current

    LaunchedEffect(currentRoute) {
        when (currentRoute) {
            "ownerdashboard", "dalalidashboard" -> {
                loadOwnerBookings()
                loadOwnerConversations()
            }
            "tenant", "bookings", "messages" -> {
                loadTenantBookings()
                loadTenantConversations()
            }
        }
    }


    fun withAuth(destination: String, action: () -> Unit) {
        if (isLoggedIn) {
            action()
        } else {
            postLoginDestination = destination
            currentRoute = "login"
        }
    }

    // ============================================================
    // DEBUG: Log auth state changes
    // ============================================================

    LaunchedEffect(authState) {
        println("App: 🔍 authState changed to: $authState")

        when (val state = authState) {
            is AuthState.Authenticated -> {
                println("App: ✅ User is authenticated - ${state.user.name}")
            }
            is AuthState.Error -> {
                println("App: ❌ Auth error: ${state.message}")
            }
            is AuthState.Loading -> {
                println("App: ⏳ Loading...")
            }
            is AuthState.LoggedOut -> {
                println("App: 👤 User is logged out")
            }
        }
    }

    // ============================================================
    // HANDLE LOGIN SUCCESS - DETERMINE DESTINATION
    // ============================================================

    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            val authenticatedState = authState as AuthState.Authenticated
            println("App: ✅✅✅ User authenticated successfully - ${authenticatedState.user.name}")
            println("App: ✅✅✅ currentRoute before navigation: $currentRoute")
            println("App: ✅✅✅ pendingRoom: ${pendingRoom?.id}")
            println("App: ✅✅✅ postLoginDestination: $postLoginDestination")

            // Determine where to go after login
            when {
                // Case 1: User came from View Details - show PropertyDetailsScreen
                postLoginDestination == "details" && pendingRoom != null -> {
                    println("App: 📋 Navigating to specific property details after login")
                    currentRoute = "details"
                    postLoginDestination = null
                }
                postLoginDestination == "booking" && pendingRoom != null -> {
                    println("App: 📅 Navigating to booking after login")
                    currentRoute = "booking"
                    postLoginDestination = null
                }
                postLoginDestination == "chat" && pendingRoom != null -> {
                    println("App: 💬 Navigating to chat after login")
                    currentRoute = "chat"
                    postLoginDestination = null
                }
                postLoginDestination == "planner" && pendingRoom != null -> {
                    println("App: 📐 Opening space planner after login")
                    currentRoute = "details"
                    showSpacePlanner = true
                    postLoginDestination = null
                }
                postLoginDestination == "ai_arranger" && pendingRoom != null -> {
                    println("App: ✨ Opening AI arranger after login")
                    currentRoute = "details"
                    showAIRoomArranger = true
                    postLoginDestination = null
                }
                postLoginDestination == "route" && pendingRoom != null -> {
                    println("App: 🧭 Showing route after login")
                    currentRoute = "details"
                    uriHandler.openUri("https://www.google.com/maps/search/?api=1&query=${pendingRoom?.latitude},${pendingRoom?.longitude}")
                    postLoginDestination = null
                }
                postLoginDestination == "call" && pendingRoom != null -> {
                    println("App: 📞 Calling owner after login")
                    currentRoute = "details"
                    uriHandler.openUri("tel:${pendingRoom?.contactPhone}")
                    postLoginDestination = null
                }
                // Case 2: Role-based routing
                authenticatedState.user.role.equals("OWNER", ignoreCase = true) -> {
                    println("App: 🏠 Owner logged in - Navigating to Owner Dashboard")
                    loadOwnerBookings()
                    currentRoute = "ownerdashboard"
                    postLoginDestination = null
                }
                authenticatedState.user.role.equals("DALALI", ignoreCase = true) -> {
                    println("App: 🤝 Dalali logged in - Navigating to Dalali Dashboard")
                    loadOwnerBookings()
                    currentRoute = "dalalidashboard"
                    postLoginDestination = null
                }
                authenticatedState.user.role.equals("TENANT", ignoreCase = true) -> {
                    println("App: 👤 Tenant logged in - Navigating to Tenant Dashboard")
                    loadTenantBookings()
                    currentRoute = "tenant"
                    postLoginDestination = null
                }
                authenticatedState.user.role.equals("ADMIN", ignoreCase = true) || authenticatedState.user.role.equals("SUPER_ADMIN", ignoreCase = true) -> {
                    println("App: ⚡ Admin logged in - Navigating to Admin Dashboard")
                    currentRoute = "admindashboard"
                    postLoginDestination = null
                }
                // Case 3: Default - show discovery
                else -> {
                    println("App: 🗺️ Navigating to Map")
                    currentRoute = "map"
                }
            }

            println("App: ✅✅✅ final currentRoute: $currentRoute")
        }
    }

    // ============================================================
    // NAVIGATION FUNCTIONS
    // ============================================================

    fun navigateTo(route: String) {
        println("App: navigateTo -> $route")
        when (route) {
            "login" -> {
                currentRoute = "login"
            }
            "map", "explore" -> {
                viewModel.clearFilters()
                currentRoute = "map"
            }
            "discovery", "filters" -> {
                viewModel.clearFilters()
                discoveryQuery = null
                currentRoute = "discovery"
            }
            "logout" -> {
                scope.launch {
                    authManager.logout()
                    currentRoute = "map"
                }
            }
            "analytics" -> {
                val user = (authState as? AuthState.Authenticated)?.user
                postLoginDestination = when {
                    user?.role?.equals("TENANT", ignoreCase = true) == true -> "tenant"
                    user?.role?.equals("OWNER", ignoreCase = true) == true || user?.role?.equals("DALALI", ignoreCase = true) == true -> "ownerdashboard"
                    user?.role?.equals("ADMIN", ignoreCase = true) == true || user?.role?.equals("SUPER_ADMIN", ignoreCase = true) == true -> "admindashboard"
                    else -> "map"
                }
                currentRoute = "analytics"
            }
            else -> {
                currentRoute = route
            }
        }
    }

    fun viewProperty(room: Room) {
        println("App: viewProperty -> ${room.id}")
        
        val roomId: Long? = room.id
        if (roomId != null) {
            var foundIndex = -1
            for (i in 0 until recentlyViewedRooms.size) {
                if (recentlyViewedRooms[i].id == roomId) {
                    foundIndex = i
                    break
                }
            }
            
            if (foundIndex != -1) {
                val existing = recentlyViewedRooms.removeAt(foundIndex)
                recentlyViewedRooms.add(0, existing)
            } else {
                recentlyViewedRooms.add(0, room)
                if (recentlyViewedRooms.size > 10) {
                    recentlyViewedRooms.removeAt(recentlyViewedRooms.size - 1)
                }
            }
        }
        
        pendingRoom = room
        currentRoute = "details"
    }

    // ============================================================
    // MAIN UI
    // ============================================================

    LaunchedEffect(splashFinished) {
        if (splashFinished) {
            delay(1500)
            showFantasticBubble = true
        }
    }

    RoomifyLocalization {
            MaterialTheme(typography = roomifyTypography) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Transparent,
                tonalElevation = 0.dp
            ) {
                when {
                    authState is AuthState.Loading -> {
                        // Show loading spinner
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = Color(0xFF1A237E),
                                modifier = Modifier.size(60.dp)
                            )
                        }
                    }

                    currentRoute == "details" && pendingRoom != null -> {
                        PropertyDetailsScreen(
                            room = pendingRoom!!,
                            currentUser = (authState as? AuthState.Authenticated)?.user,
                            otherProperties = viewModel.rooms.filter { it.postedBy == pendingRoom?.postedBy && it.id != pendingRoom?.id },
                            onBack = {
                                pendingRoom = null
                                postLoginDestination = null
                                currentRoute = "map"
                            },
                            onBookNow = { room ->
                                withAuth("booking") {
                                    pendingRoom = room
                                    currentRoute = "booking"
                                }
                            },
                            onMessageOwner = { room ->
                                withAuth("chat") {
                                    pendingRoom = room
                                    currentRoute = "chat"
                                }
                            },
                            onShowRoute = { room ->
                                withAuth("route") {
                                    uriHandler.openUri("https://www.google.com/maps/search/?api=1&query=${room.latitude},${room.longitude}")
                                }
                            },
                            onCallOwner = { room ->
                                withAuth("call") {
                                    uriHandler.openUri("tel:${room.contactPhone}")
                                }
                            },
                            onSpacePlanner = { room ->
                                withAuth("planner") {
                                    showSpacePlanner = true
                                }
                            },
                            showSpacePlanner = showSpacePlanner,
                            onDismissSpacePlanner = { showSpacePlanner = false },
                            onAIRoomArranger = { room ->
                                withAuth("ai_arranger") {
                                    showAIRoomArranger = true
                                }
                            },
                            showAIRoomArranger = showAIRoomArranger,
                            onDismissAIRoomArranger = { showAIRoomArranger = false },
                            onEditProperty = { room ->
                                postRoomViewModel.startEditing(room)
                                currentRoute = "postroom"
                            },
                            onDeleteProperty = { room ->
                                if (room.id != null) {
                                    postRoomViewModel.deleteRoom(
                                        roomId = room.id,
                                        onSuccess = {
                                            viewModel.loadRooms() // Refresh map
                                            pendingRoom = null
                                            currentRoute = "map"
                                        },
                                        onError = { error ->
                                            println("Delete Error: $error")
                                        }
                                    )
                                }
                            },
                            onViewProperty = { room ->
                                pendingRoom = room
                                // Stays on details but with new room
                            }
                        )
                    }

                    currentRoute == "furniture_dashboard" -> {
                        org.com.ui.FurnitureDashboard(
                            furnitures = furnitureList,
                            shops = shopList,
                            onBack = { currentRoute = "discovery" },
                            onViewDetail = {
                                pendingFurniture = it
                                currentRoute = "furniture_detail"
                            },
                            onViewShop = {
                                pendingShop = it
                                currentRoute = "shop_detail"
                            },
                            onNavigate = ::navigateTo
                        )
                    }

                    currentRoute == "shop_detail" && pendingShop != null -> {
                        org.com.ui.ShopDetailScreen(
                            shop = pendingShop!!,
                            shopFurniture = furnitureList.filter { it.shopId == pendingShop?.id },
                            onBack = { currentRoute = "furniture_dashboard" },
                            onViewFurniture = {
                                pendingFurniture = it
                                currentRoute = "furniture_detail"
                            }
                        )
                    }

                    currentRoute == "furniture_detail" && pendingFurniture != null -> {
                        org.com.ui.FurnitureDetailScreen(
                            furniture = pendingFurniture!!,
                            onBack = { currentRoute = "furniture_dashboard" }
                        )
                    }

                    currentRoute == "post_furniture" -> {
                        val ctx = platformContext
                        org.com.ui.PostFurniture(
                            shops = shopList,
                            onBack = { currentRoute = "furniture_dashboard" },
                            onSubmit = { furniture, files, videoFile ->
                                scope.launch {
                                    try {
                                        // 1. Create Furniture Entry
                                        val createResponse = RoomifyApi.createFurniture(furniture)
                                        if (createResponse.success && createResponse.data?.id != null) {
                                            val furnitureId = createResponse.data!!.id!!
                                            
                                            // 2. Upload Images if any
                                            if (files.isNotEmpty()) {
                                                val imageBytes = files.map { it.readByteArray(ctx) }
                                                RoomifyApi.uploadFurnitureImages(furnitureId, imageBytes)
                                            }
                                            
                                            // 3. Refresh List and Redirect
                                            val refreshResponse = RoomifyApi.getAllFurniture()
                                            if (refreshResponse.success && refreshResponse.data != null) {
                                                furnitureList = refreshResponse.data!!
                                            }
                                            currentRoute = "furniture_dashboard"
                                        }
                                    } catch (e: Exception) {
                                        println("App: Furniture posting failed: ${e.message}")
                                    }
                                }
                            }
                        )
                    }

                    currentRoute == "post_shop" -> {
                        val ctx = platformContext
                        org.com.ui.PostShop(
                            onBack = { currentRoute = "furniture_dashboard" },
                            onSubmit = { shop, images, video ->
                                scope.launch {
                                    try {
                                        val createResponse = RoomifyApi.createShop(shop.copy(ownerId = (authState as? AuthState.Authenticated)?.user?.id))
                                        if (createResponse.success && createResponse.data?.id != null) {
                                            val shopId = createResponse.data!!.id!!
                                            
                                            if (images.isNotEmpty()) {
                                                val bytes = images.map { it.readByteArray(ctx) }
                                                RoomifyApi.uploadShopImages(shopId, bytes)
                                            }
                                            
                                            video?.let {
                                                RoomifyApi.uploadShopVideo(shopId, it.readByteArray(ctx))
                                            }
                                            
                                            val refreshResponse = RoomifyApi.getAllShops()
                                            if (refreshResponse.success && refreshResponse.data != null) {
                                                shopList = refreshResponse.data!!
                                            }
                                            currentRoute = "furniture_dashboard"
                                        }
                                    } catch (e: Exception) {
                                        println("App: Shop posting failed: ${e.message}")
                                    }
                                }
                            }
                        )
                    }

                    currentRoute == "booking" && pendingRoom != null && authState is AuthState.Authenticated -> {
                        val auth = authState as AuthState.Authenticated
                        BookingScreen(
                            room = pendingRoom!!,
                            onBack = { currentRoute = "details" },
                            onConfirmBooking = { booking ->
                                val response = bookingApi.createBooking(booking.copy(userId = auth.user.id))
                                if (response?.success == true) {
                                    loadTenantBookings()
                                    null
                                } else {
                                    response?.message ?: "Failed to send booking request"
                                }
                            }
                        )
                    }

                    currentRoute == "chat" && (pendingRoom != null || activeConversation != null) && authState is AuthState.Authenticated -> {
                        val auth = authState as AuthState.Authenticated
                        ChatScreen(
                            currentUser = auth.user,
                            otherUserName = activeConversation?.otherPartyName ?: pendingRoom?.ownerName ?: "Owner",
                            otherUserId = activeConversation?.otherPartyId ?: pendingRoom?.postedBy ?: 0L,
                            roomId = activeConversation?.roomId ?: pendingRoom?.id,
                            roomTitle = activeConversation?.roomTitle ?: pendingRoom?.title,
                            onBack = { 
                                if (activeConversation != null) {
                                    activeConversation = null
                                    currentRoute = "messages"
                                } else {
                                    currentRoute = "details"
                                }
                            }
                        )

                    }

                    authState is AuthState.Authenticated -> {
                        // ================================================
                        // USER IS LOGGED IN - Main App logic
                        // ================================================
                        val user = (authState as AuthState.Authenticated).user
                        when (currentRoute) {
                                "discovery" -> {
                                    DiscoveryDashboard(initialArea = discoveryQuery) { type, area, price, status ->
                                        viewModel.setFilters(type, area, price, status)
                                        discoveryQuery = null
                                        currentRoute = "map"
                                    }
                                }
                                "map" -> {
                                    AppMapContainer(
                                        viewModel = viewModel,
                                        authState = authState,
                                        routingDestination = routingDestination,
                                        onLogout = {
                                            scope.launch {
                                                authManager.logout()
                                                currentRoute = "map"
                                            }
                                        },
                                        onClearRoute = { routingDestination = null },
                                        onViewProperty = ::viewProperty,
                                        onNavigate = ::navigateTo
                                    )
                                }
                                "ownerdashboard" -> {
                                    val user = (authState as AuthState.Authenticated).user
                                    OwnerDashboardScreen(
                                        user = user,
                                        properties = viewModel.rooms.filter { it.postedBy == user.id || it.ownerName == user.name },
                                        bookings = ownerBookings,
                                        conversations = ownerConversations,
                                        isRefreshing = viewModel.isLoading,
                                        onAddProperty = { currentRoute = "postroom" },
                                        onViewAnalytics = {
                                            postLoginDestination = "ownerdashboard"
                                            currentRoute = "analytics"
                                        },
                                        onViewFinances = { currentRoute = "finances" },
                                        onAcceptBooking = { booking ->
                                            scope.launch {
                                                if (booking.id != null && bookingApi.acceptBooking(booking.id)) {
                                                    loadOwnerBookings()
                                                }
                                            }
                                        },
                                        onRejectBooking = { booking ->
                                            scope.launch {
                                                if (booking.id != null && bookingApi.rejectBooking(booking.id)) {
                                                    loadOwnerBookings()
                                                }
                                            }
                                        },
                                        onConversationClick = { conv ->
                                            activeConversation = conv
                                            currentRoute = "chat"
                                        },
                                        onLogout = {

                                            scope.launch {
                                                authManager.logout()
                                                currentRoute = "map"
                                            }
                                        },
                                        onViewProperty = { room ->
                                            viewProperty(room)
                                        },
                                        onSearch = { type, area, price, status ->
                                            viewModel.setFilters(type, area, price, status)
                                            currentRoute = "map"
                                        },
                                        onNavigate = { route -> navigateTo(route) },
                                        onBack = {
                                            currentRoute = "map"
                                        }
                                    )
                                }
                                "dalalidashboard" -> {
                                    val user = (authState as AuthState.Authenticated).user
                                    DalaliDashboardScreen(
                                        user = user,
                                        properties = viewModel.rooms.filter { it.dalaliName == user.name || it.postedBy == user.id },
                                        bookings = ownerBookings,
                                        isRefreshing = viewModel.isLoading,
                                        onAddProperty = { currentRoute = "postroom" },
                                        onViewAnalytics = {
                                            postLoginDestination = "dalalidashboard"
                                            currentRoute = "analytics"
                                        },
                                        onLogout = {
                                            scope.launch {
                                                authManager.logout()
                                                currentRoute = "map"
                                            }
                                        },
                                        onViewProperty = { room -> viewProperty(room) },
                                        onAcceptBooking = { booking ->
                                            scope.launch {
                                                if (booking.id != null && bookingApi.acceptBooking(booking.id)) {
                                                    loadOwnerBookings()
                                                }
                                            }
                                        },
                                        onRejectBooking = { booking ->
                                            scope.launch {
                                                if (booking.id != null && bookingApi.rejectBooking(booking.id)) {
                                                    loadOwnerBookings()
                                                }
                                            }
                                        },
                                        onSearch = { type, area, price, status ->
                                            viewModel.setFilters(type, area, price, status)
                                            currentRoute = "map"
                                        },
                                        onNavigate = { route -> navigateTo(route) },
                                        onBack = { currentRoute = "map" }
                                    )
                                }
                                "analytics" -> {
                                    AnalyticsScreen(
                                        onBack = {
                                            currentRoute = if (postLoginDestination != null) postLoginDestination!! else "map"
                                        }
                                    )
                                }
                            "finances" -> {
                                org.com.ui.FinancesScreen(onBack = { currentRoute = "ownerdashboard" })
                            }
                            "postroom" -> {
                                    PostRoom(
                                        state = postRoomState,
                                        onPostModeChange = postRoomViewModel::onPostModeChange,
                                        onNumFloorsChange = postRoomViewModel::onNumFloorsChange,
                                        onUpdateFloorConfig = postRoomViewModel::updateFloorConfig,
                                        onAddRoomTemplate = postRoomViewModel::addRoomTemplate,
                                        onUpdateRoomTemplate = postRoomViewModel::updateRoomTemplate,
                                        onGenerateUnits = postRoomViewModel::generateUnits,
                                        onToggleUnitSelection = postRoomViewModel::toggleUnitSelection,
                                        onSelectAllUnits = postRoomViewModel::selectAllUnits,
                                        onClearUnitSelection = postRoomViewModel::clearUnitSelection,
                                        onBulkUpdateUnits = postRoomViewModel::bulkUpdateUnits,
                                        onDeleteSelectedUnits = postRoomViewModel::deleteSelectedUnits,
                                        onUnitImagesSelected = postRoomViewModel::onUnitImagesSelected,
                                        onLocationModeChange = postRoomViewModel::onLocationModeChange,
                                        onManualAddressChange = postRoomViewModel::onManualAddressChange,
                                        onLatitudeChange = postRoomViewModel::onLatitudeChange,
                                        onLongitudeChange = postRoomViewModel::onLongitudeChange,
                                        onTitleChange = postRoomViewModel::onTitleChange,
                                        onDescriptionChange = postRoomViewModel::onDescriptionChange,
                                        onPriceChange = postRoomViewModel::onPriceChange,
                                        onPropertyTypeChange = postRoomViewModel::onPropertyTypeChange,
                                        onRoomsChange = postRoomViewModel::onRoomsChange,
                                        onBathroomsChange = postRoomViewModel::onBathroomsChange,
                                        onAreaChange = postRoomViewModel::onAreaChange,
                                        onMaxGuestsChange = postRoomViewModel::onMaxGuestsChange,
                                        onToggleAmenity = postRoomViewModel::onToggleAmenity,
                                        onRulesChange = postRoomViewModel::onRulesChange,
                                        onContactPhoneChange = postRoomViewModel::onContactPhoneChange,
                                        onContactEmailChange = postRoomViewModel::onContactEmailChange,
                                        onLocationSelected = postRoomViewModel::onLocationSelected,
                                        onImagesSelected = postRoomViewModel::onImagesSelected,
                                        onVideoSelected = postRoomViewModel::onVideoSelected,
                                        onContractSelected = postRoomViewModel::onContractSelected,
                                        onSubmit = {
                                            postRoomViewModel.submit(
                                                context = platformContext,
                                                onSuccess = {
                                                    // Nothing needed here now, UI shows success
                                                },
                                                onError = { error ->
                                                    println("PostRoom Error: $error")
                                                }
                                            )
                                        },
                                        onSuccessDismissed = {
                                            // Refresh map rooms
                                            viewModel.loadRooms()
                                            postRoomViewModel.reset()
                                            currentRoute = "ownerdashboard"
                                        },
                                        onErrorDismissed = postRoomViewModel::dismissError,
                                        onBack = {
                                            currentRoute = "map"
                                        }
                                    )
                                }
                                "profile" -> {
                                    ProfileScreen(
                                        user = (authState as AuthState.Authenticated).user,
                                        onBack = { currentRoute = "map" },
                                        onLogout = {
                                            scope.launch {
                                                authManager.logout()
                                                currentRoute = "map"
                                            }
                                        },
                                        onUpdateUser = { updatedUser ->
                                            scope.launch {
                                                try {
                                                    val response = RoomifyApi.updateUserProfile(updatedUser)
                                                    if (response.success && response.data != null) {
                                                        authManager.updateCurrentUser(response.data)
                                                    }
                                                } catch (e: Exception) {
                                                    println("App: Profile update failed: ${e.message}")
                                                }
                                            }
                                        }
                                    )
                                }
                                "tenant" -> {
                                    val user = (authState as AuthState.Authenticated).user
                                    TenantScreen(
                                        user = user,
                                        bookings = tenantBookings,
                                        allRooms = viewModel.rooms,
                                        recentlyViewed = recentlyViewedRooms,
                                        savedRooms = tenantFavorites,
                                        isRefreshing = viewModel.isLoading,
                                        onExploreRooms = {
                                            navigateTo("map")
                                        },
                                        onViewProperty = { room ->
                                            viewProperty(room)
                                        },
                                        onViewAll = {
                                            viewModel.clearFilters()
                                            discoveryQuery = null
                                            currentRoute = "map"
                                        },
                                        onLogout = {
                                            scope.launch {
                                                authManager.logout()
                                                currentRoute = "map"
                                            }
                                        },
                                        onSearch = { type, area, price, status ->
                                            viewModel.setFilters(type, area, price, status)
                                            currentRoute = "map"
                                        },
                                        onNavigate = { route -> navigateTo(route) }
                                    )
                                }
                                "messages" -> {
                                    MessagesScreen(
                                        conversations = tenantConversations,
                                        onBack = { currentRoute = "tenant" },
                                        onConversationClick = { conv -> 
                                            activeConversation = conv
                                            currentRoute = "chat"
                                        }
                                    )
                                }

                                "bookings" -> {
                                    MyBookingsScreen(
                                        bookings = tenantBookings,
                                        allRooms = viewModel.rooms,
                                        onBack = { currentRoute = "tenant" },
                                        onViewProperty = { viewProperty(it) }
                                    )
                                }
                                "saved" -> {
                                    FavouriteScreen(
                                        savedRooms = tenantFavorites,
                                        onBack = { currentRoute = "tenant" },
                                        onViewProperty = { viewProperty(it) }
                                    )
                                }
                                "map" -> {
                                    AppMapContainer(
                                        viewModel = viewModel,
                                        authState = authState,
                                        routingDestination = routingDestination,
                                        onLogout = {
                                            scope.launch {
                                                authManager.logout()
                                                currentRoute = "map"
                                            }
                                        },
                                        onClearRoute = { routingDestination = null },
                                        onViewProperty = ::viewProperty,
                                        onNavigate = ::navigateTo
                                    )
                                }
                                "admindashboard" -> {
                                    AdminDashboardScreen(
                                        user = user,
                                        onLogout = {
                                            scope.launch {
                                                authManager.logout()
                                                currentRoute = "map"
                                            }
                                        },
                                        onNavigate = { route -> navigateTo(route) },
                                        onBack = { currentRoute = "map" }
                                    )
                                }
                            }
                        }

                    authState is AuthState.Error -> {
                        // Show error state with login/register
                        if (currentRoute == "register") {
                            RegisterScreen(
                                authState = authState,
                                onRegister = { request ->
                                    scope.launch { authManager.register(request) }
                                },
                                onGoogleRegister = { idToken, role ->
                                    scope.launch { authManager.googleRegister(idToken, role) }
                                },
                                onLoginClick = { currentRoute = "login" },
                                onBack = { currentRoute = "login" }
                            )
                        } else {
                            LoginScreen(
                                authState = authState,
                                onLogin = { email, password ->
                                    println("App: 🔐 Login called for $email")
                                    scope.launch { authManager.login(email, password) }
                                },
                                onGoogleLogin = {
                                    println("App: 🔐 Google login called")
                                    scope.launch { authManager.googleLogin("") } // Need idToken here normally
                                },
                                onRegisterClick = {
                                    println("App: 📝 Navigate to Register")
                                    currentRoute = "register"
                                },
                                onBack = {
                                    println("App: ⬅️ Back from Login")
                                    pendingRoom = null
                                    postLoginDestination = null
                                    currentRoute = "map"
                                },
                                onGuestLogin = {
                                    println("App: 🎭 Guest login")
                                    scope.launch { authManager.guestLogin() }
                                },
                                onForgotPassword = {
                                    println("App: 🔑 Forgot password")
                                }
                            )
                        }
                    }

                    else -> {
                        // Logged-out state
                        when (currentRoute) {
                            "register" -> {
                                RegisterScreen(
                                    authState = authState,
                                    onRegister = { request ->
                                        scope.launch { authManager.register(request) }
                                    },
                                    onGoogleRegister = { idToken, role ->
                                        scope.launch { authManager.googleRegister(idToken, role) }
                                    },
                                    onLoginClick = { currentRoute = "login" },
                                    onBack = { currentRoute = "login" }
                                )
                            }
                            "login" -> {
                            LoginScreen(
                                authState = authState,
                                onLogin = { email, password ->
                                    println("App: 🔐 Login called for $email")
                                    scope.launch { authManager.login(email, password) }
                                },
                                onGoogleLogin = {
                                    println("App: 🔐 Google login called")
                                    scope.launch { authManager.googleLogin("") }
                                },
                                    onRegisterClick = {
                                        println("App: 📝 Navigate to Register")
                                        currentRoute = "register"
                                    },
                                    onBack = {
                                        println("App: ⬅️ Back from Login")
                                        pendingRoom = null
                                        postLoginDestination = null
                                        currentRoute = "map"
                                    },
                                    onGuestLogin = {
                                        println("App: 🎭 Guest login")
                                        scope.launch { authManager.guestLogin() }
                                    },
                                    onForgotPassword = {
                                        println("App: 🔑 Forgot password")
                                    }
                                )
                            }
                            "discovery" -> {
                                DiscoveryDashboard { type, area, price, status ->
                                    viewModel.setFilters(type, area, price, status)
                                    currentRoute = "map"
                                }
                            }
                            else -> {
                                // Public map
                                AppMapContainer(
                                    viewModel = viewModel,
                                    authState = authState,
                                    routingDestination = routingDestination,
                                    onLogout = {
                                        scope.launch {
                                            authManager.logout()
                                            currentRoute = "map"
                                        }
                                    },
                                    onClearRoute = { routingDestination = null },
                                    onViewProperty = ::viewProperty,
                                    onNavigate = ::navigateTo
                                )
                            }
                        }
                    }
                }
                
                FantasticBubbleContainer(
                    isVisible = showFantasticBubble,
                    onSearch = {
                        showFantasticBubble = false
                        currentRoute = "furniture_dashboard"
                    },
                    onPost = {
                        showFantasticBubble = false
                        currentRoute = "post_furniture"
                    }
                )
            }
        }
    }
}


@Composable
private fun AppMapContainer(
    viewModel: MapViewModel,
    authState: AuthState,
    routingDestination: Room?,
    onLogout: () -> Unit,
    onClearRoute: () -> Unit,
    onViewProperty: (Room) -> Unit,
    onNavigate: (String) -> Unit
) {
    val user = (authState as? AuthState.Authenticated)?.user
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            org.com.ui.components.RoomifySidebar(
                user = user,
                onNavigate = onNavigate,
                onLogout = onLogout,
                onSearch = { type, area, price, status ->
                    viewModel.setFilters(type, area, price, status)
                },
                onClose = { scope.launch { drawerState.close() } }
            )
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            MapContent(
                rooms = viewModel.filteredRooms,
                selectedRoom = viewModel.selectedRoom,
                authState = authState,
                routingDestination = routingDestination,
                currentStatusFilter = viewModel.filterStatus ?: "ALL",
                shouldFitBounds = viewModel.shouldFitBounds,
                viewedRoomIds = viewModel.viewedRoomIds,
                savedRoomIds = viewModel.savedRoomIds,
                onStatusFilterChange = { viewModel.filterStatus = if (it == "ALL") null else it },
                onFiltersChange = { type, area, price, status ->
                    viewModel.setFilters(type, area, price, status)
                },
                onFitBoundsHandled = { viewModel.clearFitBounds() },
                onClearRoute = onClearRoute,
                onMenuClick = { scope.launch { drawerState.open() } },
                onRoomSelected = { room ->
                    viewModel.selectRoom(room)
                },
                onRoomCleared = {
                    viewModel.clearSelectedRoom()
                },
                onViewProperty = onViewProperty,
                onNavigate = onNavigate
            )

            // Status Legend (Bottom Left)
            Surface(
                modifier = Modifier.align(Alignment.BottomStart).padding(16.dp).padding(bottom = 24.dp),
                color = Color.White.copy(alpha = 0.95f),
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 4.dp
            ) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("PROPERTY STATUS", fontWeight = FontWeight.Black, fontSize = 10.sp, color = Color.Gray)
                    MapLegendItem("Available", Color(0xFF2E7D32))
                    MapLegendItem("Pending", Color(0xFFF9A825))
                    MapLegendItem("Rented", Color(0xFFC62828))
                }
            }
        }
    }
}

@Composable
private fun FantasticBubbleContainer(
    isVisible: Boolean,
    onSearch: () -> Unit,
    onPost: () -> Unit
) {
    org.com.ui.components.FantasticBubble(
        isVisible = isVisible,
        onSearchFurniture = onSearch,
        onPostFurniture = onPost
    )
}

@Composable
private fun MapLegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).background(color, CircleShape))
        Spacer(Modifier.width(8.dp))
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
    }
}
