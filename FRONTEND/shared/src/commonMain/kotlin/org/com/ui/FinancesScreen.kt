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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.com.model.FinancesSummary
import org.com.model.Payment
import org.com.model.User
import org.com.network.RoomifyApi
import org.com.ui.components.roomifyAmbientBackground

private val PrimaryColor = Color(0xFF142170)
private val PrimaryLight = Color(0xFF2B3A9E)
private val SuccessColor = Color(0xFF2E7D32)
private val CardBorderColor = Color(0xFFD5E0F0)
private val SubtitleColor = Color(0xFF64748B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancesScreen(
    currentUser: User? = null,
    onBack: () -> Unit
) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Finances & Earnings", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF0F172A))
                        Text("Revenue tracking & payouts", fontSize = 12.sp, color = SubtitleColor)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF0F172A)
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .roomifyAmbientBackground()
        ) {
            FinancesContent(currentUser = currentUser)
        }
    }
}

@Composable
fun FinancesContent(
    currentUser: User? = null,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var finances by remember { mutableStateOf<FinancesSummary?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showWithdrawDialog by remember { mutableStateOf(false) }
    var withdrawalSuccessMsg by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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

    BoxWithConstraints(modifier = modifier.fillMaxSize().roomifyAmbientBackground()) {
        val isMobile = maxWidth < 600.dp
        val isExpanded = maxWidth >= 840.dp

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryColor)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(if (isExpanded) 24.dp else 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Revenue Card (Rich Navy Banner)
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = PrimaryColor,
                        shadowElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Brush.linearGradient(listOf(PrimaryColor, PrimaryLight)))
                                .padding(24.dp)
                        ) {
                            if (isMobile) {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Total Revenue Earned", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                            Spacer(Modifier.height(4.dp))
                                            val totalRev = finances?.totalRevenue ?: 0.0
                                            Text(
                                                "TZS ${totalRev.toLong()}",
                                                color = Color.White,
                                                fontSize = 24.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(CircleShape)
                                                .background(Color.White.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.AccountBalanceWallet, null, tint = Color.White, modifier = Modifier.size(22.dp))
                                        }
                                    }

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

                                        val balance = finances?.totalRevenue ?: 0.0
                                        Button(
                                            onClick = {
                                                if (balance <= 0.0) {
                                                    errorMessage = "Withdrawal unavailable: Wallet balance is TZS 0."
                                                } else {
                                                    showWithdrawDialog = true
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                            shape = RoundedCornerShape(12.dp),
                                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                        ) {
                                            Text("Withdraw", color = PrimaryColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        }
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Total Revenue Earned", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                        Spacer(Modifier.height(4.dp))
                                        val totalRev = finances?.totalRevenue ?: 0.0
                                        Text(
                                            "TZS ${totalRev.toLong()}",
                                            color = Color.White,
                                            fontSize = 30.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Surface(
                                            color = Color.White.copy(alpha = 0.12f),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text(
                                                "${finances?.completedPaymentsCount ?: 0} Completed Bookings",
                                                color = Color.White,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                            )
                                        }

                                        val balance = finances?.totalRevenue ?: 0.0
                                        Button(
                                            onClick = {
                                                if (balance <= 0.0) {
                                                    errorMessage = "Withdrawal unavailable: Wallet balance is TZS 0."
                                                } else {
                                                    showWithdrawDialog = true
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                            shape = RoundedCornerShape(12.dp),
                                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
                                        ) {
                                            Text("Withdraw", color = PrimaryColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (errorMessage != null) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFEF2F2),
                            border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(Icons.Default.ErrorOutline, null, tint = Color(0xFFDC2626), modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(10.dp))
                                    Text(errorMessage!!, color = Color(0xFF991B1B), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }
                                IconButton(onClick = { errorMessage = null }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Close, null, tint = Color(0xFF991B1B), modifier = Modifier.size(16.dp))
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
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(Icons.Default.CheckCircle, null, tint = SuccessColor, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(10.dp))
                                    Text(withdrawalSuccessMsg!!, color = SuccessColor, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }
                                IconButton(onClick = { withdrawalSuccessMsg = null }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Close, null, tint = SuccessColor, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        "Recent Transactions",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Color(0xFF0F172A),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                val payments = finances?.recentPayments ?: emptyList()
                if (payments.isEmpty()) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, CardBorderColor)
                        ) {
                            Column(
                                modifier = Modifier.padding(40.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(PrimaryColor.copy(alpha = 0.08f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.ReceiptLong, null, modifier = Modifier.size(28.dp), tint = PrimaryColor)
                                }
                                Spacer(Modifier.height(14.dp))
                                Text("No Transactions Yet", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0F172A))
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Payments from tenants for your properties will appear here automatically.",
                                    fontSize = 13.sp,
                                    color = SubtitleColor,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
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

    if (showWithdrawDialog) {
        var withdrawAmount by remember { mutableStateOf("") }
        var withdrawPhone by remember { mutableStateOf(currentUser?.phone ?: "") }
        var selectedMethod by remember { mutableStateOf("M-Pesa") }

        AlertDialog(
            onDismissRequest = { showWithdrawDialog = false },
            title = { Text("Request Withdrawal", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Select payment channel and enter destination details:", fontSize = 13.sp, color = SubtitleColor)

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
                        withdrawalSuccessMsg = "Withdrawal request of TZS ${amountVal.toLong()} via $selectedMethod ($withdrawPhone) submitted for review."
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
        border = BorderStroke(1.dp, if (isPendingCash) Color(0xFFF59E0B) else CardBorderColor),
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
                            .background(if (isPendingCash) Color(0xFFFEF3C7) else SuccessColor.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isPayInHand) Icons.Default.Payments else Icons.Default.ArrowDownward,
                            null,
                            tint = if (isPendingCash) Color(0xFFD97706) else SuccessColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            payment.provider,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            payment.referenceNumber.ifEmpty { "Booking #${payment.bookingId}" },
                            fontSize = 11.sp,
                            color = SubtitleColor
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "+ TZS ${payment.amount.toLong()}",
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        color = if (isPendingCash) Color(0xFFD97706) else SuccessColor
                    )
                    Surface(
                        color = if (isPendingCash) Color(0xFFFEF3C7) else SuccessColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            if (isPendingCash) "PAY IN HAND - PENDING" else payment.status,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPendingCash) Color(0xFFD97706) else SuccessColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            if (isPendingCash) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFFFDE68A))
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Tenant will pay cash in hand upon arrival.",
                        fontSize = 11.sp,
                        color = Color(0xFF92400E)
                    )
                    Button(
                        onClick = onConfirmCash,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessColor)
                    ) {
                        Text("Confirm Cash Received", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
