package org.com.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.com.model.FinancesSummary
import org.com.model.Payment
import org.com.model.User
import org.com.network.RoomifyApi

private val PrimaryColor = Color(0xFF1A237E)
private val PrimaryLight = Color(0xFF3949AB)
private val SuccessColor = Color(0xFF2E7D32)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancesScreen(
    currentUser: User? = null,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var finances by remember { mutableStateOf<FinancesSummary?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showWithdrawDialog by remember { mutableStateOf(false) }
    var withdrawalSuccessMsg by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(currentUser?.id) {
        if (currentUser != null && currentUser.id > 0) {
            try {
                val res = RoomifyApi.getOwnerFinances(currentUser.id)
                if (res.success && res.data != null) {
                    finances = res.data
                }
            } catch (e: Exception) {
                println("Error loading finances: ${e.message}")
            } finally {
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Finances & Earnings", fontWeight = FontWeight.Bold) },
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8F9FA))
        ) {
            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryColor)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Revenue Card
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            color = PrimaryColor,
                            shadowElevation = 4.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Brush.linearGradient(listOf(PrimaryColor, PrimaryLight)))
                                    .padding(24.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Total Revenue Earned", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                                        Spacer(Modifier.height(4.dp))
                                        val totalRev = finances?.totalRevenue ?: 0.0
                                        Text(
                                            "TZS ${totalRev.toLong()}",
                                            color = Color.White,
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.AccountBalanceWallet, null, tint = Color.White, modifier = Modifier.size(24.dp))
                                    }
                                }

                                Spacer(Modifier.height(20.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "${finances?.completedPaymentsCount ?: 0} Completed Bookings",
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )

                                    Button(
                                        onClick = { showWithdrawDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                        shape = RoundedCornerShape(12.dp),
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        Text("Withdraw", color = PrimaryColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }

                    if (withdrawalSuccessMsg != null) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SuccessColor.copy(alpha = 0.1f),
                                border = BorderStroke(1.dp, SuccessColor.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, null, tint = SuccessColor, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(10.dp))
                                    Text(withdrawalSuccessMsg!!, color = SuccessColor, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }

                    // Section Title
                    item {
                        Text(
                            "Recent Transactions",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = PrimaryColor,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    val payments = finances?.recentPayments ?: emptyList()
                    if (payments.isEmpty()) {
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, Color(0xFFEEEEEE))
                            ) {
                                Column(
                                    modifier = Modifier.padding(32.dp).fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.ReceiptLong, null, modifier = Modifier.size(48.dp), tint = Color.LightGray)
                                    Spacer(Modifier.height(12.dp))
                                    Text("No Transactions Yet", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.DarkGray)
                                    Spacer(Modifier.height(4.dp))
                                    Text("Payments from tenants for your properties will appear here automatically.", fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                        }
                    } else {
                        items(payments) { payment ->
                            TransactionItem(
                                payment = payment,
                                onConfirmCash = {
                                    if (payment.id != null) {
                                        scope.launch {
                                            try {
                                                val res = RoomifyApi.confirmCashPayment(payment.id)
                                                if (res.success) {
                                                    withdrawalSuccessMsg = "Cash payment for ${payment.referenceNumber} confirmed successfully!"
                                                    // Refresh
                                                    if (currentUser != null) {
                                                        val ref = RoomifyApi.getOwnerFinances(currentUser.id)
                                                        if (ref.success && ref.data != null) {
                                                            finances = ref.data
                                                        }
                                                    }
                                                }
                                            } catch (e: Exception) {
                                                println("Error confirming cash: ${e.message}")
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showWithdrawDialog) {
        var withdrawAmount by remember { mutableStateOf("") }
        var withdrawPhone by remember { mutableStateOf(currentUser?.phone ?: "") }
        var selectedMethod by remember { mutableStateOf("M-Pesa") }

        AlertDialog(
            onDismissRequest = { showWithdrawDialog = false },
            title = { Text("Request Withdrawal", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Select payment channel and enter destination details:", fontSize = 13.sp, color = Color.Gray)

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("M-Pesa", "Tigo Pesa", "Airtel", "Bank").forEach { method ->
                            FilterChip(
                                selected = selectedMethod == method,
                                onClick = { selectedMethod = method },
                                label = { Text(method, fontSize = 12.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = withdrawAmount,
                        onValueChange = { withdrawAmount = it },
                        label = { Text("Amount (TZS)") },
                        placeholder = { Text("e.g. 150000") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = withdrawPhone,
                        onValueChange = { withdrawPhone = it },
                        label = { Text("Mobile Money / Account Number") },
                        placeholder = { Text("07XXXXXXXX") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amountVal = withdrawAmount.toDoubleOrNull() ?: 0.0
                        withdrawalSuccessMsg = "Ombi la kutoa TZS ${amountVal.toLong()} kupitia $selectedMethod ($withdrawPhone) limetumwa kwa uhakiki."
                        showWithdrawDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                ) {
                    Text("SUBMIT REQUEST", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWithdrawDialog = false }) {
                    Text("CANCEL")
                }
            }
        )
    }
}

@Composable
private fun TransactionItem(
    payment: Payment,
    onConfirmCash: () -> Unit = {}
) {
    val isPendingCash = payment.status == "PENDING_CASH"
    val isPayInHand = payment.provider.contains("Hand", ignoreCase = true) || payment.provider.contains("Cash", ignoreCase = true)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, if (isPendingCash) Color(0xFFFFB74D) else Color(0xFFEEEEEE)),
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (isPendingCash) Color(0xFFFFF3E0) else SuccessColor.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isPayInHand) Icons.Default.Payments else Icons.Default.ArrowDownward,
                            null,
                            tint = if (isPendingCash) Color(0xFFE65100) else SuccessColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            payment.provider,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            payment.referenceNumber.ifEmpty { "Booking #${payment.bookingId}" },
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "+ TZS ${payment.amount.toLong()}",
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        color = if (isPendingCash) Color(0xFFE65100) else SuccessColor
                    )
                    Surface(
                        color = if (isPendingCash) Color(0xFFFFF3E0) else SuccessColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            if (isPendingCash) "PAY IN HAND - PENDING" else payment.status,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPendingCash) Color(0xFFE65100) else SuccessColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            if (isPendingCash) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFFFFE0B2))
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Tenant will pay cash in hand upon arrival.",
                        fontSize = 11.sp,
                        color = Color(0xFF795548)
                    )
                    Button(
                        onClick = onConfirmCash,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                    ) {
                        Text("Confirm Cash Received", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
