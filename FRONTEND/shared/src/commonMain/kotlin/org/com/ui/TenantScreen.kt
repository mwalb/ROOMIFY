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
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.TrendingUp
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
    isRefreshing: Boolean = false,
    onExploreRooms: () -> Unit, 
    onLogout: () -> Unit,
    onViewProperty: (Room) -> Unit = {},
    onNavigate: (String) -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = PrimaryColor,
                drawerTonalElevation = 0.dp,
                modifier = Modifier.width(280.dp).fillMaxHeight()
            ) {
                Spacer(Modifier.height(48.dp))
                Text("TENANT MENU", modifier = Modifier.padding(16.dp), color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                
                TenantDrawerItem("Tenant Dashboard", Icons.Default.Dashboard) {
                    scope.launch { 
                        drawerState.close()
                        scrollState.animateScrollTo(0)
                    }
                }
                TenantDrawerItem("Explore Map", Icons.Default.Map) { 
                    scope.launch { drawerState.close() }
                    onExploreRooms() 
                }
                TenantDrawerItem("My Bookings", Icons.AutoMirrored.Filled.ListAlt) { 
                    scope.launch { 
                        drawerState.close()
                        scrollState.animateScrollTo(400)
                    } 
                }
                TenantDrawerItem("Analytics", Icons.AutoMirrored.Filled.TrendingUp) {
                    scope.launch { drawerState.close() }
                    onNavigate("analytics")
                }
                TenantDrawerItem("Saved Properties", Icons.Default.Favorite) {
                    scope.launch { drawerState.close() }
                    onNavigate("saved")
                }
                TenantDrawerItem("Messages", Icons.Default.Email) {
                    scope.launch { drawerState.close() }
                    onNavigate("messages")
                }
                TenantDrawerItem("Profile Settings", Icons.Default.Person) { 
                    scope.launch { drawerState.close() }
                    onNavigate("profile") 
                }
                
                Spacer(Modifier.weight(1f))
                TenantDrawerItem("Logout", Icons.AutoMirrored.Filled.Logout) { 
                    scope.launch { drawerState.close() }
                    onLogout() 
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(PrimaryColor, PrimaryLight))),
            contentAlignment = Alignment.Center
        ) {
            // Main Centered Dashboard Card
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                    .widthIn(max = 500.dp)
                    .fillMaxHeight(0.92f)
                    .padding(vertical = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header inside the Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PrimaryColor)
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { scope.launch { drawerState.open() } }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Menu, null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    @Suppress("DEPRECATION")
                                    Text("Tenant Console", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                                    if (isRefreshing) {
                                        Spacer(Modifier.width(8.dp))
                                        CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp, color = Color.White)
                                    }
                                }
                                Text("Hello, $tenantName", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Black)
                            }
                            
                            if (!profileImage.isNullOrBlank() && !profileImage.contains("profile/image")) {
                                val fullUrl = if (profileImage.startsWith("http")) profileImage 
                                              else "${ApiClient.MEDIA_BASE_URL}${if (profileImage.startsWith("/")) "" else "/"}$profileImage"
                                
                                // Cache buster to ensure refresh
                                val finalUrl = if (fullUrl.contains("?")) "$fullUrl&cb=${currentTimeMillis()}"
                                               else "$fullUrl?cb=${currentTimeMillis()}"

                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                                        .clickable { onNavigate("profile") }
                                ) {
                                    KamelImage(
                                        resource = { asyncPainterResource(finalUrl) },
                                        contentDescription = "Profile",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize(),
                                        onLoading = { _: Float -> Box(Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.1f))) },
                                        onFailure = {
                                            // Fallback to logout icon if image fails
                                            IconButton(onClick = onLogout, modifier = Modifier.fillMaxSize()) {
                                                Icon(Icons.AutoMirrored.Filled.Logout, null, tint = Color.White, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    )
                                }
                            } else {
                                IconButton(onClick = onLogout, modifier = Modifier.size(36.dp).background(Color.White.copy(alpha = 0.15f), CircleShape)) {
                                    Icon(Icons.AutoMirrored.Filled.Logout, null, tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }

                    // Scrollable content
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(scrollState)
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Hero Section
                        Surface(
                            color = PrimaryColor.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, PrimaryColor.copy(alpha = 0.1f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(20.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Find your next home", fontSize = 18.sp, fontWeight = FontWeight.Black, color = PrimaryColor, modifier = Modifier.weight(1f))
                                    TopTenantBadge()
                                }
                                Text("Browse rooms on the interactive map and book your ideal stay.", fontSize = 13.sp, color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
                                Button(
                                    onClick = onExploreRooms,
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                                ) {
                                    Icon(Icons.Default.Map, null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    @Suppress("DEPRECATION")
                                    Text("EXPLORE MAP", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }

                        // Smart Match Carousel
                        if (allRooms.isNotEmpty()) {
                            SectionTitleTenant("Smart Match ✨")
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(allRooms.take(5)) { room ->
                                    SmartMatchCard(room) { onViewProperty(room) }
                                }
                            }
                        }

                        // Platform Activity Stream
                        ActivityStream()

                        // Quick Actions Row
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            TenantActionCard("Saved", Icons.Default.Favorite, Color(0xFFE91E63), Modifier.weight(1f)) { onNavigate("saved") }
                            TenantActionCard("Messages", Icons.Default.Email, Color(0xFF2196F3), Modifier.weight(1f)) { onNavigate("messages") }
                        }

                        // My Bookings Section
                        if (bookings.isNotEmpty()) {
                            SectionTitleTenant("My Requests")
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                bookings.forEach { booking ->
                                    val room = allRooms.find { it.id == booking.roomId }
                                    BookingStatusCardCompact(booking, room) {
                                        room?.let { onViewProperty(it) }
                                    }
                                }
                            }
                        } else {
                            SectionTitleTenant("Recent Space")
                            InfoCardCompact("No active requests", "Your property inquiries will appear here.", Icons.AutoMirrored.Filled.Assignment)
                        }

                        // Quick Tips / Info
                        SectionTitleTenant("Tenant Tools")
                        InfoCardCompact("Verified Listings", "Review property info before contacting owners.", Icons.Default.Verified)
                        InfoCardCompact("Digital Contracts", "View and sign rental terms directly in app.", Icons.Default.Description)
                    }
                }
            }
        }
    }
}

