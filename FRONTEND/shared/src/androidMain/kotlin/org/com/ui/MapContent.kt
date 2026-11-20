package org.com.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.location.Location
import androidx.compose.ui.layout.ContentScale
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlin.math.roundToInt
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Bathtub
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import org.com.network.RoomifyApi
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.com.i18n.LocalRoomifyStrings
import org.com.i18n.LocalLocalizationManager
import org.com.i18n.Language
import kotlin.math.*
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
import com.google.android.gms.maps.model.LatLngBounds
import kotlinx.coroutines.suspendCancellableCoroutine
import org.com.auth.AuthState
import kotlin.coroutines.resume
import kotlin.math.roundToInt

/*
 * ============================================================
 * ROOMIFY COLORS
 * ============================================================
 */

private const val ROOMIFY_BLUE = 0xFF1976D2
private const val ROOMIFY_GREEN = 0xFF16A34A
private val RoomifyGradientStart = Color(0xFF1A237E)
private val RoomifyGradientEnd = Color(0xFF3949AB)
private val RoomifyWhite = Color(0xFFFFFFFF)
private val RoomifyWhite95 = Color(0xF2FFFFFF)
private val RoomifyWhite90 = Color(0xE6FFFFFF)
private val RoomifyWhite80 = Color(0xCCFFFFFF)
private val RoomifyWhite70 = Color(0xB3FFFFFF)
private val RoomifyWhite55 = Color(0x8CFFFFFF)
private val RoomifyGreen = Color(0xFF16A34A)
private val RoomifyYellow = Color(0xFFD97706)
private val RoomifyRed = Color(0xFFD97706)
private val RoomifyViewedGray = Color(0xFF16A34A)
private val RoomifySavedPink = Color(0xFFE91E63)

private const val ROOMIFY_VIEWED_GRAY = 0xFF16A34A
private const val ROOMIFY_SAVED_PINK = 0xFFE91E63
private const val ROOMIFY_YELLOW = 0xFFD97706
private const val ROOMIFY_RED = 0xFFD97706
private const val ROOMIFY_ORANGE = 0xFFD97706

/*
 * ============================================================
 * STATUS COLOR
 * ============================================================
 */

private fun statusColor(
    status: String?,
    selected: Boolean,
    isSaved: Boolean = false,
    isViewed: Boolean = false
): Int {
    if (selected) {
        return ROOMIFY_ORANGE.toInt()
    }
    
    if (isSaved) {
        return ROOMIFY_SAVED_PINK.toInt()
    }

    return when (status?.uppercase() ?: "AVAILABLE") {
        "AVAILABLE" -> ROOMIFY_GREEN.toInt()
        "PENDING" -> ROOMIFY_YELLOW.toInt()
        "RENTED" -> ROOMIFY_YELLOW.toInt()
        else -> ROOMIFY_GREEN.toInt()
    }
}

/*
 * ============================================================
 * STATUS TEXT
 * ============================================================
 */

private fun statusText(status: String?): String {
    return when (status?.uppercase() ?: "AVAILABLE") {
        "AVAILABLE" -> "Available"
        "PENDING" -> "Pending"
        "RENTED" -> "Rented"
        else -> status?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Available"
    }
}

/*
 * ============================================================
 * FULL PRICE
 * ============================================================
 */

private fun formatFullPrice(price: Double): String {
    if (!price.isFinite() || price <= 0.0) {
        return "Price on request"
    }
    return "${price.roundToInt().toStringWithCommas()} TZS"
}

/*
 * ============================================================
 * COMPACT PRICE
 * ============================================================
 */

