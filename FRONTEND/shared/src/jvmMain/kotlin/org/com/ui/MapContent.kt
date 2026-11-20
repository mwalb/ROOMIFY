package org.com.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import org.com.model.Room
import org.com.ui.components.LiveRouteSummaryCard

private val PrimaryNavy = Color(0xFF142170)

@Composable
actual fun MapContent(
    rooms: List<Room>,
    allRooms: List<Room>,
    selectedRoom: Room?,
    authState: org.com.auth.AuthState,
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
    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
        val width = maxWidth
        val isExpanded = width >= 840.dp

        Column(modifier = Modifier.fillMaxSize()) {
            // Top Header Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, "Menu", tint = PrimaryNavy)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text("Explore Properties", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PrimaryNavy)
                    Spacer(Modifier.weight(1f))
                    Text("${rooms.size} Available", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                }
            }

            // Main Content Area
            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(rooms) { room ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onRoomSelected(room) },
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, if (selectedRoom?.id == room.id) PrimaryNavy else Color(0xFFE2E8F0)),
                            shadowElevation = 3.dp
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                val imgUrl = room.firstImageUrl
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFF1F5F9)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (!imgUrl.isNullOrBlank()) {
                                        KamelImage(
                                            resource = { asyncPainterResource(imgUrl) },
                                            contentDescription = "Property",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Icon(Icons.Default.HomeWork, null, tint = Color.LightGray, modifier = Modifier.size(36.dp))
                                    }
                                }
                                Spacer(Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(room.title ?: "Property", fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Spacer(Modifier.height(4.dp))
                                    Text(room.locationSummary, fontSize = 12.sp, color = Color(0xFF64748B), maxLines = 1)
                                    Spacer(Modifier.height(4.dp))
                                    Text(room.formattedPrice, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = PrimaryNavy)
                                }
                                Button(
                                    onClick = { onViewProperty(room) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy)
                                ) {
                                    Text("View", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                if (routingDestination != null) {
                    val dest = routingDestination
                    val cardModifier = if (isExpanded) {
                        Modifier
                            .align(Alignment.TopStart)
                            .padding(top = 20.dp, start = 20.dp)
                            .width(380.dp)
                    } else {
                        Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                            .fillMaxWidth()
                    }

                    LiveRouteSummaryCard(
                        originLabel = "Dar es Salaam Center",
                        destinationRoom = dest,
                        originLat = -6.7924,
                        originLng = 39.2083,
                        destLat = dest.latitude,
                        destLng = dest.longitude,
                        onRecenter = {},
                        onEndRoute = onClearRoute,
                        modifier = cardModifier
                    )
                }
            }
        }
    }
}