@Composable
private fun TopTenantBadge() {
    Surface(
        color = SuccessColor,
        shape = CircleShape
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(Icons.Default.VerifiedUser, null, tint = Color.White, modifier = Modifier.size(10.dp))
            Spacer(Modifier.width(4.dp))
            Text("TOP TENANT", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun SmartMatchCard(room: Room, onClick: () -> Unit) {
    var animatedProgress by remember { mutableStateOf(0f) }
    val matchScore = remember { (85..99).random() }
    
    LaunchedEffect(Unit) { animatedProgress = matchScore / 100f }
    val progress by animateFloatAsState(animatedProgress, tween(1500))

    Card(
        onClick = onClick,
        modifier = Modifier.width(200.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box {
            KamelImage(
                resource = { asyncPainterResource(room.firstImageUrl ?: "") },
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(120.dp),
                contentScale = ContentScale.Crop
            )
            
            // Match Overlay
            Surface(
                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                color = Color.Black.copy(alpha = 0.7f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(progress = { progress }, color = SuccessColor, strokeWidth = 2.dp)
                        Text("${(progress * 100).toInt()}%", color = Color.White, fontSize = 6.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(4.dp))
                    Text("Match", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Column(Modifier.padding(12.dp)) {
            Text(room.title ?: "Property", fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(room.formattedPrice, fontSize = 11.sp, color = PrimaryColor, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun ActivityStream() {
    val activities = listOf(
        "Modern Apartment in Mbezi just booked! 🔥",
        "New property listed in Upanga 🏠",
        "Owner Sarah responded to a message 💬",
        "Trending: Studios under 500k 📈"
    )
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF121212),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.NotificationsActive, null, tint = Color.Yellow, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(8.dp))
            var currentIndex by remember { mutableStateOf(0) }
            
            LaunchedEffect(Unit) {
                while(true) {
                    delay(4000)
                    currentIndex = (currentIndex + 1) % activities.size
                }
            }

            AnimatedContent(
                targetState = currentIndex,
                transitionSpec = {
                    (slideInVertically { it } + fadeIn()).togetherWith(slideOutVertically { -it } + fadeOut())
                }
            ) { index ->
                Text(
                    text = activities[index],
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun TenantActionCard(title: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.height(80.dp).clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.1f))
    ) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(6.dp))
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
private fun TenantDrawerItem(label: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(16.dp))
        @Suppress("DEPRECATION")
        Text(label, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun SectionTitleTenant(text: String) {
    Text(text, fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFF111111))
}

@Composable
private fun InfoCardCompact(title: String, desc: String, icon: ImageVector) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).background(PrimaryColor.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = PrimaryColor, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
                Text(desc, fontSize = 12.sp, color = Color.Gray)
            }
        }
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