private fun formatCompactPrice(price: Double): String {
    if (!price.isFinite() || price <= 0.0) {
        return "Price on request"
    }
    return "${price.roundToInt().toStringWithCommas()} TZS"
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
    return if (this % 1.0 == 0.0) {
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
    selected: Boolean,
    isSaved: Boolean = false,
    isViewed: Boolean = false
): BitmapDescriptor {
    val color = statusColor(status = room.status, selected = selected, isSaved = isSaved, isViewed = isViewed)
    val status = room.status?.uppercase() ?: "AVAILABLE"
    val alpha = 255
    val text = formatCompactPrice(room.price)
    
    // Scale up for selected marker to make it "grow" and more noticeable
    val scale = if (selected) 1.3f else 1.0f
    val width = (220 * scale).toInt()
    val height = (100 * scale).toInt()
    
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Shadow/Glow effect for selected
    if (selected) {
        paint.color = android.graphics.Color.argb(40, 0, 0, 0)
        canvas.drawRoundRect(12f * scale, 12f * scale, width - (8f * scale), height - (33f * scale), 24f * scale, 24f * scale, paint)
    }

    paint.color = android.graphics.Color.argb(
        alpha,
        android.graphics.Color.red(color),
        android.graphics.Color.green(color),
        android.graphics.Color.blue(color)
    )

    val left = 10f * scale
    val top = 10f * scale
    val right = width - (10f * scale)
    val bottom = height - (35f * scale)
    val radius = 22f * scale

    canvas.drawRoundRect(left, top, right, bottom, radius, radius, paint)

    val pointer = Path().apply {
        moveTo(width / 2f - (15f * scale), bottom)
        lineTo(width / 2f, height - (10f * scale))
        lineTo(width / 2f + (15f * scale), bottom)
        close()
    }
    canvas.drawPath(pointer, paint)

    // Draw Icon with a subtle circular background to make it stand out
    val iconX = 45f * scale
    val centerY = (top + bottom) / 2f
    
    paint.color = android.graphics.Color.argb(50, 255, 255, 255)
    canvas.drawCircle(iconX, centerY, 19f * scale, paint)

    val isBuilding = room.propertyId != null
    if (isBuilding) {
        paint.color = android.graphics.Color.WHITE
        paint.style = Paint.Style.FILL

        val bWidth = 16f * scale
        val bHeight = 22f * scale
        val bLeft = iconX - bWidth / 2f
        val bTop = centerY - bHeight / 2f + (1f * scale)
        
        val bRect = RectF(bLeft, bTop, bLeft + bWidth, bTop + bHeight)
        canvas.drawRoundRect(bRect, 2f * scale, 2f * scale, paint)
        
        // Window cutouts using marker background color
        val windowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = android.graphics.Color.argb(alpha, android.graphics.Color.red(color), android.graphics.Color.green(color), android.graphics.Color.blue(color))
            this.style = Paint.Style.FILL
        }
        val wWidth = 3f * scale
        val wHeight = 3.5f * scale
        val wGapX = 3f * scale
        val wGapY = 2.5f * scale
        val wStartLeft = bLeft + 3.5f * scale
        val wStartTop = bTop + 3f * scale

        for (row in 0..2) {
            for (col in 0..1) {
                val wx = wStartLeft + col * (wWidth + wGapX)
                val wy = wStartTop + row * (wHeight + wGapY)
                canvas.drawRect(wx, wy, wx + wWidth, wy + wHeight, windowPaint)
            }
        }
    } else {
        paint.color = android.graphics.Color.WHITE
        paint.style = Paint.Style.FILL

        val hWidth = 20f * scale
        val hHeight = 13f * scale
        val hLeft = iconX - hWidth / 2f
        val hBottom = centerY + 10f * scale
        val hTop = hBottom - hHeight
        
        // Roof (Triangle)
        val roofPath = Path().apply {
            moveTo(iconX, centerY - 12f * scale)
            lineTo(iconX - hWidth / 2f - 3f * scale, hTop + 1f * scale)
            lineTo(iconX + hWidth / 2f + 3f * scale, hTop + 1f * scale)
            close()
        }
        canvas.drawPath(roofPath, paint)
        
        // House body (Rectangle)
        canvas.drawRect(hLeft, hTop + 1f * scale, hLeft + hWidth, hBottom, paint)
        
        // Door cutout using marker background color
        val doorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = android.graphics.Color.argb(alpha, android.graphics.Color.red(color), android.graphics.Color.green(color), android.graphics.Color.blue(color))
            this.style = Paint.Style.FILL
        }
        val doorWidth = 5f * scale
        val doorHeight = 7f * scale
        canvas.drawRect(iconX - doorWidth / 2f, hBottom - doorHeight, iconX + doorWidth / 2f, hBottom, doorPaint)
    }

    // Draw Text with enough spacing to avoid overlap
    paint.textSize = 22f * scale
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    paint.textAlign = Paint.Align.LEFT
    canvas.drawText(text, iconX + (35f * scale), centerY + (8f * scale), paint)

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

