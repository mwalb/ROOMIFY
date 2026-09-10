package org.com.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.*
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.com.model.Room

@Composable
fun MapScreen(
    rooms: List<Room>,
    selectedRoom: Room?,
    authState: org.com.auth.AuthState,
    routingDestination: Room?,
    currentStatusFilter: String = "ALL",
    shouldFitBounds: Boolean = false,
    isRefreshing: Boolean = false,
    activeFilters: String? = null,
    onRefresh: () -> Unit = {},
    onClearFilters: () -> Unit = {},
    onStatusFilterChange: (String) -> Unit = {},
    onFitBoundsHandled: () -> Unit = {},
    onClearRoute: () -> Unit,
    onRoomSelected: (Room) -> Unit,
    onClearSelection: () -> Unit,
    onViewProperty: (Room) -> Unit,
    modifier: Modifier = Modifier,
    onNavigate: (String) -> Unit
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {

        MapContent(
            rooms = rooms,
            selectedRoom = selectedRoom,
            authState = authState,
            routingDestination = routingDestination,
            currentStatusFilter = currentStatusFilter,
            shouldFitBounds = shouldFitBounds,
            onStatusFilterChange = onStatusFilterChange,
            onFitBoundsHandled = onFitBoundsHandled,
            onClearRoute = onClearRoute,
            onRoomSelected = { room ->

                println(
                    "MapScreen: onRoomSelected -> ${room.id}"
                )

                onRoomSelected(room)
            },

            onRoomCleared = {

                println(
                    "MapScreen: onRoomCleared"
                )

                onClearSelection()
            },

            onViewProperty = { room ->

                println(
                    "MapScreen: onViewProperty -> ${room.id}"
                )

                onViewProperty(room)
            },

            onNavigate = onNavigate
        )
        
        if (rooms.isEmpty()) {
            Box(
                Modifier.fillMaxSize().padding(top = 100.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Surface(
                    color = Color.White.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(12.dp),
                    shadowElevation = 8.dp
                ) {
                    Text(
                        if (isRefreshing) "Updating rooms..." else "No properties found matching your search",
                        modifier = Modifier.padding(16.dp),
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )
                }
            }
        }

        // Status Legend (Bottom Left)
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                color = Color.White.copy(alpha = 0.95f),
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 4.dp
            ) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("PROPERTY STATUS", fontWeight = FontWeight.Black, fontSize = 10.sp, color = Color.Gray)
                    StatusLegendItem("Available", Color(0xFF2E7D32))
                    StatusLegendItem("Pending", Color(0xFFF9A825))
                    StatusLegendItem("Rented", Color(0xFFC62828))
                }
            }
        }
        
        // Floating Back Button to Discovery
        Row(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onNavigate("discovery") },
                modifier = Modifier
                    .size(44.dp)
                    .background(Color.White, CircleShape)
                    .shadow(4.dp, CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF1A237E))
            }
            
            if (activeFilters != null) {
                Spacer(Modifier.width(12.dp))
                Surface(
                    color = Color(0xFF1A237E),
                    shape = RoundedCornerShape(22.dp),
                    modifier = Modifier.height(44.dp).clickable { onClearFilters() },
                    shadowElevation = 4.dp
                ) {
                    Row(
                        Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(activeFilters, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.Close, null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(14.dp))
                    }
                }
            }
        }

        // Refresh Button
        IconButton(
            onClick = onRefresh,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopEnd)
                .size(44.dp)
                .background(Color.White, CircleShape)
                .shadow(4.dp, CircleShape)
        ) {
            if (isRefreshing) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color(0xFF1A237E))
            } else {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color(0xFF1A237E))
            }
        }
    }
}

@Composable
private fun StatusLegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).background(color, CircleShape))
        Spacer(Modifier.width(8.dp))
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
    }
}
