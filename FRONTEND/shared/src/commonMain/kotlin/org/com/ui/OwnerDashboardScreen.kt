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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import org.com.i18n.LocalRoomifyStrings
import org.com.model.Room
import kotlinx.coroutines.launch
import org.com.model.Booking
import org.com.network.ApiClient

private val PrimaryColor = Color(0xFF1A237E)
private val PrimaryLight = Color(0xFF3949AB)
private val SuccessColor = Color(0xFF4CAF50)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerDashboardScreen(
    ownerName: String,
    profileImage: String? = null,
    properties: List<Room>,
    bookings: List<Booking> = emptyList(),
    onAddProperty: () -> Unit,
    onLogout: () -> Unit,
    onViewProperty: (Room) -> Unit,
    onViewAnalytics: () -> Unit = {},
    onViewFinances: () -> Unit = {},
    onAcceptBooking: (Booking) -> Unit = {},
    onRejectBooking: (Booking) -> Unit = {},
    onNavigate: (String) -> Unit = {},
    onBack: () -> Unit
) {
    val strings = LocalRoomifyStrings.current
    var selectedTab by remember { mutableStateOf(0) }
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
                Text("OWNER MENU", modifier = Modifier.padding(16.dp), color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                
                OwnerDrawerItem("Dashboard", Icons.Default.Dashboard) {
                    scope.launch { 
                        drawerState.close()
                        selectedTab = 0
                    }
                }
                OwnerDrawerItem("My Properties", Icons.Default.Business) { 
                    scope.launch { 
                        drawerState.close()
                        selectedTab = 1
                    }
                }
                OwnerDrawerItem("Booking Requests", Icons.Default.Notifications) { 
                    scope.launch { 
                        drawerState.close()
                        selectedTab = 2
                    } 
                }
                OwnerDrawerItem("Add Property", Icons.Default.Add) {
                    scope.launch { drawerState.close() }
                    onAddProperty()
                }
                OwnerDrawerItem("Analytics", Icons.AutoMirrored.Filled.TrendingUp) {
                    scope.launch { drawerState.close() }
                    onViewAnalytics()
                }
                OwnerDrawerItem("Finances", Icons.Default.Payments) {
                    scope.launch { drawerState.close() }
                    onViewFinances()
                }
                OwnerDrawerItem("Profile", Icons.Default.Person) {
                    scope.launch { drawerState.close() }
                    onNavigate("profile")
                }
                
                Spacer(Modifier.weight(1f))
                OwnerDrawerItem("Logout", Icons.AutoMirrored.Filled.Logout) { 
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
                    // Dashboard Header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PrimaryColor)
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { scope.launch { drawerState.open() } }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Menu, null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Owner Dashboard", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(ownerName, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                            }
                            
                            if (!profileImage.isNullOrBlank()) {
                                val fullUrl = if (profileImage.startsWith("http")) profileImage 
                                              else "${ApiClient.MEDIA_BASE_URL}${if (profileImage.startsWith("/")) "" else "/"}$profileImage"
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                                        .clickable { onNavigate("profile") }
                                ) {
                                    KamelImage(
                                        resource = { asyncPainterResource(fullUrl) },
                                        contentDescription = "Profile",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize(),
                                        onLoading = { _: Float -> Box(Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.1f))) }
                                    )
                                }
                            } else {
                                IconButton(onClick = onLogout, modifier = Modifier.size(36.dp).background(Color.White.copy(alpha = 0.15f), CircleShape)) {
                                    Icon(Icons.AutoMirrored.Filled.Logout, null, tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }

                    // Custom Tab Bar
                    Row(
                        modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        DashboardTab("Overview", selectedTab == 0) { selectedTab = 0 }
                        DashboardTab("Properties", selectedTab == 1) { selectedTab = 1 }
                        DashboardTab("Requests", selectedTab == 2) { selectedTab = 2 }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Scrollable Content
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(scrollState)
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        when (selectedTab) {
                            0 -> {
                                // Stats Section
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    StatCardCompact("Properties", properties.size.toString(), Icons.Default.Business, Modifier.weight(1f))
                                    StatCardCompact("Views", properties.sumOf { it.viewCount }.toString(), Icons.Default.Visibility, Modifier.weight(1f))
                                }

                                // Quick Actions
                                Column {
                                    Text("Quick Actions", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color.Gray)
                                    Spacer(Modifier.height(12.dp))
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        ActionItemCompact("Add New", Icons.Default.Add, PrimaryColor, Modifier.weight(1f), onAddProperty)
                                        ActionItemCompact("Analytics", Icons.AutoMirrored.Filled.TrendingUp, Color(0xFF00897B), Modifier.weight(1f), onViewAnalytics)
                                        ActionItemCompact("Finances", Icons.Default.Payments, Color(0xFFF9A825), Modifier.weight(1f), onViewFinances)
                                    }
                                }
                            }
                            1 -> {
                                // Properties List
                                Text("My Properties", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color.Gray)
                                if (properties.isEmpty()) {
                                    EmptyState("No properties listed")
                                } else {
                                    properties.forEach { room ->
                                        OwnerPropertyCard(room, onClick = { onViewProperty(room) })
                                    }
                                }
                            }
                            2 -> {
                                // Bookings List
                                Text("Pending Requests", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color.Gray)
                                val pendingBookings = bookings.filter { it.status == "PENDING" }
                                if (pendingBookings.isEmpty()) {
                                    EmptyState("No pending requests")
                                } else {
                                    pendingBookings.forEach { booking ->
                                        BookingRequestCard(booking, onAccept = { onAcceptBooking(booking) }, onReject = { onRejectBooking(booking) })
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OwnerDrawerItem(label: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(16.dp))
        Text(label, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun DashboardTab(title: String, active: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, fontSize = 13.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Medium, color = if (active) PrimaryColor else Color.Gray)
        Spacer(Modifier.height(4.dp))
        Box(Modifier.width(20.dp).height(2.dp).background(if (active) PrimaryColor else Color.Transparent))
    }
}

@Composable
private fun StatCardCompact(title: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, color = Color.White, shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Column(Modifier.padding(16.dp)) {
            Icon(icon, null, tint = PrimaryColor, modifier = Modifier.size(18.dp))
            Spacer(Modifier.height(8.dp))
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Black, color = PrimaryColor)
            Text(title, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ActionItemCompact(title: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(modifier = modifier.height(80.dp).clickable { onClick() }, shape = RoundedCornerShape(16.dp), color = color.copy(alpha = 0.05f), border = BorderStroke(1.dp, color.copy(alpha = 0.1f))) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(6.dp))
            Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
private fun EmptyState(msg: String) {
    Box(Modifier.fillMaxWidth().height(150.dp).background(Color.White, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
        Text(msg, color = Color.LightGray, fontSize = 13.sp)
    }
}

@Composable
private fun OwnerPropertyCard(room: Room, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.95f else 1f)

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
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 2.dp
    ) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(64.dp).clip(RoundedCornerShape(12.dp))) {
                KamelImage(
                    resource = { asyncPainterResource(room.firstImageUrl ?: "") },
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    onLoading = { _: Float -> Box(Modifier.fillMaxSize().background(Color(0xFFF0F2F5))) }
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(room.title ?: "Property", fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1, color = Color(0xFF1A1A1A))
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(room.formattedPrice, fontSize = 13.sp, color = PrimaryColor, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.width(8.dp))
                    Surface(color = if (room.status == "AVAILABLE") Color(0xFFE8F5E9) else Color(0xFFFFEBEE), shape = CircleShape) {
                        Text(room.status, color = if (room.status == "AVAILABLE") Color(0xFF2E7D32) else Color.Red, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.LightGray, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun BookingRequestCard(booking: Booking, onAccept: () -> Unit, onReject: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = Color.White, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(36.dp).background(PrimaryColor.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, null, tint = PrimaryColor, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(booking.userName ?: "Guest", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(booking.roomTitle ?: "Room", fontSize = 12.sp, color = Color.Gray)
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onAccept, modifier = Modifier.weight(1f).height(36.dp), shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = SuccessColor)) {
                    Text("Accept", fontSize = 12.sp)
                }
                OutlinedButton(onClick = onReject, modifier = Modifier.weight(1f).height(36.dp), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, Color.Red)) {
                    Text("Reject", fontSize = 12.sp, color = Color.Red)
                }
            }
        }
    }
}
