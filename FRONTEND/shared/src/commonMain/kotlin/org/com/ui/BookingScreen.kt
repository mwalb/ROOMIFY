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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import org.com.model.Booking
import org.com.model.Room
import kotlinx.coroutines.*
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.LocalDate
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus
import kotlinx.datetime.Instant
import kotlinx.datetime.atStartOfDayIn

private val PrimaryColor = Color(0xFF1A237E)
private val PrimaryLight = Color(0xFF3949AB)
private val SuccessColor = Color(0xFF4CAF50)

private enum class BookingStep {
    DETAILS,
    PAYMENT,
    CONFIRMATION
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    room: Room,
    onBack: () -> Unit,
    onConfirmBooking: suspend (Booking) -> String?
) {
    val scope = rememberCoroutineScope()
    val today = Instant.fromEpochMilliseconds(org.com.currentTimeMillis()).toLocalDateTime(TimeZone.currentSystemDefault()).date
    
    var currentStep by remember { mutableStateOf(BookingStep.DETAILS) }

    var startDate by remember { mutableStateOf(today.plus(1, DateTimeUnit.DAY)) }
    var endDate by remember { mutableStateOf(today.plus(2, DateTimeUnit.DAY)) }
    var guests by remember { mutableStateOf(1) }
    var specialRequests by remember { mutableStateOf("") }
    
    var selectedPaymentMethod by remember { mutableStateOf("M-Pesa") }
    var paymentPhoneNumber by remember { mutableStateOf("") }

    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showGuestDropdown by remember { mutableStateOf(false) }

    val maxGuests = if (room.maxGuests > 0) room.maxGuests else 1

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
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 480.dp)
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
                    IconButton(
                        onClick = {
                            if (currentStep == BookingStep.PAYMENT) {
                                currentStep = BookingStep.DETAILS
                            } else {
                                onBack()
                            }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = PrimaryColor, modifier = Modifier.size(20.dp))
                    }
                    Text(
                        text = when (currentStep) {
                            BookingStep.DETAILS -> "Booking Request"
                            BookingStep.PAYMENT -> "Payment Details"
                            BookingStep.CONFIRMATION -> "Booking Confirmed"
                        },
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryColor
                    )
                    Spacer(Modifier.size(32.dp))
                }

                Spacer(Modifier.height(16.dp))

                // Progress Indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StepChip("1. Details", active = currentStep == BookingStep.DETAILS, completed = currentStep != BookingStep.DETAILS)
                    Spacer(Modifier.width(8.dp))
                    Text("•", color = Color.Gray)
                    Spacer(Modifier.width(8.dp))
                    StepChip("2. Payment", active = currentStep == BookingStep.PAYMENT, completed = currentStep == BookingStep.CONFIRMATION)
                    Spacer(Modifier.width(8.dp))
                    Text("•", color = Color.Gray)
                    Spacer(Modifier.width(8.dp))
                    StepChip("3. Confirmation", active = currentStep == BookingStep.CONFIRMATION, completed = currentStep == BookingStep.CONFIRMATION)
                }

                Spacer(Modifier.height(20.dp))

                when (currentStep) {
                    BookingStep.DETAILS -> {
                        // Property Summary Card
                        PropertySummaryHeader(room)

                        Spacer(Modifier.height(20.dp))

                        // Guests Dropdown
                        Column(Modifier.fillMaxWidth()) {
                            Text("Number of Guests", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Gray)
                            Spacer(Modifier.height(8.dp))
                            Box {
                                OutlinedButton(
                                    onClick = { showGuestDropdown = true },
                                    modifier = Modifier.fillMaxWidth().height(52.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
                                ) {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Group, null, tint = PrimaryColor, modifier = Modifier.size(20.dp))
                                            Spacer(Modifier.width(12.dp))
                                            Text("$guests ${if (guests == 1) "guest" else "guests"}", fontSize = 15.sp)
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

                        Spacer(Modifier.height(16.dp))

                        // Check-in Selection
                        Column(Modifier.fillMaxWidth()) {
                            Text("Check-in Date", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Gray)
                            Spacer(Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { showStartDatePicker = true },
                                modifier = Modifier.fillMaxWidth().height(52.dp),
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

                        Spacer(Modifier.height(16.dp))

                        // Duration Chips
                        Text("Stay Duration", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Gray)
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("1 Day", "1 Week", "1 Month").forEach { duration ->
                                FilterChip(
                                    selected = false,
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

                        Spacer(Modifier.height(16.dp))

                        // Check-out Selection
                        Column(Modifier.fillMaxWidth()) {
                            Text("Check-out Date", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Gray)
                            Spacer(Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { showEndDatePicker = true },
                                modifier = Modifier.fillMaxWidth().height(52.dp),
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

                        Spacer(Modifier.height(16.dp))

                        // Special Requests
                        BookingTextField(
                            value = specialRequests,
                            onValueChange = { specialRequests = it },
                            label = "Special Requests",
                            placeholder = "Message for the owner...",
                            singleLine = false,
                            modifier = Modifier.height(90.dp)
                        )

                        Spacer(Modifier.height(24.dp))

                        Button(
                            onClick = { currentStep = BookingStep.PAYMENT },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            enabled = endDate > startDate,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                        ) {
                            Text("PROCEED TO PAYMENT PAGE", fontWeight = FontWeight.Black, fontSize = 14.sp)
                        }

                        if (endDate <= startDate) {
                            Text("Check-out must be after check-in", color = Color.Red, fontSize = 11.sp, modifier = Modifier.padding(top = 8.dp))
                        }
                    }

                    BookingStep.PAYMENT -> {
                        // Payment Information Card
                        Surface(
                            color = PrimaryColor.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, PrimaryColor.copy(alpha = 0.1f))
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text("Payment Summary", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = PrimaryColor)
                                Spacer(Modifier.height(8.dp))
                                Text("Property: ${room.title ?: "Property"}", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("Location: ${room.locationSummary}", fontSize = 12.sp, color = Color.Gray)
                                Text("Check-in: $startDate  •  Check-out: $endDate", fontSize = 12.sp, color = Color.DarkGray)
                                Text("Guests: $guests", fontSize = 12.sp, color = Color.DarkGray)
                                HorizontalDivider(Modifier.padding(vertical = 10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Amount to Pay:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(room.formattedPrice, fontWeight = FontWeight.Black, fontSize = 18.sp, color = PrimaryColor)
                                }
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        // Payment Methods
                        Text("Select Payment Method", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Gray)
                        Spacer(Modifier.height(8.dp))
                        
                        val paymentMethods = listOf("M-Pesa", "Tigo Pesa", "Airtel Money", "Bank Card", "Pay on Check-In")
                        paymentMethods.forEach { method ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { selectedPaymentMethod = method },
                                shape = RoundedCornerShape(12.dp),
                                color = if (selectedPaymentMethod == method) PrimaryColor.copy(alpha = 0.1f) else Color.White,
                                border = BorderStroke(
                                    1.dp,
                                    if (selectedPaymentMethod == method) PrimaryColor else Color(0xFFE0E0E0)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = selectedPaymentMethod == method,
                                        onClick = { selectedPaymentMethod = method },
                                        colors = RadioButtonDefaults.colors(selectedColor = PrimaryColor)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(method, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                }
                            }
                        }

                        if (selectedPaymentMethod in listOf("M-Pesa", "Tigo Pesa", "Airtel Money")) {
                            Spacer(Modifier.height(16.dp))
                            BookingTextField(
                                value = paymentPhoneNumber,
                                onValueChange = { paymentPhoneNumber = it },
                                label = "$selectedPaymentMethod Phone Number",
                                placeholder = "e.g. 0712345678",
                                singleLine = true
                            )
                        }

                        Spacer(Modifier.height(24.dp))

                        Button(
                            onClick = {
                                scope.launch {
                                    isSubmitting = true
                                    errorMessage = null
                                    val booking = Booking(
                                        roomId = room.id,
                                        roomTitle = room.title,
                                        roomImageUrl = room.images.firstOrNull(),
                                        startDate = startDate.toString(),
                                        endDate = endDate.toString(),
                                        numberOfGuests = guests,
                                        specialRequests = specialRequests,
                                        totalPrice = room.price
                                    )
                                    val error = onConfirmBooking(booking)
                                    if (error == null) {
                                        currentStep = BookingStep.CONFIRMATION
                                    } else {
                                        errorMessage = error
                                    }
                                    isSubmitting = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            enabled = !isSubmitting,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            } else {
                                Text("CONFIRM & PROCESS PAYMENT (${room.formattedPrice})", fontWeight = FontWeight.Black, fontSize = 13.sp)
                            }
                        }

                        if (errorMessage != null) {
                            Text(errorMessage!!, color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 8.dp))
                        }
                    }

                    BookingStep.CONFIRMATION -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CheckCircle, null, tint = SuccessColor, modifier = Modifier.size(64.dp))
                            Spacer(Modifier.height(16.dp))
                            Text("Booking & Payment Submitted!", fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                            Spacer(Modifier.height(8.dp))
                            Text("Your booking request is pending owner confirmation. Payment status: PENDING_PAYMENT / AWAITING_APPROVAL", color = Color.Gray, fontSize = 13.sp, textAlign = TextAlign.Center)
                            Spacer(Modifier.height(24.dp))
                            
                            Surface(
                                color = Color(0xFFF5F5F5),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Text("Booking Confirmation Details", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PrimaryColor)
                                    Spacer(Modifier.height(6.dp))
                                    Text("• Property: ${room.title}", fontSize = 12.sp)
                                    Text("• Dates: $startDate to $endDate", fontSize = 12.sp)
                                    Text("• Payment Method: $selectedPaymentMethod", fontSize = 12.sp)
                                    Text("• Amount: ${room.formattedPrice}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(Modifier.height(24.dp))
                            Button(
                                onClick = onBack, 
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                            ) {
                                Text("Back to Explore", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

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

@Composable
private fun StepChip(text: String, active: Boolean, completed: Boolean) {
    Surface(
        color = when {
            completed -> SuccessColor.copy(alpha = 0.15f)
            active -> PrimaryColor.copy(alpha = 0.15f)
            else -> Color(0xFFEEEEEE)
        },
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = text,
            color = when {
                completed -> SuccessColor
                active -> PrimaryColor
                else -> Color.Gray
            },
            fontSize = 11.sp,
            fontWeight = if (active || completed) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun PropertySummaryHeader(room: Room) {
    Surface(
        color = PrimaryColor.copy(alpha = 0.05f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, PrimaryColor.copy(alpha = 0.1f))
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            val imgUrl = room.firstImageUrl
            if (!imgUrl.isNullOrBlank()) {
                Box(Modifier.size(60.dp).clip(RoundedCornerShape(8.dp))) {
                    KamelImage(
                        resource = { asyncPainterResource(imgUrl) },
                        contentDescription = "Property",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        onLoading = { _: Float -> Box(Modifier.fillMaxSize().background(Color.LightGray)) },
                        onFailure = { Box(Modifier.fillMaxSize().background(Color.LightGray)) }
                    )
                }
                Spacer(Modifier.width(12.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(room.title ?: "Property", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(room.locationSummary, fontSize = 12.sp, color = Color.Gray, maxLines = 1)
                Text(room.formattedPrice, color = PrimaryColor, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
            }
        }
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
                val today = Instant.fromEpochMilliseconds(org.com.currentTimeMillis()).toLocalDateTime(TimeZone.currentSystemDefault()).date
                
                if (date < today) return false
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
