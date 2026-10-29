package org.com.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import org.com.i18n.LocalRoomifyStrings
import org.com.model.*
import kotlinx.coroutines.launch
import org.com.currentTimeMillis
import org.com.network.ApiClient

private val PrimaryColor = Color(0xFF1A237E)
private val PrimaryLight = Color(0xFF3949AB)
private val SuccessColor = Color(0xFF4CAF50)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DalaliDashboardScreen(
    user: User,
    properties: List<Room>,
    bookings: List<Booking> = emptyList(),
    isRefreshing: Boolean = false,
    onAddProperty: () -> Unit,
    onLogout: () -> Unit,
    onViewProperty: (Room) -> Unit,
    onViewAnalytics: () -> Unit = {},
    onAcceptBooking: (Booking) -> Unit = {},
    onRejectBooking: (Booking) -> Unit = {},
    onSearch: (type: String?, area: String?, maxPrice: Double?, status: String?) -> Unit = { _, _, _, _ -> },
    onNavigate: (String) -> Unit = {},
    onBack: () -> Unit
) {
    val strings = LocalRoomifyStrings.current
    var selectedTab by remember { mutableStateOf(0) }
    var isSidebarVisible by remember { mutableStateOf(true) }
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(PrimaryColor, PrimaryLight))),
        contentAlignment = Alignment.Center
    ) {
        // Responsive Dashboard Container (Animated Side Navigation + Main Content)
        Card(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.94f)
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Animated Left Sidebar
                AnimatedVisibility(
                    visible = isSidebarVisible,
                    enter = expandHorizontally() + fadeIn(),
                    exit = shrinkHorizontally() + fadeOut()
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(260.dp),
                        color = PrimaryColor,
                        shape = RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .background(Color.White.copy(alpha = 0.15f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.SupportAgent, null, tint = Color.White, modifier = Modifier.size(22.dp))
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text("ROOMIFY", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                                        Text("Agent Console", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(Modifier.height(32.dp))

                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    SidebarNavItem("Dashboard", Icons.Default.Dashboard, selectedTab == 0) { selectedTab = 0 }
                                    SidebarNavItem("Listings", Icons.Default.HomeWork, selectedTab == 1) { selectedTab = 1 }
                                    SidebarNavItem("Requests", Icons.Default.Bookmark, selectedTab == 2) { selectedTab = 2 }
                                    SidebarNavItem("Analytics", Icons.AutoMirrored.Filled.TrendingUp, false) { onViewAnalytics() }
                                }
                            }

                            // Profile & Logout Footer
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigate("profile") },
                                color = Color.White.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            user.name.take(1).uppercase(),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(user.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text("Agent Online", color = SuccessColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                    IconButton(onClick = onLogout, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.AutoMirrored.Filled.Logout, null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                // Right Main Content Area
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(Color(0xFFF8F9FA))
                ) {
                    // Top Bar with Menu Toggle, Back, Welcome & Commission Summary
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = onBack,
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(PrimaryColor.copy(alpha = 0.08f), CircleShape)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = PrimaryColor, modifier = Modifier.size(18.dp))
                                }
                                Spacer(Modifier.width(8.dp))
                                IconButton(
                                    onClick = { isSidebarVisible = !isSidebarVisible },
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(PrimaryColor.copy(alpha = 0.08f), CircleShape)
                                ) {
                                    Icon(Icons.Default.Menu, "Toggle Sidebar", tint = PrimaryColor, modifier = Modifier.size(18.dp))
                                }
                                Spacer(Modifier.width(14.dp))
                                Column {
                                    Text("Welcome back, ${user.name} 👋", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                                    Text("Manage agent listings and tenant requests", fontSize = 12.sp, color = Color.Gray)
                                }
                            }

                            // Commission / Wallet Summary Card
                            Surface(
                                color = PrimaryColor,
                                shape = RoundedCornerShape(16.dp),
                                shadowElevation = 4.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Payments, null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(10.dp))
                                    Column {
                                        Text("Total Commission", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Text("TZS 450,000", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Scrollable Content
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(scrollState)
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        when (selectedTab) {
                            0 -> {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    StatBox("Listings", "${properties.size}", Icons.Default.Business, Modifier.weight(1f))
                                    StatBox("Commission", "TZS 450K", Icons.Default.Payments, Modifier.weight(1f))
                                    StatBox("Pending", "${bookings.count { it.status == "PENDING" }}", Icons.Default.BookmarkBorder, Modifier.weight(1f))
                                }

                                Column {
                                    Text("Quick Actions", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color.Gray)
                                    Spacer(Modifier.height(12.dp))
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                        ActionItem("Add Listing", Icons.Default.Add, PrimaryColor, Modifier.weight(1f), onClick = onAddProperty)
                                        ActionItem("Analytics", Icons.AutoMirrored.Filled.TrendingUp, Color(0xFF00897B), Modifier.weight(1f), onClick = onViewAnalytics)
                                    }
                                }

                                Column {
                                    Text("Active Listings (${properties.size})", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color.Gray)
                                    Spacer(Modifier.height(12.dp))
                                    if (properties.isEmpty()) {
                                        EmptyStatePlaceholder("No assigned properties yet")
                                    } else {
                                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            properties.forEach { room ->
                                                DalaliPropertyCard(room, onClick = { onViewProperty(room) })
                                            }
                                        }
                                    }
                                }
                            }
                            1 -> {
                                Text("Active Listings (${properties.size})", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color.Gray)
                                if (properties.isEmpty()) {
                                    EmptyStatePlaceholder("No assigned properties")
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        properties.forEach { room ->
                                            DalaliPropertyCard(room, onClick = { onViewProperty(room) })
                                        }
                                    }
                                }
                            }
                            2 -> {
                                Text("Pending Booking Requests", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color.Gray)
                                val pendingBookings = bookings.filter { it.status == "PENDING" }
                                if (pendingBookings.isEmpty()) {
                                    EmptyStatePlaceholder("No pending requests")
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        pendingBookings.forEach { booking ->
                                            DalaliBookingRequestCard(
                                                booking = booking,
                                                onAccept = { onAcceptBooking(booking) },
                                                onReject = { onRejectBooking(booking) }
                                            )
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
}

@Composable
private fun SidebarNavItem(title: String, icon: ImageVector, active: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = if (active) Color.White.copy(alpha = 0.18f) else Color.Transparent,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = if (active) Color.White else Color.White.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(12.dp))
            Text(
                title,
                color = if (active) Color.White else Color.White.copy(alpha = 0.7f),
                fontSize = 13.sp,
                fontWeight = if (active) FontWeight.Black else FontWeight.Bold
            )
        }
    }
}

@Composable
private fun StatBox(title: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = Color.White,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 2.dp
    ) {
        Column(Modifier.padding(18.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(PrimaryColor.copy(alpha = 0.08f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = PrimaryColor, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Black, color = PrimaryColor)
            Text(title, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ActionItem(title: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier
            .height(84.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.06f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
            Spacer(Modifier.height(6.dp))
            Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
private fun EmptyStatePlaceholder(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .background(Color.White, RoundedCornerShape(20.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Inventory, null, tint = Color.LightGray, modifier = Modifier.size(40.dp))
            Spacer(Modifier.height(8.dp))
            Text(text, color = Color.Gray, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun DalaliPropertyCard(room: Room, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 2.dp
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                if (room.firstImageUrl != null) {
                    KamelImage(
                        resource = { asyncPainterResource(room.firstImageUrl!!) },
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(Icons.Default.Home, null, tint = Color.Gray, modifier = Modifier.size(24.dp))
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(Modifier.weight(1f)) {
                Text(room.title ?: "Property", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(2.dp))
                Text(room.address ?: room.locationSummary, fontSize = 12.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(4.dp))
                Text(room.formattedPrice, fontWeight = FontWeight.Black, fontSize = 14.sp, color = PrimaryColor)
            }

            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.LightGray, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun DalaliBookingRequestCard(booking: Booking, onAccept: () -> Unit, onReject: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 2.dp
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(44.dp).clip(CircleShape).background(PrimaryColor.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, null, tint = PrimaryColor, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(booking.userName ?: "Guest", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))
                    Text(booking.roomTitle ?: "Property", fontSize = 12.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Text("TZS ${booking.totalPrice.toInt()}", fontWeight = FontWeight.Black, fontSize = 14.sp, color = PrimaryColor)
            }
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = onAccept, modifier = Modifier.weight(1f).height(40.dp), shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(containerColor = SuccessColor)) {
                    Text("Accept", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(onClick = onReject, modifier = Modifier.weight(1f).height(40.dp), shape = RoundedCornerShape(10.dp), border = BorderStroke(1.dp, Color.Red)) {
                    Text("Reject", fontSize = 13.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
