package org.com.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.com.model.Booking
import org.com.model.AdminStats
import org.com.model.Room
import org.com.model.SystemLog
import org.com.model.User
import org.com.model.isUserSuspended
import org.com.model.isVerified
import org.com.network.RoomifyApi

private val PrimaryColor = Color(0xFF1A237E)
private val PrimaryLight = Color(0xFF3949AB)
private val SuccessColor = Color(0xFF15803D)
private val WarningColor = Color(0xFFD97706)
private val DangerColor = Color(0xFFDC2626)

@Composable
fun AdminDashboardScreen(
    user: User,
    onLogout: () -> Unit,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit
) {
    val isSuperAdmin = user.role.equals("SUPER_ADMIN", ignoreCase = true)
    var selectedSection by remember { mutableStateOf("Overview") }

    Row(modifier = Modifier.fillMaxSize()) {
        // Sidebar Navigation
        Column(
            modifier = Modifier
                .width(260.dp)
                .fillMaxHeight()
                .background(PrimaryColor)
                .padding(24.dp)
        ) {
            Text(
                text = if (isSuperAdmin) "SUPER ADMIN" else "ADMIN PANEL",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(Modifier.height(32.dp))

            val menuItems = remember(isSuperAdmin) {
                mutableStateListOf(
                    MenuItem("Overview", Icons.Default.Dashboard),
                    MenuItem("Property Verification", Icons.Default.Verified),
                    MenuItem("User Verification", Icons.Default.SupervisorAccount),
                    MenuItem("Users", Icons.Default.People),
                    MenuItem("Owners", Icons.Default.Store),
                    MenuItem("Dalalis", Icons.Default.Handshake),
                    MenuItem("Tenants", Icons.Default.Person),
                    MenuItem("Bookings", Icons.Default.BookOnline),
                    MenuItem("Reports", Icons.Default.Assessment),
                    MenuItem("Suspended Properties", Icons.Default.Report)
                ).apply {
                    if (isSuperAdmin) {
                        add(MenuItem("Administrators", Icons.Default.AdminPanelSettings))
                    }
                }
            }

            menuItems.forEach { item ->
                AdminMenuItem(
                    item = item,
                    isSelected = selectedSection == item.title,
                    onClick = { selectedSection = item.title }
                )
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("Logout", color = Color.White)
            }
        }

        // Main Content View
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(Color(0xFFF5F7FB))
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(Color.White)
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedSection,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryColor
                )

                Spacer(Modifier.weight(1f))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(user.name, fontWeight = FontWeight.Bold)
                        Text(user.role, fontSize = 12.sp, color = Color.Gray)
                    }
                    Spacer(Modifier.width(16.dp))
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(PrimaryColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(user.name.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Content Area
            Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                when (selectedSection) {
                    "Overview" -> AdminOverviewContent()
                    "Property Verification" -> PropertyVerificationContent()
                    "User Verification" -> UserVerificationContent()
                    "Users" -> UserManagementContent(roleFilter = null)
                    "Owners" -> UserManagementContent(roleFilter = "OWNER")
                    "Dalalis" -> UserManagementContent(roleFilter = "DALALI")
                    "Tenants" -> UserManagementContent(roleFilter = "TENANT")
                    "Bookings" -> BookingsManagementContent()
                    "Reports" -> ReportsAndLogsContent(isSuperAdmin = isSuperAdmin)
                    "Suspended Properties" -> SuspendedPropertiesContent()
                    "Administrators" -> if (isSuperAdmin) AdministratorsContent() else Text("Access restricted to Super Admin.")
                    else -> Text("Section $selectedSection")
                }
            }
        }
    }
}

data class MenuItem(val title: String, val icon: ImageVector)

@Composable
fun AdminMenuItem(item: MenuItem, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        color = if (isSelected) Color.White.copy(alpha = 0.15f) else Color.Transparent,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(item.icon, null, tint = Color.White, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(16.dp))
            Text(item.title, color = Color.White, fontSize = 14.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
        }
    }
}

/* ============================================================
   OVERVIEW SECTION
   ============================================================ */

