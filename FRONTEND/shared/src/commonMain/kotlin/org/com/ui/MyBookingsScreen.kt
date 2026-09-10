package org.com.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import org.com.model.Booking
import org.com.model.Room

private val PrimaryColor = Color(0xFF1A237E)
private val SuccessColor = Color(0xFF4CAF50)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBookingsScreen(
    bookings: List<Booking> = emptyList(),
    allRooms: List<Room> = emptyList(),
    onBack: () -> Unit,
    onViewProperty: (Room) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Bookings", fontWeight = FontWeight.Bold) },
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
            if (bookings.isEmpty()) {
                EmptyBookingsState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(bookings) { booking ->
                        val room = allRooms.find { it.id == booking.roomId }
                        BookingStatusCard(booking, room) {
                            room?.let { onViewProperty(it) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BookingStatusCard(booking: Booking, room: Room?, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.98f else 1f)

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
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
        shadowElevation = 2.dp
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            // Property Image
            Box(Modifier.size(80.dp).clip(RoundedCornerShape(16.dp))) {
                KamelImage(
                    resource = { asyncPainterResource(room?.firstImageUrl ?: "") },
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    onLoading = { _: Float -> 
                        Box(Modifier.fillMaxSize().background(Color(0xFFF0F2F5)), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                        }
                    },
                    onFailure = { _: Throwable ->
                        Box(Modifier.fillMaxSize().background(Color(0xFFF0F2F5)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.BrokenImage, null, tint = Color.LightGray, modifier = Modifier.size(24.dp))
                        }
                    }
                )
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(Modifier.weight(1f)) {
                @Suppress("DEPRECATION")
                Text(booking.roomTitle ?: room?.title ?: "Room", fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                
                if (room != null) {
                    Text(room.propertyType ?: "Property", fontSize = 12.sp, color = Color.Gray)
                    Text(room.formattedPrice, fontSize = 13.sp, color = PrimaryColor, fontWeight = FontWeight.ExtraBold)
                }

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
                
                Surface(
                    color = statusColor.copy(alpha = 0.1f), 
                    shape = CircleShape,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        text = statusText, 
                        fontSize = 10.sp, 
                        color = statusColor, 
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Icon(
                imageVector = if (booking.status == "ACCEPTED") Icons.Default.CheckCircle else Icons.Default.Info, 
                contentDescription = null, 
                tint = if (booking.status == "ACCEPTED") SuccessColor else Color.LightGray,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun EmptyBookingsState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = PrimaryColor.copy(alpha = 0.05f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Filled.Assignment, null, tint = PrimaryColor, modifier = Modifier.size(40.dp))
                }
            }
            Spacer(Modifier.height(24.dp))
            Text("No bookings yet", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111111))
            Spacer(Modifier.height(8.dp))
            Text(
                "Your property inquiries and bookings will appear here.",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 40.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