/*
 * ============================================================
 * BUILDING PRICE MARKER
 * ============================================================
 */

private fun createBuildingPriceMarker(
    buildingRooms: List<Room>,
    selected: Boolean,
    isSaved: Boolean = false,
    isViewed: Boolean = false
): BitmapDescriptor {
    val minPrice = buildingRooms.map { it.price }.filter { it > 0 }.minOrNull() ?: 0.0
    val totalCount = buildingRooms.size
    val availableCount = buildingRooms.count { it.status.equals("AVAILABLE", ignoreCase = true) }

    val labelText = "From ${formatCompactPrice(minPrice)} • $availableCount/$totalCount rooms"

    val color = if (selected) ROOMIFY_ORANGE.toInt() else if (isSaved) ROOMIFY_SAVED_PINK.toInt() else ROOMIFY_BLUE.toInt()
    val scale = if (selected) 1.3f else 1.0f
    val width = (260 * scale).toInt()
    val height = (100 * scale).toInt()

    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    if (selected) {
        paint.color = android.graphics.Color.argb(40, 0, 0, 0)
        canvas.drawRoundRect(12f * scale, 12f * scale, width - (8f * scale), height - (33f * scale), 24f * scale, 24f * scale, paint)
    }

    paint.color = color

    val left = 10f * scale
    val top = 10f * scale
    val right = width - (10f * scale)
    val bottom = height - (35f * scale)
    val radius = 22f * scale

    canvas.drawRoundRect(left, top, right, bottom, radius, radius, paint)

    val pointer = Path().apply {
        moveTo(width / 2f - (15f * scale), bottom)
        lineTo(width / 2f, height - (10f * scale))
        lineTo(width / 2f + (15f * scale), bottom)
        close()
    }
    canvas.drawPath(pointer, paint)

    val iconX = 42f * scale
    val centerY = (top + bottom) / 2f
    paint.color = android.graphics.Color.argb(50, 255, 255, 255)
    canvas.drawCircle(iconX, centerY, 19f * scale, paint)

    // Draw Building Icon
    paint.color = android.graphics.Color.WHITE
    paint.style = Paint.Style.FILL
    val bWidth = 16f * scale
    val bHeight = 22f * scale
    val bLeft = iconX - bWidth / 2f
    val bTop = centerY - bHeight / 2f + (1f * scale)
    val bRect = RectF(bLeft, bTop, bLeft + bWidth, bTop + bHeight)
    canvas.drawRoundRect(bRect, 2f * scale, 2f * scale, paint)

    val windowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        this.style = Paint.Style.FILL
    }
    val wWidth = 3f * scale
    val wHeight = 3.5f * scale
    val wGapX = 3f * scale
    val wGapY = 2.5f * scale
    val wStartLeft = bLeft + 3.5f * scale
    val wStartTop = bTop + 3f * scale

    for (row in 0..2) {
        for (col in 0..1) {
            val wx = wStartLeft + col * (wWidth + wGapX)
            val wy = wStartTop + row * (wHeight + wGapY)
            canvas.drawRect(wx, wy, wx + wWidth, wy + wHeight, windowPaint)
        }
    }

    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = android.graphics.Color.WHITE
        this.textSize = 12f * scale * 2.1f
        this.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        this.textAlign = Paint.Align.CENTER
    }

    val textX = (iconX + right) / 2f
    val textY = centerY - ((textPaint.descent() + textPaint.ascent()) / 2f)
    canvas.drawText(labelText, textX, textY, textPaint)

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

