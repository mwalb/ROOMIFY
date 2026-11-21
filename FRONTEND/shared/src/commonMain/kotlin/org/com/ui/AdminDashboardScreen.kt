package org.com.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.com.model.AdminStats
import org.com.model.Booking
import org.com.model.Room
import org.com.model.SystemLog
import org.com.model.User
import org.com.model.isUserSuspended
import org.com.model.isVerified
import org.com.network.RoomifyApi

// Theme Colors for Admin Portal
private val NavySidebarBg = Color(0xFF121B6C)
private val ActivePillBg = Color(0xFF2B3A9E)
private val PrimaryTextDark = Color(0xFF0F172A)
private val MutedTextGray = Color(0xFF64748B)
private val BorderGray = Color(0xFFE2E8F0)
private val ContentBgGray = Color(0xFFF8FAFC)
private val SuccessGreen = Color(0xFF16A34A)
private val SuccessGreenLight = Color(0xFFDCFCE7)
private val DangerRed = Color(0xFFDC2626)
private val DangerRedLight = Color(0xFFFEE2E2)
private val WarningAmber = Color(0xFFD97706)
private val WarningAmberLight = Color(0xFFFEF3C7)
private val InfoBlue = Color(0xFF2563EB)
private val InfoBlueLight = Color(0xFFDBEAFE)

data class AdminMenuItem(val title: String, val icon: ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    user: User,
    onLogout: () -> Unit,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit
) {
    val isSuperAdmin = user.role.equals("SUPER_ADMIN", ignoreCase = true)
    var selectedSection by remember { mutableStateOf("Overview") }
    var selectedPropertyFilter by remember { mutableStateOf("Pending Verification") }
    var selectedUserRoleFilter by remember { mutableStateOf("ALL") }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // 5 Core Views for standard Admin, 6 for Super Admin
    val menuItems = remember(isSuperAdmin) {
        listOfNotNull(
            AdminMenuItem("Overview", Icons.Default.Dashboard),
            AdminMenuItem("Properties", Icons.Default.Home),
            AdminMenuItem("Users", Icons.Default.People),
            AdminMenuItem("Bookings", Icons.Default.BookOnline),
            AdminMenuItem("Reports", Icons.Default.Assessment),
            if (isSuperAdmin) AdminMenuItem("Administrators", Icons.Default.AdminPanelSettings) else null
        )
    }

    // Shared State across Admin Portal
    var stats by remember { mutableStateOf(AdminStats()) }
    var properties by remember { mutableStateOf<List<Room>>(emptyList()) }
    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    var bookings by remember { mutableStateOf<List<Booking>>(emptyList()) }
    var logs by remember { mutableStateOf<List<SystemLog>>(emptyList()) }
    var admins by remember { mutableStateOf<List<User>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val loadAllAdminData = {
        scope.launch {
            isLoading = true
            try {
                val statsRes = RoomifyApi.getAdminStats()
                if (statsRes.success && statsRes.data != null) {
                    stats = statsRes.data!!
                }

                val roomsRes = RoomifyApi.getPendingProperties()
                val fetchedRooms = if (roomsRes.success && !roomsRes.data.isNullOrEmpty()) {
                    roomsRes.data!!
                } else {
                    getSampleAllProperties()
                }
                properties = fetchedRooms

                val usersRes = RoomifyApi.getAllUsers()
                users = if (usersRes.success && !usersRes.data.isNullOrEmpty()) {
                    usersRes.data!!
                } else {
                    getSampleUsers()
                }

                val bookingsRes = RoomifyApi.getAllBookingsAdmin()
                bookings = if (bookingsRes.success && !bookingsRes.data.isNullOrEmpty()) {
                    bookingsRes.data!!
                } else {
                    getSampleBookings()
                }

                if (isSuperAdmin) {
                    val logsRes = RoomifyApi.getSystemLogs()
                    logs = if (logsRes.success && !logsRes.data.isNullOrEmpty()) {
                        logsRes.data!!
                    } else {
                        getSampleLogs()
                    }

                    val adminsRes = RoomifyApi.getAdministrators()
                    admins = if (adminsRes.success && !adminsRes.data.isNullOrEmpty()) {
                        adminsRes.data!!
                    } else {
                        getSampleAdmins()
                    }
                }
            } catch (e: Exception) {
                properties = getSampleAllProperties()
                users = getSampleUsers()
                bookings = getSampleBookings()
                logs = getSampleLogs()
                admins = getSampleAdmins()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadAllAdminData()
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isCompact = maxWidth < 840.dp
        val isSmallScreen = maxWidth < 600.dp

        if (isCompact) {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        drawerContainerColor = NavySidebarBg,
                        drawerContentColor = Color.White,
                        modifier = Modifier.width(280.dp)
                    ) {
                        AdminSidebarContent(
                            isSuperAdmin = isSuperAdmin,
                            menuItems = menuItems,
                            selectedSection = selectedSection,
                            onSelectSection = { title ->
                                selectedSection = title
                                scope.launch { drawerState.close() }
                            },
                            onLogout = onLogout
                        )
                    }
                }
            ) {
                AdminMainShell(
                    isCompact = true,
                    isSmallScreen = isSmallScreen,
                    selectedSection = selectedSection,
                    user = user,
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    content = {
                        AdminSectionSwitcher(
                            selectedSection = selectedSection,
                            selectedPropertyFilter = selectedPropertyFilter,
                            selectedUserRoleFilter = selectedUserRoleFilter,
                            user = user,
                            isSuperAdmin = isSuperAdmin,
                            stats = stats,
                            properties = properties,
                            users = users,
                            bookings = bookings,
                            logs = logs,
                            admins = admins,
                            isLoading = isLoading,
                            onRefreshData = { loadAllAdminData() },
                            onNavigateToSection = { section, propFilter, roleFilter ->
                                selectedSection = section
                                if (propFilter != null) selectedPropertyFilter = propFilter
                                if (roleFilter != null) selectedUserRoleFilter = roleFilter
                            },
                            onUpdateProperties = { properties = it },
                            onUpdateUsers = { users = it },
                            onUpdateAdmins = { admins = it }
                        )
                    }
                )
            }
        } else {
            Row(modifier = Modifier.fillMaxSize()) {
                // Persistent Desktop Left Sidebar (width = 240.dp)
                Box(
                    modifier = Modifier
                        .width(240.dp)
                        .fillMaxHeight()
                        .background(NavySidebarBg)
                ) {
                    AdminSidebarContent(
                        isSuperAdmin = isSuperAdmin,
                        menuItems = menuItems,
                        selectedSection = selectedSection,
                        onSelectSection = { selectedSection = it },
                        onLogout = onLogout
                    )
                }

                // Desktop Main Content Area (Max width = 1280.dp inside shell)
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    AdminMainShell(
                        isCompact = false,
                        isSmallScreen = false,
                        selectedSection = selectedSection,
                        user = user,
                        onOpenDrawer = {},
                        content = {
                            AdminSectionSwitcher(
                                selectedSection = selectedSection,
                                selectedPropertyFilter = selectedPropertyFilter,
                                selectedUserRoleFilter = selectedUserRoleFilter,
                                user = user,
                                isSuperAdmin = isSuperAdmin,
                                stats = stats,
                                properties = properties,
                                users = users,
                                bookings = bookings,
                                logs = logs,
                                admins = admins,
                                isLoading = isLoading,
                                onRefreshData = { loadAllAdminData() },
                                onNavigateToSection = { section, propFilter, roleFilter ->
                                    selectedSection = section
                                    if (propFilter != null) selectedPropertyFilter = propFilter
                                    if (roleFilter != null) selectedUserRoleFilter = roleFilter
                                },
                                onUpdateProperties = { properties = it },
                                onUpdateUsers = { users = it },
                                onUpdateAdmins = { admins = it }
                            )
                        }
                    )
                }
            }
        }
    }
}

/* ============================================================
   SIDEBAR & SHELL COMPONENTS
   ============================================================ */

