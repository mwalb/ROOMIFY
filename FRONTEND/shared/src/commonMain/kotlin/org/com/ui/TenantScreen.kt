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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.Logout
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
    isRefreshing: Boolean = false,
    onExploreRooms: () -> Unit, 
    onLogout: () -> Unit,
    onViewProperty: (Room) -> Unit = {},
    onSearchQuery: (String) -> Unit = {},
    onViewAll: () -> Unit = {},
    onNavigate: (String) -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

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
            // HEADER & SEARCH BOX (Blue Background)
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
                        .padding(top = 48.dp, bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Profile + Name
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Good morning, $tenantName 👋", 
                                color = Color.White, 
                                fontSize = 26.sp, 
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                "Find a place that feels like home.", 
                                color = Color.White.copy(alpha = 0.7f), 
                                fontSize = 15.sp
                            )
                        }

                        if (!profileImage.isNullOrBlank() && !profileImage.contains("profile/image")) {
                            val fullUrl = if (profileImage.startsWith("http")) profileImage 
                                          else "${ApiClient.MEDIA_BASE_URL}${if (profileImage.startsWith("/")) "" else "/"}$profileImage"
                            
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
                                    Text(tenantName.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                }
                            }
                        }
                    }

                    // SEARCH BAR
                    var dashboardSearchQuery by remember { mutableStateOf("") }
                    Surface(
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        shape = RoundedCornerShape(18.dp),
                        color = Color.White,
                        shadowElevation = 8.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Search, null, tint = PrimaryColor, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(12.dp))
                            TextField(
                                value = dashboardSearchQuery,
                                onValueChange = { dashboardSearchQuery = it },
                                placeholder = { Text("Search area, location, property...", color = Color.Gray.copy(alpha = 0.5f), fontSize = 15.sp) },
                                modifier = Modifier.weight(1f),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(
                                    onSearch = {
                                        if (dashboardSearchQuery.isNotBlank()) {
                                            onSearchQuery(dashboardSearchQuery)
                                        }
                                    }
                                )
                            )
                        }
                    }
                }
            }

            // MAIN DASHBOARD CONTENT
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 800.dp)
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    // QUICK ACTIONS GRID
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
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
                                title = "Saved Rooms", 
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
                        DashboardSection("Recommended For You") {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                contentPadding = PaddingValues(bottom = 8.dp)
                            ) {
                                items(allRooms.filter { it.status == "AVAILABLE" }.take(5)) { room ->
                                    TenantPropertyCard(room) { onViewProperty(room) }
                                }
                            }
                        }
                    }

                    // RECENTLY VIEWED
                    if (recentlyViewed.isNotEmpty()) {
                        DashboardSection("Recently Viewed") {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                contentPadding = PaddingValues(bottom = 8.dp)
                            ) {
                                items(recentlyViewed) { room ->
                                    TenantPropertyCard(room) { onViewProperty(room) }
                                }
                            }
                        }
                    }

                    // MY BOOKINGS (Real Data)
                    if (bookings.isNotEmpty()) {
                        DashboardSection("My Bookings") {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                bookings.take(3).forEach { booking ->
                                    val room = allRooms.find { it.id == booking.roomId }
                                    BookingStatusCardCompact(booking, room) {
                                        room?.let { onViewProperty(it) }
                                    }
                                }
                                if (bookings.size > 3) {
                                    TextButton(
                                        onClick = { onNavigate("bookings") }, 
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    ) {
                                        @Suppress("DEPRECATION")
                                        Text("View all bookings", fontWeight = FontWeight.Bold, color = PrimaryColor)
                                    }
                                }
                            }
                        }
                    }

                    // MESSAGES (Direct Dashboard Section)
                    DashboardSection("Messages") {
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
                                    modifier = Modifier.size(48.dp).background(Color(0xFF2196F3), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Email, null, tint = Color.White, modifier = Modifier.size(24.dp))
                                }
                                Spacer(Modifier.width(16.dp))
                                Column(Modifier.weight(1f)) {
                                    Text("Inbox", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFF1976D2))
                                    Text("Check your conversations with owners", fontSize = 12.sp, color = Color.Gray)
                                }
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color(0xFF1976D2), modifier = Modifier.size(20.dp))
                            }
                        }
                    }

                    // NEARBY PROPERTIES MAP SHORTCUT
                    DashboardSection("Nearby Properties") {
                        Surface(
                            modifier = Modifier.fillMaxWidth().clickable { onExploreRooms() },
                            shape = RoundedCornerShape(24.dp),
                            color = Color.Black
                        ) {
                            Box(modifier = Modifier.height(180.dp)) {
                                // Abstract map background visualization
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
                                    Text("Explore on Map", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                                    Text("See available properties around you", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    // VIEW ALL PROPERTIES Action
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { onViewAll() },
                        shape = RoundedCornerShape(20.dp),
                        color = PrimaryColor
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            @Suppress("DEPRECATION")
                            Text("View All Properties", color = Color.White, fontWeight = FontWeight.Black, fontSize = 17.sp)
                            Text("Browse the complete property catalog", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                        }
                    }

                    // PROFILE & SETTINGS Section (At the bottom)
                    DashboardSection("Account") {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFEEEEEE))
                        ) {
                            Column {
                                AccountActionRow("Profile", Icons.Default.Person) { onNavigate("profile") }
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFF5F5F5))
                                AccountActionRow("Settings", Icons.Default.Settings) { onNavigate("profile") }
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFF5F5F5))
                                AccountActionRow("Logout", Icons.AutoMirrored.Filled.Logout, color = Color.Red) { onLogout() }
                            }
                        }
                    }

                    Spacer(Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
private fun AccountActionRow(label: String, icon: ImageVector, color: Color = PrimaryColor, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = color.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(16.dp))
        Text(label, color = if (color == Color.Red) color else Color(0xFF333333), fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.weight(1f))
        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
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
                    onLoading = { Box(Modifier.fillMaxSize().background(Color(0xFFF5F5F5))) }
                )
                Surface(
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(8.dp)
                ) {
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
private fun DashboardSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111111))
        content()
    }
}

