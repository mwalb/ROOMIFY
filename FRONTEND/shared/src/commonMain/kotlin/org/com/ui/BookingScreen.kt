package org.com.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.com.model.Booking
import org.com.model.Room

private val PrimaryColor = Color(0xFF1A237E)
private val PrimaryLight = Color(0xFF3949AB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    room: Room,
    onBack: () -> Unit,
    onConfirmBooking: (Booking) -> Unit
) {
    var startDate by remember { mutableStateOf("2026-09-10") }
    var endDate by remember { mutableStateOf("2026-10-10") }
    var guests by remember { mutableStateOf(1) }
    var specialRequests by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(PrimaryColor, PrimaryLight))),
        contentAlignment = Alignment.Center
    ) {
        // Main Centered Card
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 440.dp)
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = PrimaryColor, modifier = Modifier.size(20.dp))
                    }
                    Text(
                        text = "Request Booking",
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryColor
                    )
                    Spacer(Modifier.size(32.dp))
                }

                Spacer(Modifier.height(24.dp))

                if (showSuccess) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(64.dp))
                        Spacer(Modifier.height(16.dp))
                        Text("Request Sent!", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Text("Waiting for owner approval", color = Color.Gray, fontSize = 14.sp)
                        Spacer(Modifier.height(32.dp))
                        Button(
                            onClick = onBack, 
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Back to Explore", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // Room Summary - Inline Header
                    Surface(
                        color = PrimaryColor.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(room.title ?: "Property", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                            Text(room.formattedPrice, color = PrimaryColor, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // Date Selection - Reference Size (60dp height)
                    Text("Select Dates", modifier = Modifier.fillMaxWidth().padding(start = 4.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Gray)
                    Spacer(Modifier.height(8.dp))
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        BookingTextField(
                            value = startDate,
                            onValueChange = { startDate = it },
                            label = "Start Date",
                            modifier = Modifier.weight(1f)
                        )
                        BookingTextField(
                            value = endDate,
                            onValueChange = { endDate = it },
                            label = "End Date",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // Guests
                    Text("Number of Guests", modifier = Modifier.fillMaxWidth().padding(start = 4.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Gray)
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                        Icon(Icons.Default.Group, null, tint = PrimaryColor, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(12.dp))
                        Slider(
                            value = guests.toFloat(),
                            onValueChange = { guests = it.toInt() },
                            valueRange = 1f..5f,
                            steps = 3,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(activeTrackColor = PrimaryColor, thumbColor = PrimaryColor)
                        )
                        Text("${guests}", modifier = Modifier.padding(start = 12.dp), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PrimaryColor)
                    }

                    Spacer(Modifier.height(16.dp))

                    // Special Requests
                    BookingTextField(
                        value = specialRequests,
                        onValueChange = { specialRequests = it },
                        label = "Special Requests",
                        placeholder = "Message for the owner...",
                        singleLine = false,
                        modifier = Modifier.height(100.dp)
                    )

                    Spacer(Modifier.height(32.dp))

                    // Main Action Button (48dp height)
                    Button(
                        onClick = {
                            isSubmitting = true
                            val booking = Booking(
                                roomId = room.id,
                                startDate = startDate,
                                endDate = endDate,
                                numberOfGuests = guests,
                                specialRequests = specialRequests,
                                totalPrice = room.price
                            )
                            onConfirmBooking(booking)
                            showSuccess = true
                            isSubmitting = false
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isSubmitting,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                    ) {
                        if (isSubmitting) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        else Text("SEND REQUEST", fontWeight = FontWeight.Bold, fontSize = 14.sp, letterSpacing = 0.5.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun BookingTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    singleLine: Boolean = true,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label, fontSize = 12.sp) },
        placeholder = { if (placeholder.isNotEmpty()) Text(placeholder, fontSize = 13.sp) },
        singleLine = singleLine,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryColor,
            focusedLabelColor = PrimaryColor,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        ),
        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
    )
}
