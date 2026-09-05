package org.com.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Bathtub
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.com.i18n.LocalRoomifyStrings
import org.com.i18n.LocalLocalizationManager
import org.com.i18n.Language
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import org.com.model.*
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.suspendCancellableCoroutine
import androidx.compose.runtime.mutableStateListOf
import org.com.model.*
import kotlin.coroutines.resume
import kotlin.math.roundToInt


/*
 * ============================================================
 * ROOMIFY COLORS
 * ============================================================
 */

private const val ROOMIFY_BLUE = 0xFF1976D2

private val RoomifyGradientStart =
    Color(0xFF1A237E)

private val RoomifyGradientEnd =
    Color(0xFF3949AB)

private val RoomifyWhite =
    Color(0xFFFFFFFF)

private val RoomifyWhite95 =
    Color(0xF2FFFFFF)

private val RoomifyWhite90 =
    Color(0xE6FFFFFF)

private val RoomifyWhite80 =
    Color(0xCCFFFFFF)

private val RoomifyWhite70 =
    Color(0xB3FFFFFF)

private val RoomifyWhite55 =
    Color(0x8CFFFFFF)

private val RoomifyWhite25 =
    Color(0x40FFFFFF)

private val RoomifyWhite18 =
    Color(0x2EFFFFFF)

private val RoomifyWhite12 =
    Color(0x1FFFFFFF)

private val RoomifyGreen =
    Color(0xFF2E7D32)

private val RoomifyYellow =
    Color(0xFFF9A825)

private val RoomifyRed =
    Color(0xFFC62828)

private val RoomifyOrange =
    Color(0xFFFF9800)


private const val ROOMIFY_GREEN = 0xFF2E7D32
private const val ROOMIFY_YELLOW = 0xFFF9A825
private const val ROOMIFY_RED = 0xFFC62828
private const val ROOMIFY_ORANGE = 0xFFFF9800


/*
 * ============================================================
 * STATUS COLOR
 * ============================================================
 */

private fun statusColor(
    status: String?,
    selected: Boolean
): Int {

    if (selected) {
        return ROOMIFY_ORANGE.toInt()
    }

    return when (
        status?.uppercase() ?: "AVAILABLE"
    ) {

        "AVAILABLE" ->
            ROOMIFY_GREEN.toInt()

        "PENDING" ->
            ROOMIFY_YELLOW.toInt()

        "RENTED" ->
            ROOMIFY_RED.toInt()

        else ->
            ROOMIFY_BLUE.toInt()
    }
}


/*
 * ============================================================
 * STATUS TEXT
 * ============================================================
 */

private fun statusText(
    status: String?
): String {

    return when (
        status?.uppercase() ?: "AVAILABLE"
    ) {

        "AVAILABLE" ->
            "Available"

        "PENDING" ->
            "Pending"

        "RENTED" ->
            "Rented"

        else ->
            status
                ?.lowercase()
                ?.replaceFirstChar {
                    it.uppercase()
                }
                ?: "Available"
    }
}


/*
 * ============================================================
 * FULL PRICE
 * ============================================================
 */

private fun formatFullPrice(
    price: Double
): String {

    if (
        !price.isFinite() ||
        price <= 0.0
    ) {
        return "Price on request"
    }

    return "TZS ${
        price
            .roundToInt()
            .toStringWithCommas()
    } / month"
}


/*
 * ============================================================
 * COMPACT PRICE
 * ============================================================
 */

private fun formatCompactPrice(
    price: Double
): String {

    if (
        !price.isFinite() ||
        price <= 0.0
    ) {
        return "Price"
    }

    return when {

        price >= 1_000_000.0 -> {

            val value =
                ((price / 1_000_000.0) * 100.0)
                    .roundToInt() / 100.0

            "TZS ${value.removeTrailingZeros()}M"
        }

        price >= 1_000.0 -> {

            val value =
                ((price / 1_000.0) * 100.0)
                    .roundToInt() / 100.0

            "TZS ${value.removeTrailingZeros()}K"
        }

        else -> {

            "TZS ${
                price
                    .roundToInt()
                    .toStringWithCommas()
            }"
        }
    }
}


/*
 * ============================================================
 * NUMBER FORMAT HELPERS
 * ============================================================
 */

private fun Int.toStringWithCommas(): String {

    return toString()
        .reversed()
        .chunked(3)
        .joinToString(",")
        .reversed()
}


private fun Double.removeTrailingZeros(): String {

    return if (
        this % 1.0 == 0.0
    ) {

        toInt().toString()

    } else {

        toString()
    }
}


/*
 * ============================================================
 * PRICE MARKER
 * ============================================================
 */

