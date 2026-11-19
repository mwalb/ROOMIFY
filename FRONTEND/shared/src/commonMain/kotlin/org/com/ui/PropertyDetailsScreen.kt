package org.com.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlinx.coroutines.*
import org.com.i18n.LocalRoomifyStrings
import org.com.model.*
import org.com.network.RoomifyApi
import org.com.ui.components.AIRoomArrangerDialog
import org.com.ui.components.SpacePlannerDialog
import kotlin.math.roundToInt

private val PrimaryColor = Color(0xFF142170)
private val PrimaryLight = Color(0xFF1A237E)
private val SuccessColor = Color(0xFF10B981)

@Composable
fun PropertyDetailsScreen(
    room: Room,
    currentUser: User? = null,
    otherProperties: List<Room> = emptyList(),
    onBack: () -> Unit,
    onBookNow: (Room) -> Unit,
    onMessageOwner: (Room) -> Unit,
    onShowRoute: (Room) -> Unit = {},
    onCallOwner: (Room) -> Unit = {},
    onSpacePlanner: (Room) -> Unit = {},
    showSpacePlanner: Boolean = false,
    onDismissSpacePlanner: () -> Unit = {},
    onAIRoomArranger: (Room) -> Unit = {},
    showAIRoomArranger: Boolean = false,
    onDismissAIRoomArranger: () -> Unit = {},
    onEditProperty: (Room) -> Unit = {},
    onDeleteProperty: (Room) -> Unit = {},
    onViewProperty: (Room) -> Unit = {},
    onLoginRequired: () -> Unit = {}
) {
    val strings = LocalRoomifyStrings.current
    val uriHandler = LocalUriHandler.current
    val scope = rememberCoroutineScope()
    var isSaved by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    var selectedImageIndex by remember { mutableStateOf<Int?>(null) }
    var previewAsTenant by remember { mutableStateOf(false) }

    var parentProperty by remember { mutableStateOf<Property?>(null) }
    var otherUnitsInComplex by remember { mutableStateOf<List<Room>>(emptyList()) }
    var isLoadingComplex by remember { mutableStateOf(false) }

    var reviews by remember { mutableStateOf<List<Review>>(emptyList()) }
    var showWriteReviewDialog by remember { mutableStateOf(false) }

    LaunchedEffect(room.id) {
        try {
            val res = RoomifyApi.getRoomReviews(room.id ?: 0L)
            if (res.success && res.data != null) {
                reviews = res.data!!
            }
        } catch (e: Exception) {
            println("PropertyDetails: Error loading reviews: ${e.message}")
        }
    }

    LaunchedEffect(currentUser?.id, room.id) {
        if (currentUser != null) {
            try {
                val favRes = RoomifyApi.isFavorite(currentUser.id, room.id ?: 0L)
                if (favRes.success) {
                    isSaved = favRes.data == true
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    LaunchedEffect(room.propertyId) {
        if (room.propertyId != null) {
            isLoadingComplex = true
            try {
                val propResponse = RoomifyApi.getPropertyById(room.propertyId)
                if (propResponse.success) {
                    parentProperty = propResponse.data
                }

                val unitsResponse = RoomifyApi.getRoomsByProperty(room.propertyId)
                if (unitsResponse.success) {
                    otherUnitsInComplex = unitsResponse.data?.filter { it.id != room.id } ?: emptyList()
                }
            } catch (e: Exception) {
                println("PropertyDetails: Error loading complex info: ${e.message}")
            } finally {
                isLoadingComplex = false
            }
        }
    }

    val isMyProperty = currentUser?.id == room.postedBy
    val isOwnerView = isMyProperty && !previewAsTenant
    val statusUpper = (room.status ?: "AVAILABLE").uppercase().trim()
    val verificationStatus = (room.verificationStatus ?: "").uppercase().trim()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(PrimaryColor, PrimaryLight))),
        contentAlignment = Alignment.Center
    ) {
        val width = maxWidth
        val isExpanded = width >= 840.dp
        val isMedium = width in 600.dp..839.dp
        val isCompact = width < 600.dp

        val imageUrls = room.fullImageUrls

        if (isExpanded) {
            // Expanded 2-Column Desktop Layout
            Surface(
                modifier = Modifier
                    .widthIn(max = 1200.dp)
                    .fillMaxWidth()
                    .padding(24.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFFF8FAFC),
                shadowElevation = 12.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    // Desktop Top Navigation Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier
                                    .size(40.dp)
                                    .pointerHoverIcon(PointerIcon.Hand)
                                    .clickable { onBack() },
                                shape = CircleShape,
                                color = Color(0xFFF1F5F9)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color(0xFF0F172A), modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(Modifier.width(14.dp))
                            Text(
                                text = room.title ?: "Property Details",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (isMyProperty) {
                                Surface(
                                    color = if (previewAsTenant) Color(0xFFEEF2FF) else Color(0xFFF1F5F9),
                                    shape = RoundedCornerShape(20.dp),
                                    border = BorderStroke(1.dp, if (previewAsTenant) Color(0xFFC7D2FE) else Color(0xFFE2E8F0)),
                                    modifier = Modifier
                                        .pointerHoverIcon(PointerIcon.Hand)
                                        .clickable { previewAsTenant = !previewAsTenant }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (previewAsTenant) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = null,
                                            tint = PrimaryColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = if (previewAsTenant) "Previewing as Tenant" else "Tenant Preview",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryColor
                                        )
                                    }
                                }
                            }

                            IconButton(
                                onClick = {
                                    if (currentUser == null) {
                                        onLoginRequired()
                                    } else {
                                        scope.launch {
                                            try {
                                                val res = RoomifyApi.toggleFavorite(currentUser.id, room.id ?: 0L)
                                                if (res.success && res.data != null) {
                                                    isSaved = res.data!!
                                                }
                                            } catch (e: Exception) {
                                                println("Favorite error: ${e.message}")
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color(0xFFF1F5F9), CircleShape)
                            ) {
                                Icon(
                                    imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Save",
                                    tint = if (isSaved) Color(0xFFEC4899) else Color(0xFF64748B),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // 2-Column Grid
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // Left Column (weight = 0.56f)
                        Column(
                            modifier = Modifier
                                .weight(0.56f)
                                .verticalScroll(scrollState),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            // Photo Carousel
                            PropertyPhotoCarousel(
                                imageUrls = imageUrls,
                                height = 360.dp,
                                cornerRadius = 18.dp,
                                onImageClick = { index -> selectedImageIndex = index }
                            )

                            // Title & Verification Badges
                            PropertyHeaderDetails(
                                room = room,
                                statusUpper = statusUpper,
                                verificationStatus = verificationStatus
                            )

                            // Fee Transparency Banner
                            LandlordTransparencyBanner(room = room)

                            // Specs & Features
                            PropertySpecsRow(room = room)

                            // Description
                            SectionTitleDetails(strings.description)
                            Text(
                                text = room.description?.takeIf { it.isNotBlank() } ?: "No detailed description provided.",
                                fontSize = 14.sp,
                                color = Color(0xFF475569),
                                lineHeight = 22.sp
                            )

                            // Amenities
                            if (room.amenities.isNotEmpty()) {
                                SectionTitleDetails(strings.amenities)
                                FlowRow(mainAxisSpacing = 8.dp, crossAxisSpacing = 8.dp) {
                                    room.amenities.forEach { amenity ->
                                        Surface(
                                            color = Color(0xFFF1F5F9),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.Check, null, tint = SuccessColor, modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(6.dp))
                                                Text(amenity, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                                            }
                                        }
                                    }
                                }
                            }

                            // Neighborhood Insights
                            NeighborhoodInsights()

                            // Building Complex Inventory
                            if (room.propertyId != null && parentProperty != null) {
                                SectionTitleDetails("Building Inventory")
                                BuildingInventoryList(
                                    currentRoomId = room.id ?: 0,
                                    units = otherUnitsInComplex + listOf(room),
                                    onViewUnit = { onViewProperty(it) }
                                )

                                Spacer(Modifier.height(8.dp))
                                SectionTitleDetails("Complex Details")
                                ComplexOverviewCard(parentProperty!!)
                            }

                            // Virtual Tour Video
                            if (room.hasVideo && !room.videoUrl.isNullOrBlank()) {
                                SectionTitleDetails("Virtual Tour Video")
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .pointerHoverIcon(PointerIcon.Hand)
                                        .clickable { uriHandler.openUri(room.getFullUrl(room.videoUrl!!)) },
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color(0xFF0F172A)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(Icons.Rounded.PlayCircle, null, tint = Color.White, modifier = Modifier.size(48.dp))
                                            Spacer(Modifier.height(8.dp))
                                            Text("Watch Video Tour", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        }
                                    }
                                }
                            }

                            // Reviews & Ratings Section
                            ReviewsSection(
                                room = room,
                                reviews = reviews,
                                currentUser = currentUser,
                                strings = strings,
                                onLoginRequired = onLoginRequired,
                                onWriteReviewClick = { showWriteReviewDialog = true }
                            )
                        }

                        // Right Column: Action Card (weight = 0.44f)
                        Column(
                            modifier = Modifier.weight(0.44f),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                shadowElevation = 4.dp
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    // Rent & Status Header
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("MONTHLY RENT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B), letterSpacing = 0.6.sp)
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                text = room.formattedPrice,
                                                fontSize = 24.sp,
                                                fontWeight = FontWeight.Black,
                                                color = PrimaryColor
                                            )
                                        }

                                        StatusBadgeChip(statusUpper = statusUpper, verificationStatus = verificationStatus)
                                    }

                                    HorizontalDivider(
                                        color = Color(0xFFF1F5F9),
                                        thickness = 1.dp,
                                        modifier = Modifier.padding(vertical = 16.dp)
                                    )

                                    // Interactive Tools (AI Room Arranger & Show Route)
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .pointerHoverIcon(PointerIcon.Hand)
                                                .clickable { onAIRoomArranger(room) },
                                            shape = RoundedCornerShape(14.dp),
                                            color = Color(0xFFF3E8FF),
                                            border = BorderStroke(1.dp, Color(0xFFE9D5FF))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(14.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Rounded.AutoAwesome, null, tint = Color(0xFF7C3AED), modifier = Modifier.size(20.dp))
                                                Spacer(Modifier.width(10.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text("AI Arrange My Room", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF6B21A8))
                                                    Text("Visualize dream layout with AI", fontSize = 11.sp, color = Color(0xFF7E22CE))
                                                }
                                                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color(0xFF7C3AED), modifier = Modifier.size(16.dp))
                                            }
                                        }

                                        Button(
                                            onClick = { onShowRoute(room) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(42.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                                        ) {
                                            Icon(Icons.Rounded.Directions, null, modifier = Modifier.size(18.dp))
                                            Spacer(Modifier.width(8.dp))
                                            Text("Show Route to Property", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Spacer(Modifier.height(20.dp))

                                    // Action Area: Tenant vs Owner
                                    if (isOwnerView) {
                                        Text("LISTING MANAGEMENT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8), letterSpacing = 0.8.sp)
                                        Spacer(Modifier.height(10.dp))
                                        OwnerManagementGrid(
                                            room = room,
                                            onEditProperty = onEditProperty,
                                            onDeleteProperty = onDeleteProperty
                                        )
                                    } else {
                                        Text("TENANT ACTIONS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8), letterSpacing = 0.8.sp)
                                        Spacer(Modifier.height(10.dp))
                                        TenantActionButtons(
                                            room = room,
                                            currentUser = currentUser,
                                            onBookNow = onBookNow,
                                            onMessageOwner = onMessageOwner,
                                            onCallOwner = onCallOwner,
                                            onLoginRequired = onLoginRequired
                                        )
                                    }
                                }
                            }

                            // Owner Information Card
                            if (!isOwnerView) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(18.dp),
                                    color = Color.White,
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .background(Color(0xFFEEF2FF), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Person, null, tint = PrimaryColor, modifier = Modifier.size(24.dp))
                                        }
                                        Spacer(Modifier.width(12.dp))
                                        Column(Modifier.weight(1f)) {
                                            Text(room.ownerName ?: "Landlord / Host", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                            Text("Verified Property Manager", fontSize = 12.sp, color = Color(0xFF64748B))
                                        }
                                        IconButton(
                                            onClick = { onMessageOwner(room) },
                                            modifier = Modifier.background(Color(0xFFEEF2FF), CircleShape)
                                        ) {
                                            Icon(Icons.AutoMirrored.Filled.Message, null, tint = PrimaryColor, modifier = Modifier.size(18.dp))
                                        }
                                        Spacer(Modifier.width(6.dp))
                                        IconButton(
                                            onClick = { onCallOwner(room) },
                                            modifier = Modifier.background(Color(0xFFECFDF5), CircleShape)
                                        ) {
                                            Icon(Icons.Default.Call, null, tint = SuccessColor, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Compact (<600dp) & Medium Layout
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.White
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(bottom = 76.dp)
                    ) {
                        // Full-Bleed Photo Header
                        Box(modifier = Modifier.fillMaxWidth()) {
                            PropertyPhotoCarousel(
                                imageUrls = imageUrls,
                                height = if (isMedium) 300.dp else 240.dp,
                                cornerRadius = 0.dp,
                                onImageClick = { index -> selectedImageIndex = index }
                            )

                            // Top Controls Overlay
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .pointerHoverIcon(PointerIcon.Hand)
                                        .clickable { onBack() },
                                    shape = CircleShape,
                                    color = Color.Black.copy(alpha = 0.45f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Surface(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .pointerHoverIcon(PointerIcon.Hand)
                                            .clickable {
                                                if (currentUser == null) {
                                                    onLoginRequired()
                                                } else {
                                                    scope.launch {
                                                        try {
                                                            val res = RoomifyApi.toggleFavorite(currentUser.id, room.id ?: 0L)
                                                            if (res.success && res.data != null) {
                                                                isSaved = res.data!!
                                                            }
                                                        } catch (e: Exception) {
                                                            println("Favorite error: ${e.message}")
                                                        }
                                                    }
                                                }
                                            },
                                        shape = CircleShape,
                                        color = Color.Black.copy(alpha = 0.45f)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                contentDescription = "Save",
                                                tint = if (isSaved) Color(0xFFEC4899) else Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Content Padding
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Header & Badges
                            PropertyHeaderDetails(
                                room = room,
                                statusUpper = statusUpper,
                                verificationStatus = verificationStatus
                            )

                            // Fee Transparency Banner
                            LandlordTransparencyBanner(room = room)

                            // Specs Row
                            PropertySpecsRow(room = room)

                            // AI Room Arranger & Route
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .clickable { onAIRoomArranger(room) },
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFF3E8FF),
                                    border = BorderStroke(1.dp, Color(0xFFE9D5FF))
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(Icons.Rounded.AutoAwesome, null, tint = Color(0xFF7C3AED), modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("AI Arrange Room", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B21A8))
                                    }
                                }

                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .clickable { onShowRoute(room) },
                                    shape = RoundedCornerShape(12.dp),
                                    color = PrimaryColor
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(Icons.Rounded.Directions, null, tint = Color.White, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Show Route", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }

                            // Description
                            SectionTitleDetails(strings.description)
                            Text(
                                text = room.description?.takeIf { it.isNotBlank() } ?: "No detailed description provided.",
                                fontSize = 14.sp,
                                color = Color(0xFF475569),
                                lineHeight = 20.sp
                            )

                            // Amenities
                            if (room.amenities.isNotEmpty()) {
                                SectionTitleDetails(strings.amenities)
                                FlowRow(mainAxisSpacing = 8.dp, crossAxisSpacing = 8.dp) {
                                    room.amenities.forEach { amenity ->
                                        Surface(color = Color(0xFFF1F5F9), shape = RoundedCornerShape(8.dp)) {
                                            Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Check, null, tint = SuccessColor, modifier = Modifier.size(14.dp))
                                                Spacer(Modifier.width(6.dp))
                                                Text(amenity, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                            }
                                        }
                                    }
                                }
                            }

                            // Neighborhood Insights
                            NeighborhoodInsights()

                            // Building Complex Inventory
                            if (room.propertyId != null && parentProperty != null) {
                                SectionTitleDetails("Building Inventory")
                                BuildingInventoryList(
                                    currentRoomId = room.id ?: 0,
                                    units = otherUnitsInComplex + listOf(room),
                                    onViewUnit = { onViewProperty(it) }
                                )

                                Spacer(Modifier.height(8.dp))
                                SectionTitleDetails("Complex Details")
                                ComplexOverviewCard(parentProperty!!)
                            }

                            // Owner Management controls if owner
                            if (isOwnerView) {
                                SectionTitleDetails("Listing Management")
                                OwnerManagementGrid(
                                    room = room,
                                    onEditProperty = onEditProperty,
                                    onDeleteProperty = onDeleteProperty
                                )
                            }

                            // Reviews
                            ReviewsSection(
                                room = room,
                                reviews = reviews,
                                currentUser = currentUser,
                                strings = strings,
                                onLoginRequired = onLoginRequired,
                                onWriteReviewClick = { showWriteReviewDialog = true }
                            )
                        }
                    }

                    // Sticky Bottom Bar for Mobile / Compact
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(68.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        shadowElevation = 8.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("MONTHLY RENT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                                Text(room.formattedPrice, fontSize = 18.sp, fontWeight = FontWeight.Black, color = PrimaryColor)
                            }

                            if (isOwnerView) {
                                Button(
                                    onClick = { onEditProperty(room) },
                                    modifier = Modifier.height(44.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                                ) {
                                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Edit Property", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            } else {
                                Button(
                                    onClick = {
                                        if (currentUser == null) {
                                            onLoginRequired()
                                        } else {
                                            onBookNow(room)
                                        }
                                    },
                                    modifier = Modifier.height(44.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                                ) {
                                    Text("Book / Request Viewing", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Space Planner Dialog
    if (showSpacePlanner) {
        SpacePlannerDialog(
            roomArea = room.area,
            roomImageUrl = room.firstImageUrl,
            onDismiss = onDismissSpacePlanner
        )
    }

    // AI Room Arranger Dialog
    if (showAIRoomArranger) {
        AIRoomArrangerDialog(
            room = room,
            onDismiss = onDismissAIRoomArranger
        )
    }

    // Full Screen Multi-Photo Viewer Modal
    if (selectedImageIndex != null && room.fullImageUrls.isNotEmpty()) {
        FullscreenImageViewerModal(
            imageUrls = room.fullImageUrls,
            initialIndex = selectedImageIndex!!,
            onDismiss = { selectedImageIndex = null }
        )
    }
}

@Composable
private fun PropertyPhotoCarousel(
    imageUrls: List<String>,
    height: Dp,
    cornerRadius: Dp,
    onImageClick: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(Color(0xFFF1F5F9))
    ) {
        if (imageUrls.isNotEmpty()) {
            val lazyListState = rememberLazyListState()
            val currentIndex = remember { derivedStateOf { lazyListState.firstVisibleItemIndex } }

            LazyRow(
                state = lazyListState,
                modifier = Modifier.fillMaxSize(),
                flingBehavior = rememberSnapFlingBehavior(lazyListState)
            ) {
                itemsIndexed(imageUrls) { index, imageUrl ->
                    Box(
                        modifier = Modifier
                            .fillParentMaxSize()
                            .pointerHoverIcon(PointerIcon.Hand)
                            .clickable { onImageClick(index) }
                    ) {
                        KamelImage(
                            resource = { asyncPainterResource(imageUrl) },
                            contentDescription = "Property Photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            onLoading = {
                                Box(Modifier.fillMaxSize().background(Color(0xFFF1F5F9)), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = PrimaryColor)
                                }
                            },
                            onFailure = {
                                Box(Modifier.fillMaxSize().background(Color(0xFFF1F5F9)), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.BrokenImage, null, tint = Color.LightGray, modifier = Modifier.size(40.dp))
                                }
                            }
                        )
                    }
                }
            }

            // Photo Count Chip (Bottom-Right)
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp),
                color = Color.Black.copy(alpha = 0.65f),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = "${currentIndex.value + 1} / ${imageUrls.size}",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.PhotoLibrary, null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("No photos uploaded", color = Color(0xFF64748B), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun PropertyHeaderDetails(
    room: Room,
    statusUpper: String,
    verificationStatus: String
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(color = PrimaryColor.copy(alpha = 0.08f), shape = RoundedCornerShape(6.dp)) {
                Text(
                    text = room.propertyType?.uppercase() ?: "PROPERTY",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryColor
                )
            }

            StatusBadgeChip(statusUpper = statusUpper, verificationStatus = verificationStatus)
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = room.title ?: "Property",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A),
            lineHeight = 28.sp
        )

        val cleanLocation = formatLocationDisplay(room.address, room.latitude, room.longitude)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 6.dp)
        ) {
            Icon(Icons.Default.LocationOn, null, tint = Color(0xFF64748B), modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(text = cleanLocation, fontSize = 13.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun StatusBadgeChip(statusUpper: String, verificationStatus: String) {
    val (statusBg, statusFg, statusText) = when {
        verificationStatus == "PENDING" -> Triple(Color(0xFFFEF3C7), Color(0xFFD97706), "Pending Verification")
        statusUpper == "RENTED" -> Triple(Color(0xFFFEF2F2), Color(0xFFDC2626), "Rented")
        else -> Triple(Color(0xFFDCFCE7), Color(0xFF16A34A), "Available")
    }

    Surface(color = statusBg, shape = RoundedCornerShape(6.dp)) {
        Text(
            text = statusText,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = statusFg
        )
    }
}

@Composable
private fun LandlordTransparencyBanner(room: Room) {
    if (room.isDirectByOwner) {
        Surface(
            color = Color(0xFFECFDF5),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.VerifiedUser, null, tint = Color(0xFF059669), modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
                Text(
                    "Direct from Landlord • 0% Broker Fee (No Dalali)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF047857)
                )
            }
        }
    } else {
        Surface(
            color = Color(0xFFEFF6FF),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Handshake, null, tint = PrimaryColor, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        "Dalali Listing (${room.dalaliName ?: "Verified Agent"}) • Facilitation Fee: ${room.formattedFacilitationFee}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryColor
                    )
                    Text(
                        "Rent is paid 100% to landlord. Facilitation fee covers viewing and service.",
                        fontSize = 11.sp,
                        color = Color(0xFF1D4ED8)
                    )
                }
            }
        }
    }
}

@Composable
private fun PropertySpecsRow(room: Room) {
    val strings = LocalRoomifyStrings.current
    val areaSqm = room.area

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFFF8FAFC),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SmallFeature(Icons.Default.Bed, "${room.roomsCount}", strings.beds)
            SmallFeature(Icons.Default.Bathtub, "${room.bathroomsCount}", strings.baths)
            if (areaSqm > 0.0) {
                SmallFeature(Icons.Default.SquareFoot, "${areaSqm.roundToInt()} m²", strings.area)
            }
        }
    }
}

@Composable
private fun TenantActionButtons(
    room: Room,
    currentUser: User?,
    onBookNow: (Room) -> Unit,
    onMessageOwner: (Room) -> Unit,
    onCallOwner: (Room) -> Unit,
    onLoginRequired: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(
            onClick = {
                if (currentUser == null) onLoginRequired()
                else onBookNow(room)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
        ) {
            Text("Book / Request Viewing", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = {
                    if (currentUser == null) onLoginRequired()
                    else onMessageOwner(room)
                },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Color(0xFFCBD5E1))
            ) {
                Icon(Icons.AutoMirrored.Filled.Message, null, tint = PrimaryColor, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Message", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PrimaryColor)
            }

            OutlinedButton(
                onClick = { onCallOwner(room) },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = SuccessColor)
            ) {
                Icon(Icons.Default.Call, null, tint = SuccessColor, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Call Host", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SuccessColor)
            }
        }
    }
}

@Composable
private fun OwnerManagementGrid(
    room: Room,
    onEditProperty: (Room) -> Unit,
    onDeleteProperty: (Room) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OwnerActionItem(Icons.Default.Edit, "Edit Info", Modifier.weight(1f)) { onEditProperty(room) }
            OwnerActionItem(Icons.Default.PhotoLibrary, "Manage Photos", Modifier.weight(1f)) { onEditProperty(room) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OwnerActionItem(Icons.Default.PlayCircle, "Update Video", Modifier.weight(1f)) { onEditProperty(room) }
            OwnerActionItem(Icons.Default.Description, "Update Contract", Modifier.weight(1f)) { onEditProperty(room) }
        }

        Spacer(Modifier.height(4.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .pointerHoverIcon(PointerIcon.Hand)
                .clickable { showDeleteDialog = true },
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFFFEF2F2),
            border = BorderStroke(1.dp, Color(0xFFFECACA))
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Delete, null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Delete Property", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Property?") },
            text = { Text("This action is permanent and cannot be undone. Are you sure?") },
            confirmButton = {
                TextButton(onClick = { onDeleteProperty(room); showDeleteDialog = false }) {
                    Text("DELETE", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("CANCEL")
                }
            }
        )
    }
}

@Composable
private fun ReviewsSection(
    room: Room,
    reviews: List<Review>,
    currentUser: User?,
    strings: org.com.i18n.RoomifyStrings,
    onLoginRequired: () -> Unit,
    onWriteReviewClick: () -> Unit
) {
    val avgRating = if (reviews.isNotEmpty()) reviews.map { it.rating }.average() else 0.0
    val formattedAvg = if (reviews.isNotEmpty()) ((avgRating * 10).roundToInt() / 10.0).toString() else "New"

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionTitleDetails("Reviews & Ratings (${reviews.size})")
            OutlinedButton(
                onClick = {
                    if (currentUser == null) onLoginRequired()
                    else onWriteReviewClick()
                },
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                border = BorderStroke(1.dp, PrimaryColor)
            ) {
                Icon(Icons.Default.RateReview, null, modifier = Modifier.size(16.dp), tint = PrimaryColor)
                Spacer(Modifier.width(6.dp))
                Text(strings.writeAReview, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryColor)
            }
        }

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = PrimaryColor.copy(alpha = 0.04f),
            border = BorderStroke(1.dp, PrimaryColor.copy(alpha = 0.1f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formattedAvg,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = PrimaryColor
                )
                Spacer(Modifier.width(14.dp))
                Column {
                    Row {
                        val ratingInt = avgRating.toInt().coerceIn(0, 5)
                        repeat(5) { index ->
                            Icon(
                                imageVector = if (index < ratingInt) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = null,
                                tint = Color(0xFFFFB300),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = if (reviews.isNotEmpty()) "Based on ${reviews.size} tenant reviews" else "No ratings yet",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }
        }

        if (reviews.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                reviews.take(5).forEach { review ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = review.userName.ifEmpty { "Verified Tenant" },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF0F172A)
                                )
                                Row {
                                    repeat(review.rating.coerceIn(1, 5)) {
                                        Icon(Icons.Default.Star, null, tint = Color(0xFFFFB300), modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                            if (review.comment.isNotBlank()) {
                                Spacer(Modifier.height(6.dp))
                                Text(review.comment, fontSize = 13.sp, color = Color(0xFF475569))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FullscreenImageViewerModal(
    imageUrls: List<String>,
    initialIndex: Int,
    onDismiss: () -> Unit
) {
    var currentIndex by remember { mutableStateOf(initialIndex.coerceIn(0, imageUrls.lastIndex)) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            // Main Photo
            KamelImage(
                resource = { asyncPainterResource(imageUrls[currentIndex]) },
                contentDescription = "Fullscreen Photo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )

            // Header Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${currentIndex + 1} / ${imageUrls.size}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(Icons.Rounded.Close, "Close", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }

            // Left / Right Navigation Arrows
            if (imageUrls.size > 1) {
                if (currentIndex > 0) {
                    IconButton(
                        onClick = { currentIndex -= 1 },
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 16.dp)
                            .size(44.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Previous", tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                }

                if (currentIndex < imageUrls.lastIndex) {
                    IconButton(
                        onClick = { currentIndex += 1 },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 16.dp)
                            .size(44.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, "Next", tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun DocumentCard(title: String, subtitle: String, onClick: () -> Unit, onDownload: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF8F9FA),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Description, null, tint = PrimaryColor, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(subtitle, fontSize = 11.sp, color = Color.Gray)
            }
            IconButton(onClick = onDownload) {
                Icon(Icons.Default.FileDownload, null, tint = PrimaryColor)
            }
        }
    }
}

@Composable
private fun RelatedPropertyCard(room: Room, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(200.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column {
            KamelImage(
                resource = { asyncPainterResource(room.firstImageUrl ?: "") },
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(110.dp),
                contentScale = ContentScale.Crop
            )
            Column(Modifier.padding(10.dp)) {
                Text(room.title ?: "Property", fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(room.formattedPrice, fontSize = 14.sp, color = PrimaryColor, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun OwnerActionItem(icon: ImageVector, title: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.height(44.dp).clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(Modifier.padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Icon(icon, null, tint = PrimaryColor, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryColor)
        }
    }
}

@Composable
private fun SmallFeature(icon: ImageVector, valText: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = PrimaryColor, modifier = Modifier.size(18.dp))
        Spacer(Modifier.height(4.dp))
        Text(valText, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
        Text(label, fontSize = 10.sp, color = Color(0xFF64748B))
    }
}

@Composable
private fun SectionTitleDetails(text: String) {
    Text(
        text = text,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF0F172A),
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun NeighborhoodInsights() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF8FAFC),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("NEIGHBORHOOD PULSE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B), letterSpacing = 0.6.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                InsightItem(Icons.Default.Commute, "Transport", "2 min to bus station", Color(0xFF0284C7))
                InsightItem(Icons.Default.School, "Schools", "3 nearby colleges", Color(0xFF7C3AED))
            }
        }
    }
}

@Composable
private fun InsightItem(icon: ImageVector, title: String, subtitle: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(32.dp).background(color.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.width(8.dp))
        Column {
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
            Text(subtitle, fontSize = 10.sp, color = Color(0xFF64748B))
        }
    }
}

@Composable
private fun BuildingInventoryList(currentRoomId: Long, units: List<Room>, onViewUnit: (Room) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(units) { unit ->
            val isCurrent = unit.id == currentRoomId
            Surface(
                modifier = Modifier
                    .width(140.dp)
                    .clickable { onViewUnit(unit) },
                shape = RoundedCornerShape(12.dp),
                color = if (isCurrent) PrimaryColor.copy(alpha = 0.08f) else Color.White,
                border = BorderStroke(1.dp, if (isCurrent) PrimaryColor else Color(0xFFE2E8F0))
            ) {
                Column(Modifier.padding(10.dp)) {
                    Text("Unit ${unit.unitNumber ?: unit.title}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF0F172A))
                    Text(unit.formattedPrice, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = PrimaryColor)
                }
            }
        }
    }
}

@Composable
private fun ComplexOverviewCard(property: Property) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(property.title ?: "Complex Overview", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F172A))
            Text(property.address ?: "", fontSize = 12.sp, color = Color(0xFF64748B))
        }
    }
}

private fun formatLocationDisplay(rawLocation: String?, lat: Double? = null, lng: Double? = null): String {
    if (rawLocation.isNullOrBlank()) {
        if (lat != null && lng != null && lat != 0.0 && lng != 0.0) {
            val shortLat = (lat * 10000.0).roundToInt() / 10000.0
            val shortLng = (lng * 10000.0).roundToInt() / 10000.0
            return "$shortLat, $shortLng"
        }
        return "Location available"
    }
    val isCoords = rawLocation.matches(Regex("^-?\\d+(\\.\\d+)?,\\s*-?\\d+(\\.\\d+)?$"))
    if (isCoords) {
        val parts = rawLocation.split(",").map { it.trim().toDoubleOrNull() }
        if (parts.size == 2 && parts[0] != null && parts[1] != null) {
            val shortLat = (parts[0]!! * 10000.0).roundToInt() / 10000.0
            val shortLng = (parts[1]!! * 10000.0).roundToInt() / 10000.0
            return "$shortLat, $shortLng"
        }
    }
    return rawLocation
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRow(mainAxisSpacing: Dp, crossAxisSpacing: Dp, content: @Composable () -> Unit) {
    androidx.compose.foundation.layout.FlowRow(
        horizontalArrangement = Arrangement.spacedBy(mainAxisSpacing),
        verticalArrangement = Arrangement.spacedBy(crossAxisSpacing)
    ) { content() }
}
