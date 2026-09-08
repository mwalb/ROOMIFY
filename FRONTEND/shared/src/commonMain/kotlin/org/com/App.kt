package org.com

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.com.auth.AuthManager
import org.com.auth.AuthState
import org.com.model.Room
import org.com.network.RoomApi
import org.com.ui.MapScreen
import org.com.ui.OwnerDashboardScreen
import org.com.ui.PostRoom
import org.com.ui.ProfileScreen
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
import org.com.ui.DiscoveryDashboard

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

    var ownerBookings by remember { mutableStateOf<List<org.com.model.Booking>>(emptyList()) }
    var tenantBookings by remember { mutableStateOf<List<org.com.model.Booking>>(emptyList()) }

    fun loadOwnerBookings() {
        val user = (authState as? AuthState.Authenticated)?.user
        if (user != null && (user.role == "OWNER" || user.role == "DALALI")) {
            scope.launch {
                ownerBookings = bookingApi.getOwnerBookings(user.id)
            }
        }
    }

    fun loadTenantBookings() {
        val user = (authState as? AuthState.Authenticated)?.user
        if (user != null && user.role == "TENANT") {
            scope.launch {
                tenantBookings = bookingApi.getUserBookings(user.id)
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
    }

    // ============================================================
    // POST ROOM STATE
    // ============================================================

    val postRoomState by postRoomViewModel.uiState.collectAsState()

    // ============================================================
    // NAVIGATION
    // ============================================================

    var currentRoute by remember {
        mutableStateOf("discovery")
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

    var showSpacePlanner by remember {
        mutableStateOf(false)
    }

    val isLoggedIn = authState is AuthState.Authenticated

    val uriHandler = LocalUriHandler.current

    val platformContext = LocalPlatformContext.current

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
                authenticatedState.user.role.equals("ADMIN", ignoreCase = true) -> {
                    println("App: ⚡ Admin logged in - Navigating to Admin Dashboard")
                    currentRoute = "admindashboard"
                    postLoginDestination = null
                }
                // Case 3: Default - show discovery
                else -> {
                    println("App: 🗺️ Navigating to Discovery")
                    currentRoute = "discovery"
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
                currentRoute = "map"
            }
            "discovery", "filters" -> {
                viewModel.clearFilters()
                currentRoute = "discovery"
            }
            "logout" -> {
                scope.launch {
                    authManager.logout()
                    currentRoute = "discovery"
                }
            }
            "analytics" -> {
                val user = (authState as? AuthState.Authenticated)?.user
                postLoginDestination = when {
                    user?.role == "TENANT" -> "tenant"
                    user?.role == "OWNER" || user?.role == "DALALI" -> "ownerdashboard"
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
        pendingRoom = room
        currentRoute = "details"
    }

    // ============================================================
    // MAIN UI
    // ============================================================

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

                    currentRoute == "booking" && pendingRoom != null && authState is AuthState.Authenticated -> {
                        val auth = authState as AuthState.Authenticated
                        BookingScreen(
                            room = pendingRoom!!,
                            onBack = { currentRoute = "details" },
                            onConfirmBooking = { booking ->
                                scope.launch {
                                    bookingApi.createBooking(booking.copy(userId = auth.user.id))
                                }
                            }
                        )
                    }

                    currentRoute == "chat" && pendingRoom != null && authState is AuthState.Authenticated -> {
                        val auth = authState as AuthState.Authenticated
                        ChatScreen(
                            currentUser = auth.user,
                            otherUserName = pendingRoom?.ownerName ?: "Owner",
                            onBack = { currentRoute = "details" }
                        )
                    }

                    authState is AuthState.Authenticated -> {
                        // ================================================
                        // USER IS LOGGED IN - Main App logic
                        // ================================================
                        val user = (authState as AuthState.Authenticated).user
                        when (currentRoute) {
                                "discovery" -> {
                                    DiscoveryDashboard(
                                        onSearch = { type, area, price ->
                                            viewModel.setFilters(type, area, price)
                                            currentRoute = "map"
                                        }
                                    )
                                }
                                "map" -> {
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        MapScreen(
                                            rooms = viewModel.filteredRooms,
                                            selectedRoom = viewModel.selectedRoom,
                                            authState = authState,
                                            routingDestination = routingDestination,
                                            isRefreshing = viewModel.isLoading,
                                            activeFilters = if (viewModel.filterArea != null || viewModel.filterType != null || viewModel.filterMaxPrice != null) {
                                                buildString {
                                                    viewModel.filterArea?.let { append(it) }
                                                    viewModel.filterType?.let { if (isNotEmpty()) append(" • "); append(it) }
                                                    viewModel.filterMaxPrice?.let { if (isNotEmpty()) append(" • "); append("<${it.toInt() / 1000}k") }
                                                }
                                            } else null,
                                            onRefresh = viewModel::loadRooms,
                                            onClearFilters = viewModel::clearFilters,
                                            onClearRoute = {
                                                routingDestination = null
                                            },
                                            onRoomSelected = { room ->
                                                viewModel.selectRoom(room)
                                            },
                                            onClearSelection = {
                                                viewModel.clearSelectedRoom()
                                            },
                                            onViewProperty = { room ->
                                                viewProperty(room)
                                            },
                                            onNavigate = { route ->
                                                navigateTo(route)
                                            },
                                            modifier = Modifier
                                        )
                                    }
                                }
                                "ownerdashboard" -> {
                                    val user = (authState as AuthState.Authenticated).user
                                    OwnerDashboardScreen(
                                        ownerName = user.name,
                                        profileImage = user.profileImage,
                                        properties = viewModel.rooms.filter { it.postedBy == user.id || it.ownerName == user.name },
                                        bookings = ownerBookings,
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
                                        onLogout = {
                                            scope.launch {
                                                authManager.logout()
                                                currentRoute = "map"
                                            }
                                        },
                                        onViewProperty = { room ->
                                            viewProperty(room)
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
                                        tenantName = user.name,
                                        profileImage = user.profileImage,
                                        bookings = tenantBookings,
                                        allRooms = viewModel.rooms,
                                        isRefreshing = viewModel.isLoading,
                                        onExploreRooms = {
                                            currentRoute = "map"
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
                                        onNavigate = { route -> navigateTo(route) }
                                    )
                                }
                                else -> {
                                    // Default - show map
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        MapScreen(
                                            rooms = viewModel.filteredRooms,
                                            selectedRoom = viewModel.selectedRoom,
                                            authState = authState,
                                            routingDestination = routingDestination,
                                            isRefreshing = viewModel.isLoading,
                                            activeFilters = if (viewModel.filterArea != null || viewModel.filterType != null || viewModel.filterMaxPrice != null) {
                                                buildString {
                                                    viewModel.filterArea?.let { append(it) }
                                                    viewModel.filterType?.let { if (isNotEmpty()) append(" • "); append(it) }
                                                    viewModel.filterMaxPrice?.let { if (isNotEmpty()) append(" • "); append("<${it.toInt() / 1000}k") }
                                                }
                                            } else null,
                                            onRefresh = viewModel::loadRooms,
                                            onClearFilters = viewModel::clearFilters,
                                            onClearRoute = {
                                                routingDestination = null
                                            },
                                            onRoomSelected = { room ->
                                                viewModel.selectRoom(room)
                                            },
                                            onClearSelection = {
                                                viewModel.clearSelectedRoom()
                                            },
                                            onViewProperty = { room ->
                                                viewProperty(room)
                                            },
                                            onNavigate = { route ->
                                                navigateTo(route)
                                            },
                                            modifier = Modifier
                                        )
                                    }
                                }
                            }
                        }
                    }

                    is AuthState.Error -> {
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
                                DiscoveryDashboard(
                                    onSearch = { type, area, price ->
                                        viewModel.setFilters(type, area, price)
                                        currentRoute = "map"
                                    }
                                )
                            }
                            else -> {
                                // Public map
                                MapScreen(
                                    rooms = viewModel.filteredRooms,
                                    selectedRoom = viewModel.selectedRoom,
                                    authState = authState,
                                    routingDestination = routingDestination,
                                    onClearRoute = {
                                        routingDestination = null
                                    },
                                    onRoomSelected = viewModel::selectRoom,
                                    onClearSelection = viewModel::clearSelectedRoom,
                                    onViewProperty = ::viewProperty,
                                    onNavigate = ::navigateTo,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