private fun createPriceMarker(
    room: Room,
    selected: Boolean
): BitmapDescriptor {

    val color =
        statusColor(
            status = room.status,
            selected = selected
        )

    val status =
        room.status
            ?.uppercase()
            ?: "AVAILABLE"

    val alpha =
        if (
            !selected &&
            status == "RENTED"
        ) {
            140
        } else {
            255
        }

    val text =
        formatCompactPrice(
            room.price
        )

    val width = 192
    val height = 92

    val bitmap =
        Bitmap.createBitmap(
            width,
            height,
            Bitmap.Config.ARGB_8888
        )

    val canvas =
        Canvas(bitmap)

    val paint =
        Paint(
            Paint.ANTI_ALIAS_FLAG
        )

    paint.color =
        android.graphics.Color.argb(
            alpha,

            android.graphics.Color.red(
                color
            ),

            android.graphics.Color.green(
                color
            ),

            android.graphics.Color.blue(
                color
            )
        )

    val left = 8f
    val top = 8f
    val right = 184f
    val bottom = 58f
    val radius = 25f

    canvas.drawRoundRect(
        left,
        top,
        right,
        bottom,
        radius,
        radius,
        paint
    )

    val pointer =
        Path().apply {

            moveTo(
                80f,
                bottom
            )

            lineTo(
                96f,
                78f
            )

            lineTo(
                112f,
                bottom
            )

            close()
        }

    canvas.drawPath(
        pointer,
        paint
    )

    paint.color =
        android.graphics.Color.WHITE

    paint.alpha =
        255

    paint.textSize =
        22f

    paint.typeface =
        Typeface.create(
            Typeface.DEFAULT,
            Typeface.BOLD
        )

    paint.textAlign =
        Paint.Align.CENTER

    canvas.drawText(
        text,
        96f,
        43f,
        paint
    )

    return BitmapDescriptorFactory.fromBitmap(
        bitmap
    )
}


/*
 * ============================================================
 * MAIN ANDROID MAP
 * ============================================================
 */