@Composable
private fun BookingStatusCardCompact(booking: Booking, room: Room?, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.96f else 1f)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick
            ),
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
        shadowElevation = 2.dp
    ) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            // Property Image with reload support
            Box(Modifier.size(60.dp).clip(RoundedCornerShape(12.dp))) {
                KamelImage(
                    resource = { asyncPainterResource(room?.firstImageUrl ?: "") },
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    onLoading = { _: Float -> 
                        Box(Modifier.fillMaxSize().background(Color(0xFFF0F2F5)), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                        }
                    },
                    onFailure = { _: Throwable ->
                        Box(Modifier.fillMaxSize().background(Color(0xFFF0F2F5)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.BrokenImage, null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
                        }
                    }
                )
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(Modifier.weight(1f)) {
                @Suppress("DEPRECATION")
                Text(booking.roomTitle ?: "Room", fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                
                val statusText = when(booking.status) {
                    "PENDING" -> "Waiting for approval"
                    "ACCEPTED" -> "Request Approved"
                    "REJECTED" -> "Request Declined"
                    else -> booking.status
                }
                val statusColor = when(booking.status) {
                    "PENDING" -> Color(0xFFF9A825)
                    "ACCEPTED" -> SuccessColor
                    else -> Color.Red
                }
                
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                    Surface(color = statusColor.copy(alpha = 0.1f), shape = CircleShape) {
                        Text(
                            text = statusText, 
                            fontSize = 10.sp, 
                            color = statusColor, 
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                // Video / Document indicators
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (room?.hasVideo == true) Icon(Icons.Default.PlayCircle, null, tint = PrimaryColor, modifier = Modifier.size(14.dp))
                    if (room?.hasContract == true) Icon(Icons.Default.Description, null, tint = Color(0xFF2196F3), modifier = Modifier.size(14.dp))
                }
                Spacer(Modifier.height(4.dp))
                Icon(
                    imageVector = if (booking.status == "ACCEPTED") Icons.Default.CheckCircle else Icons.Default.Info, 
                    contentDescription = null, 
                    tint = if (booking.status == "ACCEPTED") SuccessColor else Color.LightGray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