/*
 * ============================================================
 * DOT MARKER
 * ============================================================
 */

private fun createDotMarker(
    room: Room,
    selected: Boolean,
    isSaved: Boolean = false,
    isViewed: Boolean = false
): BitmapDescriptor {
    val size = if (selected) 64 else 48
    val color = statusColor(room.status, selected, isSaved, isViewed)
    
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    // Outer glow/shadow
    paint.color = android.graphics.Color.argb(60, 0, 0, 0)
    canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
    
    // White border
    paint.color = android.graphics.Color.WHITE
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 2f, paint)
    
    // Main dot
    paint.color = color
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 6f, paint)
    
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

/*
 * ============================================================
 * CLUSTER MARKER
 * ============================================================
 */

private fun createClusterMarker(count: Int): BitmapDescriptor {
    val size = 120
    val color = 0xFF1A237E.toInt() // Roomify Blue
    
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    // White border
    paint.color = android.graphics.Color.WHITE
    canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
    
    // Blue circle
    paint.color = color
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 8f, paint)
    
    // Count text
    paint.color = android.graphics.Color.WHITE
    paint.textSize = 42f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    paint.textAlign = Paint.Align.CENTER
    
    canvas.drawText(count.toString(), size / 2f, size / 2f + 15f, paint)
    
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

/*
 * ============================================================
 * MAIN ANDROID MAP
 * ============================================================
 */

