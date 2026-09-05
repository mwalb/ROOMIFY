package org.com.ui


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.com.model.Room

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomDetailSheet(
    room: Room,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Title
        Text(
            text = room.title ?: "Room Details",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        // Price badge
        Surface(
            color = Color(0xFF1A237E),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = room.formattedPrice,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        // Property summary
        Text(
            text = room.propertySummary,
            fontSize = 14.sp,
            color = Color.Gray
        )

        // Address
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.LocationOn,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = Color.Gray
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = room.address ?: "No address",
                color = Color.Gray,
                fontSize = 14.sp
            )
        }

        // Status badge
        val statusColor = when (room.status) {
            "AVAILABLE" -> Color(0xFF4CAF50)
            "PENDING" -> Color(0xFFFFA000)
            "RENTED" -> Color(0xFFE53935)
            else -> Color.Gray
        }
        Surface(
            color = statusColor.copy(alpha = 0.1f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "Status: ${room.status}",
                color = statusColor,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        // Contact
        if (!room.contactPhone.isNullOrBlank()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Phone,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = Color(0xFF1A237E)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = room.contactPhone,
                    fontSize = 14.sp
                )
            }
        }

        // Amenities
        if (room.amenities.isNotEmpty()) {
            Text(
                text = "Amenities",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                items(room.amenities.filterNotNull()) { amenity ->
                    AssistChip(
                        onClick = {},
                        label = { Text(amenity) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Close button
        Button(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1A237E)
            )
        ) {
            Text("Close")
        }
    }
}