package org.com.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import kotlinx.coroutines.*
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.LocalDate
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus
import kotlinx.datetime.Instant
import kotlinx.datetime.atStartOfDayIn

private val PrimaryColor = Color(0xFF1A237E)
private val PrimaryLight = Color(0xFF3949AB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    room: Room,
    onBack: () -> Unit,
    onConfirmBooking: suspend (Booking) -> Boolean
) {
    val scope = rememberCoroutineScope()
    val today = kotlinx.datetime.Instant.fromEpochMilliseconds(org.com.currentTimeMillis()).toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).date
    
    var startDate by remember { mutableStateOf(today.plus(1, DateTimeUnit.DAY)) }
    var endDate by remember { mutableStateOf(today.plus(2, DateTimeUnit.DAY)) }
    var guests by remember { mutableStateOf(1) }
    var specialRequests by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showGuestDropdown by remember { mutableStateOf(false) }

    val maxGuests = if (room.roomsCount > 0) room.roomsCount * 2 else 10

    // Smart Date Calculation
    fun updateEndDateByDuration(duration: String) {
        endDate = when (duration) {
            "1 Day" -> startDate.plus(1, DateTimeUnit.DAY)
            "1 Week" -> startDate.plus(1, DateTimeUnit.WEEK)
            "1 Month" -> startDate.plus(1, DateTimeUnit.MONTH)
            else -> endDate
        }
    }

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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = PrimaryColor, modifier = Modifier.size(20.dp))
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
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                        ) {
                            Text("Back to Explore", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // Room Summary
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

                    Spacer(Modifier.height(24.dp))

                    // Guests Dropdown
                    Column(Modifier.fillMaxWidth()) {
                        Text("Number of Guests", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Gray)
                        Spacer(Modifier.height(8.dp))
                        Box {
                            OutlinedButton(
                                onClick = { showGuestDropdown = true },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
                            ) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Group, null, tint = PrimaryColor, modifier = Modifier.size(20.dp))
                                        Spacer(Modifier.width(12.dp))
                                        Text("${guests} ${if (guests == 1) "guest" else "guests"}", fontSize = 15.sp)
                                    }
                                    Icon(Icons.Default.ArrowDropDown, null, tint = Color.Gray)
                                }
                            }
                            
                            DropdownMenu(
                                expanded = showGuestDropdown,
                                onDismissRequest = { showGuestDropdown = false },
                                modifier = Modifier.fillMaxWidth(0.8f).background(Color.White)
                            ) {
                                (1..maxGuests).forEach { count ->
                                    DropdownMenuItem(
                                        text = { Text("$count ${if (count == 1) "guest" else "guests"}") },
                                        onClick = {
                                            guests = count
                                            showGuestDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // Check-in Selection
                    Column(Modifier.fillMaxWidth()) {
                        Text("Check-in Date", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Gray)
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { showStartDatePicker = true },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
                        ) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarToday, null, tint = PrimaryColor, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(12.dp))
                                Text(startDate.toString(), fontSize = 15.sp)
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // Duration Chips
                    Text("Stay Duration", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Gray)
                    Spacer(Modifier.height(10.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("1 Day", "1 Week", "1 Month").forEach { duration ->
                            val isSelected = false // We don't track selection, just action
                            FilterChip(
                                selected = isSelected,
                                onClick = { updateEndDateByDuration(duration) },
                                label = { Text(duration, fontSize = 12.sp) },
                                shape = RoundedCornerShape(8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryColor.copy(alpha = 0.1f),
                                    selectedLabelColor = PrimaryColor
                                )
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // Check-out Selection
                    Column(Modifier.fillMaxWidth()) {
                        Text("Check-out Date", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Gray)
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { showEndDatePicker = true },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
                        ) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarToday, null, tint = PrimaryColor, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(12.dp))
                                Text(endDate.toString(), fontSize = 15.sp)
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

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

                    // Main Action Button
                    Button(
                        onClick = {
                            scope.launch {
                                isSubmitting = true
                                val booking = Booking(
                                    roomId = room.id,
                                    startDate = startDate.toString(),
                                    endDate = endDate.toString(),
                                    numberOfGuests = guests,
                                    specialRequests = specialRequests,
                                    totalPrice = room.price
                                )
                                val success = onConfirmBooking(booking)
                                if (success) {
                                    showSuccess = true
                                }
                                isSubmitting = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !isSubmitting && endDate > startDate,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                    ) {
                        if (isSubmitting) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        else Text("SEND BOOKING REQUEST", fontWeight = FontWeight.Black, fontSize = 15.sp)
                    }
                    
                    if (endDate <= startDate) {
                        Text("Check-out must be after check-in", color = Color.Red, fontSize = 11.sp, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }
        }
    }

    // Success dialog or state needs to be managed via a new prop or by making onConfirmBooking a suspend fun


    // Date Picker Dialogs
    if (showStartDatePicker) {
        BookingDatePickerDialog(
            initialDate = startDate,
            onDismiss = { showStartDatePicker = false },
            onDateSelected = { 
                startDate = it
                if (endDate <= it) {
                    endDate = it.plus(1, DateTimeUnit.DAY)
                }
                showStartDatePicker = false
            }
        )
    }

    if (showEndDatePicker) {
        BookingDatePickerDialog(
            initialDate = endDate,
            minDate = startDate.plus(1, DateTimeUnit.DAY),
            onDismiss = { showEndDatePicker = false },
            onDateSelected = { 
                endDate = it
                showEndDatePicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookingDatePickerDialog(
    initialDate: LocalDate,
    minDate: LocalDate? = null,
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate) -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val date = Instant.fromEpochMilliseconds(utcTimeMillis).toLocalDateTime(TimeZone.UTC).date
                val today = kotlinx.datetime.Instant.fromEpochMilliseconds(org.com.currentTimeMillis()).toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).date
                
                // Block past dates
                if (date < today) return false
                
                // If minDate is provided, block dates before it
                if (minDate != null && date < minDate) return false
                
                return true
            }
        }
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let {
                    val selectedDate = Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.UTC).date
                    onDateSelected(selectedDate)
                }
            }) {
                Text("OK", fontWeight = FontWeight.Bold, color = PrimaryColor)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = Color.Gray)
            }
        }
    ) {
        DatePicker(state = datePickerState)
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
