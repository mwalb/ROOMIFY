package org.com.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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

private val PrimaryColor = Color(0xFF1A237E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavouriteScreen(
    savedRooms: List<Room> = emptyList(),
    onBack: () -> Unit,
    onViewProperty: (Room) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Favourites", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = PrimaryColor,
                    navigationIconContentColor = PrimaryColor
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8F9FA))
        ) {
            if (savedRooms.isEmpty()) {
                EmptySavedState(onBack)
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 300.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(savedRooms) { room ->
                        SavedRoomCard(room) { onViewProperty(room) }
                    }
                }
            }
        }
    }
}

@Composable
private fun SavedRoomCard(room: Room, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box {
                KamelImage(
                    resource = { asyncPainterResource(room.firstImageUrl ?: "") },
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    contentScale = ContentScale.Crop,
                    onLoading = { _: Float ->
                        Box(Modifier.fillMaxSize().background(Color(0xFFF5F5F5)), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = PrimaryColor)
                        }
                    },
                    onFailure = {
                        Box(Modifier.fillMaxSize().background(Color(0xFFF5F5F5)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Warning, null, tint = Color.LightGray, modifier = Modifier.size(32.dp))
                        }
                    }
                )
                
                Surface(
                    modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
                    color = Color.White,
                    shape = CircleShape,
                    shadowElevation = 4.dp
                ) {
                    Icon(
                        Icons.Default.Favorite, 
                        null, 
                        tint = Color.Red, 
                        modifier = Modifier.padding(8.dp).size(20.dp)
                    )
                }

                Surface(
                    modifier = Modifier.align(Alignment.BottomStart).padding(12.dp),
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        room.propertyType ?: "Property", 
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = Color.White, 
                        fontSize = 10.sp, 
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Column(Modifier.padding(16.dp)) {
                Text(
                    room.title ?: "Property", 
                    fontSize = 17.sp, 
                    fontWeight = FontWeight.Bold, 
                    maxLines = 1, 
                    overflow = TextOverflow.Ellipsis
                )
                
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Icon(Icons.Default.LocationOn, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        room.address ?: "", 
                        fontSize = 13.sp, 
                        color = Color.Gray, 
                        maxLines = 1, 
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Spacer(Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        room.formattedPrice, 
                        fontSize = 18.sp, 
                        color = PrimaryColor, 
                        fontWeight = FontWeight.Black
                    )
                    
                    Text(
                        "View Details →",
                        fontSize = 12.sp,
                        color = PrimaryColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptySavedState(onBack: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = Color.Red.copy(alpha = 0.05f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Favorite, null, tint = Color.Red, modifier = Modifier.size(40.dp))
                }
            }
            Spacer(Modifier.height(24.dp))
            Text("No favourite properties yet", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111111))
            Spacer(Modifier.height(8.dp))
            Text(
                "Tap the heart icon on any property to add it to your favourites for quick access.",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 40.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = onBack,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
            ) {
                Text("EXPLORE PROPERTIES", fontWeight = FontWeight.Bold)
            }
        }
    }
}