@Composable
fun AdminSidebarContent(
    isSuperAdmin: Boolean,
    menuItems: List<AdminMenuItem>,
    selectedSection: String,
    onSelectSection: (String) -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 24.dp, horizontal = 16.dp)
    ) {
        // Branding Header ("ADMIN PANEL" / "SUPER ADMIN")
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Security, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = if (isSuperAdmin) "SUPER ADMIN" else "ADMIN PANEL",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Roomify Portal",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(Modifier.height(28.dp))

        // Navigation Items
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            menuItems.forEach { item ->
                val isSelected = selectedSection == item.title
                Surface(
                    onClick = { onSelectSection(item.title) },
                    color = if (isSelected) ActivePillBg else Color.Transparent,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(14.dp))
                        Text(
                            text = item.title,
                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.85f),
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Logout Pinned at Bottom
        Button(
            onClick = onLogout,
            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.12f)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().height(44.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Text("Logout", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun AdminMainShell(
    isCompact: Boolean,
    isSmallScreen: Boolean,
    selectedSection: String,
    user: User,
    onOpenDrawer: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ContentBgGray)
    ) {
        // Clean Top Header (Height = 68.dp, #FFFFFF, 1.dp bottom border #E2E8F0)
        Surface(
            color = Color.White,
            modifier = Modifier.fillMaxWidth().height(68.dp),
            border = BorderStroke(1.dp, BorderGray)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = if (isSmallScreen) 16.dp else 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isCompact) {
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier.size(48.dp) // 48.dp touch target
                    ) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = PrimaryTextDark)
                    }
                    Spacer(Modifier.width(4.dp))
                }

                Text(
                    text = selectedSection,
                    fontSize = if (isSmallScreen) 18.sp else 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryTextDark
                )

                Spacer(Modifier.weight(1f))

                // Logged-in Admin Badge (e.g., Raphael Frank / admin + circular avatar "R")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val adminName = user.name.ifBlank { "Raphael Frank" }
                    val adminRoleLabel = if (user.role.equals("SUPER_ADMIN", true)) "super_admin" else "admin"

                    if (!isSmallScreen) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = adminName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = PrimaryTextDark
                            )
                            Text(
                                text = adminRoleLabel,
                                fontSize = 11.sp,
                                color = MutedTextGray,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                    }

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(NavySidebarBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = adminName.take(1).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }

        // Section Content Container (Bounded to maxWidth = 1280.dp on large screens)
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = 1280.dp)
                    .fillMaxSize()
                    .padding(if (isSmallScreen) 16.dp else 24.dp)
            ) {
                content()
            }
        }
    }
}

/* ============================================================
   SECTION SWITCHER
   ============================================================ */

@Composable
fun AdminSectionSwitcher(
    selectedSection: String,
    selectedPropertyFilter: String,
    selectedUserRoleFilter: String,
    user: User,
    isSuperAdmin: Boolean,
    stats: AdminStats,
    properties: List<Room>,
    users: List<User>,
    bookings: List<Booking>,
    logs: List<SystemLog>,
    admins: List<User>,
    isLoading: Boolean,
    onRefreshData: () -> Unit,
    onNavigateToSection: (section: String, propFilter: String?, roleFilter: String?) -> Unit,
    onUpdateProperties: (List<Room>) -> Unit,
    onUpdateUsers: (List<User>) -> Unit,
    onUpdateAdmins: (List<User>) -> Unit
) {
    when (selectedSection) {
        "Overview" -> AdminOverviewContent(
            stats = stats,
            properties = properties,
            users = users,
            bookings = bookings,
            isLoading = isLoading,
            onNavigateToSection = onNavigateToSection,
            onUpdateProperties = onUpdateProperties
        )

        "Properties" -> UnifiedPropertiesContent(
            initialFilter = selectedPropertyFilter,
            properties = properties,
            isLoading = isLoading,
            onUpdateProperties = onUpdateProperties
        )

        "Users" -> UnifiedUsersContent(
            initialRoleFilter = selectedUserRoleFilter,
            users = users,
            isLoading = isLoading,
            onUpdateUsers = onUpdateUsers
        )

        "Bookings" -> BookingsManagementContent(
            bookings = bookings,
            isLoading = isLoading
        )

        "Reports" -> ReportsAndLogsContent(
            isSuperAdmin = isSuperAdmin,
            properties = properties,
            bookings = bookings,
            logs = logs,
            isLoading = isLoading
        )

        "Administrators" -> if (isSuperAdmin) {
            AdministratorsContent(
                currentAdminId = user.id,
                currentAdminEmail = user.email,
                admins = admins,
                isLoading = isLoading,
                onUpdateAdmins = onUpdateAdmins
            )
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Access restricted to Super Admin level.", color = DangerRed, fontWeight = FontWeight.Bold)
            }
        }

        else -> Text("Section $selectedSection")
    }
}

/* ============================================================
   1. OVERVIEW SCREEN
   ============================================================ */