@Composable
actual fun MapContent(
    rooms: List<Room>,
    selectedRoom: Room?,
    authState: org.com.auth.AuthState,
    routingDestination: Room?,
    onClearRoute: () -> Unit,
    onRoomSelected: (Room) -> Unit,
    onRoomCleared: () -> Unit,
    onViewProperty: (Room) -> Unit,
    onNavigate: (String) -> Unit
) {

    /*
     * ========================================================
     * MAP CAMERA
     * ========================================================
     */

    val cameraPositionState =
        rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(
                LatLng(-6.7924, 39.2083),
                12f
            )
        }

    val routePoints = remember { mutableStateListOf<LatLng>() }
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(routingDestination?.id) {
        if (routingDestination == null) {
            routePoints.clear()
            return@LaunchedEffect
        }

        val dest = routingDestination
        if (dest.latitude != 0.0 && dest.longitude != 0.0) {
            try {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                @Suppress("MissingPermission")
                val location = suspendCancellableCoroutine<android.location.Location?> { continuation ->
                    fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                        continuation.resume(loc)
                    }.addOnFailureListener {
                        continuation.resume(null)
                    }
                }
                
                if (location != null) {
                    routePoints.clear()
                    routePoints.add(LatLng(location.latitude, location.longitude))
                    routePoints.add(LatLng(dest.latitude, dest.longitude))
                    
                    // Zoom to fit route
                    val bounds = com.google.android.gms.maps.model.LatLngBounds.builder()
                        .include(LatLng(location.latitude, location.longitude))
                        .include(LatLng(dest.latitude, dest.longitude))
                        .build()
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngBounds(bounds, 100),
                        1000
                    )
                }
            } catch (e: Exception) {
                println("Routing error: ${e.message}")
            }
        }
    }


    /*
     * ========================================================
     * MENU STATE
     * ========================================================
     *
     * This state belongs entirely to Android MapContent.
     *
     * Therefore the web MapContent is not affected.
     */

    var menuOpen by remember {
        mutableStateOf(false)
    }


    /*
     * ========================================================
     * SEARCH STATE
     * ========================================================
     */

    var searchQuery by remember {
        mutableStateOf("")
    }
    /*
     * ========================================================
     * ROUTING LOGIC
     * ========================================================
     */

    val controller = createMapController(
        onLocationSelected = {}, // Not used here
        onError = {}
    )

    LaunchedEffect(routingDestination?.id) {
        routingDestination?.let { room ->
            if (room.latitude != 0.0 && room.longitude != 0.0) {
                val origin = controller.getCurrentLocation()
                controller.showRoute(
                    origin = org.com.ui.LatLng(origin.latitude, origin.longitude),
                    destination = org.com.ui.LatLng(room.latitude, room.longitude)
                )
            } else {
                controller.clearRoute()
            }
        } ?: run {
            controller.clearRoute()
        }
    }


    /*
     * ========================================================
     * SEARCH FOCUS
     * ========================================================
     */

    val searchFocusRequester =
        remember {
            FocusRequester()
        }


    /*
     * ========================================================
     * MOVE CAMERA TO SELECTED ROOM
     * ========================================================
     */

    LaunchedEffect(
        selectedRoom?.id
    ) {

        selectedRoom?.let { room ->

            if (
                room.latitude != 0.0 &&
                room.longitude != 0.0
            ) {

                cameraPositionState.animate(

                    CameraUpdateFactory.newLatLngZoom(

                        LatLng(
                            room.latitude,
                            room.longitude
                        ),

                        15.5f
                    ),

                    700
                )
            }
        }
    }


    /*
     * ========================================================
     * MAIN ROOT
     * ========================================================
     */

    Box(
        modifier =
            Modifier.fillMaxSize()
    ) {


        /*
         * ====================================================
         * GOOGLE MAP
         * ====================================================
         */

        GoogleMap(

            modifier =
                Modifier.fillMaxSize(),

            cameraPositionState =
                cameraPositionState,

            properties =
                MapProperties(

                    isBuildingEnabled =
                        true,

                    isIndoorEnabled =
                        true,

                    isTrafficEnabled =
                        false,

                    isMyLocationEnabled =
                        false
                ),

            uiSettings =
                MapUiSettings(

                    scrollGesturesEnabled =
                        true,

                    zoomGesturesEnabled =
                        true,

                    rotationGesturesEnabled =
                        true,

                    tiltGesturesEnabled =
                        true,

                    zoomControlsEnabled =
                        false,

                    compassEnabled =
                        true,

                    myLocationButtonEnabled =
                        false,

                    mapToolbarEnabled =
                        false
                ),

            onMapClick = {
                onClearRoute()
                onRoomCleared()
            }
        ) {

            if (routePoints.isNotEmpty()) {
                Polyline(
                    points = routePoints,
                    color = Color(0xFF1A237E),
                    width = 8f
                )
            }


            /*
             * =================================================
             * ROOM MARKERS
             * =================================================
             */

            rooms.forEach { room ->

                if (
                    room.latitude == 0.0 ||
                    room.longitude == 0.0
                ) {

                    return@forEach
                }

                val position =
                    LatLng(
                        room.latitude,
                        room.longitude
                    )

                val isSelected =
                    selectedRoom?.id == room.id

                val markerState =
                    remember(
                        room.id
                    ) {

                        MarkerState(
                            position =
                                position
                        )
                    }

                LaunchedEffect(
                    position
                ) {

                    markerState.position =
                        position
                }

                val markerIcon =
                    remember(
                        room.id,
                        room.price,
                        room.status,
                        isSelected
                    ) {

                        createPriceMarker(
                            room =
                                room,

                            selected =
                                isSelected
                        )
                    }

                Marker(

                    state =
                        markerState,

                    icon =
                        markerIcon,

                    title =
                        room.title
                            ?.takeIf {
                                it.isNotBlank()
                            }
                            ?: "Room",

                    tag =
                        room,

                    onClick = {

                        println(
                            "Roomify Android: marker clicked ${room.id}"
                        )

                        onRoomSelected(
                            room
                        )

                        true
                    }
                )
            }
        }


        /*
         * ========================================================
         * MAP HEADER
         * ========================================================
         *
         * The header is positioned above the map.
         *
         * It does NOT belong to MapScreen.
         */

        MapHeader(

            menuOpen =
                menuOpen,

            searchQuery =
                searchQuery,

            onMenuClick = {

                menuOpen =
                    !menuOpen

                println(
                    "Roomify Android: menuOpen = $menuOpen"
                )
            },

            onSearchChange = { value ->

                searchQuery =
                    value

                println(
                    "Roomify Android: search = $value"
                )
            },

            onSearchClick = {

                /*
                 * Explicitly request focus.
                 *
                 * This fixes the issue where tapping
                 * the search box did not place the cursor.
                 */

                searchFocusRequester.requestFocus()
            },

            searchFocusRequester =
                searchFocusRequester
        )


        /*
         * ========================================================
         * ANDROID SIDEBAR
         * ========================================================
         */

        if (menuOpen) {

            RoomifySideBar(
                authState = authState,
                onClose = {
                    menuOpen = false
                },
                onExplore = {
                    menuOpen = false
                    onRoomCleared()
                    onNavigate("map")
                },
                onSavedProperties = {
                    menuOpen = false
                    onNavigate("saved")
                },
                onMySearches = {
                    menuOpen = false
                    onNavigate("searches")
                },
                onFilters = {
                    menuOpen = false
                    onNavigate("filters")
                },
                onLogin = {
                    menuOpen = false
                    onNavigate("login")
                },
                onNavigate = { route ->
                    menuOpen = false
                    onNavigate(route)
                }
            )
        }


        /*
         * ========================================================
         * PROPERTY POPUP
         * ========================================================
         */

        selectedRoom?.let { room ->

            RoomPropertyPopup(

                room =
                    room,

                onClose = {

                    println(
                        "Roomify Android: closing property popup ${room.id}"
                    )

                    onRoomCleared()
                },

                onViewProperty = {

                    println(
                        "Roomify Android: View Property ${room.id}"
                    )

                    onViewProperty(
                        room
                    )
                }
            )
        }
    }
}