@Composable
actual fun MapContent(
    rooms: List<Room>,
    allRooms: List<Room>,
    selectedRoom: Room?,
    authState: AuthState,
    routingDestination: Room?,
    currentStatusFilter: String,
    shouldFitBounds: Boolean,
    viewedRoomIds: Set<Long>,
    savedRoomIds: Set<Long>,
    onStatusFilterChange: (String) -> Unit,
    onFiltersChange: (type: String?, area: String?, maxPrice: Double?, status: String?) -> Unit,
    onFitBoundsHandled: () -> Unit,
    onClearRoute: () -> Unit,
    onMenuClick: () -> Unit,
    onRoomSelected: (Room) -> Unit,
    onRoomCleared: () -> Unit,
    onViewProperty: (Room) -> Unit,
    onNavigate: (String) -> Unit
) {
    val areaSuggestions = remember { mutableStateListOf<String>() }
    LaunchedEffect(Unit) {
        try {
            val suggestions = RoomifyApi.getAreaSuggestions()
            areaSuggestions.clear()
            areaSuggestions.addAll(suggestions)
        } catch (e: Exception) {}
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(-6.7924, 39.2083), 13f)
    }
    val mapScope = rememberCoroutineScope()

    LaunchedEffect(rooms, shouldFitBounds) {
        if (shouldFitBounds && rooms.isNotEmpty()) {
            val validRooms = rooms.filter { it.latitude != 0.0 && it.longitude != 0.0 && !it.latitude.isNaN() && !it.latitude.isInfinite() && !it.longitude.isNaN() && !it.longitude.isInfinite() }
            if (validRooms.size == 1) {
                val room = validRooms[0]
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngZoom(LatLng(room.latitude, room.longitude), 16f),
                    durationMs = 1000
                )
                onFitBoundsHandled()
            } else if (validRooms.isNotEmpty()) {
                val boundsBuilder = LatLngBounds.builder()
                validRooms.forEach { room ->
                    boundsBuilder.include(LatLng(room.latitude, room.longitude))
                }
                val bounds = boundsBuilder.build()
                
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngBounds(bounds, 150),
                    durationMs = 1000
                )
                onFitBoundsHandled()
            }
        }
    }

    val routePoints = remember { mutableStateListOf<LatLng>() }
    val context = LocalContext.current

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
                val location = suspendCancellableCoroutine<Location?> { continuation ->
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
                    
                    val bounds = LatLngBounds.builder()
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

    var searchQuery by remember { mutableStateOf("") }

    val controller = createMapController(
        onLocationSelected = {},
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

    val searchFocusRequester = remember { FocusRequester() }

    LaunchedEffect(selectedRoom?.id) {
        selectedRoom?.let { room ->
            if (room.latitude != 0.0 && room.longitude != 0.0) {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(LatLng(room.latitude, room.longitude), 15.5f),
                    700
                )
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isBuildingEnabled = true,
                isIndoorEnabled = true,
                isTrafficEnabled = false,
                isMyLocationEnabled = false,
                mapStyleOptions = null 
            ),
            uiSettings = MapUiSettings(
                scrollGesturesEnabled = true,
                zoomGesturesEnabled = true,
                rotationGesturesEnabled = true,
                tiltGesturesEnabled = true,
                zoomControlsEnabled = false,
                compassEnabled = true,
                myLocationButtonEnabled = false,
                mapToolbarEnabled = false
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

            val zoom = cameraPositionState.position.zoom
            val showClusters = zoom < 12f
            
            if (showClusters) {
                val clusters = rooms.filter { it.latitude != 0.0 && it.longitude != 0.0 }
                    .groupBy { room ->
                        val latStep = 0.5
                        val lngStep = 0.5
                        val latKey = (room.latitude / latStep).roundToInt()
                        val lngKey = (room.longitude / lngStep).roundToInt()
                        latKey to lngKey
                    }

                clusters.forEach { (gridKey, clusteredRooms) ->
                    if (clusteredRooms.size > 1) {
                        val avgLat = clusteredRooms.map { it.latitude }.average()
                        val avgLng = clusteredRooms.map { it.longitude }.average()
                        
                        Marker(
                            state = MarkerState(position = LatLng(avgLat, avgLng)),
                            icon = createClusterMarker(clusteredRooms.size),
                            onClick = {
                                mapScope.launch {
                                    cameraPositionState.animate(
                                        CameraUpdateFactory.newLatLngZoom(LatLng(avgLat, avgLng), zoom + 2f),
                                        500
                                    )
                                }
                                true
                            }
                        )
                    } else {
                        val room = clusteredRooms.first()
                        val isSelected = selectedRoom?.id == room.id
                        val isSaved = savedRoomIds.contains(room.id ?: -1L)
                        val isViewed = viewedRoomIds.contains(room.id ?: -1L)

                        Marker(
                            state = MarkerState(position = LatLng(room.latitude, room.longitude)),
                            icon = createDotMarker(room, isSelected, isSaved, isViewed),
                            onClick = {
                                onRoomSelected(room)
                                true
                            }
                        )
                    }
                }
            } else {
                val buildingMap = rooms.filter { it.latitude != 0.0 && it.longitude != 0.0 }
                    .groupBy { room ->
                        if (room.propertyId != null && room.propertyId > 0L) {
                            "prop_${room.propertyId}"
                        } else {
                            val roundedLat = (room.latitude * 10000.0).roundToInt()
                            val roundedLng = (room.longitude * 10000.0).roundToInt()
                            "coord_${roundedLat}_${roundedLng}"
                        }
                    }

                buildingMap.forEach { (key, groupRooms) ->
                    if (groupRooms.size > 1 || (groupRooms.first().propertyId != null && groupRooms.first().propertyId!! > 0L) || groupRooms.first().propertyType.equals("BUILDING", ignoreCase = true)) {
                        val first = groupRooms.first()
                        val isSelected = groupRooms.any { it.id == selectedRoom?.id }
                        val isSaved = groupRooms.any { savedRoomIds.contains(it.id ?: -1L) }
                        val isViewed = groupRooms.any { viewedRoomIds.contains(it.id ?: -1L) }

                        val primaryRoom = groupRooms.firstOrNull { it.status.equals("AVAILABLE", ignoreCase = true) } ?: first

                        Marker(
                            state = remember(key) { MarkerState(position = LatLng(first.latitude, first.longitude)) }.apply {
                                position = LatLng(first.latitude, first.longitude)
                            },
                            icon = createBuildingPriceMarker(groupRooms, isSelected, isSaved, isViewed),
                            title = first.title ?: "Building",
                            onClick = {
                                onRoomSelected(primaryRoom)
                                true
                            }
                        )
                    } else {
                        val room = groupRooms.first()
                        val isSelected = selectedRoom?.id == room.id
                        val isSaved = savedRoomIds.contains(room.id ?: -1L)
                        val isViewed = viewedRoomIds.contains(room.id ?: -1L)

                        Marker(
                            state = remember(room.id) { MarkerState(position = LatLng(room.latitude, room.longitude)) }.apply {
                                position = LatLng(room.latitude, room.longitude)
                            },
                            icon = createPriceMarker(room, isSelected, isSaved, isViewed),
                            title = room.title ?: "Room",
                            onClick = {
                                onRoomSelected(room)
                                true
                            }
                        )
                    }
                }
            }
        }

        // My Location Button
        val myLocationScope = rememberCoroutineScope()
        FloatingActionButton(
            onClick = {
                myLocationScope.launch {
                    try {
                        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                        @Suppress("MissingPermission")
                        fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                            if (loc != null) {
                                myLocationScope.launch {
                                    cameraPositionState.animate(
                                        CameraUpdateFactory.newLatLngZoom(LatLng(loc.latitude, loc.longitude), 15.5f),
                                        800
                                    )
                                }
                            }
                        }.addOnFailureListener {
                            println("Location request failed: ${it.message}")
                        }
                    } catch (e: Exception) {
                        println("Location error: ${e.message}")
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = if (selectedRoom != null) 280.dp else 90.dp, end = 16.dp),
            containerColor = Color.White,
            contentColor = RoomifyGradientStart,
            shape = CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = "My Location",
                modifier = Modifier.size(24.dp)
            )
        }

        MapHeader(
            searchQuery = searchQuery,
            currentStatusFilter = currentStatusFilter,
            onStatusFilterChange = onStatusFilterChange,
            onFiltersChange = onFiltersChange,
            onMenuClick = onMenuClick,
            onSearchChange = { searchQuery = it },
            onSearchClick = { searchFocusRequester.requestFocus() },
            searchFocusRequester = searchFocusRequester
        )

        selectedRoom?.let { room ->
            RoomPropertyPopup(
                room = room,
                onClose = { onRoomCleared() },
                onViewProperty = { onViewProperty(room) }
            )
        }

        if (routingDestination != null) {
            val dest = routingDestination
            val originLat = routePoints.firstOrNull()?.latitude ?: -6.7924
            val originLng = routePoints.firstOrNull()?.longitude ?: 39.2083

            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val isExpanded = maxWidth >= 840.dp
                val cardModifier = if (isExpanded) {
                    Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 90.dp, start = 16.dp)
                        .width(380.dp)
                } else {
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 20.dp, start = 16.dp, end = 16.dp)
                        .fillMaxWidth()
                }

                org.com.ui.components.LiveRouteSummaryCard(
                    originLabel = "Your Current Location",
                    destinationRoom = dest,
                    originLat = originLat,
                    originLng = originLng,
                    destLat = dest.latitude,
                    destLng = dest.longitude,
                    onRecenter = {
                        val bounds = com.google.android.gms.maps.model.LatLngBounds.builder()
                            .include(com.google.android.gms.maps.model.LatLng(originLat, originLng))
                            .include(com.google.android.gms.maps.model.LatLng(dest.latitude, dest.longitude))
                            .build()
                        mapScope.launch {
                            cameraPositionState.animate(
                                com.google.android.gms.maps.CameraUpdateFactory.newLatLngBounds(bounds, 100),
                                800
                            )
                        }
                    },
                    onEndRoute = {
                        routePoints.clear()
                        onClearRoute()
                    },
                    modifier = cardModifier
                )
            }
        }
    }
}

