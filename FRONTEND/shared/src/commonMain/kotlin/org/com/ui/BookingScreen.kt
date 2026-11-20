package org.com.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.VerifiedUser
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

private val PrimaryColor = Color(0xFF142170)
private val PrimaryLight = Color(0xFF1A237E)
private val SuccessColor = Color(0xFF10B981)
private val PendingYellow = Color(0xFFD97706)

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
    onConfirmBooking: suspend (Booking, String, String) -> String?
) {
    val scope = rememberCoroutineScope()
    val today = Instant.fromEpochMilliseconds(org.com.currentTimeMillis()).toLocalDateTime(TimeZone.currentSystemDefault()).date
    
    var currentStep by remember { mutableStateOf(BookingStep.DETAILS) }

    var startDate by remember { mutableStateOf(today.plus(1, DateTimeUnit.DAY)) }
    var selectedDuration by remember { mutableStateOf("1 Month") }
    var durationMonths by remember { mutableStateOf(1) }
    var endDate by remember { mutableStateOf(startDate.plus(1, DateTimeUnit.MONTH)) }
    
    var guests by remember { mutableStateOf(1) }
    var specialRequests by remember { mutableStateOf("") }
    
    var selectedPaymentMethod by remember { mutableStateOf("Pay in Hand (Cash)") }
    var paymentPhoneNumber by remember { mutableStateOf("") }

    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showGuestDropdown by remember { mutableStateOf(false) }

    val maxGuests = if (room.maxGuests > 0) room.maxGuests else 1

    fun updateDuration(durationLabel: String) {
        selectedDuration = durationLabel
        durationMonths = when (durationLabel) {
            "1 Month" -> 1
            "3 Months" -> 3
            "6 Months" -> 6
            "12 Months" -> 12
            else -> 1
        }
        endDate = startDate.plus(durationMonths, DateTimeUnit.MONTH)
    }

    val monthlyRent = room.price
    val totalRentTzs = monthlyRent * durationMonths
    val dalaliFee = room.effectiveFacilitationFee
    val totalPaymentTzs = totalRentTzs + dalaliFee

    fun formatTzs(amount: Double): String {
        return amount.toLong().toString()
            .reversed()
            .chunked(3)
            .joinToString(",")
            .reversed() + " TZS"
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(PrimaryColor, PrimaryLight))),
        contentAlignment = Alignment.Center
    ) {
        val width = maxWidth
        val isExpanded = width >= 840.dp
        val isCompact = width < 600.dp

        Card(
            modifier = Modifier
                .padding(horizontal = if (isCompact) 16.dp else 24.dp, vertical = 24.dp)
                .widthIn(max = if (isExpanded) 1040.dp else 560.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(if (isExpanded) 20.dp else 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(if (isExpanded) 28.dp else 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Navigation
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
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = PrimaryColor, modifier = Modifier.size(20.dp))
                    }
                    Text(
                        text = when (currentStep) {
                            BookingStep.DETAILS -> "1. Booking Details"
                            BookingStep.PAYMENT -> "2. Payment Details"
                            BookingStep.CONFIRMATION -> "3. Booking Confirmation"
                        },
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = if (isCompact) 16.sp else 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryColor
                    )
                    Spacer(Modifier.size(36.dp))
                }

                Spacer(Modifier.height(16.dp))

                // Progress Stepper Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StepChip("1. Details", active = currentStep == BookingStep.DETAILS, completed = currentStep != BookingStep.DETAILS)
                    Spacer(Modifier.width(8.dp))
                    Text("•", color = Color(0xFF94A3B8))
                    Spacer(Modifier.width(8.dp))
                    StepChip("2. Payment", active = currentStep == BookingStep.PAYMENT, completed = currentStep == BookingStep.CONFIRMATION)
                    Spacer(Modifier.width(8.dp))
                    Text("•", color = Color(0xFF94A3B8))
                    Spacer(Modifier.width(8.dp))
                    StepChip("3. Confirmation", active = currentStep == BookingStep.CONFIRMATION, completed = currentStep == BookingStep.CONFIRMATION)
                }

                Spacer(Modifier.height(20.dp))

                when (currentStep) {
                    BookingStep.DETAILS -> {
                        // Step 1: Property Summary Card
                        PropertySummaryHeader(room)

                        Spacer(Modifier.height(20.dp))

                        // Guests Dropdown Selection
                        Column(Modifier.fillMaxWidth()) {
                            Text("Number of Guests", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF64748B))
                            Spacer(Modifier.height(8.dp))
                            Box {
                                OutlinedButton(
                                    onClick = { showGuestDropdown = true },
                                    modifier = Modifier.fillMaxWidth().height(52.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0F172A))
                                ) {
                                    Row(
                                        Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Group, null, tint = PrimaryColor, modifier = Modifier.size(20.dp))
                                            Spacer(Modifier.width(12.dp))
                                            Text("$guests ${if (guests == 1) "guest" else "guests"}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                        Icon(Icons.Default.ArrowDropDown, null, tint = Color(0xFF64748B))
                                    }
                                }

                                DropdownMenu(
                                    expanded = showGuestDropdown,
                                    onDismissRequest = { showGuestDropdown = false },
                                    modifier = Modifier.fillMaxWidth(0.85f).background(Color.White)
                                ) {
                                    (1..maxGuests).forEach { count ->
                                        DropdownMenuItem(
                                            text = { Text("$count ${if (count == 1) "guest" else "guests"}", fontWeight = FontWeight.Medium) },
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

                        // Check-in Selection Date Picker
                        Column(Modifier.fillMaxWidth()) {
                            Text("Check-in Date", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF64748B))
                            Spacer(Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { showStartDatePicker = true },
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0F172A))
                            ) {
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CalendarToday, null, tint = PrimaryColor, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(12.dp))
                                    Text(startDate.toString(), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Stay Duration Chips (1 Month default, 3 Months, 6 Months, 12 Months)
                        Column(Modifier.fillMaxWidth()) {
                            Text("Stay Duration", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF64748B))
                            Spacer(Modifier.height(8.dp))

                            val durationOptions = listOf("1 Month", "3 Months", "6 Months", "12 Months")

                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(durationOptions) { duration ->
                                    val isSelected = selectedDuration == duration
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { updateDuration(duration) },
                                        label = {
                                            Text(
                                                text = duration,
                                                fontSize = 13.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                            )
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = PrimaryColor,
                                            selectedLabelColor = Color.White,
                                            containerColor = Color(0xFFF1F5F9),
                                            labelColor = Color(0xFF334155)
                                        ),
                                        modifier = Modifier.height(44.dp)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Calculated Check-out Date Display
                        Column(Modifier.fillMaxWidth()) {
                            Text("Calculated Check-out Date", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF64748B))
                            Spacer(Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { showEndDatePicker = true },
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0F172A))
                            ) {
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Event, null, tint = PrimaryColor, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(12.dp))
                                    Text("$endDate ($selectedDuration)", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Special Requests Note Box
                        BookingTextField(
                            value = specialRequests,
                            onValueChange = { specialRequests = it },
                            label = "Special Requests (Optional)",
                            placeholder = "Message or specific instructions for landlord/dalali...",
                            singleLine = false,
                            modifier = Modifier.height(90.dp)
                        )

                        Spacer(Modifier.height(20.dp))

                        // Rent Summary Total Card
                        Surface(
                            color = Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Estimated Total Rent ($selectedDuration)", fontSize = 12.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                                    Text("${formatTzs(monthlyRent)} / mo", fontSize = 12.sp, color = Color(0xFF475569))
                                }
                                Text(formatTzs(totalRentTzs), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = PrimaryColor)
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        Button(
                            onClick = { currentStep = BookingStep.PAYMENT },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            enabled = endDate > startDate,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                        ) {
                            Text("PROCEED TO PAYMENT PAGE", fontWeight = FontWeight.Black, fontSize = 14.sp)
                        }

                        if (endDate <= startDate) {
                            Text("Check-out date must be after check-in date", color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                        }
                    }

                    BookingStep.PAYMENT -> {
                        // Step 2: Payment Details
                        Surface(
                            color = PrimaryColor.copy(alpha = 0.04f),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, PrimaryColor.copy(alpha = 0.12f))
                        ) {
                            Column(Modifier.padding(18.dp)) {
                                Text("Payment Summary", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PrimaryColor)
                                Spacer(Modifier.height(8.dp))
                                Text("Property: ${room.title ?: "Property"}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F172A))
                                Text("Location: ${room.locationSummary}", fontSize = 12.sp, color = Color(0xFF64748B))
                                Text("Check-in: $startDate  •  Check-out: $endDate ($selectedDuration)", fontSize = 12.sp, color = Color(0xFF334155))
                                Text("Guests: $guests", fontSize = 12.sp, color = Color(0xFF334155))
                                HorizontalDivider(Modifier.padding(vertical = 12.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Rent ($selectedDuration @ ${formatTzs(monthlyRent)}/mo):", fontSize = 13.sp, color = Color(0xFF475569))
                                    Text(formatTzs(totalRentTzs), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F172A))
                                }
                                
                                if (!room.isDirectByOwner) {
                                    Spacer(Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Dalali Facilitation Fee:", fontSize = 13.sp, color = Color(0xFF475569))
                                        Text(room.formattedFacilitationFee, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PrimaryLight)
                                    }
                                }
                                
                                Spacer(Modifier.height(8.dp))
                                HorizontalDivider(Modifier.padding(vertical = 6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Total Due:", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))
                                    Text(formatTzs(totalPaymentTzs), fontWeight = FontWeight.Black, fontSize = 18.sp, color = PrimaryColor)
                                }

                                Spacer(Modifier.height(10.dp))

                                // Native Vector Icon 0% Broker Fee / Verification Badge
                                Surface(
                                    color = if (room.isDirectByOwner) Color(0xFFECFDF5) else Color(0xFFEEF2FF),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, if (room.isDirectByOwner) Color(0xFFA7F3D0) else Color(0xFFC7D2FE)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (room.isDirectByOwner) Icons.Rounded.VerifiedUser else Icons.Rounded.CheckCircle,
                                            contentDescription = null,
                                            tint = if (room.isDirectByOwner) SuccessColor else PrimaryColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(Modifier.width(10.dp))
                                        Text(
                                            text = if (room.isDirectByOwner)
                                                "Direct Owner Listing: 0% Broker Fee. Landlord receives 100% rent."
                                            else
                                                "Decoupled Fee Model: Landlord receives 100% rent. Dalali fee covers listing verification.",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (room.isDirectByOwner) Color(0xFF065F46) else PrimaryColor,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        // Selectable Payment Methods
                        Text("Select Payment Method", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF64748B))
                        Spacer(Modifier.height(8.dp))
                        
                        val paymentMethods = listOf(
                            "Pay in Hand (Cash)",
                            "M-Pesa",
                            "Tigo Pesa",
                            "Airtel Money"
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            paymentMethods.forEach { method ->
                                val isSelected = selectedPaymentMethod == method
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .clickable { selectedPaymentMethod = method },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) PrimaryColor.copy(alpha = 0.08f) else Color.White,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) PrimaryColor else Color(0xFFE2E8F0)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { selectedPaymentMethod = method },
                                            colors = RadioButtonDefaults.colors(selectedColor = PrimaryColor)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        if (method.contains("Hand", ignoreCase = true)) {
                                            Icon(Icons.Default.Payments, null, tint = SuccessColor, modifier = Modifier.size(22.dp))
                                            Spacer(Modifier.width(8.dp))
                                        } else {
                                            Icon(Icons.Default.PhonelinkRing, null, tint = PrimaryColor, modifier = Modifier.size(20.dp))
                                            Spacer(Modifier.width(8.dp))
                                        }
                                        Text(
                                            text = method,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 14.sp,
                                            color = Color(0xFF0F172A)
                                        )
                                    }
                                }
                            }
                        }

                        if (selectedPaymentMethod.contains("Hand", ignoreCase = true)) {
                            Surface(
                                color = Color(0xFFF0FDF4),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                                modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                            ) {
                                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Payments, null, tint = Color(0xFF166534), modifier = Modifier.size(24.dp))
                                    Spacer(Modifier.width(10.dp))
                                    Column {
                                        Text("Lipa Mkononi (Cash on Arrival)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF14532D))
                                        Text("Huna haja ya kulipa sasa. Utalipa fedha taslimu moja kwa moja kwa mwenye nyumba wakati wa kufika kukagua na kukabidhiwa chumba.", fontSize = 12.sp, color = Color(0xFF166534), lineHeight = 16.sp)
                                    }
                                }
                            }
                        } else if (selectedPaymentMethod in listOf("M-Pesa", "Tigo Pesa", "Airtel Money")) {
                            Spacer(Modifier.height(16.dp))
                            BookingTextField(
                                value = paymentPhoneNumber,
                                onValueChange = { paymentPhoneNumber = it },
                                label = "$selectedPaymentMethod Mobile Phone Number",
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
                                        roomImageUrl = room.firstImageUrl,
                                        startDate = startDate.toString(),
                                        endDate = endDate.toString(),
                                        numberOfGuests = guests,
                                        specialRequests = specialRequests,
                                        rentAmount = totalRentTzs,
                                        facilitationFee = dalaliFee,
                                        listingType = if (room.isDirectByOwner) "DIRECT_BY_OWNER" else "DALALI_VERIFIED",
                                        dalaliId = room.dalaliId,
                                        totalPrice = totalPaymentTzs
                                    )
                                    val error = onConfirmBooking(booking, selectedPaymentMethod, paymentPhoneNumber)
                                    if (error == null) {
                                        currentStep = BookingStep.CONFIRMATION
                                    } else {
                                        errorMessage = error
                                    }
                                    isSubmitting = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            enabled = !isSubmitting,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            } else {
                                val buttonText = if (selectedPaymentMethod.contains("Hand", ignoreCase = true)) {
                                    "CONFIRM & PAY IN HAND (${formatTzs(totalPaymentTzs)})"
                                } else {
                                    "CONFIRM & PROCESS PAYMENT (${formatTzs(totalPaymentTzs)})"
                                }
                                Text(buttonText, fontWeight = FontWeight.Black, fontSize = 13.sp)
                            }
                        }

                        if (errorMessage != null) {
                            Text(errorMessage!!, color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 8.dp))
                        }
                    }

                    BookingStep.CONFIRMATION -> {
                        // Step 3: Confirmation
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CheckCircle, null, tint = SuccessColor, modifier = Modifier.size(64.dp))
                            Spacer(Modifier.height(16.dp))
                            Text("Booking Request Submitted!", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center, color = Color(0xFF0F172A))
                            Spacer(Modifier.height(8.dp))
                            
                            val isHandPay = selectedPaymentMethod.contains("Hand", ignoreCase = true)

                            // Pending Status Yellow Chip
                            Surface(
                                color = PendingYellow.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(1.dp, PendingYellow.copy(alpha = 0.4f)),
                                modifier = Modifier.padding(vertical = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier.size(8.dp).clip(CircleShape).background(PendingYellow)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("Status: PENDING CONFIRMATION", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PendingYellow)
                                }
                            }

                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = if (isHandPay)
                                    "Your property viewing & room reservation request has been submitted to the landlord. Payment status: PENDING DIRECT CASH PAYMENT UPON ARRIVAL."
                                else
                                    "Your booking request has been submitted and is pending owner confirmation. Payment status: PROCESSING / AWAITING CHECK-IN.",
                                color = Color(0xFF475569),
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                            Spacer(Modifier.height(20.dp))

                            Surface(
                                color = Color(0xFFF8FAFC),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Text("Booking Summary Details", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PrimaryColor)
                                    Spacer(Modifier.height(8.dp))
                                    Text("• Property: ${room.title}", fontSize = 13.sp, color = Color(0xFF0F172A), fontWeight = FontWeight.Medium)
                                    Text("• Location: ${room.locationSummary}", fontSize = 12.sp, color = Color(0xFF64748B))
                                    Text("• Dates: $startDate to $endDate ($selectedDuration)", fontSize = 12.sp, color = Color(0xFF334155))
                                    Text("• Payment Method: $selectedPaymentMethod", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155))
                                    Text("• Landlord Rent: ${formatTzs(totalRentTzs)} (100% Direct)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryColor)
                                    if (!room.isDirectByOwner) {
                                        Text("• Dalali Fee: ${room.formattedFacilitationFee}", fontSize = 12.sp, color = PrimaryLight)
                                    }
                                    Text("• Total Amount: ${formatTzs(totalPaymentTzs)}", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = PrimaryColor)
                                }
                            }

                            Spacer(Modifier.height(24.dp))

                            Button(
                                onClick = onBack,
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                            ) {
                                Text("Back to Explore", fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
                endDate = it.plus(durationMonths, DateTimeUnit.MONTH)
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
            else -> Color(0xFFF1F5F9)
        },
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = text,
            color = when {
                completed -> SuccessColor
                active -> PrimaryColor
                else -> Color(0xFF64748B)
            },
            fontSize = 12.sp,
            fontWeight = if (active || completed) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun PropertySummaryHeader(room: Room) {
    Surface(
        color = PrimaryColor.copy(alpha = 0.05f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, PrimaryColor.copy(alpha = 0.12f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val imgUrl = room.firstImageUrl
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                if (!imgUrl.isNullOrBlank()) {
                    KamelImage(
                        resource = { asyncPainterResource(imgUrl) },
                        contentDescription = "Property",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        onLoading = { _: Float ->
                            Box(Modifier.fillMaxSize().background(Color(0xFFE2E8F0)), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = PrimaryColor)
                            }
                        },
                        onFailure = { err ->
                            println("BookingScreen summary photo load failure for '$imgUrl': ${err.message}")
                            Box(Modifier.fillMaxSize().background(Color(0xFFE2E8F0)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.HomeWork, null, tint = Color(0xFF94A3B8), modifier = Modifier.size(32.dp))
                            }
                        }
                    )
                } else {
                    Icon(Icons.Default.HomeWork, null, tint = Color(0xFF94A3B8), modifier = Modifier.size(32.dp))
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = room.title ?: "Property",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF0F172A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = room.locationSummary,
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = room.formattedPrice,
                    color = PrimaryColor,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                )
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
                Text("CANCEL", color = Color(0xFF64748B))
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
            unfocusedBorderColor = Color(0xFFCBD5E1)
        ),
        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
    )
}