/*
 * ============================================================
 * MAP HEADER
 * ============================================================
 */

@Composable
private fun MapHeader(
    menuOpen: Boolean,
    searchQuery: String,
    onMenuClick: () -> Unit,
    onSearchChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    searchFocusRequester: FocusRequester
) {
    val strings = LocalRoomifyStrings.current

    Row(

        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    start = 14.dp,
                    end = 14.dp,
                    top = 18.dp
                ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {


        /*
         * ====================================================
         * MENU BUTTON
         * ====================================================
         */

        Surface(

            modifier =
                Modifier
                    .size(52.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = CircleShape
                    )
                    .clip(
                        CircleShape
                    )
                    .clickable(
                        onClick =
                            onMenuClick
                    ),

            shape =
                CircleShape,

            color =
                RoomifyGradientStart
                    .copy(
                        alpha = 0.94f
                    )
        ) {

            Box(
                contentAlignment =
                    Alignment.Center
            ) {

                Icon(

                    imageVector =
                        if (menuOpen) {
                            Icons.Default.Close
                        } else {
                            Icons.Default.Menu
                        },

                    contentDescription =
                        if (menuOpen) {
                            "Close menu"
                        } else {
                            "Open menu"
                        },

                    tint =
                        RoomifyWhite,

                    modifier =
                        Modifier.size(
                            24.dp
                        )
                )
            }
        }


        Spacer(
            modifier =
                Modifier.width(
                    10.dp
                )
        )


        /*
         * ====================================================
         * SEARCH BAR
         * ====================================================
         */

        Surface(

            modifier =
                Modifier
                    .weight(1f)
                    .height(52.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape =
                            RoundedCornerShape(
                                18.dp
                            )
                    ),

            shape =
                RoundedCornerShape(
                    18.dp
                ),

            color =
                RoomifyWhite
                    .copy(
                        alpha = 0.96f
                    ),

            border = BorderStroke(1.dp, Color(0xFFBDBDBD))
        ) {

            Row(

                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(
                            horizontal = 14.dp
                        )
                        .clickable(
                            onClick =
                                onSearchClick
                        ),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Icon(

                    imageVector =
                        Icons.Default.Search,

                    contentDescription =
                        "Search",

                    tint =
                        RoomifyGradientStart
                            .copy(
                                alpha = 0.78f
                            ),

                    modifier =
                        Modifier.size(
                            22.dp
                        )
                )


                Spacer(
                    modifier =
                        Modifier.width(
                            10.dp
                        )
                )


                /*
                 * =================================================
                 * REAL EDITABLE SEARCH FIELD
                 * =================================================
                 *
                 * BasicTextField ensures:
                 *
                 * - cursor appears
                 * - text is editable
                 * - tapping works
                 * - typed text is visible
                 */

                BasicTextField(

                    value =
                        searchQuery,

                    onValueChange =
                        onSearchChange,

                    modifier =
                        Modifier
                            .weight(1f)
                            .focusRequester(
                                searchFocusRequester
                            )
                            .focusable(),

                    singleLine =
                        true,

                    textStyle =
                        androidx.compose.ui.text.TextStyle(

                            color =
                                RoomifyGradientStart,

                            fontSize =
                                14.sp,

                            fontWeight =
                                FontWeight.Medium
                        ),

                    decorationBox = { innerTextField ->

                        Box {

                            if (
                                searchQuery.isEmpty()
                            ) {

                                Text(

                                    text =
                                        strings.searchPlaceholder,

                                    color =
                                        RoomifyGradientStart
                                            .copy(
                                                alpha = 0.52f
                                            ),

                                    fontSize =
                                        13.sp,

                                    maxLines =
                                        1,

                                    overflow =
                                        TextOverflow.Ellipsis
                                )
                            }

                            innerTextField()
                        }
                    }
                )


                /*
                 * =================================================
                 * CLEAR SEARCH BUTTON
                 * =================================================
                 */

                if (
                    searchQuery.isNotEmpty()
                ) {

                    Box(

                        modifier =
                            Modifier
                                .size(
                                    32.dp
                                )
                                .clip(
                                    CircleShape
                                )
                                .clickable {

                                    onSearchChange(
                                        ""
                                    )
                                },

                        contentAlignment =
                            Alignment.Center
                    ) {

                        Icon(

                            imageVector =
                                Icons.Default.Close,

                            contentDescription =
                                "Clear search",

                            tint =
                                RoomifyGradientStart
                                    .copy(
                                        alpha = 0.70f
                                    ),

                            modifier =
                                Modifier.size(
                                    18.dp
                                )
                        )
                    }
                }
            }
        }
    }
}


/*
 * ============================================================
 * ROOMIFY SIDEBAR - MATCHES WEB STYLE
 * ============================================================
 */

@Composable
private fun RoomifySideBar(
    authState: org.com.auth.AuthState,
    onClose: () -> Unit,
    onExplore: () -> Unit,
    onSavedProperties: () -> Unit,
    onMySearches: () -> Unit,
    onFilters: () -> Unit,
    onLogin: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val strings = LocalRoomifyStrings.current
    val localizationManager = LocalLocalizationManager.current

    /*
     * ========================================================
     * SIDEBAR ROOT
     * ========================================================
     *
     * The sidebar is placed over the Android map only.
     */

    Box(
        modifier =
            Modifier.fillMaxSize()
    ) {


        /*
         * ====================================================
         * DARK SCRIM
         * ====================================================
         */

        Box(

            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        Color.Black
                            .copy(
                                alpha = 0.22f
                            )
                    )
                    .clickable(
                        onClick =
                            onClose
                    )
        )


        /*
         * ====================================================
         * SIDEBAR
         * ====================================================
         */

        Surface(

            modifier =
                Modifier
                    .fillMaxHeight()
                    .width(
                        315.dp
                    )
                    .shadow(
                        elevation = 22.dp
                    ),

            color =
                Color.Transparent
        ) {

            Column(

                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(
                            brush =
                                Brush.verticalGradient(
                                    colors =
                                        listOf(

                                            RoomifyGradientStart
                                                .copy(
                                                    alpha = 0.98f
                                                ),

                                            RoomifyGradientEnd
                                                .copy(
                                                    alpha = 0.97f
                                                )
                                        )
                                )
                        )
                        .padding(
                            horizontal = 18.dp
                        )
            ) {


                /*
                 * =================================================
                 * SIDEBAR HEADER - MATCHES WEB
                 * =================================================
                 */

                Row(

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                top = 22.dp,
                                bottom = 22.dp
                            ),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    /*
                     * Brand text only - matches web
                     */
                    Text(

                        text =
                            "ROOMIFY",

                        color =
                            RoomifyWhite,

                        fontSize =
                            21.sp,

                        fontWeight =
                            FontWeight.ExtraBold,

                        letterSpacing =
                            1.5.sp,

                        modifier =
                            Modifier.weight(
                                1f
                            )
                    )


                    /*
                     * =================================================
                     * WORKING X BUTTON
                     * =================================================
                     */

                    Surface(

                        modifier =
                            Modifier
                                .size(
                                    38.dp
                                )
                                .clip(
                                    CircleShape
                                )
                                .clickable(
                                    onClick =
                                        onClose
                                ),

                        color =
                            RoomifyWhite
                                .copy(
                                    alpha = 0.12f
                                ),

                        shape =
                            CircleShape
                    ) {

                        Box(
                            contentAlignment =
                                Alignment.Center
                        ) {

                            Icon(

                                imageVector =
                                    Icons.Default.Close,

                                contentDescription =
                                    "Close menu",

                                tint =
                                    RoomifyWhite,

                                modifier =
                                    Modifier.size(
                                        20.dp
                                    )
                            )
                        }
                    }
                }


                /*
                 * =================================================
                 * SECTION 1: DISCOVER / DASHBOARD
                 * =================================================
                 */

                val user = (authState as? org.com.auth.AuthState.Authenticated)?.user

                Text(
                    text = if (user != null) "MENU" else "DISCOVER",
                    color = RoomifyWhite55,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.3.sp,
                    modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                )

                // Common: Explore
                SidebarItem(
                    title = "Explore",
                    subtitle = "Discover available properties",
                    selected = true,
                    onClick = onExplore
                )

                if (user != null && user.isTenant()) {
                    // Tenant Specific
                    SidebarItem(
                        title = "Dashboard",
                        subtitle = "My activity overview",
                        selected = false,
                        onClick = { onNavigate("tenant") }
                    )
                    SidebarItem(
                        title = "Saved Rooms",
                        subtitle = "Rooms you liked",
                        selected = false,
                        onClick = { onNavigate("saved") }
                    )
                    SidebarItem(
                        title = "My Bookings",
                        subtitle = "Manage your rentals",
                        selected = false,
                        onClick = { onNavigate("bookings") }
                    )
                    SidebarItem(
                        title = "Messages",
                        subtitle = "Chat with owners",
                        selected = false,
                        onClick = { onNavigate("messages") }
                    )
                } else if (user == null) {
                    // Guest Specific
                    SidebarItem(
                        title = strings.savedRooms,
                        subtitle = "Properties you've bookmarked",
                        selected = false,
                        onClick = onSavedProperties
                    )
                    SidebarItem(
                        title = strings.mySearches,
                        subtitle = "View your search history",
                        selected = false,
                        onClick = onMySearches
                    )
                } else if (user != null && user.isOwner()) {
                    // Owner Specific
                    SidebarItem(
                        title = "Owner Dashboard",
                        subtitle = "Manage your properties",
                        selected = false,
                        onClick = { onNavigate("ownerdashboard") }
                    )
                    SidebarItem(
                        title = "Post a Room",
                        subtitle = "Add new listing",
                        selected = false,
                        onClick = { onNavigate("postroom") }
                    )
                } else if (user != null && user.isAdmin()) {
                    // Admin Specific
                    SidebarItem(
                        title = "Admin Panel",
                        subtitle = "System administration",
                        selected = false,
                        onClick = { onNavigate("admindashboard") }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                /*
                 * =================================================
                 * SECTION 2: SEARCH / ACCOUNT
                 * =================================================
                 */

                Text(
                    text = if (user != null) strings.account else strings.search,
                    color = RoomifyWhite55,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.3.sp,
                    modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                )

                if (user == null) {
                    SidebarItem(
                        title = strings.filters,
                        subtitle = "Price, property type and more",
                        selected = false,
                        onClick = onFilters
                    )
                } else {
                    SidebarItem(
                        title = strings.profile,
                        subtitle = "Account settings",
                        selected = false,
                        onClick = { onNavigate("profile") }
                    )
                    SidebarItem(
                        title = strings.logout,
                        subtitle = "Sign out of your account",
                        selected = false,
                        onClick = { onNavigate("logout") }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = strings.language,
                    color = RoomifyWhite55,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.3.sp,
                    modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                )

                LanguageSelector(
                    currentLanguage = localizationManager.currentLanguage,
                    onLanguageChange = { localizationManager.changeLanguage(it) }
                )

                Spacer(modifier = Modifier.weight(1f))


                /*
                 * =================================================
                 * SECTION 3: ACCOUNT BOX (Login/Register)
                 * =================================================
                 */

                if (user == null) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp)
                            .clickable(onClick = onLogin),
                        shape = RoundedCornerShape(17.dp),
                        color = RoomifyWhite.copy(alpha = 0.09f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(RoomifyWhite.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "Account",
                                    tint = RoomifyWhite,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Login / Register",
                                    color = RoomifyWhite,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Access your saved properties and searches",
                                    color = RoomifyWhite70,
                                    fontSize = 10.5.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = RoomifyWhite55,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                } else {
                    // Show User Info box
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                        shape = RoundedCornerShape(17.dp),
                        color = RoomifyWhite.copy(alpha = 0.09f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(RoomifyWhite.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = user.initials,
                                    color = RoomifyWhite,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = user.displayName,
                                    color = RoomifyWhite,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = user.email,
                                    color = RoomifyWhite70,
                                    fontSize = 10.5.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


/*
 * ============================================================
 * SIDEBAR ITEM
 * ============================================================
 */

@Composable
private fun SidebarItem(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {

    Row(

        modifier =
            Modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        16.dp
                    )
                )
                .background(

                    if (selected) {

                        RoomifyWhite
                            .copy(
                                alpha = 0.14f
                            )

                    } else {

                        Color.Transparent
                    }
                )
                .clickable(
                    onClick =
                        onClick
                )
                .padding(
                    horizontal = 16.dp,
                    vertical = 14.dp
                ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        /*
         * TEXT
         */

        Column(
            modifier =
                Modifier.weight(
                    1f
                )
        ) {

            Text(

                text =
                    title,

                color =
                    RoomifyWhite,

                fontSize =
                    15.sp,

                fontWeight =
                    FontWeight.Bold
            )

            Spacer(
                modifier =
                    Modifier.height(
                        2.dp
                    )
            )

            Text(

                text =
                    subtitle,

                color =
                    RoomifyWhite70,

                fontSize =
                    11.sp,

                maxLines =
                    1,

                overflow =
                    TextOverflow.Ellipsis
            )
        }


        /*
         * ARROW INDICATOR - Matches web
         */

        Text(
            text = "→",
            fontSize = 14.sp,
            color = RoomifyWhite55.copy(alpha = 0.30f),
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}


/*
 * ============================================================
 * ROOM PROPERTY POPUP
 * ============================================================
 */

@Composable
private fun RoomPropertyPopup(
    room: Room,
    onClose: () -> Unit,
    onViewProperty: () -> Unit
) {

    Box(

        modifier =
            Modifier
                .fillMaxSize()
                .padding(
                    start = 14.dp,
                    end = 14.dp,
                    bottom = 20.dp
                ),

        contentAlignment =
            Alignment.BottomCenter
    ) {

        Card(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 18.dp,
                        shape =
                            RoundedCornerShape(
                                26.dp
                            )
                    ),

            shape =
                RoundedCornerShape(
                    26.dp
                ),

            colors =
                CardDefaults.cardColors(
                    containerColor =
                        Color.Transparent
                ),

            elevation =
                CardDefaults.cardElevation(
                    defaultElevation =
                        0.dp
                )
        ) {

            Column(

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(
                            RoundedCornerShape(
                                26.dp
                            )
                        )
                        .background(
                            brush =
                                Brush.verticalGradient(
                                    colors =
                                        listOf(

                                            RoomifyGradientStart
                                                .copy(
                                                    alpha = 0.97f
                                                ),

                                            RoomifyGradientEnd
                                                .copy(
                                                    alpha = 0.95f
                                                )
                                        )
                                )
                        )
                        .border(
                            width = 1.dp,

                            color =
                                RoomifyWhite
                                    .copy(
                                        alpha = 0.16f
                                    ),

                            shape =
                                RoundedCornerShape(
                                    26.dp
                                )
                        )
                        .padding(
                            16.dp
                        ),

                verticalArrangement =
                    Arrangement.spacedBy(
                        11.dp
                    )
            ) {

                Row(

                    modifier =
                        Modifier.fillMaxWidth(),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Column(
                        modifier =
                            Modifier.weight(
                                1f
                            )
                    ) {

                        Text(

                            text =
                                room.propertyType
                                    ?.takeIf {
                                        it.isNotBlank()
                                    }
                                    ?: "Property",

                            color =
                                RoomifyWhite80,

                            fontSize =
                                11.sp,

                            fontWeight =
                                FontWeight.SemiBold,

                            letterSpacing =
                                0.6.sp
                        )

                        Spacer(
                            modifier =
                                Modifier.height(
                                    2.dp
                                )
                        )

                        Text(

                            text =
                                room.title
                                    ?.takeIf {
                                        it.isNotBlank()
                                    }
                                    ?: "Room",

                            color =
                                RoomifyWhite,

                            fontSize =
                                18.sp,

                            fontWeight =
                                FontWeight.Bold,

                            maxLines =
                                1,

                            overflow =
                                TextOverflow.Ellipsis
                        )
                    }


                    Surface(

                        modifier =
                            Modifier
                                .size(
                                    38.dp
                                )
                                .clip(
                                    CircleShape
                                )
                                .clickable(
                                    onClick =
                                        onClose
                                ),

                        color =
                            RoomifyWhite
                                .copy(
                                    alpha = 0.13f
                                ),

                        shape =
                            CircleShape
                    ) {

                        Box(
                            contentAlignment =
                                Alignment.Center
                        ) {

                            Icon(

                                imageVector =
                                    Icons.Default.Close,

                                contentDescription =
                                    "Close",

                                tint =
                                    RoomifyWhite,

                                modifier =
                                    Modifier.size(
                                        20.dp
                                    )
                            )
                        }
                    }
                }


                val location =
                    room.locationSummary
                        .takeIf {
                            it.isNotBlank()
                        }
                        ?: room.address
                            ?.takeIf {
                                it.isNotBlank()
                            }


                location?.let {

                    Row(

                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clip(
                                    RoundedCornerShape(
                                        13.dp
                                    )
                                )
                                .background(
                                    RoomifyWhite
                                        .copy(
                                            alpha = 0.09f
                                        )
                                )
                                .padding(
                                    horizontal = 11.dp,
                                    vertical = 9.dp
                                ),

                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Icon(

                            imageVector =
                                Icons.Default.LocationOn,

                            contentDescription =
                                "Location",

                            tint =
                                RoomifyWhite90,

                            modifier =
                                Modifier.size(
                                    18.dp
                                )
                        )

                        Spacer(
                            modifier =
                                Modifier.width(
                                    7.dp
                                )
                        )

                        Text(

                            text =
                                it,

                            color =
                                RoomifyWhite90,

                            fontSize =
                                12.sp,

                            maxLines =
                                1,

                            overflow =
                                TextOverflow.Ellipsis
                        )
                    }
                }


                Row(

                    modifier =
                        Modifier.fillMaxWidth(),

                    verticalAlignment =
                        Alignment.CenterVertically,

                    horizontalArrangement =
                        Arrangement.SpaceBetween
                ) {

                    Column {

                        Text(

                            text =
                                "MONTHLY RENT",

                            color =
                                RoomifyWhite55,

                            fontSize =
                                9.sp,

                            fontWeight =
                                FontWeight.Bold,

                            letterSpacing =
                                1.sp
                        )

                        Spacer(
                            modifier =
                                Modifier.height(
                                    2.dp
                                )
                        )

                        Text(

                            text =
                                formatFullPrice(
                                    room.price
                                ),

                            color =
                                RoomifyWhite,

                            fontSize =
                                19.sp,

                            fontWeight =
                                FontWeight.ExtraBold
                        )
                    }


                    StatusBadge(
                        status =
                            room.status
                    )
                }


                PopupFeatures(
                    room =
                        room
                )


                Button(

                    onClick =
                        onViewProperty,

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(
                                50.dp
                            ),

                    shape =
                        RoundedCornerShape(
                            15.dp
                        ),

                    colors =
                        ButtonDefaults.buttonColors(

                            containerColor =
                                RoomifyWhite95,

                            contentColor =
                                RoomifyGradientStart
                        )
                ) {

                    Text(

                        text =
                            "View Property Details",

                        fontSize =
                            14.sp,

                        fontWeight =
                            FontWeight.Bold
                    )
                }
            }
        }
    }
}


/*
 * ============================================================
 * STATUS BADGE
 * ============================================================
 */

@Composable
private fun StatusBadge(
    status: String?
) {

    val normalized =
        status?.uppercase()
            ?: "AVAILABLE"

    val color =
        when (normalized) {

            "AVAILABLE" ->
                RoomifyGreen

            "PENDING" ->
                RoomifyYellow

            "RENTED" ->
                RoomifyRed

            else ->
                RoomifyWhite
        }

    Row(

        modifier =
            Modifier
                .clip(
                    RoundedCornerShape(
                        50.dp
                    )
                )
                .background(
                    RoomifyWhite
                        .copy(
                            alpha = 0.10f
                        )
                )
                .border(
                    width = 1.dp,

                    color =
                        RoomifyWhite
                            .copy(
                                alpha = 0.17f
                            ),

                    shape =
                        RoundedCornerShape(
                            50.dp
                        )
                )
                .padding(
                    horizontal = 10.dp,
                    vertical = 6.dp
                ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Box(

            modifier =
                Modifier
                    .size(
                        7.dp
                    )
                    .clip(
                        CircleShape
                    )
                    .background(
                        color
                    )
        )

        Spacer(
            modifier =
                Modifier.width(
                    6.dp
                )
        )

        Text(

            text =
                statusText(
                    status
                ),

            color =
                RoomifyWhite,

            fontSize =
                11.sp,

            fontWeight =
                FontWeight.Bold
        )
    }
}


/*
 * ============================================================
 * POPUP FEATURES
 * ============================================================
 */

@Composable
private fun PopupFeatures(
    room: Room
) {

    Row(

        modifier =
            Modifier.fillMaxWidth(),

        horizontalArrangement =
            Arrangement.spacedBy(
                7.dp
            )
    ) {

        if (
            room.roomsCount > 0
        ) {

            FeatureItem(

                icon =
                    Icons.Default.Bed,

                value =
                    "${room.roomsCount}",

                label =
                    "Beds"
            )
        }


        if (
            room.bathroomsCount > 0
        ) {

            FeatureItem(

                icon =
                    Icons.Default.Bathtub,

                value =
                    "${room.bathroomsCount}",

                label =
                    "Baths"
            )
        }


        if (
            room.area > 0
        ) {

            FeatureItem(

                icon =
                    Icons.Default.SquareFoot,

                value =
                    "${room.area.roundToInt()}",

                label =
                    "m²"
            )
        }
    }
}


/*
 * ============================================================
 * SINGLE FEATURE
 * ============================================================
 */

@Composable
private fun FeatureItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String
) {

    Row(

        modifier =
            Modifier
                .clip(
                    RoundedCornerShape(
                        11.dp
                    )
                )
                .background(
                    RoomifyWhite
                        .copy(
                            alpha = 0.10f
                        )
                )
                .border(
                    width = 1.dp,

                    color =
                        RoomifyWhite
                            .copy(
                                alpha = 0.10f
                            ),

                    shape =
                        RoundedCornerShape(
                            11.dp
                        )
                )
                .padding(
                    horizontal = 9.dp,
                    vertical = 7.dp
                ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Icon(

            imageVector =
                icon,

            contentDescription =
                null,

            tint =
                RoomifyWhite80,

            modifier =
                Modifier.size(
                    16.dp
                )
        )

        Spacer(
            modifier =
                Modifier.width(
                    5.dp
                )
        )

        Text(

            text =
                "$value $label",

            fontSize =
                11.sp,

            fontWeight =
                FontWeight.Medium,

            color =
                RoomifyWhite90
        )
    }
}

@Composable
private fun LanguageSelector(
    currentLanguage: Language,
    onLanguageChange: (Language) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Language.entries.forEach { lang ->
            val isSelected = lang == currentLanguage
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onLanguageChange(lang) },
                color = if (isSelected) Color.White.copy(alpha = 0.2f) else Color.Transparent,
                shape = RoundedCornerShape(10.dp),
                border = if (isSelected) null else BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = lang.displayName,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