/*
 * ============================================================
 * HELPER FOR SEARCH QUERY PARSING
 * ============================================================
 */
fun parseSearchQuery(query: String): Triple<String?, Double?, String?> {
    if (query.isBlank()) return Triple(null, null, null)

    val validTypes = listOf("ROOM", "APARTMENT", "STUDIO", "HOUSE", "OFFICE")
    var extractedType: String? = null
    var extractedPrice: Double? = null
    val locationTokens = mutableListOf<String>()

    val parts = if (query.contains(",")) {
        query.split(",").map { it.trim() }
    } else {
        query.split("\\s+".toRegex()).map { it.trim() }
    }

    for (part in parts) {
        if (part.isBlank()) continue

        val matchedType = validTypes.find { it.equals(part, ignoreCase = true) }
        if (matchedType != null && extractedType == null) {
            extractedType = matchedType
            continue
        }

        val cleanPart = part.replace("TZS", "", ignoreCase = true)
            .replace(",", "")
            .trim()

        val parsedPrice = when {
            cleanPart.lowercase().endsWith("k") -> {
                cleanPart.dropLast(1).toDoubleOrNull()?.let { it * 1_000.0 }
            }
            cleanPart.lowercase().endsWith("m") -> {
                cleanPart.dropLast(1).toDoubleOrNull()?.let { it * 1_000_000.0 }
            }
            else -> cleanPart.toDoubleOrNull()
        }

        if (parsedPrice != null && parsedPrice > 0 && extractedPrice == null) {
            extractedPrice = parsedPrice
            continue
        }

        locationTokens.add(part)
    }

    val extractedArea = locationTokens.joinToString(" ").ifBlank { null }
    return Triple(extractedArea, extractedPrice, extractedType)
}

