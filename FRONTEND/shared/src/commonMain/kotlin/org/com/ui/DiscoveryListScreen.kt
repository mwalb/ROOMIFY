package org.com.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import org.com.model.Room
import org.com.network.ApiClient
import org.com.viewmodel.RoomViewModel

@Composable
fun DiscoveryListScreen(
    viewModel: RoomViewModel,
    onViewProperty: (Room) -> Unit,
    onNavigate: (String) -> Unit
) {
    val rooms = viewModel.filteredRooms
    val isLoading = viewModel.isLoading
    val error = viewModel.error

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5))) {
        // Simple Top Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF1A237E),
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Explore Rooms",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { onNavigate("discovery") }) {
                    Icon(Icons.Default.Tune, "Filters", tint = Color.White)
                }
            }
        }

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF1A237E))
            }
        } else if (error != null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(error, color = Color.Red, fontWeight = FontWeight.Bold)
            }
        } else if (rooms.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No rooms found matching your criteria", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(rooms) { room ->
                    RoomListItem(room, onClick = { onViewProperty(room) })
                }
            }
        }
    }
}

@Composable
private fun RoomListItem(room: Room, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                if (room.firstImageUrl != null) {
                    KamelImage(
                        resource = asyncPainterResource(ApiClient.resolveUrl(room.firstImageUrl)),
                        contentDescription = room.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(Modifier.fillMaxSize().background(Color.LightGray), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Image, null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                    }
                }
                
                Surface(
                    modifier = Modifier.padding(12.dp).align(Alignment.TopEnd),
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "TZS ${room.price.toLong()}",
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Column(Modifier.padding(16.dp)) {
                Text(room.title ?: "Room", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                    Spacer(Modifier.width(4.dp))
                    Text(room.address ?: "Dar es Salaam", fontSize = 12.sp, color = Color.Gray)
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Bed, null, modifier = Modifier.size(16.dp), tint = Color(0xFF1A237E))
                        Spacer(Modifier.width(4.dp))
                        Text("${room.roomsCount} Beds", fontSize = 12.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Bathtub, null, modifier = Modifier.size(16.dp), tint = Color(0xFF1A237E))
                        Spacer(Modifier.width(4.dp))
                        Text("${room.bathroomsCount} Baths", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
