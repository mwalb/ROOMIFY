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
import org.com.ui.auth.LoginScreen
import org.com.ui.auth.RegisterScreen
import org.com.viewmodel.MapViewModel
import org.com.viewmodel.PostRoomViewModel

@Composable
fun App() {

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

    val isLoggedIn = authState is AuthState.Authenticated

    val platformContext = LocalPlatformContext.current

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
                // Case 0: User is an Owner - go to postroom as requested
                authenticatedState.user.role.equals("OWNER", ignoreCase = true) -> {
                    println("App: 🏠 Owner logged in - Navigating to PostRoom")
                    loadOwnerBookings()
                    currentRoute = "postroom"
                    postLoginDestination = null
                }
                // Case 0.5: User is a Dalali - go to dalali dashboard
                authenticatedState.user.role.equals("DALALI", ignoreCase = true) -> {
                    println("App: 🤝 Dalali logged in - Navigating to Dalali Dashboard")
                    loadOwnerBookings()
                    currentRoute = "dalalidashboard"
                    postLoginDestination = null
                }
                // Case 2: User came from Login/Register button - show TenantScreen
                postLoginDestination == "tenant" -> {
                    println("App: 👤 Navigating to TenantScreen")
                    loadTenantBookings()
                    currentRoute = "tenant"
                    postLoginDestination = null
                }
                // Case 3: Default - show map
                else -> {
                    println("App: 🗺️ Navigating to MapScreen")
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
                // User wants to login - set postLoginDestination to "tenant"
                postLoginDestination = "tenant"
                currentRoute = "login"
            }
            "map", "explore" -> {
                currentRoute = "map"
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
        if (isLoggedIn) {
            // Already logged in - show details directly
            pendingRoom = room
            currentRoute = "details"
        } else {
            // Not logged in - store room and go to login
            pendingRoom = room
            // Set destination to "details" so after login we go to property details
            postLoginDestination = "details"
            currentRoute = "login"
        }
    }

    // ============================================================
    // MAIN UI
    // ============================================================

    RoomifyLocalization {
        MaterialTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Transparent,
                tonalElevation = 0.dp
            ) {
                when (authState) {
                    is AuthState.Loading -> {
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

                    is AuthState.Authenticated -> {
                        // ================================================
                        // USER IS LOGGED IN
                        // ================================================

                        // Check if user should see property details
                        if (currentRoute == "details" && pendingRoom != null) {
                            PropertyDetailsScreen(
                                room = pendingRoom!!,
                                currentUser = (authState as AuthState.Authenticated).user,
                                otherProperties = viewModel.rooms.filter { it.postedBy == pendingRoom?.postedBy && it.id != pendingRoom?.id },
                                onBack = {
                                    pendingRoom = null
                                    postLoginDestination = null
                                    currentRoute = "map"
                                },
                                onBookNow = { room ->
                                    pendingRoom = room
                                    currentRoute = "booking"
                                },
                                onMessageOwner = { room ->
                                    pendingRoom = room
                                    currentRoute = "chat"
                                },
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
                        } else if (currentRoute == "booking" && pendingRoom != null) {
                            BookingScreen(
                                room = pendingRoom!!,
                                onBack = { currentRoute = "details" },
                                onConfirmBooking = { booking ->
                                    scope.launch {
                                        bookingApi.createBooking(booking.copy(userId = (authState as AuthState.Authenticated).user.id))
                                    }
                                }
                            )
                        } else if (currentRoute == "chat" && pendingRoom != null) {
                            ChatScreen(
                                currentUser = (authState as AuthState.Authenticated).user,
                                otherUserName = pendingRoom?.ownerName ?: "Owner",
                                onBack = { currentRoute = "details" }
                            )
                        } else {
                            // Show main app
                            when (currentRoute) {
                                "map" -> {
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        MapScreen(
                                            rooms = viewModel.rooms,
                                            selectedRoom = viewModel.selectedRoom,
                                            authState = authState,
                                            routingDestination = routingDestination,
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
                                        properties = viewModel.rooms,
                                        bookings = ownerBookings,
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
                                        properties = viewModel.rooms.filter { it.dalaliName == user.name },
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
                                            rooms = viewModel.rooms,
                                            selectedRoom = viewModel.selectedRoom,
                                            authState = authState,
                                            routingDestination = routingDestination,
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
                                onLogin = { email, password, role ->
                                    println("App: 🔐 Login called for $email")
                                    scope.launch { authManager.login(email, password, role) }
                                },
                                onGoogleLogin = { idToken ->
                                    println("App: 🔐 Google login called")
                                    scope.launch { authManager.googleLogin(idToken) }
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
                        } else if (currentRoute == "login") {
                            LoginScreen(
                                authState = authState,
                                onLogin = { email, password, role ->
                                    println("App: 🔐 Login called for $email")
                                    scope.launch { authManager.login(email, password, role) }
                                },
                                onGoogleLogin = { idToken ->
                                    println("App: 🔐 Google login called")
                                    scope.launch { authManager.googleLogin(idToken) }
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
                        } else {
                            // Public map
                            MapScreen(
                                rooms = viewModel.rooms,
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