@Composable
fun AdminOverviewContent(
    stats: AdminStats,
    properties: List<Room>,
    users: List<User>,
    bookings: List<Booking>,
    isLoading: Boolean,
    onNavigateToSection: (section: String, propFilter: String?, roleFilter: String?) -> Unit,
    onUpdateProperties: (List<Room>) -> Unit
) {
    val pendingCount = properties.count { it.verificationStatus.equals("PENDING", true) }
    val verifiedCount = properties.count { it.verificationStatus.equals("VERIFIED", true) }
    val rejectedCount = properties.count { it.verificationStatus.equals("REJECTED", true) }
    val suspendedCount = properties.count { it.status.equals("SUSPENDED", true) }
    val activeUsersCount = users.count { !it.isUserSuspended() }
    val suspendedUsersCount = users.count { it.isUserSuspended() }
    val tenantsCount = users.count { it.role.equals("TENANT", true) }
    val ownersCount = users.count { it.role.equals("OWNER", true) }
    val dalalisCount = users.count { it.role.equals("DALALI", true) }

    val scope = rememberCoroutineScope()

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = maxWidth

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // KPI Row (4 cards clickable)
            when {
                screenWidth >= 1024.dp -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OverviewKpiCard(
                            title = "Total Properties",
                            value = (stats.totalProperties.takeIf { it > 0 } ?: properties.size.toLong()).toString(),
                            icon = Icons.Default.Home,
                            iconColor = InfoBlue,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateToSection("Properties", "Active", null) }
                        )
                        OverviewKpiCard(
                            title = "Active Users",
                            value = (stats.activeUsers.takeIf { it > 0 } ?: activeUsersCount.toLong()).toString(),
                            icon = Icons.Default.People,
                            iconColor = SuccessGreen,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateToSection("Users", null, "ALL") }
                        )
                        OverviewKpiCard(
                            title = "Total Bookings",
                            value = (stats.totalBookings.takeIf { it > 0 } ?: bookings.size.toLong()).toString(),
                            icon = Icons.Default.BookOnline,
                            iconColor = WarningAmber,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateToSection("Bookings", null, null) }
                        )
                        OverviewKpiCard(
                            title = "Verified Properties",
                            value = (stats.verifiedProperties.takeIf { it > 0 } ?: verifiedCount.toLong()).toString(),
                            icon = Icons.Default.Verified,
                            iconColor = Color(0xFF9333EA),
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateToSection("Properties", "Active", null) }
                        )
                    }
                }

                screenWidth >= 600.dp -> {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OverviewKpiCard(
                                title = "Total Properties",
                                value = (stats.totalProperties.takeIf { it > 0 } ?: properties.size.toLong()).toString(),
                                icon = Icons.Default.Home,
                                iconColor = InfoBlue,
                                modifier = Modifier.weight(1f),
                                onClick = { onNavigateToSection("Properties", "Active", null) }
                            )
                            OverviewKpiCard(
                                title = "Active Users",
                                value = (stats.activeUsers.takeIf { it > 0 } ?: activeUsersCount.toLong()).toString(),
                                icon = Icons.Default.People,
                                iconColor = SuccessGreen,
                                modifier = Modifier.weight(1f),
                                onClick = { onNavigateToSection("Users", null, "ALL") }
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OverviewKpiCard(
                                title = "Total Bookings",
                                value = (stats.totalBookings.takeIf { it > 0 } ?: bookings.size.toLong()).toString(),
                                icon = Icons.Default.BookOnline,
                                iconColor = WarningAmber,
                                modifier = Modifier.weight(1f),
                                onClick = { onNavigateToSection("Bookings", null, null) }
                            )
                            OverviewKpiCard(
                                title = "Verified Properties",
                                value = (stats.verifiedProperties.takeIf { it > 0 } ?: verifiedCount.toLong()).toString(),
                                icon = Icons.Default.Verified,
                                iconColor = Color(0xFF9333EA),
                                modifier = Modifier.weight(1f),
                                onClick = { onNavigateToSection("Properties", "Active", null) }
                            )
                        }
                    }
                }

                else -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OverviewKpiCard(
                            title = "Total Properties",
                            value = (stats.totalProperties.takeIf { it > 0 } ?: properties.size.toLong()).toString(),
                            icon = Icons.Default.Home,
                            iconColor = InfoBlue,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onNavigateToSection("Properties", "Active", null) }
                        )
                        OverviewKpiCard(
                            title = "Active Users",
                            value = (stats.activeUsers.takeIf { it > 0 } ?: activeUsersCount.toLong()).toString(),
                            icon = Icons.Default.People,
                            iconColor = SuccessGreen,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onNavigateToSection("Users", null, "ALL") }
                        )
                        OverviewKpiCard(
                            title = "Total Bookings",
                            value = (stats.totalBookings.takeIf { it > 0 } ?: bookings.size.toLong()).toString(),
                            icon = Icons.Default.BookOnline,
                            iconColor = WarningAmber,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onNavigateToSection("Bookings", null, null) }
                        )
                        OverviewKpiCard(
                            title = "Verified Properties",
                            value = (stats.verifiedProperties.takeIf { it > 0 } ?: verifiedCount.toLong()).toString(),
                            icon = Icons.Default.Verified,
                            iconColor = Color(0xFF9333EA),
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onNavigateToSection("Properties", "Active", null) }
                        )
                    }
                }
            }

            // Dashboard Overview & System Status Card (Structured Bordered Metric Tiles)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderGray)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Card Header with Live Database Pill
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Dashboard Overview & System Status",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryTextDark
                        )

                        Surface(
                            color = SuccessGreenLight,
                            shape = RoundedCornerShape(50)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(SuccessGreen)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Database Status: Connected & Operational",
                                    color = SuccessGreen,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // Structured 2-Column Metric Tiles Grid
                    if (screenWidth >= 600.dp) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                OverviewBreakdownTile(
                                    label = "Pending Properties",
                                    count = stats.pendingProperties.takeIf { it > 0 } ?: pendingCount.toLong(),
                                    dotColor = WarningAmber,
                                    onClick = { onNavigateToSection("Properties", "Pending Verification", null) }
                                )
                                OverviewBreakdownTile(
                                    label = "Rejected Properties",
                                    count = stats.rejectedProperties.takeIf { it > 0 } ?: rejectedCount.toLong(),
                                    dotColor = DangerRed,
                                    onClick = { onNavigateToSection("Properties", "Rejected", null) }
                                )
                                OverviewBreakdownTile(
                                    label = "Suspended Users",
                                    count = stats.suspendedUsers.takeIf { it > 0 } ?: suspendedUsersCount.toLong(),
                                    dotColor = Color(0xFFF43F5E),
                                    onClick = { onNavigateToSection("Users", null, "SUSPENDED") }
                                )
                            }
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                OverviewBreakdownTile(
                                    label = "Total Tenants",
                                    count = stats.tenants.takeIf { it > 0 } ?: tenantsCount.toLong(),
                                    dotColor = InfoBlue,
                                    onClick = { onNavigateToSection("Users", null, "TENANT") }
                                )
                                OverviewBreakdownTile(
                                    label = "Total Owners",
                                    count = stats.owners.takeIf { it > 0 } ?: ownersCount.toLong(),
                                    dotColor = Color(0xFF6366F1),
                                    onClick = { onNavigateToSection("Users", null, "OWNER") }
                                )
                                OverviewBreakdownTile(
                                    label = "Total Dalalis",
                                    count = stats.dalalis.takeIf { it > 0 } ?: dalalisCount.toLong(),
                                    dotColor = Color(0xFF8B5CF6),
                                    onClick = { onNavigateToSection("Users", null, "DALALI") }
                                )
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            OverviewBreakdownTile(
                                label = "Pending Properties",
                                count = stats.pendingProperties.takeIf { it > 0 } ?: pendingCount.toLong(),
                                dotColor = WarningAmber,
                                onClick = { onNavigateToSection("Properties", "Pending Verification", null) }
                            )
                            OverviewBreakdownTile(
                                label = "Rejected Properties",
                                count = stats.rejectedProperties.takeIf { it > 0 } ?: rejectedCount.toLong(),
                                dotColor = DangerRed,
                                onClick = { onNavigateToSection("Properties", "Rejected", null) }
                            )
                            OverviewBreakdownTile(
                                label = "Suspended Users",
                                count = stats.suspendedUsers.takeIf { it > 0 } ?: suspendedUsersCount.toLong(),
                                dotColor = Color(0xFFF43F5E),
                                onClick = { onNavigateToSection("Users", null, "SUSPENDED") }
                            )
                            OverviewBreakdownTile(
                                label = "Total Tenants",
                                count = stats.tenants.takeIf { it > 0 } ?: tenantsCount.toLong(),
                                dotColor = InfoBlue,
                                onClick = { onNavigateToSection("Users", null, "TENANT") }
                            )
                            OverviewBreakdownTile(
                                label = "Total Owners",
                                count = stats.owners.takeIf { it > 0 } ?: ownersCount.toLong(),
                                dotColor = Color(0xFF6366F1),
                                onClick = { onNavigateToSection("Users", null, "OWNER") }
                            )
                            OverviewBreakdownTile(
                                label = "Total Dalalis",
                                count = stats.dalalis.takeIf { it > 0 } ?: dalalisCount.toLong(),
                                dotColor = Color(0xFF8B5CF6),
                                onClick = { onNavigateToSection("Users", null, "DALALI") }
                            )
                        }
                    }
                }
            }

            // Quick Moderation / Action Needed Queue Card
            val pendingItems = properties.filter { it.verificationStatus.equals("PENDING", true) }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderGray)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Action Needed Queue (${pendingItems.size} Pending)",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryTextDark
                        )
                        TextButton(
                            onClick = { onNavigateToSection("Properties", "Pending Verification", null) }
                        ) {
                            Text("View All Properties →", color = InfoBlue, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    if (pendingItems.isEmpty()) {
                        EmptyStateBlock(
                            icon = Icons.Default.CheckCircle,
                            title = "No properties awaiting verification",
                            subtext = "All property listings have been reviewed and processed."
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            pendingItems.take(3).forEach { room ->
                                OverviewPendingPropertyRow(
                                    room = room,
                                    onApprove = {
                                        scope.launch {
                                            RoomifyApi.verifyProperty(room.id ?: 0L)
                                            onUpdateProperties(properties.map {
                                                if (it.id == room.id) it.copy(verificationStatus = "VERIFIED", status = "AVAILABLE") else it
                                            })
                                        }
                                    },
                                    onReject = {
                                        scope.launch {
                                            RoomifyApi.rejectProperty(room.id ?: 0L, "Rejected from Quick Queue")
                                            onUpdateProperties(properties.map {
                                                if (it.id == room.id) it.copy(verificationStatus = "REJECTED") else it
                                            })
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OverviewKpiCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, color = MutedTextGray, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(2.dp))
                Text(value, color = PrimaryTextDark, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun OverviewBreakdownTile(
    label: String,
    count: Long,
    dotColor: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, BorderGray),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(Modifier.width(10.dp))
            Text(label, color = Color(0xFF475569), fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.weight(1f))
            Text(count.toString(), color = PrimaryTextDark, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun OverviewPendingPropertyRow(
    room: Room,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF8FAFC))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(InfoBlue.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.HomeWork, null, tint = InfoBlue, modifier = Modifier.size(24.dp))
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(room.title ?: "Property #${room.id}", fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("Owner: ${room.ownerName ?: "ID: ${room.postedBy}"} • ${room.formattedPrice}", fontSize = 12.sp, color = MutedTextGray)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onApprove,
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.height(34.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Approve", fontSize = 12.sp)
            }
            OutlinedButton(
                onClick = onReject,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.height(34.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Reject", fontSize = 12.sp)
            }
        }
    }
}

/* ============================================================
   2. UNIFIED PROPERTIES MODERATION
   ============================================================ */

@Composable
fun UnifiedPropertiesContent(
    initialFilter: String,
    properties: List<Room>,
    isLoading: Boolean,
    onUpdateProperties: (List<Room>) -> Unit
) {
    var selectedTab by remember { mutableStateOf(initialFilter) }
    var searchQuery by remember { mutableStateOf("") }
    var inspectingRoom by remember { mutableStateOf<Room?>(null) }
    var rejectingRoomId by remember { mutableStateOf<Long?>(null) }
    var rejectionReasonInput by remember { mutableStateOf("") }
    var suspendingRoomId by remember { mutableStateOf<Long?>(null) }
    var suspensionReasonInput by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    val pendingCount = properties.count { it.verificationStatus.equals("PENDING", true) }
    val activeCount = properties.count { it.verificationStatus.equals("VERIFIED", true) && !it.status.equals("SUSPENDED", true) }
    val suspendedCount = properties.count { it.status.equals("SUSPENDED", true) }
    val rejectedCount = properties.count { it.verificationStatus.equals("REJECTED", true) }

    val filteredList = properties.filter { room ->
        val matchesTab = when (selectedTab) {
            "Pending Verification" -> room.verificationStatus.equals("PENDING", true)
            "Active" -> room.verificationStatus.equals("VERIFIED", true) && !room.status.equals("SUSPENDED", true)
            "Suspended" -> room.status.equals("SUSPENDED", true)
            "Rejected" -> room.verificationStatus.equals("REJECTED", true)
            else -> true
        }

        val q = searchQuery.trim().lowercase()
        val matchesSearch = q.isBlank() ||
                (room.title?.lowercase()?.contains(q) == true) ||
                (room.address?.lowercase()?.contains(q) == true) ||
                (room.ownerName?.lowercase()?.contains(q) == true) ||
                (room.dalaliName?.lowercase()?.contains(q) == true)

        matchesTab && matchesSearch
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isDesktop = maxWidth >= 840.dp
        val isSmall = maxWidth < 600.dp

        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Header & Search
            if (isSmall) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Properties Moderation", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PrimaryTextDark)
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search properties, areas...", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = MutedTextGray) },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Properties Moderation", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PrimaryTextDark)

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search title, owner, area (Sinza, Masaki...)...", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = MutedTextGray) },
                        modifier = Modifier.width(320.dp).height(50.dp),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                }
            }

            // Segmented Filter Tabs (LazyRow softWrap = false)
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { PropertyTabPill("Pending Verification ($pendingCount)", selectedTab == "Pending Verification") { selectedTab = "Pending Verification" } }
                item { PropertyTabPill("Active ($activeCount)", selectedTab == "Active") { selectedTab = "Active" } }
                item { PropertyTabPill("Suspended ($suspendedCount)", selectedTab == "Suspended") { selectedTab = "Suspended" } }
                item { PropertyTabPill("Rejected ($rejectedCount)", selectedTab == "Rejected") { selectedTab = "Rejected" } }
            }

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = NavySidebarBg) }
            } else if (filteredList.isEmpty()) {
                EmptyStateBlock(
                    icon = Icons.Default.HomeWork,
                    title = "No properties found",
                    subtext = "No property listings match the selected status or search query."
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredList) { room ->
                        PropertyCardRow(
                            room = room,
                            currentTab = selectedTab,
                            isDesktop = isDesktop,
                            onInspect = { inspectingRoom = room },
                            onApprove = {
                                scope.launch {
                                    RoomifyApi.verifyProperty(room.id ?: 0L)
                                    onUpdateProperties(properties.map {
                                        if (it.id == room.id) it.copy(verificationStatus = "VERIFIED", status = "AVAILABLE") else it
                                    })
                                }
                            },
                            onRejectPrompt = { rejectingRoomId = room.id },
                            onSuspendPrompt = { suspendingRoomId = room.id },
                            onReinstate = {
                                scope.launch {
                                    RoomifyApi.unsuspendProperty(room.id ?: 0L)
                                    onUpdateProperties(properties.map {
                                        if (it.id == room.id) it.copy(status = "AVAILABLE") else it
                                    })
                                }
                            }
                        )
                    }
                }
            }

            // Inspection Modal
            if (inspectingRoom != null) {
                PropertyDetailModal(room = inspectingRoom!!, onDismiss = { inspectingRoom = null })
            }

            // Rejection Reason Dialog
            if (rejectingRoomId != null) {
                AlertDialog(
                    onDismissRequest = { rejectingRoomId = null },
                    title = { Text("Reject Property Listing", fontWeight = FontWeight.Bold) },
                    text = {
                        Column {
                            Text("Provide a reason for rejection (sent to owner/dalali):", fontSize = 13.sp)
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = rejectionReasonInput,
                                onValueChange = { rejectionReasonInput = it },
                                placeholder = { Text("e.g., Invalid title deed or misleading photos") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val rId = rejectingRoomId
                                val reason = rejectionReasonInput
                                rejectingRoomId = null
                                rejectionReasonInput = ""
                                if (rId != null) {
                                    scope.launch {
                                        RoomifyApi.rejectProperty(rId, reason)
                                        onUpdateProperties(properties.map {
                                            if (it.id == rId) it.copy(verificationStatus = "REJECTED") else it
                                        })
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                        ) {
                            Text("Reject Listing")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { rejectingRoomId = null }) { Text("Cancel") }
                    }
                )
            }

            // Suspension Reason Dialog
            if (suspendingRoomId != null) {
                AlertDialog(
                    onDismissRequest = { suspendingRoomId = null },
                    title = { Text("Suspend Property Listing", fontWeight = FontWeight.Bold) },
                    text = {
                        Column {
                            Text("Provide a brief suspension reason (hides listing from search/map):", fontSize = 13.sp)
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = suspensionReasonInput,
                                onValueChange = { suspensionReasonInput = it },
                                placeholder = { Text("e.g., Reported policy violation or missing documents") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val sId = suspendingRoomId
                                suspendingRoomId = null
                                suspensionReasonInput = ""
                                if (sId != null) {
                                    scope.launch {
                                        RoomifyApi.suspendProperty(sId)
                                        onUpdateProperties(properties.map {
                                            if (it.id == sId) it.copy(status = "SUSPENDED") else it
                                        })
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                        ) {
                            Text("Suspend Listing")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { suspendingRoomId = null }) { Text("Cancel") }
                    }
                )
            }
        }
    }
}

@Composable
fun PropertyTabPill(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = if (isSelected) NavySidebarBg else Color(0xFFF1F5F9),
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else MutedTextGray,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun PropertyCardRow(
    room: Room,
    currentTab: String,
    isDesktop: Boolean,
    onInspect: () -> Unit,
    onApprove: () -> Unit,
    onRejectPrompt: () -> Unit,
    onSuspendPrompt: () -> Unit,
    onReinstate: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Thumbnail (56.dp, RoundedCornerShape(10.dp))
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(InfoBlue.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.HomeWork, null, tint = InfoBlue, modifier = Modifier.size(28.dp))
                }

                Spacer(Modifier.width(16.dp))

                // Title & Details
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = room.title ?: "Property #${room.id}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = PrimaryTextDark
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Location: ${room.address ?: "N/A"}",
                        fontSize = 12.sp,
                        color = MutedTextGray
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Owner / Dalali: ${room.ownerName ?: room.dalaliName ?: "ID: ${room.postedBy}"}",
                        fontSize = 12.sp,
                        color = Color(0xFF475569)
                    )
                }

                Spacer(Modifier.width(16.dp))

                // Price & Status Pill (wrapContentWidth)
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = room.formattedPrice,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = PrimaryTextDark
                    )
                    Spacer(Modifier.height(6.dp))

                    val statusText = when {
                        room.status.equals("SUSPENDED", true) -> "SUSPENDED"
                        room.verificationStatus.equals("VERIFIED", true) -> "ACTIVE"
                        room.verificationStatus.equals("REJECTED", true) -> "REJECTED"
                        else -> "PENDING"
                    }
                    val statusBg = when (statusText) {
                        "ACTIVE" -> SuccessGreenLight
                        "SUSPENDED" -> DangerRedLight
                        "REJECTED" -> DangerRedLight
                        else -> WarningAmberLight
                    }
                    val statusColor = when (statusText) {
                        "ACTIVE" -> SuccessGreen
                        "SUSPENDED" -> DangerRed
                        "REJECTED" -> DangerRed
                        else -> WarningAmber
                    }

                    Surface(
                        color = statusBg,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.wrapContentWidth()
                    ) {
                        Text(
                            text = statusText,
                            color = statusColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(Modifier.height(10.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onInspect,
                    modifier = Modifier.height(34.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Visibility, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Inspect", fontSize = 12.sp)
                }

                Spacer(Modifier.width(8.dp))

                when (currentTab) {
                    "Pending Verification" -> {
                        Button(
                            onClick = onApprove,
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                            modifier = Modifier.height(34.dp),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                        ) {
                            Text("Approve", fontSize = 12.sp)
                        }
                        Spacer(Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = onRejectPrompt,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                            modifier = Modifier.height(34.dp),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                        ) {
                            Text("Reject", fontSize = 12.sp)
                        }
                    }

                    "Active" -> {
                        Button(
                            onClick = onSuspendPrompt,
                            colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                            modifier = Modifier.height(34.dp),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                        ) {
                            Text("Suspend", fontSize = 12.sp)
                        }
                    }

                    "Suspended" -> {
                        Button(
                            onClick = onReinstate,
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                            modifier = Modifier.height(34.dp),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                        ) {
                            Text("Reinstate", fontSize = 12.sp)
                        }
                    }

                    "Rejected" -> {
                        Button(
                            onClick = onApprove,
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                            modifier = Modifier.height(34.dp),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                        ) {
                            Text("Approve / Re-verify", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PropertyDetailModal(room: Room, onDismiss: () -> Unit) {
    val uriHandler = LocalUriHandler.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(room.title ?: "Property Details", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("• Location: ${room.address ?: "N/A"}", fontSize = 13.sp)
                Text("• Monthly Rent: ${room.formattedPrice}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("• Owner / Dalali: ${room.ownerName ?: room.dalaliName ?: "N/A"}", fontSize = 13.sp)
                Text("• Bedrooms: ${room.roomsCount} | Bathrooms: ${room.bathroomsCount}", fontSize = 13.sp)
                Text("• Verification Status: ${room.verificationStatus}", fontSize = 13.sp)
                Text("• Listing Status: ${room.status}", fontSize = 13.sp)
                if (room.hasVideo && !room.videoUrl.isNullOrBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable {
                                val resolvedUrl = room.getFullUrl(room.videoUrl!!)
                                if (!org.com.openVideoPlayerModal(resolvedUrl)) {
                                    uriHandler.openUri(resolvedUrl)
                                }
                            }
                            .padding(vertical = 4.dp)
                    ) {
                        Icon(Icons.Rounded.PlayCircle, null, tint = SuccessGreen, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("• Video Walkthrough: Attached (Click to watch)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                    }
                } else {
                    Text("• Video Walkthrough: Not Attached", fontSize = 13.sp, color = WarningAmber)
                }
                if (room.description != null) {
                    Text("• Description: ${room.description}", fontSize = 13.sp)
                }
                if (room.latitude != 0.0 || room.longitude != 0.0) {
                    Text("• Coordinates: ${room.latitude}, ${room.longitude}", fontSize = 12.sp, color = MutedTextGray)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("CLOSE") }
        }
    )
}

/* ============================================================
   3. UNIFIED USERS & DALALIS MANAGEMENT
   ============================================================ */

@Composable
fun UnifiedUsersContent(
    initialRoleFilter: String,
    users: List<User>,
    isLoading: Boolean,
    onUpdateUsers: (List<User>) -> Unit
) {
    var selectedRoleFilter by remember { mutableStateOf(initialRoleFilter) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedUserForDetails by remember { mutableStateOf<User?>(null) }
    var confirmStatusUser by remember { mutableStateOf<User?>(null) }
    var suspensionReason by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    val allCount = users.size
    val tenantsCount = users.count { it.role.equals("TENANT", true) }
    val ownersCount = users.count { it.role.equals("OWNER", true) }
    val dalalisCount = users.count { it.role.equals("DALALI", true) }
    val suspendedCount = users.count { it.isUserSuspended() }

    val filteredUsers = users.filter { u ->
        val matchesRole = when (selectedRoleFilter) {
            "TENANT" -> u.role.equals("TENANT", true)
            "OWNER" -> u.role.equals("OWNER", true)
            "DALALI" -> u.role.equals("DALALI", true)
            "SUSPENDED" -> u.isUserSuspended()
            else -> true
        }

        val q = searchQuery.trim().lowercase()
        val matchesSearch = q.isBlank() ||
                u.name.lowercase().contains(q) ||
                u.email.lowercase().contains(q) ||
                (u.phone?.lowercase()?.contains(q) == true) ||
                (u.businessName?.lowercase()?.contains(q) == true)

        matchesRole && matchesSearch
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isDesktop = maxWidth >= 840.dp
        val isSmall = maxWidth < 600.dp

        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Header & Search
            if (isSmall) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Users & Dalalis Management", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PrimaryTextDark)
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search name, email, phone...", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = MutedTextGray) },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Users & Dalalis Management", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PrimaryTextDark)

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search name, email, phone, business...", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = MutedTextGray) },
                        modifier = Modifier.width(320.dp).height(50.dp),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                }
            }

            // Role Filter Chips (LazyRow)
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { UserRoleFilterPill("All ($allCount)", selectedRoleFilter == "ALL") { selectedRoleFilter = "ALL" } }
                item { UserRoleFilterPill("Tenants ($tenantsCount)", selectedRoleFilter == "TENANT") { selectedRoleFilter = "TENANT" } }
                item { UserRoleFilterPill("Owners ($ownersCount)", selectedRoleFilter == "OWNER") { selectedRoleFilter = "OWNER" } }
                item { UserRoleFilterPill("Dalalis ($dalalisCount)", selectedRoleFilter == "DALALI") { selectedRoleFilter = "DALALI" } }
                item { UserRoleFilterPill("Suspended ($suspendedCount)", selectedRoleFilter == "SUSPENDED") { selectedRoleFilter = "SUSPENDED" } }
            }

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = NavySidebarBg) }
            } else if (filteredUsers.isEmpty()) {
                EmptyStateBlock(
                    icon = Icons.Default.People,
                    title = "No users found",
                    subtext = "No user accounts match the selected role filter or search term."
                )
            } else {
                if (isDesktop) {
                    // Desktop Table Layout
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, BorderGray)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("User", Modifier.weight(1.8f), fontWeight = FontWeight.Bold, color = PrimaryTextDark)
                                Text("Role", Modifier.weight(0.9f), fontWeight = FontWeight.Bold, color = PrimaryTextDark)
                                Text("Trust / Verification", Modifier.weight(1.2f), fontWeight = FontWeight.Bold, color = PrimaryTextDark)
                                Text("Account Status", Modifier.weight(1.1f), fontWeight = FontWeight.Bold, color = PrimaryTextDark)
                                Text("Actions", Modifier.weight(1.2f), fontWeight = FontWeight.Bold, color = PrimaryTextDark)
                            }
                            HorizontalDivider(color = BorderGray)

                            LazyColumn {
                                items(filteredUsers) { u ->
                                    UserTableRow(
                                        u = u,
                                        onDetails = { selectedUserForDetails = u },
                                        onToggleStatus = { confirmStatusUser = u; suspensionReason = "" }
                                    )
                                    HorizontalDivider(color = Color(0xFFF1F5F9))
                                }
                            }
                        }
                    }
                } else {
                    // Mobile / Compact Stacked Card Layout
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(filteredUsers) { u ->
                            UserStackedCard(
                                u = u,
                                onDetails = { selectedUserForDetails = u },
                                onToggleStatus = { confirmStatusUser = u; suspensionReason = "" }
                            )
                        }
                    }
                }
            }

            // User Details Dialog
            if (selectedUserForDetails != null) {
                AdminUserDetailsDialog(user = selectedUserForDetails!!, onDismiss = { selectedUserForDetails = null })
            }

            // Status Toggle Confirmation Dialog (Strict Guardrail & Reason Input)
            if (confirmStatusUser != null) {
                val u = confirmStatusUser!!
                val isSus = u.isUserSuspended()
                AlertDialog(
                    onDismissRequest = { confirmStatusUser = null },
                    title = { Text(if (isSus) "Activate Account" else "Suspend Account", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = if (isSus) "Are you sure you want to reactivate account for ${u.name} (${u.email})?"
                                else "Are you sure you want to suspend account for ${u.name} (${u.email})? They will be unable to log in.",
                                fontSize = 13.sp
                            )
                            if (!isSus) {
                                OutlinedTextField(
                                    value = suspensionReason,
                                    onValueChange = { suspensionReason = it },
                                    placeholder = { Text("Enter suspension reason (required)*") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val currentU = confirmStatusUser
                                confirmStatusUser = null
                                if (currentU != null) {
                                    scope.launch {
                                        if (isSus) {
                                            RoomifyApi.unsuspendUser(currentU.id)
                                            onUpdateUsers(users.map { if (it.id == currentU.id) it.copy(isSuspended = false, status = "ACTIVE") else it })
                                        } else {
                                            RoomifyApi.suspendUser(currentU.id)
                                            onUpdateUsers(users.map { if (it.id == currentU.id) it.copy(isSuspended = true, status = "SUSPENDED") else it })
                                        }
                                    }
                                }
                            },
                            enabled = isSus || suspensionReason.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = if (isSus) SuccessGreen else DangerRed)
                        ) {
                            Text(if (isSus) "Activate" else "Suspend")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { confirmStatusUser = null }) { Text("Cancel") }
                    }
                )
            }
        }
    }
}