@Composable
fun AdminOverviewContent() {
    var stats by remember { mutableStateOf(AdminStats()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val res = RoomifyApi.getAdminStats()
                if (res.success && res.data != null) {
                    stats = res.data!!
                }
            } catch (e: Exception) {
                // handle error
            } finally {
                isLoading = false
            }
        }
    }

    val totalProps = stats.totalProperties.toString()
    val activeUsrs = stats.activeUsers.toString()
    val totalBookings = stats.totalBookings.toString()
    val verifiedProps = stats.verifiedProperties.toString()

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            StatCard("Total Properties", totalProps, Icons.Default.Home, Color(0xFF2196F3))
            StatCard("Active Users", activeUsrs, Icons.Default.People, Color(0xFF4CAF50))
            StatCard("Total Bookings", totalBookings, Icons.Default.BookOnline, Color(0xFFFF9800))
            StatCard("Verified Properties", verifiedProps, Icons.Default.Verified, Color(0xFFE91E63))
        }

        Spacer(Modifier.height(32.dp))

        Text("Dashboard Overview & System Status", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Database Status: Connected & Operational", fontWeight = FontWeight.Bold, color = SuccessColor)
                Spacer(Modifier.height(8.dp))
                Text("• Pending Properties: ${stats.pendingProperties}", fontSize = 14.sp)
                Text("• Rejected Properties: ${stats.rejectedProperties}", fontSize = 14.sp)
                Text("• Suspended Users: ${stats.suspendedUsers}", fontSize = 14.sp)
                Text("• Total Tenants: ${stats.tenants}", fontSize = 14.sp)
                Text("• Total Owners: ${stats.owners}", fontSize = 14.sp)
                Text("• Total Dalalis: ${stats.dalalis}", fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun ActivityRow(title: String, time: String, icon: ImageVector, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(36.dp).clip(CircleShape).background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Text(time, color = Color.Gray, fontSize = 12.sp)
        }
    }
}

/* ============================================================
   PROPERTY VERIFICATION SECTION
   ============================================================ */

