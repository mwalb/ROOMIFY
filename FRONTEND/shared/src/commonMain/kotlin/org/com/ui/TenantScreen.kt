package org.com.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlinx.coroutines.delay
import org.com.currentTimeMillis
import org.com.model.Room
import org.com.model.Booking
import org.com.network.ApiClient

private val PrimaryColor = Color(0xFF1A237E)
private val PrimaryLight = Color(0xFF3949AB)
private val SuccessColor = Color(0xFF4CAF50)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TenantScreen(
    tenantName: String, 
    profileImage: String? = null,
    bookings: List<Booking> = emptyList(),
    allRooms: List<Room> = emptyList(),
    recentlyViewed: List<Room> = emptyList(),
    savedRooms: List<Room> = emptyList(),
    isRefreshing: Boolean = false,
    onExploreRooms: () -> Unit, 
    onViewProperty: (Room) -> Unit = {},
    onViewAll: () -> Unit = {},
    onNavigate: (String) -> Unit = {}
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // HEADER (Blue Background)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(PrimaryColor, PrimaryLight)
                        ),
                        shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 800.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(top = 48.dp, bottom = 48.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Top row with Profile info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            @Suppress("DEPRECATION")
                            Text(
                                "$tenantName 👋", 
                                color = Color.White, 
                                fontSize = 24.sp, 
                                fontWeight = FontWeight.Black
                            )
                            @Suppress("DEPRECATION")
                            Text(
                                "Find a place that feels like home.", 
                                color = Color.White.copy(alpha = 0.7f), 
                                fontSize = 14.sp
                            )
                        }

                        if (!profileImage.isNullOrBlank() && !profileImage.contains("profile/image")) {
                            val fullUrl = ApiClient.resolveUrl(profileImage)
                            
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                                    .clickable { onNavigate("profile") }
                            ) {
                                KamelImage(
                                    resource = { asyncPainterResource(fullUrl) },
                                    contentDescription = "Profile",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        } else {
                            Surface(
                                modifier = Modifier.size(52.dp).clickable { onNavigate("profile") },
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    @Suppress("DEPRECATION")
                                    Text(tenantName.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                }
                            }
                        }
                    }
                }
            }

            // DASHBOARD CONTENT
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 800.dp)
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    // QUICK ACTIONS
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        @Suppress("DEPRECATION")
                        Text("Quick Actions", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color(0xFF111111))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            QuickActionItem(
                                title = "Explore Map", 
                                icon = Icons.Default.Map, 
                                color = Color(0xFF2196F3),
                                modifier = Modifier.weight(1f)
                            ) { onExploreRooms() }
                            
                            QuickActionItem(
                                title = "Filter Rooms", 
                                icon = Icons.Default.Tune, 
                                color = Color(0xFF4CAF50),
                                modifier = Modifier.weight(1f)
                            ) { onNavigate("discovery") }
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                QuickActionItem(
                                    title = "Favourites", 
                                    icon = Icons.Default.Favorite, 
                                    color = Color(0xFFE91E63),
                                    modifier = Modifier.weight(1f)
                                ) { onNavigate("saved") }
                            
                            QuickActionItem(
                                title = "My Bookings", 
                                icon = Icons.AutoMirrored.Filled.ListAlt, 
                                color = Color(0xFFFF9800),
                                modifier = Modifier.weight(1f)
                            ) { onNavigate("bookings") }
                        }
                    }

                    // RECOMMENDED FOR YOU
                    if (allRooms.isNotEmpty()) {
                        DashboardSectionWithAction(
                            title = "Recommended For You", 
                            actionLabel = null,
                            content = {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    contentPadding = PaddingValues(bottom = 8.dp)
                                ) {
                                    items(allRooms.filter { it.status == "AVAILABLE" }.take(5)) { room ->
                                        TenantPropertyCard(room) { onViewProperty(room) }
                                    }
                                }
                            }
                        )
                    }

                    // RECENTLY VIEWED
                    if (recentlyViewed.isNotEmpty()) {
                        DashboardSectionWithAction(
                            title = "Recently Viewed", 
                            actionLabel = null,
                            content = {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    contentPadding = PaddingValues(bottom = 8.dp)
                                ) {
                                    items(recentlyViewed) { room ->
                                        TenantPropertyCard(room) { onViewProperty(room) }
                                    }
                                }
                            }
                        )
                    }

                    // FAVOURITES PREVIEW
                    DashboardSectionWithAction(
                        title = "Favourites", 
                        actionLabel = "View All", 
                        onActionClick = { onNavigate("saved") },
                        content = {
                            Surface(
                                modifier = Modifier.fillMaxWidth().clickable { onNavigate("saved") },
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0xFFFFF1F0),
                                border = BorderStroke(1.dp, Color(0xFFFFCDD2).copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(20.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier.size(44.dp).background(Color(0xFFF44336), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Favorite, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(Modifier.width(16.dp))
                                    @Suppress("DEPRECATION")
                                    Column(Modifier.weight(1f)) {
                                        Text("My Favourites", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFFD32F2F))
                                        Text(
                                            if (savedRooms.isEmpty()) "Access your bookmarked properties" 
                                            else "${savedRooms.size} favourite properties",
                                            fontSize = 12.sp, 
                                            color = Color.Gray
                                        )
                                    }
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color(0xFFD32F2F), modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    )

                    // MY BOOKINGS PREVIEW
                    if (bookings.isNotEmpty()) {
                        DashboardSectionWithAction(
                            title = "My Bookings", 
                            actionLabel = "View All", 
                            onActionClick = { onNavigate("bookings") },
                            content = {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    bookings.take(2).forEach { booking ->
                                        val room = allRooms.find { it.id == booking.roomId }
                                        BookingStatusCardCompactPreview(booking, room) {
                                            room?.let { onViewProperty(it) }
                                        }
                                    }
                                }
                            }
                        )
                    }

                    // MESSAGES PREVIEW
                    DashboardSectionWithAction(
                        title = "Messages", 
                        actionLabel = "View All", 
                        onActionClick = { onNavigate("messages") },
                        content = {
                            Surface(
                                modifier = Modifier.fillMaxWidth().clickable { onNavigate("messages") },
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0xFFE3F2FD),
                                border = BorderStroke(1.dp, Color(0xFFBBDEFB).copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(20.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier.size(44.dp).background(Color(0xFF2196F3), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Email, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(Modifier.width(16.dp))
                                    @Suppress("DEPRECATION")
                                    Column(Modifier.weight(1f)) {
                                        Text("Inbox", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1976D2))
                                        Text("Chat with owners and agents", fontSize = 12.sp, color = Color.Gray)
                                    }
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color(0xFF1976D2), modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    )

                    // NEARBY PROPERTIES MAP SHORTCUT
                    DashboardSectionWithAction(
                        title = "Nearby Properties", 
                        actionLabel = null,
                        content = {
                            Surface(
                                modifier = Modifier.fillMaxWidth().clickable { onExploreRooms() },
                                shape = RoundedCornerShape(24.dp),
                                color = Color.Black
                            ) {
                                Box(modifier = Modifier.height(180.dp)) {
                                    Box(modifier = Modifier.fillMaxSize().background(
                                        brush = Brush.linearGradient(listOf(Color(0xFF1E3C72), Color(0xFF2A5298)))
                                    ).graphicsLayer { alpha = 0.6f })
                                    
                                    Column(
                                        modifier = Modifier.fillMaxSize().padding(24.dp),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(Icons.Default.LocationOn, null, tint = Color.White, modifier = Modifier.size(36.dp))
                                        Spacer(Modifier.height(12.dp))
                                        @Suppress("DEPRECATION")
                                        Text("Explore on Map", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                                        @Suppress("DEPRECATION")
                                        Text("See available properties around you", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    )

                    // VIEW ALL Action
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { onViewAll() },
                        shape = RoundedCornerShape(20.dp),
                        color = PrimaryColor
                    ) {
                        @Suppress("DEPRECATION")
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("View All Properties", color = Color.White, fontWeight = FontWeight.Black, fontSize = 17.sp)
                            Text("Browse the complete property catalog", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                        }
                    }

                    Spacer(Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
private fun QuickActionItem(title: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.height(100.dp).clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(color = color.copy(alpha = 0.1f), shape = CircleShape, modifier = Modifier.size(36.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
                }
            }
            @Suppress("DEPRECATION")
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
        }
    }
}

@Composable
private fun TenantPropertyCard(room: Room, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(220.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFF0F0F0))
    ) {
        Column {
            Box {
                KamelImage(
                    resource = { asyncPainterResource(room.firstImageUrl ?: "") },
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(130.dp),
                    contentScale = ContentScale.Crop,
                    onLoading = { _: Float ->
                        Box(Modifier.fillMaxSize().background(Color(0xFFF5F5F5)), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = PrimaryColor)
                        }
                    },
                    onFailure = {
                        Box(Modifier.fillMaxSize().background(Color(0xFFF5F5F5)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.BrokenImage, null, tint = Color.LightGray, modifier = Modifier.size(24.dp))
                        }
                    }
                )
                Surface(
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    @Suppress("DEPRECATION")
                    Text(
                        room.propertyType ?: "Room", 
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = Color.White, 
                        fontSize = 10.sp, 
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            @Suppress("DEPRECATION")
            Column(Modifier.padding(12.dp)) {
                Text(room.title ?: "Property", fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Icon(Icons.Default.LocationOn, null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(room.address ?: "", fontSize = 11.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Spacer(Modifier.height(8.dp))
                Text(room.formattedPrice, fontSize = 15.sp, color = PrimaryColor, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun DashboardSectionWithAction(title: String, actionLabel: String?, onActionClick: () -> Unit = {}, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            @Suppress("DEPRECATION")
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF111111))
            if (actionLabel != null) {
                TextButton(onClick = onActionClick) {
                    @Suppress("DEPRECATION")
                    Text(actionLabel, fontWeight = FontWeight.Bold, color = PrimaryColor, fontSize = 14.sp)
                }
            }
        }
        content()
    }
}

@Composable
private fun BookingStatusCardCompactPreview(booking: Booking, room: Room?, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(50.dp).clip(RoundedCornerShape(10.dp))) {
                KamelImage(
                    resource = { asyncPainterResource(room?.firstImageUrl ?: "") },
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    onLoading = { _: Float ->
                        Box(Modifier.fillMaxSize().background(Color(0xFFF5F5F5)), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp, color = PrimaryColor)
                        }
                    },
                    onFailure = {
                        Box(Modifier.fillMaxSize().background(Color(0xFFF5F5F5)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.BrokenImage, null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                        }
                    }
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                @Suppress("DEPRECATION")
                Text(booking.roomTitle ?: "Room", fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                val statusColor = when(booking.status) {
                    "ACCEPTED" -> SuccessColor
                    "PENDING" -> Color(0xFFF9A825)
                    else -> Color.Red
                }
                @Suppress("DEPRECATION")
                Text(booking.status, fontSize = 11.sp, color = statusColor, fontWeight = FontWeight.Bold)
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
        }
    }
}