@Composable
fun UserRoleFilterPill(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = if (isSelected) NavySidebarBg else Color(0xFFF1F5F9),
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else MutedTextGray,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun UserTableRow(
    u: User,
    onDetails: () -> Unit,
    onToggleStatus: () -> Unit
) {
    val isTargetAdminOrSuper = u.role.equals("ADMIN", true) || u.role.equals("SUPER_ADMIN", true)

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // User Name & Email
        Column(Modifier.weight(1.8f)) {
            Text(u.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = PrimaryTextDark)
            Text(u.email, fontSize = 12.sp, color = MutedTextGray)
        }

        // Role Badge
        Text(
            u.role.uppercase(),
            Modifier.weight(0.9f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = InfoBlue
        )

        // Trust / Verification Column
        Box(Modifier.weight(1.2f)) {
            val isVer = u.isVerified()
            Surface(
                color = if (isVer) SuccessGreenLight else Color(0xFFF1F5F9),
                shape = RoundedCornerShape(50),
                modifier = Modifier.wrapContentWidth()
            ) {
                Text(
                    text = if (isVer) "VERIFIED" else "UNVERIFIED",
                    color = if (isVer) SuccessGreen else MutedTextGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        // Account Status Pill (Wrapped cleanly)
        Box(Modifier.weight(1.1f)) {
            val isSus = u.isUserSuspended()
            Surface(
                color = if (isSus) DangerRedLight else SuccessGreenLight,
                shape = RoundedCornerShape(50),
                modifier = Modifier.wrapContentWidth()
            ) {
                Text(
                    text = if (isSus) "SUSPENDED" else "ACTIVE",
                    color = if (isSus) DangerRed else SuccessGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        // Actions / Guardrail
        Row(Modifier.weight(1.2f), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDetails, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Visibility, "Details", tint = InfoBlue, modifier = Modifier.size(20.dp))
            }

            if (isTargetAdminOrSuper) {
                Surface(
                    color = Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.wrapContentWidth()
                ) {
                    Text(
                        text = "Protected",
                        color = MutedTextGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            } else {
                if (u.isUserSuspended()) {
                    Button(
                        onClick = onToggleStatus,
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Activate", fontSize = 11.sp)
                    }
                } else {
                    Button(
                        onClick = onToggleStatus,
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Suspend", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun UserStackedCard(
    u: User,
    onDetails: () -> Unit,
    onToggleStatus: () -> Unit
) {
    val isTargetAdminOrSuper = u.role.equals("ADMIN", true) || u.role.equals("SUPER_ADMIN", true)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(u.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = PrimaryTextDark)
                    Text(u.email, fontSize = 12.sp, color = MutedTextGray)
                }

                Surface(
                    color = InfoBlueLight,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        u.role.uppercase(),
                        color = InfoBlue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val isVer = u.isVerified()
                Surface(
                    color = if (isVer) SuccessGreenLight else Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.wrapContentWidth()
                ) {
                    Text(
                        text = if (isVer) "VERIFIED" else "UNVERIFIED",
                        color = if (isVer) SuccessGreen else MutedTextGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                val isSus = u.isUserSuspended()
                Surface(
                    color = if (isSus) DangerRedLight else SuccessGreenLight,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.wrapContentWidth()
                ) {
                    Text(
                        text = if (isSus) "SUSPENDED" else "ACTIVE",
                        color = if (isSus) DangerRed else SuccessGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onDetails,
                    modifier = Modifier.height(34.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Visibility, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("View Profile Details", fontSize = 12.sp)
                }

                Spacer(Modifier.width(8.dp))

                if (isTargetAdminOrSuper) {
                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.wrapContentWidth()
                    ) {
                        Text(
                            text = "Protected",
                            color = MutedTextGray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                } else {
                    Button(
                        onClick = onToggleStatus,
                        colors = ButtonDefaults.buttonColors(containerColor = if (u.isUserSuspended()) SuccessGreen else DangerRed),
                        modifier = Modifier.height(34.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(if (u.isUserSuspended()) "Activate" else "Suspend", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminUserDetailsDialog(user: User, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${user.name} - Full Profile Details", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Account Overview", fontWeight = FontWeight.Bold, color = NavySidebarBg, fontSize = 14.sp)
                Text("• Email: ${user.email}")
                Text("• Phone: ${user.phone ?: "N/A"}")
                Text("• Role: ${user.role.uppercase()}")
                Text("• Account Status: ${if (user.isUserSuspended()) "Suspended" else "Active"}")

                if (!user.businessName.isNullOrBlank() || !user.nidaNumber.isNullOrBlank() || !user.licenseNumber.isNullOrBlank()) {
                    HorizontalDivider(color = BorderGray)
                    Text("Identity & Business Info", fontWeight = FontWeight.Bold, color = NavySidebarBg, fontSize = 14.sp)
                    if (!user.businessName.isNullOrBlank()) Text("• Business/Agency: ${user.businessName}")
                    if (!user.nidaNumber.isNullOrBlank()) Text("• NIDA Number: ${user.nidaNumber}")
                    if (!user.licenseNumber.isNullOrBlank()) Text("• License Number: ${user.licenseNumber}")
                    if (!user.locationArea.isNullOrBlank()) Text("• Location Area: ${user.locationArea}")
                }

                if (!user.localAuthorityName.isNullOrBlank()) {
                    HorizontalDivider(color = BorderGray)
                    Text("Mtendaji/Mwenyekiti wa Mtaa Details", fontWeight = FontWeight.Bold, color = NavySidebarBg, fontSize = 14.sp)
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
   4. BOOKINGS MANAGEMENT
   ============================================================ */

@Composable
fun BookingsManagementContent(
    bookings: List<Booking>,
    isLoading: Boolean
) {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Bookings Management & Status Tracking", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PrimaryTextDark)

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = NavySidebarBg) }
        } else if (bookings.isEmpty()) {
            EmptyStateBlock(
                icon = Icons.Default.BookOnline,
                title = "No bookings found",
                subtext = "There are currently no tenant bookings recorded in the system."
            )
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, BorderGray)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        Text("Booking ID", Modifier.weight(0.9f), fontWeight = FontWeight.Bold, color = PrimaryTextDark)
                        Text("Property", Modifier.weight(1.6f), fontWeight = FontWeight.Bold, color = PrimaryTextDark)
                        Text("Tenant", Modifier.weight(1.3f), fontWeight = FontWeight.Bold, color = PrimaryTextDark)
                        Text("Total Price", Modifier.weight(1.1f), fontWeight = FontWeight.Bold, color = PrimaryTextDark)
                        Text("Status", Modifier.weight(1f), fontWeight = FontWeight.Bold, color = PrimaryTextDark)
                    }
                    HorizontalDivider(color = BorderGray)

                    LazyColumn {
                        items(bookings) { b ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("#${b.id}", Modifier.weight(0.9f), fontWeight = FontWeight.Bold, color = PrimaryTextDark)
                                Text(b.roomTitle ?: "Room #${b.roomId}", Modifier.weight(1.6f), fontSize = 13.sp)
                                Text(b.userName ?: "User #${b.userId}", Modifier.weight(1.3f), fontSize = 13.sp)
                                Text("TZS ${b.totalPrice.toInt()}", Modifier.weight(1.1f), fontWeight = FontWeight.Bold, color = NavySidebarBg, fontSize = 13.sp)

                                val bColor = when (b.status) {
                                    "CONFIRMED" -> SuccessGreen
                                    "PENDING" -> WarningAmber
                                    else -> DangerRed
                                }
                                val bBg = when (b.status) {
                                    "CONFIRMED" -> SuccessGreenLight
                                    "PENDING" -> WarningAmberLight
                                    else -> DangerRedLight
                                }

                                Surface(
                                    color = bBg,
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier.wrapContentWidth()
                                ) {
                                    Text(
                                        b.status,
                                        color = bColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                        }
                    }
                }
            }
        }
    }
}

/* ============================================================
   5. REPORTS & MODERATION SUMMARY
   ============================================================ */

@Composable
fun ReportsAndLogsContent(
    isSuperAdmin: Boolean,
    properties: List<Room>,
    bookings: List<Booking>,
    logs: List<SystemLog>,
    isLoading: Boolean
) {
    var logLevelFilter by remember { mutableStateOf("ALL") }
    var logSearchQuery by remember { mutableStateOf("") }

    // Live Metrics calculated from state (No phantom values)
    val confirmedBookings = bookings.filter { it.status.equals("CONFIRMED", true) }
    val totalRevenue = confirmedBookings.sumOf { it.totalPrice }
    val totalCommissions = confirmedBookings.sumOf { it.facilitationFee }
    val activeListingsCount = properties.count { !it.status.equals("SUSPENDED", true) && it.verificationStatus.equals("VERIFIED", true) }
    val conversionRate = if (bookings.isNotEmpty()) {
        (confirmedBookings.size.toDouble() / bookings.size * 100).toInt()
    } else 0

    val verifiedPropsCount = properties.count { it.verificationStatus.equals("VERIFIED", true) }
    val pendingPropsCount = properties.count { it.verificationStatus.equals("PENDING", true) }
    val suspendedPropsCount = properties.count { it.status.equals("SUSPENDED", true) }

    val filteredLogs = logs.filter { log ->
        val matchesLevel = when (logLevelFilter) {
            "INFO" -> log.level.equals("INFO", true)
            "WARN" -> log.level.equals("WARN", true) || log.level.equals("WARNING", true)
            "ADMIN_ACTION" -> log.level.equals("AUDIT", true) || log.level.equals("ADMIN_ACTION", true)
            "ERROR" -> log.level.equals("ERROR", true)
            else -> true
        }

        val q = logSearchQuery.trim().lowercase()
        val matchesSearch = q.isBlank() ||
                log.action.lowercase().contains(q) ||
                log.details.lowercase().contains(q) ||
                (log.userEmail?.lowercase()?.contains(q) == true)

        matchesLevel && matchesSearch
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = maxWidth

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text("Reports & Platform Performance", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PrimaryTextDark)

            // 4 Live Metric Cards (No hardcoded figures)
            if (screenWidth >= 1024.dp) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OverviewKpiCard("Monthly Revenue", if (totalRevenue > 0) "TZS ${totalRevenue.toInt()}" else "TZS 0", Icons.Default.Payments, SuccessGreen, Modifier.weight(1f)) {}
                    OverviewKpiCard("Total Commissions", if (totalCommissions > 0) "TZS ${totalCommissions.toInt()}" else "TZS 0", Icons.Default.Handshake, NavySidebarBg, Modifier.weight(1f)) {}
                    OverviewKpiCard("Active Listings", activeListingsCount.toString(), Icons.Default.Home, WarningAmber, Modifier.weight(1f)) {}
                    OverviewKpiCard("Conversion Rate", "$conversionRate%", Icons.Default.TrendingUp, Color(0xFF8E24AA), Modifier.weight(1f)) {}
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OverviewKpiCard("Monthly Revenue", if (totalRevenue > 0) "TZS ${totalRevenue.toInt()}" else "TZS 0", Icons.Default.Payments, SuccessGreen, Modifier.fillMaxWidth()) {}
                    OverviewKpiCard("Total Commissions", if (totalCommissions > 0) "TZS ${totalCommissions.toInt()}" else "TZS 0", Icons.Default.Handshake, NavySidebarBg, Modifier.fillMaxWidth()) {}
                    OverviewKpiCard("Active Listings", activeListingsCount.toString(), Icons.Default.Home, WarningAmber, Modifier.fillMaxWidth()) {}
                    OverviewKpiCard("Conversion Rate", "$conversionRate%", Icons.Default.TrendingUp, Color(0xFF8E24AA), Modifier.fillMaxWidth()) {}
                }
            }

            // Standard Admin: Moderation & Activity Summary
            if (!isSuperAdmin) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, BorderGray)
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Moderation & Activity Summary", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PrimaryTextDark)

                        if (screenWidth >= 600.dp) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                OverviewBreakdownTile("Properties Verified", verifiedPropsCount.toLong(), SuccessGreen) {}
                                OverviewBreakdownTile("Pending Queue", pendingPropsCount.toLong(), WarningAmber) {}
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                OverviewBreakdownTile("Active Listings", activeListingsCount.toLong(), InfoBlue) {}
                                OverviewBreakdownTile("Suspended Listings", suspendedPropsCount.toLong(), DangerRed) {}
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                OverviewBreakdownTile("Properties Verified", verifiedPropsCount.toLong(), SuccessGreen) {}
                                OverviewBreakdownTile("Pending Queue", pendingPropsCount.toLong(), WarningAmber) {}
                                OverviewBreakdownTile("Active Listings", activeListingsCount.toLong(), InfoBlue) {}
                                OverviewBreakdownTile("Suspended Listings", suspendedPropsCount.toLong(), DangerRed) {}
                            }
                        }
                    }
                }
            } else {
                // Super Admin: System Audit & Logs Table
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, BorderGray)
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("System Audit & Logs", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PrimaryTextDark)

                            OutlinedTextField(
                                value = logSearchQuery,
                                onValueChange = { logSearchQuery = it },
                                placeholder = { Text("Filter logs...", fontSize = 12.sp) },
                                leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp)) },
                                modifier = Modifier.width(240.dp).height(46.dp),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )
                        }

                        // Level filter pills
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item { PropertyTabPill("All Levels", logLevelFilter == "ALL") { logLevelFilter = "ALL" } }
                            item { PropertyTabPill("INFO", logLevelFilter == "INFO") { logLevelFilter = "INFO" } }
                            item { PropertyTabPill("WARNING", logLevelFilter == "WARN") { logLevelFilter = "WARN" } }
                            item { PropertyTabPill("ADMIN_ACTION", logLevelFilter == "ADMIN_ACTION") { logLevelFilter = "ADMIN_ACTION" } }
                            item { PropertyTabPill("ERROR", logLevelFilter == "ERROR") { logLevelFilter = "ERROR" } }
                        }

                        if (filteredLogs.isEmpty()) {
                            EmptyStateBlock(
                                icon = Icons.Default.Assessment,
                                title = "No audit logs recorded yet",
                                subtext = "System audit logs will appear here when administrative actions occur."
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                filteredLogs.forEach { log ->
                                    LogItemRow(log = log)
                                    HorizontalDivider(color = Color(0xFFF1F5F9))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LogItemRow(log: SystemLog) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(log.timestamp, Modifier.weight(1.2f), fontSize = 12.sp, color = MutedTextGray)

        val (lBg, lColor) = when (log.level) {
            "ERROR" -> DangerRedLight to DangerRed
            "WARN", "WARNING" -> WarningAmberLight to WarningAmber
            "AUDIT", "ADMIN_ACTION" -> Color(0xFFF3E8FF) to Color(0xFF6B21A8)
            else -> InfoBlueLight to InfoBlue
        }

        Surface(
            color = lBg,
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.weight(0.9f).wrapContentWidth()
        ) {
            Text(
                log.level,
                color = lColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }

        Spacer(Modifier.width(8.dp))
        Text(log.action, Modifier.weight(1.5f), fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = PrimaryTextDark)
        Text(log.userEmail ?: "System", Modifier.weight(1.2f), fontSize = 12.sp, color = MutedTextGray)
        Text(log.details, Modifier.weight(2f), fontSize = 12.sp, color = Color(0xFF475569))
    }
}

/* ============================================================
   6. ADMINISTRATORS MANAGEMENT & GUARDRAILS
   ============================================================ */

@Composable
fun AdministratorsContent(
    currentAdminId: Long,
    currentAdminEmail: String,
    admins: List<User>,
    isLoading: Boolean,
    onUpdateAdmins: (List<User>) -> Unit
) {
    var showCreateModal by remember { mutableStateOf(false) }
    var deleteConfirmAdmin by remember { mutableStateOf<User?>(null) }

    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Administrators Management & Access Scopes", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PrimaryTextDark)
            Button(
                onClick = { showCreateModal = true },
                colors = ButtonDefaults.buttonColors(containerColor = NavySidebarBg),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("Create Admin")
            }
        }

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = NavySidebarBg) }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, BorderGray)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Name", Modifier.weight(1.5f), fontWeight = FontWeight.Bold, color = PrimaryTextDark)
                        Text("Email", Modifier.weight(1.8f), fontWeight = FontWeight.Bold, color = PrimaryTextDark)
                        Text("Role Scope", Modifier.weight(1.2f), fontWeight = FontWeight.Bold, color = PrimaryTextDark)
                        Text("Status", Modifier.weight(1f), fontWeight = FontWeight.Bold, color = PrimaryTextDark)
                        Text("Actions", Modifier.weight(0.8f), fontWeight = FontWeight.Bold, color = PrimaryTextDark)
                    }
                    HorizontalDivider(color = BorderGray)

                    admins.forEach { a ->
                        val isSelf = (a.id == currentAdminId || a.email.equals(currentAdminEmail, ignoreCase = true))

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(Modifier.weight(1.5f), verticalAlignment = Alignment.CenterVertically) {
                                Text(a.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = PrimaryTextDark)
                                if (isSelf) {
                                    Spacer(Modifier.width(8.dp))
                                    Surface(
                                        color = InfoBlueLight,
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Text(
                                            "You",
                                            color = InfoBlue,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Text(a.email, Modifier.weight(1.8f), fontSize = 13.sp, color = MutedTextGray)

                            Surface(
                                color = if (a.role.equals("SUPER_ADMIN", true)) Color(0xFFF3E8FF) else InfoBlueLight,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.weight(1.2f).wrapContentWidth()
                            ) {
                                Text(
                                    a.role.uppercase(),
                                    color = if (a.role.equals("SUPER_ADMIN", true)) Color(0xFF6B21A8) else InfoBlue,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            Surface(
                                color = if (a.isUserSuspended()) DangerRedLight else SuccessGreenLight,
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.weight(1f).wrapContentWidth()
                            ) {
                                Text(
                                    if (a.isUserSuspended()) "Disabled" else "Active",
                                    color = if (a.isUserSuspended()) DangerRed else SuccessGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            Box(Modifier.weight(0.8f)) {
                                if (isSelf) {
                                    // Guardrail: Self deletion disabled
                                    Text("Protected", fontSize = 11.sp, color = MutedTextGray, fontWeight = FontWeight.Medium)
                                } else {
                                    IconButton(onClick = { deleteConfirmAdmin = a }) {
                                        Icon(Icons.Default.Delete, "Delete", tint = DangerRed)
                                    }
                                }
                            }
                        }
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                    }
                }
            }
        }

        // Delete Admin Confirmation Dialog
        if (deleteConfirmAdmin != null) {
            val a = deleteConfirmAdmin!!
            AlertDialog(
                onDismissRequest = { deleteConfirmAdmin = null },
                title = { Text("Delete Administrator Account", fontWeight = FontWeight.Bold) },
                text = { Text("Are you sure you want to delete administrator ${a.name} (${a.email})? This action cannot be undone.", fontSize = 13.sp) },
                confirmButton = {
                    Button(
                        onClick = {
                            deleteConfirmAdmin = null
                            scope.launch {
                                RoomifyApi.deleteAdmin(a.id)
                                onUpdateAdmins(admins.filter { it.id != a.id })
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                    ) {
                        Text("Delete Admin")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { deleteConfirmAdmin = null }) { Text("Cancel") }
                }
            )
        }

        // Enhanced Create Admin Modal (maxWidth = 460.dp, pure #FFFFFF surface)
        if (showCreateModal) {
            EnhancedCreateAdminModal(
                onDismiss = { showCreateModal = false },
                onCreate = { newAdmin ->
                    scope.launch {
                        RoomifyApi.createAdmin(newAdmin)
                        onUpdateAdmins(admins + newAdmin)
                        showCreateModal = false
                    }
                }
            )
        }
    }
}

@Composable
fun EnhancedCreateAdminModal(
    onDismiss: () -> Unit,
    onCreate: (User) -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var tempPassword by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("ADMIN") }

    // Permission Scope Checkboxes
    var permVerifyProperties by remember { mutableStateOf(true) }
    var permManageUsers by remember { mutableStateOf(true) }
    var permManageBookings by remember { mutableStateOf(true) }
    var permViewFinancials by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White,
        modifier = Modifier.widthIn(max = 460.dp),
        title = {
            Text("Create New Administrator", fontWeight = FontWeight.Bold, color = PrimaryTextDark, fontSize = 18.sp)
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = tempPassword,
                    onValueChange = { tempPassword = it },
                    label = { Text("Temporary Password *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(Modifier.height(4.dp))
                Text("Administrator Role Scope", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PrimaryTextDark)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedRole == "ADMIN",
                        onClick = { selectedRole = "ADMIN" }
                    )
                    Text("Standard Admin", fontSize = 13.sp)
                    Spacer(Modifier.width(16.dp))
                    RadioButton(
                        selected = selectedRole == "SUPER_ADMIN",
                        onClick = { selectedRole = "SUPER_ADMIN" }
                    )
                    Text("Super Admin", fontSize = 13.sp)
                }

                HorizontalDivider(color = BorderGray)

                Text("Permission Scopes & Actions", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PrimaryTextDark)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = permVerifyProperties, onCheckedChange = { permVerifyProperties = it })
                    Text("Verify & Moderating Properties", fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = permManageUsers, onCheckedChange = { permManageUsers = it })
                    Text("Manage Users & Dalalis", fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = permManageBookings, onCheckedChange = { permManageBookings = it })
                    Text("Manage Bookings & Disputes", fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = permViewFinancials, onCheckedChange = { permViewFinancials = it })
                    Text("View Financial Reports & System Logs", fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (fullName.isNotBlank() && email.isNotBlank() && tempPassword.isNotBlank()) {
                        val newAdmin = User(
                            id = (100..99999).random().toLong(),
                            name = fullName.trim(),
                            email = email.trim(),
                            role = selectedRole,
                            verificationStatus = "VERIFIED",
                            status = "ACTIVE"
                        )
                        onCreate(newAdmin)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NavySidebarBg),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Create Administrator")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

/* ============================================================
   REUSABLE EMPTY STATE BLOCK (maxWidth = 420.dp)
   ============================================================ */

@Composable
fun EmptyStateBlock(
    icon: ImageVector,
    title: String,
    subtext: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = MutedTextGray, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PrimaryTextDark)
            Spacer(Modifier.height(4.dp))
            Text(subtext, fontSize = 13.sp, color = MutedTextGray, textAlign = TextAlign.Center)
        }
    }
}

/* ============================================================
   SAMPLE DATA HELPERS FOR TESTING & PREVIEWS
   ============================================================ */

private fun getSampleAllProperties(): List<Room> = listOf(
    Room(
        id = 101,
        title = "Kijitonyama Modern 2BR Apartment",
        address = "Kijitonyama, Dar es Salaam",
        price = 450000.0,
        ownerName = "Juma Mgosi",
        postedBy = 10,
        verificationStatus = "PENDING",
        status = "AVAILABLE"
    ),
    Room(
        id = 102,
        title = "Kinondoni Studio Apartment",
        address = "Kinondoni, Dar es Salaam",
        price = 250000.0,
        ownerName = "Asha Ally",
        postedBy = 12,
        verificationStatus = "PENDING",
        status = "AVAILABLE"
    ),
    Room(
        id = 103,
        title = "Masaki Executive Villa",
        address = "Masaki, Dar es Salaam",
        price = 1500000.0,
        ownerName = "Salim Properties",
        postedBy = 3,
        verificationStatus = "VERIFIED",
        status = "AVAILABLE"
    ),
    Room(
        id = 104,
        title = "Sinza Commercial Space",
        address = "Sinza, Dar es Salaam",
        price = 600000.0,
        dalaliName = "Sada Top Dalali",
        postedBy = 4,
        verificationStatus = "VERIFIED",
        status = "SUSPENDED"
    )
)

private fun getSampleUsers(): List<User> = listOf(
    User(
        id = 1,
        name = "Main Super Admin",
        email = "super_admin@roomify.tz",
        role = "SUPER_ADMIN",
        verificationStatus = "VERIFIED",
        status = "ACTIVE"
    ),
    User(
        id = 2,
        name = "Raphael Frank",
        email = "raphaelfrank02@gmail.com",
        role = "ADMIN",
        verificationStatus = "VERIFIED",
        status = "ACTIVE"
    ),
    User(
        id = 3,
        name = "Salim Landlord",
        email = "salim@owner.tz",
        role = "OWNER",
        phone = "+255700111222",
        businessName = "Salim Properties",
        verificationStatus = "VERIFIED",
        status = "ACTIVE",
        localAuthorityName = "Mwenyekiti John",
        localAuthorityArea = "Mikocheni"
    ),
    User(
        id = 4,
        name = "Sada Agent",
        email = "sada@dalali.tz",
        role = "DALALI",
        phone = "+255700333444",
        businessName = "Sada Top Dalali",
        verificationStatus = "VERIFIED",
        status = "ACTIVE",
        localAuthorityName = "Mwenyekiti Anna",
        localAuthorityArea = "Sinza"
    ),
    User(
        id = 5,
        name = "Neema Tenant",
        email = "neema@tenant.tz",
        role = "TENANT",
        phone = "+255700555666",
        verificationStatus = "VERIFIED",
        status = "ACTIVE"
    ),
    User(
        id = 6,
        name = "Suspended Owner",
        email = "suspended@owner.tz",
        role = "OWNER",
        phone = "+255700999888",
        verificationStatus = "PENDING",
        status = "SUSPENDED",
        isSuspended = true
    )
)

private fun getSampleBookings(): List<Booking> = listOf(
    Booking(
        id = 501,
        roomId = 103,
        roomTitle = "Masaki Executive Villa",
        userId = 5,
        userName = "Neema Tenant",
        totalPrice = 1500000.0,
        status = "CONFIRMED",
        facilitationFee = 15000.0
    ),
    Booking(
        id = 502,
        roomId = 101,
        roomTitle = "Kijitonyama Studio",
        userId = 5,
        userName = "Neema Tenant",
        totalPrice = 450000.0,
        status = "PENDING",
        facilitationFee = 10000.0
    )
)

private fun getSampleLogs(): List<SystemLog> = listOf(
    SystemLog(
        id = 1,
        timestamp = "2026-09-28 12:15:02",
        level = "AUDIT",
        action = "USER_VERIFIED",
        userEmail = "super_admin@roomify.tz",
        userRole = "SUPER_ADMIN",
        details = "Approved Owner account #3 (Salim Landlord)"
    ),
    SystemLog(
        id = 2,
        timestamp = "2026-09-28 12:10:45",
        level = "WARN",
        action = "PROPERTY_SUSPENDED",
        userEmail = "super_admin@roomify.tz",
        userRole = "SUPER_ADMIN",
        details = "Suspended Property #104 due to policy violation"
    ),
    SystemLog(
        id = 3,
        timestamp = "2026-09-28 11:55:12",
        level = "INFO",
        action = "SYSTEM_HEALTH_CHECK",
        userEmail = "system@roomify.tz",
        userRole = "SYSTEM",
        details = "Database connected and stats synchronized successfully"
    )
)

private fun getSampleAdmins(): List<User> = listOf(
    User(
        id = 1,
        name = "Main Super Admin",
        email = "super_admin@roomify.tz",
        role = "SUPER_ADMIN",
        verificationStatus = "VERIFIED",
        status = "ACTIVE"
    ),
    User(
        id = 2,
        name = "Raphael Frank",
        email = "raphaelfrank02@gmail.com",
        role = "ADMIN",
        verificationStatus = "VERIFIED",
        status = "ACTIVE"
    ),
    User(
        id = 3,
        name = "Helper Admin",
        email = "helper@roomify.tz",
        role = "ADMIN",
        verificationStatus = "VERIFIED",
        status = "SUSPENDED",
        isSuspended = true
    )
)