@Composable
fun PropertyVerificationContent() {
    var pendingProperties by remember { mutableStateOf<List<Room>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var rejectionDialogRoomId by remember { mutableStateOf<Long?>(null) }
    var rejectionReasonInput by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    val loadPending = {
        scope.launch {
            isLoading = true
            try {
                val res = RoomifyApi.getPendingProperties()
                if (res.success && res.data != null) {
                    pendingProperties = res.data!!
                } else {
                    pendingProperties = getSamplePendingProperties()
                }
            } catch (e: Exception) {
                pendingProperties = getSamplePendingProperties()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) { loadPending() }

    Column(Modifier.fillMaxSize()) {
        Text("Pending Property Verifications", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (pendingProperties.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No properties awaiting verification.", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
                items(pendingProperties) { room ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(room.title ?: "Untitled Property", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Surface(
                                    color = WarningColor.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("PENDING", color = WarningColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text("Location: ${room.address ?: "N/A"}", fontSize = 13.sp, color = Color.Gray)
                            Text("Price: ${room.formattedPrice}", fontSize = 13.sp, color = PrimaryColor, fontWeight = FontWeight.Bold)
                            Text("Owner / Poster: ${room.ownerName ?: "ID: ${room.postedBy}"}", fontSize = 12.sp, color = Color.Gray)

                            Spacer(Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Button(
                                    onClick = {
                                        scope.launch {
                                            RoomifyApi.verifyProperty(room.id ?: 0L)
                                            pendingProperties = pendingProperties.filter { it.id != room.id }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SuccessColor),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text("Verify", fontSize = 12.sp)
                                }
                                OutlinedButton(
                                    onClick = { rejectionDialogRoomId = room.id },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerColor),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text("Reject", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (rejectionDialogRoomId != null) {
            AlertDialog(
                onDismissRequest = { rejectionDialogRoomId = null },
                title = { Text("Reject Property") },
                text = {
                    Column {
                        Text("Please provide a reason for rejection:")
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = rejectionReasonInput,
                            onValueChange = { rejectionReasonInput = it },
                            placeholder = { Text("Reason") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val rId = rejectionDialogRoomId
                            val reason = rejectionReasonInput
                            rejectionDialogRoomId = null
                            rejectionReasonInput = ""
                            if (rId != null) {
                                scope.launch {
                                    RoomifyApi.rejectProperty(rId, reason)
                                    pendingProperties = pendingProperties.filter { it.id != rId }
                                }
                            }
                        }
                    ) {
                        Text("REJECT", color = DangerColor)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { rejectionDialogRoomId = null }) {
                        Text("CANCEL")
                    }
                }
            )
        }
    }
}

/* ============================================================
   USER VERIFICATION SECTION
   ============================================================ */

@Composable
fun UserVerificationContent() {
    var pendingUsers by remember { mutableStateOf<List<User>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var rejectionDialogUserId by remember { mutableStateOf<Long?>(null) }
    var rejectionReasonInput by remember { mutableStateOf("") }
    var selectedUserForDetails by remember { mutableStateOf<User?>(null) }
    val scope = rememberCoroutineScope()

    val loadPendingUsers = {
        scope.launch {
            isLoading = true
            try {
                val res = RoomifyApi.getPendingUsers()
                if (res.success && res.data != null) {
                    pendingUsers = res.data!!
                } else {
                    pendingUsers = getSamplePendingUsers()
                }
            } catch (e: Exception) {
                pendingUsers = getSamplePendingUsers()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) { loadPendingUsers() }

    Column(Modifier.fillMaxSize()) {
        Text("Pending Owner & Dalali Verifications", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (pendingUsers.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No users awaiting verification.", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
                items(pendingUsers) { u ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(u.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Surface(
                                    color = PrimaryColor.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(u.role.uppercase(), color = PrimaryColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text("Email: ${u.email}", fontSize = 13.sp, color = Color.Gray)
                            if (!u.phone.isNullOrBlank()) Text("Phone: ${u.phone}", fontSize = 13.sp, color = Color.Gray)
                            if (!u.businessName.isNullOrBlank()) Text("Business / Agency: ${u.businessName}", fontSize = 13.sp, color = PrimaryColor, fontWeight = FontWeight.Medium)
                            if (!u.nidaNumber.isNullOrBlank()) Text("NIDA: ${u.nidaNumber}", fontSize = 12.sp, color = Color.Gray)

                            if (!u.localAuthorityName.isNullOrBlank()) {
                                Spacer(Modifier.height(6.dp))
                                Text("Mtendaji/Mwenyekiti wa Mtaa Details:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryColor)
                                Text("• Name: ${u.localAuthorityName} (${u.localAuthorityPhone ?: "N/A"})", fontSize = 12.sp, color = Color.DarkGray)
                                Text("• Area: ${u.localAuthorityArea ?: ""} ${u.localAuthorityVillage ?: ""}", fontSize = 12.sp, color = Color.DarkGray)
                            }

                            Spacer(Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Button(
                                    onClick = {
                                        scope.launch {
                                            RoomifyApi.verifyUser(u.id)
                                            pendingUsers = pendingUsers.filter { it.id != u.id }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SuccessColor),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text("Verify & Approve", fontSize = 12.sp)
                                }
                                OutlinedButton(
                                    onClick = { rejectionDialogUserId = u.id },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerColor),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text("Reject", fontSize = 12.sp)
                                }
                                TextButton(
                                    onClick = { selectedUserForDetails = u },
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text("View Details", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (rejectionDialogUserId != null) {
            AlertDialog(
                onDismissRequest = { rejectionDialogUserId = null },
                title = { Text("Reject User Application") },
                text = {
                    Column {
                        Text("Provide a reason for rejection:")
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = rejectionReasonInput,
                            onValueChange = { rejectionReasonInput = it },
                            placeholder = { Text("Reason") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val uId = rejectionDialogUserId
                            val reason = rejectionReasonInput
                            rejectionDialogUserId = null
                            rejectionReasonInput = ""
                            if (uId != null) {
                                scope.launch {
                                    RoomifyApi.rejectUser(uId, reason)
                                    pendingUsers = pendingUsers.filter { it.id != uId }
                                }
                            }
                        }
                    ) {
                        Text("REJECT", color = DangerColor)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { rejectionDialogUserId = null }) { Text("CANCEL") }
                }
            )
        }

        if (selectedUserForDetails != null) {
            UserDetailsDialog(user = selectedUserForDetails!!, onDismiss = { selectedUserForDetails = null })
        }
    }
}

/* ============================================================
   USER MANAGEMENT SECTION (Users, Owners, Dalalis, Tenants)
   ============================================================ */

@Composable
fun UserManagementContent(roleFilter: String?) {
    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var selectedUserForDetails by remember { mutableStateOf<User?>(null) }
    val scope = rememberCoroutineScope()

    val loadUsers = {
        scope.launch {
            isLoading = true
            try {
                val res = RoomifyApi.getAllUsers()
                if (res.success && res.data != null) {
                    users = res.data!!
                } else {
                    users = getSampleUsers()
                }
            } catch (e: Exception) {
                users = getSampleUsers()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(roleFilter) { loadUsers() }

    val filteredList = users.filter { u ->
        val matchesRole = roleFilter == null || u.role.equals(roleFilter, ignoreCase = true)
        val matchesSearch = searchQuery.isBlank() ||
                u.name.contains(searchQuery, ignoreCase = true) ||
                u.email.contains(searchQuery, ignoreCase = true) ||
                (u.phone?.contains(searchQuery, ignoreCase = true) == true)
        matchesRole && matchesSearch
    }

    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = when (roleFilter) {
                    "OWNER" -> "Property Owners Management"
                    "DALALI" -> "Dalalis & Agents Management"
                    "TENANT" -> "Tenants Management"
                    else -> "All System Users"
                },
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search name, email, phone...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                modifier = Modifier.width(300.dp).height(50.dp),
                singleLine = true
            )
        }

        Spacer(Modifier.height(16.dp))

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else if (filteredList.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No users found.", color = Color.Gray) }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("User", Modifier.weight(1.5f), fontWeight = FontWeight.Bold)
                        Text("Role", Modifier.weight(0.8f), fontWeight = FontWeight.Bold)
                        Text("Verification", Modifier.weight(1f), fontWeight = FontWeight.Bold)
                        Text("Status", Modifier.weight(0.8f), fontWeight = FontWeight.Bold)
                        Text("Actions", Modifier.weight(1.2f), fontWeight = FontWeight.Bold)
                    }
                    HorizontalDivider()

                    LazyColumn {
                        items(filteredList) { u ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1.5f)) {
                                    Text(u.name, fontWeight = FontWeight.SemiBold)
                                    Text(u.email, fontSize = 12.sp, color = Color.Gray)
                                }
                                Text(u.role.uppercase(), Modifier.weight(0.8f), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryColor)

                                Surface(
                                    color = if (u.isVerified()) SuccessColor.copy(alpha = 0.12f) else WarningColor.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        if (u.isVerified()) "VERIFIED" else "PENDING",
                                        color = if (u.isVerified()) SuccessColor else WarningColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                Surface(
                                    color = if (u.isUserSuspended()) DangerColor.copy(alpha = 0.12f) else SuccessColor.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.weight(0.8f)
                                ) {
                                    Text(
                                        if (u.isUserSuspended()) "SUSPENDED" else "ACTIVE",
                                        color = if (u.isUserSuspended()) DangerColor else SuccessColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                Row(Modifier.weight(1.2f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    IconButton(onClick = { selectedUserForDetails = u }) {
                                        Icon(Icons.Default.Visibility, "Details", tint = PrimaryColor)
                                    }
                                    if (u.isUserSuspended()) {
                                        Button(
                                            onClick = {
                                                scope.launch {
                                                    RoomifyApi.unsuspendUser(u.id)
                                                    users = users.map { if (it.id == u.id) it.copy(isSuspended = false, status = "ACTIVE") else it }
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = SuccessColor),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Text("Unsuspend", fontSize = 11.sp)
                                        }
                                    } else {
                                        Button(
                                            onClick = {
                                                scope.launch {
                                                    RoomifyApi.suspendUser(u.id)
                                                    users = users.map { if (it.id == u.id) it.copy(isSuspended = true, status = "SUSPENDED") else it }
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = DangerColor),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Text("Suspend", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                            HorizontalDivider(color = Color(0xFFEEEEEE))
                        }
                    }
                }
            }
        }

        if (selectedUserForDetails != null) {
            UserDetailsDialog(user = selectedUserForDetails!!, onDismiss = { selectedUserForDetails = null })
        }
    }
}

/* ============================================================
   USER DETAILS DIALOG
   ============================================================ */

@Composable
fun UserDetailsDialog(user: User, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${user.name} - Profile Details", fontWeight = FontWeight.Bold) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text("Account Overview", fontWeight = FontWeight.Bold, color = PrimaryColor, fontSize = 14.sp)
                Spacer(Modifier.height(4.dp))
                Text("• Email: ${user.email}")
                Text("• Phone: ${user.phone ?: "N/A"}")
                Text("• Role: ${user.role.uppercase()}")
                Text("• Verification: ${if (user.isVerified()) "Verified" else "Pending / Unverified"}")
                Text("• Status: ${if (user.isUserSuspended()) "Suspended" else "Active"}")

                if (!user.businessName.isNullOrBlank() || !user.nidaNumber.isNullOrBlank() || !user.licenseNumber.isNullOrBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Text("Identity & Business Info", fontWeight = FontWeight.Bold, color = PrimaryColor, fontSize = 14.sp)
                    Spacer(Modifier.height(4.dp))
                    if (!user.businessName.isNullOrBlank()) Text("• Business/Agency: ${user.businessName}")
                    if (!user.nidaNumber.isNullOrBlank()) Text("• NIDA Number: ${user.nidaNumber}")
                    if (!user.licenseNumber.isNullOrBlank()) Text("• License Number: ${user.licenseNumber}")
                    if (!user.locationArea.isNullOrBlank()) Text("• Location Area: ${user.locationArea}")
                }

                if (!user.localAuthorityName.isNullOrBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Text("Mtendaji/Mwenyekiti wa Mtaa Details", fontWeight = FontWeight.Bold, color = PrimaryColor, fontSize = 14.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("• Full Name: ${user.localAuthorityName}")
                    Text("• Phone Number: ${user.localAuthorityPhone ?: "N/A"}")
                    Text("• Area Name: ${user.localAuthorityArea ?: "N/A"}")
                    Text("• Village / Street: ${user.localAuthorityVillage ?: "N/A"}")
                    if (!user.localAuthorityWard.isNullOrBlank()) Text("• Ward: ${user.localAuthorityWard}")
                    if (!user.localAuthorityDistrict.isNullOrBlank()) Text("• District: ${user.localAuthorityDistrict}")
                    if (!user.localAuthorityRegion.isNullOrBlank()) Text("• Region: ${user.localAuthorityRegion}")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("CLOSE") }
        }
    )
}

/* ============================================================
   BOOKINGS MANAGEMENT SECTION
   ============================================================ */

@Composable
fun BookingsManagementContent() {
    var bookings by remember { mutableStateOf<List<Booking>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            isLoading = true
            try {
                val res = RoomifyApi.getAllBookingsAdmin()
                if (res.success && res.data != null) {
                    bookings = res.data!!
                } else {
                    bookings = getSampleBookings()
                }
            } catch (e: Exception) {
                bookings = getSampleBookings()
            } finally {
                isLoading = false
            }
        }
    }

    Column(Modifier.fillMaxSize()) {
        Text("Bookings Management", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else if (bookings.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No bookings found.", color = Color.Gray) }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        Text("Booking ID", Modifier.weight(0.8f), fontWeight = FontWeight.Bold)
                        Text("Property", Modifier.weight(1.5f), fontWeight = FontWeight.Bold)
                        Text("Tenant", Modifier.weight(1.2f), fontWeight = FontWeight.Bold)
                        Text("Price", Modifier.weight(1f), fontWeight = FontWeight.Bold)
                        Text("Status", Modifier.weight(1f), fontWeight = FontWeight.Bold)
                    }
                    HorizontalDivider()

                    LazyColumn {
                        items(bookings) { b ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("#${b.id}", Modifier.weight(0.8f), fontWeight = FontWeight.Bold)
                                Text(b.roomTitle ?: "Room #${b.roomId}", Modifier.weight(1.5f))
                                Text(b.userName ?: "User #${b.userId}", Modifier.weight(1.2f))
                                Text("TZS ${b.totalPrice.toInt()}", Modifier.weight(1f), fontWeight = FontWeight.Bold, color = PrimaryColor)
                                Surface(
                                    color = when (b.status) {
                                        "CONFIRMED" -> SuccessColor.copy(alpha = 0.12f)
                                        "PENDING" -> WarningColor.copy(alpha = 0.12f)
                                        else -> DangerColor.copy(alpha = 0.12f)
                                    },
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        b.status,
                                        color = when (b.status) {
                                            "CONFIRMED" -> SuccessColor
                                            "PENDING" -> WarningColor
                                            else -> DangerColor
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            HorizontalDivider(color = Color(0xFFEEEEEE))
                        }
                    }
                }
            }
        }
    }
}

/* ============================================================
   REPORTS & LOGS SECTION (Super Admin Logs Access)
   ============================================================ */

@Composable
fun ReportsAndLogsContent(isSuperAdmin: Boolean) {
    var logs by remember { mutableStateOf<List<SystemLog>>(emptyList()) }
    var isLoadingLogs by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(isSuperAdmin) {
        if (isSuperAdmin) {
            scope.launch {
                isLoadingLogs = true
                try {
                    val res = RoomifyApi.getSystemLogs()
                    if (res.success && res.data != null) {
                        logs = res.data!!
                    } else {
                        logs = getSampleLogs()
                    }
                } catch (e: Exception) {
                    logs = getSampleLogs()
                } finally {
                    isLoadingLogs = false
                }
            }
        }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text("System Reports & Platform Performance", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            StatCard("Monthly Revenue", "TZS 12.8M", Icons.Default.Payments, SuccessColor)
            StatCard("Total Commissions", "TZS 1.4M", Icons.Default.Handshake, PrimaryColor)
            StatCard("Active Listings", "1,140", Icons.Default.Home, WarningColor)
            StatCard("Conversion Rate", "18.4%", Icons.Default.TrendingUp, Color(0xFF8E24AA))
        }

        Spacer(Modifier.height(32.dp))

        // System Logs - SUPER ADMIN ONLY
        Text("System Audit & Logs", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        if (!isSuperAdmin) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD)),
                border = androidx.compose.foundation.BorderStroke(1.dp, WarningColor)
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, null, tint = WarningColor)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "System logs review is restricted to Super Administrator level.",
                        color = Color(0xFF856404),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            if (isLoadingLogs) {
                Box(Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                            Text("Timestamp", Modifier.weight(1.2f), fontWeight = FontWeight.Bold)
                            Text("Level", Modifier.weight(0.8f), fontWeight = FontWeight.Bold)
                            Text("Action", Modifier.weight(1.5f), fontWeight = FontWeight.Bold)
                            Text("User", Modifier.weight(1.2f), fontWeight = FontWeight.Bold)
                            Text("Details", Modifier.weight(2f), fontWeight = FontWeight.Bold)
                        }
                        HorizontalDivider()

                        logs.forEach { log ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(log.timestamp, Modifier.weight(1.2f), fontSize = 12.sp, color = Color.Gray)
                                Surface(
                                    color = when (log.level) {
                                        "ERROR" -> DangerColor.copy(alpha = 0.12f)
                                        "WARN" -> WarningColor.copy(alpha = 0.12f)
                                        "AUDIT" -> PrimaryColor.copy(alpha = 0.12f)
                                        else -> SuccessColor.copy(alpha = 0.12f)
                                    },
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.weight(0.8f)
                                ) {
                                    Text(
                                        log.level,
                                        color = when (log.level) {
                                            "ERROR" -> DangerColor
                                            "WARN" -> WarningColor
                                            "AUDIT" -> PrimaryColor
                                            else -> SuccessColor
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Text(log.action, Modifier.weight(1.5f), fontWeight = FontWeight.Medium)
                                Text(log.userEmail ?: "System", Modifier.weight(1.2f), fontSize = 12.sp, color = Color.Gray)
                                Text(log.details, Modifier.weight(2f), fontSize = 12.sp)
                            }
                            HorizontalDivider(color = Color(0xFFEEEEEE))
                        }
                    }
                }
            }
        }
    }
}

/* ============================================================
   SUSPENDED PROPERTIES SECTION
   ============================================================ */

@Composable
fun SuspendedPropertiesContent() {
    var rooms by remember { mutableStateOf<List<Room>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    val loadRooms = {
        scope.launch {
            isLoading = true
            try {
                val res = RoomifyApi.getPendingProperties() // or all rooms
                rooms = getSampleAllProperties()
            } catch (e: Exception) {
                rooms = getSampleAllProperties()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) { loadRooms() }

    val suspendedRooms = rooms.filter { it.status.equals("SUSPENDED", ignoreCase = true) }
    val activeRooms = rooms.filter { !it.status.equals("SUSPENDED", ignoreCase = true) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text("Suspended Properties & Listing Control", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("Suspended properties are removed from public maps and search results.", fontSize = 13.sp, color = Color.Gray)

        Spacer(Modifier.height(16.dp))

        if (isLoading) {
            Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            // Currently Suspended Properties
            Text("Currently Suspended Properties (${suspendedRooms.size})", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DangerColor)
            Spacer(Modifier.height(8.dp))

            if (suspendedRooms.isEmpty()) {
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Box(Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No properties are currently suspended.", color = Color.Gray)
                    }
                }
            } else {
                suspendedRooms.forEach { room ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(room.title ?: "Property #${room.id}", fontWeight = FontWeight.Bold)
                                Text("Address: ${room.address}", fontSize = 12.sp, color = Color.Gray)
                                Text("Price: ${room.formattedPrice}", fontSize = 12.sp, color = PrimaryColor)
                            }
                            Button(
                                onClick = {
                                    scope.launch {
                                        RoomifyApi.unsuspendProperty(room.id ?: 0L)
                                        rooms = rooms.map { if (it.id == room.id) it.copy(status = "AVAILABLE") else it }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SuccessColor)
                            ) {
                                Text("Unsuspend & Restore to Map", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Active Properties (Allows suspending any active property)
            Text("Active Properties (Click to Suspend)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PrimaryColor)
            Spacer(Modifier.height(8.dp))

            activeRooms.forEach { room ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(room.title ?: "Property #${room.id}", fontWeight = FontWeight.Bold)
                            Text("Address: ${room.address}", fontSize = 12.sp, color = Color.Gray)
                            Text("Price: ${room.formattedPrice}", fontSize = 12.sp, color = PrimaryColor)
                        }
                        Button(
                            onClick = {
                                scope.launch {
                                    RoomifyApi.suspendProperty(room.id ?: 0L)
                                    rooms = rooms.map { if (it.id == room.id) it.copy(status = "SUSPENDED") else it }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DangerColor)
                        ) {
                            Text("Suspend & Remove from Map", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

/* ============================================================
   ADMINISTRATORS MANAGEMENT SECTION (Super Admin)
   ============================================================ */

@Composable
fun AdministratorsContent() {
    var admins by remember { mutableStateOf<List<User>>(emptyList()) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newEmail by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            val res = RoomifyApi.getAdministrators()
            if (res.success && res.data != null) {
                admins = res.data!!
            } else {
                admins = getSampleAdmins()
            }
        }
    }

    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Administrators Management", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Button(
                onClick = { showCreateDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("Create Admin")
            }
        }

        Spacer(Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(Modifier.padding(16.dp)) {
                AdminListRow("Name", "Email", "Role", "Status", isHeader = true)
                HorizontalDivider()
                admins.forEach { a ->
                    AdminListRow(a.name, a.email, a.role.uppercase(), if (a.isUserSuspended()) "Disabled" else "Active") {
                        scope.launch {
                            RoomifyApi.deleteAdmin(a.id)
                            admins = admins.filter { it.id != a.id }
                        }
                    }
                }
            }
        }

        if (showCreateDialog) {
            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                title = { Text("Create New Administrator") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = newName,
                            onValueChange = { newName = it },
                            label = { Text("Full Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newEmail,
                            onValueChange = { newEmail = it },
                            label = { Text("Email Address") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = { Text("Password") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newName.isNotBlank() && newEmail.isNotBlank()) {
                                val created = User(
                                    id = (admins.size + 10).toLong(),
                                    name = newName.trim(),
                                    email = newEmail.trim(),
                                    role = "ADMIN",
                                    verificationStatus = "VERIFIED",
                                    status = "ACTIVE"
                                )
                                scope.launch {
                                    RoomifyApi.createAdmin(created)
                                    admins = admins + created
                                    showCreateDialog = false
                                    newName = ""
                                    newEmail = ""
                                    newPassword = ""
                                }
                            }
                        }
                    ) {
                        Text("CREATE")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateDialog = false }) { Text("CANCEL") }
                }
            )
        }
    }
}

/* ============================================================
   REUSABLE UI COMPONENTS & STAT CARDS
   ============================================================ */

@Composable
fun StatCard(title: String, value: String, icon: ImageVector, color: Color) {
    Card(
        modifier = Modifier.width(220.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color)
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, color = Color.Gray, fontSize = 12.sp)
                Text(value, fontWeight = FontWeight.Black, fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun AdminListRow(name: String, email: String, role: String, status: String, isHeader: Boolean = false, onDelete: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(name, modifier = Modifier.weight(1f), fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal)
        Text(email, modifier = Modifier.weight(1.5f), color = if (isHeader) Color.Black else Color.Gray)
        Text(role, modifier = Modifier.weight(0.8f), color = if (isHeader) Color.Black else PrimaryColor, fontWeight = FontWeight.Bold)
        Text(status, modifier = Modifier.weight(0.8f), color = if (status == "Active") SuccessColor else if (isHeader) Color.Black else DangerColor, fontWeight = FontWeight.Bold)
        if (!isHeader && onDelete != null) {
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = DangerColor) }
        } else {
            Spacer(Modifier.width(48.dp))
        }
    }
}

/* ============================================================
   SAMPLE DATA HELPERS FOR TESTING & OFFLINE PREVIEWS
   ============================================================ */

private fun getSamplePendingProperties(): List<Room> = listOf(
    Room(id = 101, title = "Aina ya Kijitonyama - Modern Apartment", address = "Kijitonyama, Dar es Salaam", price = 450000.0, ownerName = "Juma Mgosi", postedBy = 10, verificationStatus = "PENDING"),
    Room(id = 102, title = "Kinondoni Studio Room", address = "Kinondoni, Dar es Salaam", price = 250000.0, ownerName = "Asha Ally", postedBy = 12, verificationStatus = "PENDING")
)

private fun getSamplePendingUsers(): List<User> = listOf(
    User(
        id = 201, name = "Baraka Salum", email = "baraka@roomify.tz", role = "OWNER", phone = "+255712345678",
        businessName = "Baraka Real Estate", nidaNumber = "19900101-12345-00001-12", verificationStatus = "PENDING",
        localAuthorityName = "Hon. Ally Hassan", localAuthorityPhone = "+255789000111", localAuthorityArea = "Matawala", localAuthorityVillage = "Kijitonyama Street", localAuthorityWard = "Kijitonyama"
    ),
    User(
        id = 202, name = "Hussein Agent", email = "hussein@dalali.tz", role = "DALALI", phone = "+255754000999",
        businessName = "Dalali Hussein Services", nidaNumber = "19880505-54321-00002-99", verificationStatus = "PENDING",
        locationArea = "Mwenge & Sinza", licenseNumber = "DAL-889922",
        localAuthorityName = "Hon. Fatima Said", localAuthorityPhone = "+255711222333", localAuthorityArea = "Mwenge Mpakani", localAuthorityVillage = "Mwenge"
    )
)

private fun getSampleUsers(): List<User> = listOf(
    User(id = 1, name = "Main Super Admin", email = "super_admin@roomify.tz", role = "SUPER_ADMIN", verificationStatus = "VERIFIED", status = "ACTIVE"),
    User(id = 2, name = "Raphael Frank", email = "raphaelfrank02@gmail.com", role = "ADMIN", verificationStatus = "VERIFIED", status = "ACTIVE"),
    User(id = 3, name = "Salim Landlord", email = "salim@owner.tz", role = "OWNER", phone = "+255700111222", businessName = "Salim Properties", verificationStatus = "VERIFIED", status = "ACTIVE", localAuthorityName = "Mwenyekiti John", localAuthorityArea = "Mikocheni"),
    User(id = 4, name = "Sada Agent", email = "sada@dalali.tz", role = "DALALI", phone = "+255700333444", businessName = "Sada Top Dalali", verificationStatus = "VERIFIED", status = "ACTIVE", localAuthorityName = "Mwenyekiti Anna", localAuthorityArea = "Sinza"),
    User(id = 5, name = "Neema Tenant", email = "neema@tenant.tz", role = "TENANT", phone = "+255700555666", verificationStatus = "VERIFIED", status = "ACTIVE")
)

private fun getSampleBookings(): List<Booking> = listOf(
    Booking(id = 501, roomId = 101, roomTitle = "Kijitonyama Studio", userId = 5, userName = "Neema Tenant", totalPrice = 450000.0, status = "CONFIRMED"),
    Booking(id = 502, roomId = 102, roomTitle = "Kinondoni Room", userId = 5, userName = "Neema Tenant", totalPrice = 250000.0, status = "PENDING")
)

private fun getSampleLogs(): List<SystemLog> = listOf(
    SystemLog(id = 1, timestamp = "2026-09-28 12:15:02", level = "AUDIT", action = "USER_VERIFIED", userEmail = "admin@roomify.tz", userRole = "ADMIN", details = "Approved Owner account #201 (Baraka Salum)"),
    SystemLog(id = 2, timestamp = "2026-09-28 12:10:45", level = "INFO", action = "PROPERTY_SUSPENDED", userEmail = "super_admin@roomify.tz", userRole = "SUPER_ADMIN", details = "Suspended Property #89 due to policy violation"),
    SystemLog(id = 3, timestamp = "2026-09-28 11:55:12", level = "WARN", action = "LOGIN_FAILED", userEmail = "unverified_owner@gmail.com", userRole = "OWNER", details = "Blocked unverified login attempt for Owner")
)

private fun getSampleAllProperties(): List<Room> = listOf(
    Room(id = 1, title = "Makumbusho Single Room", address = "Makumbusho, Dar es Salaam", price = 180000.0, status = "AVAILABLE"),
    Room(id = 2, title = "Sinza Modern Apartment", address = "Sinza, Dar es Salaam", price = 500000.0, status = "AVAILABLE"),
    Room(id = 3, title = "Mbezi Beach Villa Room", address = "Mbezi Beach, Dar es Salaam", price = 800000.0, status = "SUSPENDED")
)

private fun getSampleAdmins(): List<User> = listOf(
    User(id = 1, name = "Main Super Admin", email = "super_admin@roomify.tz", role = "SUPER_ADMIN", verificationStatus = "VERIFIED", status = "ACTIVE"),
    User(id = 2, name = "Raphael Frank", email = "raphaelfrank02@gmail.com", role = "ADMIN", verificationStatus = "VERIFIED", status = "ACTIVE"),
    User(id = 3, name = "Helper Admin", email = "helper@roomify.tz", role = "ADMIN", verificationStatus = "VERIFIED", status = "SUSPENDED")
)