/*
 * ============================================================
 * MAP HEADER
 * ============================================================
 */

@Composable
private fun MapHeader(
    searchQuery: String,
    currentStatusFilter: String,
    onStatusFilterChange: (String) -> Unit,
    onFiltersChange: (type: String?, area: String?, maxPrice: Double?, status: String?) -> Unit,
    onMenuClick: () -> Unit,
    onSearchChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    searchFocusRequester: FocusRequester
) {
    val strings = LocalRoomifyStrings.current

    val triggerFilter = {
        val (area, price, type) = parseSearchQuery(searchQuery)
        onFiltersChange(type, area, price, currentStatusFilter)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 18.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            /* SEARCH BAR */
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                color = RoomifyWhite.copy(alpha = 0.96f),
                border = BorderStroke(1.dp, Color(0xFFBDBDBD))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp)
                        .clickable(onClick = onSearchClick),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = RoomifyGradientStart.copy(alpha = 0.78f),
                        modifier = Modifier.size(22.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    BasicTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(searchFocusRequester)
                            .focusable(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { triggerFilter() }),
                        textStyle = TextStyle(
                            color = RoomifyGradientStart,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        decorationBox = { innerTextField ->
                            Box {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = strings.searchPlaceholder,
                                        color = RoomifyGradientStart.copy(alpha = 0.52f),
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )

                    if (searchQuery.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .clickable {
                                    onSearchChange("")
                                    onFiltersChange(null, null, null, currentStatusFilter)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear search",
                                tint = RoomifyGradientStart.copy(alpha = 0.70f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
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
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 360.dp)
                .fillMaxWidth(0.92f),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = room.propertyType?.uppercase()?.takeIf { it.isNotBlank() } ?: "APARTMENT",
                            color = Color(0xFF142170),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.6.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = room.title?.takeIf { it.isNotBlank() } ?: "Modern apartment",
                            color = Color(0xFF0F172A),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFFF1F5F9), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF334155),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Image Preview (150.dp height, RoundedCornerShape(12.dp))
                val imageUrls = room.fullImageUrls
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF1F5F9))
                ) {
                    if (imageUrls.isNotEmpty()) {
                        KamelImage(
                            resource = { asyncPainterResource(imageUrls.first()) },
                            contentDescription = "Property photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            onFailure = {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoLibrary,
                                        contentDescription = null,
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }
                        )

                        if (imageUrls.size > 1) {
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(8.dp),
                                color = Color(0xCC0F172A),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "1 / ${imageUrls.size}",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = null,
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "No photo available",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                val location = formatLocationDisplay(room.address, room.latitude, room.longitude, room.neighbourhoodPlaces)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = location,
                        color = Color(0xFF64748B),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Rent & Status Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "MONTHLY RENT",
                            color = Color(0xFF64748B),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.6.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = formatFullPrice(room.price),
                            color = Color(0xFF0F172A),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    StatusBadge(status = room.status)
                }

                PopupFeatures(room = room)

                Button(
                    onClick = onViewProperty,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF142170),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "View Property Details",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun formatLocationDisplay(
    rawLocation: String?,
    lat: Double? = null,
    lng: Double? = null,
    neighbourhoodPlaces: List<String> = emptyList()
): String {
    if (!rawLocation.isNullOrBlank()) {
        val clean = rawLocation.removePrefix("Coordinates:").trim()
        val isCoords = clean.matches(Regex("^-?\\d+(\\.\\d+)?,\\s*-?\\d+(\\.\\d+)?$"))
        if (!isCoords && clean.isNotBlank()) {
            return clean
        }
    }
    if (neighbourhoodPlaces.isNotEmpty()) {
        return neighbourhoodPlaces.first()
    }
    return "Dar es Salaam, Tanzania"
}

/*
 * ============================================================
 * STATUS BADGE
 * ============================================================
 */

@Composable
private fun StatusBadge(status: String?) {
    val normalized = status?.uppercase()?.trim() ?: "AVAILABLE"
    val (bgColor, dotColor, textColor) = when (normalized) {
        "AVAILABLE" -> Triple(Color(0xFFDCFCE7), Color(0xFF16A34A), Color(0xFF15803D))
        "PENDING" -> Triple(Color(0xFFFEF3C7), Color(0xFFD97706), Color(0xFFB45309))
        else -> Triple(Color(0xFFFEF2F2), Color(0xFFDC2626), Color(0xFF991B1B))
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = statusText(status),
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/*
 * ============================================================
 * POPUP FEATURES
 * ============================================================
 */

@Composable
private fun PopupFeatures(room: Room) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        if (room.roomsCount > 0) {
            FeatureItem(
                icon = Icons.Default.Bed,
                value = "${room.roomsCount}",
                label = "Beds"
            )
        }

        if (room.bathroomsCount > 0) {
            FeatureItem(
                icon = Icons.Default.Bathtub,
                value = "${room.bathroomsCount}",
                label = "Baths"
            )
        }

        if (room.area > 0) {
            FeatureItem(
                icon = Icons.Default.SquareFoot,
                value = "${room.area.roundToInt()}",
                label = "m²"
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
    icon: ImageVector,
    value: String,
    label: String
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF1F5F9))
            .border(
                width = 1.dp,
                color = Color(0xFFE2E8F0),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF64748B),
            modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = "$value $label",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF334155)
        )
    }
}
