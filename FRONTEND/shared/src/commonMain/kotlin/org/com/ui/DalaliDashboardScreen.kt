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
    val scrollState = rememberScrollState()

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
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Agent Console", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                                if (isRefreshing) {
                                    Spacer(Modifier.width(8.dp))
                                    CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp, color = Color.White)
                                }
                            }
                            Text(user.name, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                        }
                            
                            if (!user.profileImage.isNullOrBlank()) {
                                val fullUrl = ApiClient.resolveUrl(user.profileImage!!)

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
                                        onLoading = { _: Float -> Box(Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.1f))) },
                                        onFailure = {
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

                    // Scrollable Content
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(scrollState)
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Key Stats Row
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            StatBox(title = "Properties", value = "${properties.size}", icon = Icons.Default.Business, modifier = Modifier.weight(1f))
                            StatBox(title = "Commission", value = "TZS 450K", icon = Icons.Default.Payments, modifier = Modifier.weight(1f))
                        }

                        // Quick Actions
                        Column {
                            Text("Actions", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color.Gray)
                            Spacer(Modifier.height(12.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                ActionItem(title = "Add Listing", icon = Icons.Default.Add, color = PrimaryColor, modifier = Modifier.weight(1f), onClick = onAddProperty)
                                ActionItem(title = "Analytics", icon = Icons.AutoMirrored.Filled.TrendingUp, color = Color(0xFF00897B), modifier = Modifier.weight(1f), onClick = onViewAnalytics)
                            }
                        }

                        // Listings
                        Column {
                            Text("Active Listings", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color.Gray)
                            Spacer(Modifier.height(12.dp))
                            if (properties.isEmpty()) {
                                Surface(Modifier.fillMaxWidth().height(100.dp), color = Color.White, shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, Color(0xFFEEEEEE))) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("No assigned properties", color = Color.LightGray, fontSize = 13.sp)
                                    }
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    properties.forEach { room ->
                                        DalaliPropertyCard(room, onClick = { onViewProperty(room) })
                                    }
                                }
                            }
                        }

                        // Pending Requests
                        Column {
                            Text("Pending Requests", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color.Gray)
                            Spacer(Modifier.height(12.dp))
                            val pendingBookings = bookings.filter { it.status == "PENDING" }
                            if (pendingBookings.isEmpty()) {
                                Surface(Modifier.fillMaxWidth().height(100.dp), color = Color.White, shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, Color(0xFFEEEEEE))) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("No pending requests", color = Color.LightGray, fontSize = 13.sp)
                                    }
                                }
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

@Composable
private fun DalaliDrawerItem(label: String, icon: ImageVector, onClick: () -> Unit) {
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
private fun StatBox(title: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = Color.White,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(16.dp)) {
            Icon(icon, null, tint = PrimaryColor, modifier = Modifier.size(22.dp))
            Spacer(Modifier.height(10.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = PrimaryColor)
            Text(title, fontSize = 13.sp, color = Color.DarkGray, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun ActionItem(title: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.height(72.dp).clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.1f))
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(10.dp))
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
private fun DalaliPropertyCard(room: Room, onClick: () -> Unit) {
    // ...
}

@Composable
private fun DalaliBookingRequestCard(booking: Booking, onAccept: () -> Unit, onReject: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(40.dp).clip(CircleShape).background(PrimaryColor.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, null, tint = PrimaryColor, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(booking.userName ?: "Guest", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(booking.roomTitle ?: "Property", fontSize = 11.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Text("TZS ${booking.totalPrice.toInt()}", fontWeight = FontWeight.Black, fontSize = 13.sp, color = PrimaryColor)
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onAccept, modifier = Modifier.weight(1f).height(36.dp), shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))) {
                    Text("Accept", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(onClick = onReject, modifier = Modifier.weight(1f).height(36.dp), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, Color.Red)) {
                    Text("Reject", fontSize = 12.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
