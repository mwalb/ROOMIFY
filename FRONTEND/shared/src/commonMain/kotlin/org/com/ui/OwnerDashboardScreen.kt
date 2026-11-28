package org.com.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import org.com.i18n.LocalRoomifyStrings
import org.com.model.Room
import org.com.model.Booking
import org.com.model.Conversation
import org.com.model.User
import org.com.network.ApiClient
import org.com.ui.components.roomifyAmbientBackground
import org.com.ui.theme.RoomifyColors

private val PrimaryColor = Color(0xFF142170)
private val PrimaryLight = Color(0xFF2B3A9E)
private val SuccessColor = Color(0xFF2E7D32)
private val CardBorderColor = Color(0xFFE2E8F0)
private val SubtitleColor = Color(0xFF64748B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerDashboardScreen(
    user: User,
    properties: List<Room>,
    bookings: List<Booking> = emptyList(),
    conversations: List<Conversation> = emptyList(),
    isRefreshing: Boolean = false,
    initialTab: Int = 0,
    onAddProperty: () -> Unit,
    onLogout: () -> Unit,
    onViewProperty: (Room) -> Unit,
    onViewAnalytics: () -> Unit = {},
    onViewFinances: () -> Unit = {},
    onAcceptBooking: (Booking) -> Unit = {},
    onRejectBooking: (Booking) -> Unit = {},
    onConversationClick: (Conversation) -> Unit = {},
    onSearch: (type: String?, area: String?, maxPrice: Double?, status: String?) -> Unit = { _, _, _, _ -> },
    onNavigate: (String) -> Unit = {},
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(initialTab) }
    var showMoreSheet by remember { mutableStateOf(false) }

    var finances by remember { mutableStateOf<org.com.model.FinancesSummary?>(null) }
    LaunchedEffect(user.id) {
        if (user.id > 0) {
            try {
                val res = org.com.network.RoomifyApi.getOwnerFinances(user.id)
                if (res.success && res.data != null) {
                    finances = res.data
                }
            } catch (e: Exception) {
                println("OwnerDashboard: Failed to load finances: ${e.message}")
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .roomifyAmbientBackground()
    ) {
        val isExpanded = maxWidth >= 840.dp
        val isMedium = maxWidth >= 600.dp && maxWidth < 840.dp
        val isCompact = maxWidth < 600.dp

        Row(modifier = Modifier.fillMaxSize()) {
            // Persistent Left Navy Sidebar on Expanded (maxWidth >= 840.dp)
            if (isExpanded) {
                Surface(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(244.dp),
                    color = PrimaryColor
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            // Brand Header
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 28.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(Color.White.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Business, null, tint = Color.White, modifier = Modifier.size(22.dp))
                                }
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text("ROOMIFY", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                                    Text("Owner Portal", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // 6 Navigation Items
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                OwnerSidebarNavItem("Dashboard", Icons.Default.Dashboard, selectedTab == 0) { selectedTab = 0 }
                                OwnerSidebarNavItem("Properties", Icons.Default.Home, selectedTab == 1) { selectedTab = 1 }
                                OwnerSidebarNavItem("Requests", Icons.Default.Bookmark, selectedTab == 2) { selectedTab = 2 }
                                OwnerSidebarNavItem("Messages", Icons.Default.Message, selectedTab == 3) { selectedTab = 3 }
                                OwnerSidebarNavItem("Analytics", Icons.AutoMirrored.Filled.TrendingUp, selectedTab == 4) {
                                    selectedTab = 4
                                    onViewAnalytics()
                                }
                                OwnerSidebarNavItem("Finances", Icons.Default.Payments, selectedTab == 5) {
                                    selectedTab = 5
                                    onViewFinances()
                                }
                            }
                        }

                        // User Footer
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigate("profile") },
                            color = Color.White.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        user.name.take(1).uppercase(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(user.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF4ADE80)))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Online", color = Color(0xFF4ADE80), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                                IconButton(onClick = onLogout, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.AutoMirrored.Filled.Logout, "Logout", tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Main Content Area
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .roomifyAmbientBackground()
            ) {
                // Top App Bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    border = BorderStroke(1.dp, CardBorderColor)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (isCompact) 60.dp else 72.dp)
                            .padding(horizontal = if (isCompact) 16.dp else 24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFFF1F5F9), CircleShape)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = PrimaryColor, modifier = Modifier.size(18.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f, fill = false)) {
                                val currentTitle = when (selectedTab) {
                                    0 -> "Welcome back, ${user.name}"
                                    1 -> "My Properties"
                                    2 -> "Pending Requests"
                                    3 -> "Messages & Inquiries"
                                    4 -> "Analytics Dashboard"
                                    5 -> "Finances & Earnings"
                                    else -> "Owner Portal"
                                }
                                val currentSub = when (selectedTab) {
                                    0 -> "Property performance overview"
                                    1 -> "Manage your listed rooms and units"
                                    2 -> "Review incoming tenant booking requests"
                                    3 -> "Conversations with prospective tenants"
                                    4 -> "Real-time property and revenue metrics"
                                    5 -> "Earnings summary and transaction history"
                                    else -> "Manage your property business"
                                }
                                Text(
                                    currentTitle,
                                    fontSize = if (isCompact) 15.sp else 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    currentSub,
                                    fontSize = 11.sp,
                                    color = SubtitleColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Wallet Balance Pill
                        val revenue = finances?.totalRevenue?.toLong() ?: 0L
                        Surface(
                            color = PrimaryColor,
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = if (isCompact) 10.dp else 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.AccountBalanceWallet, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "TZS ${revenue.toString().reversed().chunked(3).joinToString(",").reversed()}",
                                    color = Color.White,
                                    fontSize = if (isCompact) 12.sp else 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }

                // Selected Tab View
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when (selectedTab) {
                        0 -> DashboardTabView(
                            user = user,
                            properties = properties,
                            bookings = bookings,
                            finances = finances,
                            isExpanded = isExpanded,
                            isMedium = isMedium,
                            isCompact = isCompact,
                            onAddProperty = onAddProperty,
                            onViewAnalytics = {
                                selectedTab = 4
                                onViewAnalytics()
                            },
                            onViewFinances = {
                                selectedTab = 5
                                onViewFinances()
                            }
                        )
                        1 -> PropertiesTabView(
                            properties = properties,
                            isExpanded = isExpanded,
                            isMedium = isMedium,
                            isCompact = isCompact,
                            onAddProperty = onAddProperty,
                            onViewProperty = onViewProperty
                        )
                        2 -> RequestsTabView(
                            bookings = bookings,
                            onAcceptBooking = onAcceptBooking,
                            onRejectBooking = onRejectBooking
                        )
                        3 -> MessagesTabView(
                            conversations = conversations,
                            user = user,
                            isExpanded = isExpanded,
                            onConversationClick = onConversationClick
                        )
                        4 -> AnalyticsContent()
                        5 -> FinancesContent(currentUser = user)
                    }
                }

                // Compact Bottom Navigation Bar (< 600.dp)
                if (isCompact) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, CardBorderColor)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BottomNavItem("Dashboard", Icons.Default.Dashboard, selectedTab == 0) { selectedTab = 0 }
                            BottomNavItem("Properties", Icons.Default.Home, selectedTab == 1) { selectedTab = 1 }
                            BottomNavItem("Requests", Icons.Default.Bookmark, selectedTab == 2) { selectedTab = 2 }
                            BottomNavItem("Messages", Icons.Default.Message, selectedTab == 3) { selectedTab = 3 }
                            BottomNavItem("More", Icons.Default.Menu, selectedTab in 4..5) { showMoreSheet = true }
                        }
                    }
                }
            }
        }
    }

    // Compact Bottom Sheet / Modal Drawer for "More" Destinations
    if (showMoreSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMoreSheet = false },
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("More Options", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                HorizontalDivider(color = CardBorderColor)

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showMoreSheet = false
                            selectedTab = 4
                            onViewAnalytics()
                        },
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF8FAFC)
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.TrendingUp, null, tint = PrimaryColor, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Analytics Dashboard", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                    }
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showMoreSheet = false
                            selectedTab = 5
                            onViewFinances()
                        },
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF8FAFC)
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Payments, null, tint = PrimaryColor, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Finances & Earnings", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                    }
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showMoreSheet = false
                            onNavigate("profile")
                        },
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF8FAFC)
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, null, tint = PrimaryColor, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Profile & Settings", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                    }
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showMoreSheet = false
                            onLogout()
                        },
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFEF2F2)
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.Logout, null, tint = Color(0xFFDC2626), modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Logout", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                    }
                }
            }
        }
    }
}

// Sidebar Item Composable
@Composable
private fun OwnerSidebarNavItem(title: String, icon: ImageVector, active: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = if (active) PrimaryLight else Color.Transparent,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = if (active) Color.White else Color.White.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(12.dp))
            Text(
                title,
                color = if (active) Color.White else Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            if (active) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF10B981)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Active",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

// Bottom Nav Item Composable
@Composable
private fun BottomNavItem(label: String, icon: ImageVector, selected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = if (selected) PrimaryColor else SubtitleColor,
            modifier = Modifier.size(22.dp)
        )
        Text(
            label,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) PrimaryColor else SubtitleColor
        )
    }
}

// TAB 0: Dashboard Tab View
@Composable
private fun DashboardTabView(
    user: User,
    properties: List<Room>,
    bookings: List<Booking>,
    finances: org.com.model.FinancesSummary?,
    isExpanded: Boolean,
    isMedium: Boolean,
    isCompact: Boolean,
    onAddProperty: () -> Unit,
    onViewAnalytics: () -> Unit,
    onViewFinances: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(if (isExpanded) 24.dp else 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // KPI Summary Row
        if (isExpanded || isMedium) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                KPICard(
                    title = "Properties",
                    value = properties.size.toString(),
                    icon = Icons.Default.Business,
                    modifier = Modifier.weight(1f)
                )
                KPICard(
                    title = "Total Views",
                    value = properties.sumOf { it.viewCount }.toString(),
                    icon = Icons.Default.Visibility,
                    modifier = Modifier.weight(1f)
                )
                KPICard(
                    title = "Pending Requests",
                    value = bookings.count { it.status == "PENDING" }.toString(),
                    icon = Icons.Default.BookmarkBorder,
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                KPICard(title = "Properties", value = properties.size.toString(), icon = Icons.Default.Business, modifier = Modifier.fillMaxWidth())
                KPICard(title = "Total Views", value = properties.sumOf { it.viewCount }.toString(), icon = Icons.Default.Visibility, modifier = Modifier.fillMaxWidth())
                KPICard(title = "Pending Requests", value = bookings.count { it.status == "PENDING" }.toString(), icon = Icons.Default.BookmarkBorder, modifier = Modifier.fillMaxWidth())
            }
        }

        // Revenue Performance Card
        RevenuePerformanceCard(propertiesCount = properties.size)

        // Quick Management Actions
        Column {
            Text("Quick Management Actions", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SubtitleColor)
            Spacer(Modifier.height(12.dp))

            if (isExpanded || isMedium) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    QuickActionCard("Add Property", Icons.Default.Add, PrimaryColor, Modifier.weight(1f), onAddProperty)
                    QuickActionCard("Analytics", Icons.AutoMirrored.Filled.TrendingUp, Color(0xFF00897B), Modifier.weight(1f), onViewAnalytics)
                    QuickActionCard("Finances", Icons.Default.Payments, Color(0xFFD97706), Modifier.weight(1f), onViewFinances)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickActionCard("Add Property", Icons.Default.Add, PrimaryColor, Modifier.fillMaxWidth(), onAddProperty)
                    QuickActionCard("Analytics", Icons.AutoMirrored.Filled.TrendingUp, Color(0xFF00897B), Modifier.fillMaxWidth(), onViewAnalytics)
                    QuickActionCard("Finances", Icons.Default.Payments, Color(0xFFD97706), Modifier.fillMaxWidth(), onViewFinances)
                }
            }
        }
    }
}

// KPI Card
@Composable
private fun KPICard(title: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, CardBorderColor),
        shadowElevation = 1.dp
    ) {
        Column(Modifier.padding(20.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(PrimaryColor.copy(alpha = 0.08f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = PrimaryColor, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(14.dp))
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
            Text(title, fontSize = 13.sp, color = SubtitleColor, fontWeight = FontWeight.Medium)
        }
    }
}

// Revenue Performance Card
@Composable
private fun RevenuePerformanceCard(propertiesCount: Int) {
    val revenueData = listOf(1.2f, 2.4f, 1.8f, 3.6f, 4.2f, 3.1f)
    val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun")
    val maxRevenue = revenueData.maxOrNull() ?: 1f

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, CardBorderColor),
        shadowElevation = 1.dp
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(PrimaryColor.copy(alpha = 0.08f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.ShowChart, null, tint = PrimaryColor, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Text("Monthly Earnings", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (propertiesCount == 0) {
                        Surface(
                            color = Color(0xFFFEF3C7),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "Sample / Preview",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD97706),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "TZS (Millions)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Chart area with sufficient bottom padding so month labels never clip
            Column(
                modifier = Modifier.fillMaxWidth().height(160.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    revenueData.forEachIndexed { index, value ->
                        val barRatio = value / maxRevenue
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            Text(
                                text = "${value}M",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryColor,
                                softWrap = false
                            )
                            Spacer(Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(barRatio.coerceIn(0.1f, 1f))
                                    .background(
                                        Brush.verticalGradient(listOf(PrimaryLight, PrimaryColor)),
                                        RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                                    )
                            )
                        }
                    }
                }

                // Month tick labels row with >= 24.dp bottom space
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    months.forEach { month ->
                        Text(
                            text = month,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = SubtitleColor,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

// Quick Action Card
@Composable
private fun QuickActionCard(title: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = BorderStroke(1.dp, CardBorderColor),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
        }
    }
}

// TAB 1: Properties Tab View
@Composable
private fun PropertiesTabView(
    properties: List<Room>,
    isExpanded: Boolean,
    isMedium: Boolean,
    isCompact: Boolean,
    onAddProperty: () -> Unit,
    onViewProperty: (Room) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val q = searchQuery.trim().lowercase()
    val filteredProperties = remember(properties, q) {
        if (q.isBlank()) properties
        else properties.filter { room ->
            val loc = room.displayLocation.lowercase()
            (room.title?.lowercase()?.contains(q) == true) ||
            (room.propertyType?.lowercase()?.contains(q) == true) ||
            loc.contains(q) ||
            (room.address?.lowercase()?.contains(q) == true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(if (isExpanded) 24.dp else 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("My Properties (${properties.size})", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
            Button(
                onClick = onAddProperty,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("+ Add Property", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        Spacer(Modifier.height(12.dp))

        // Search Bar for owner listings with instant search & place suggestions
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by title, property type, or place (Sinza, Mikocheni...)", fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = SubtitleColor, modifier = Modifier.size(18.dp)) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, null, tint = SubtitleColor, modifier = Modifier.size(18.dp))
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = PrimaryColor,
                unfocusedBorderColor = CardBorderColor
            ),
            singleLine = true
        )

        // Instant Place Suggestions Chips
        if (searchQuery.isNotBlank()) {
            val placeSuggestions = remember(searchQuery) {
                PlaceSuggestionEngine.suggest(searchQuery).primary.take(5)
            }
            if (placeSuggestions.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(placeSuggestions) { suggestion ->
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { searchQuery = suggestion.name },
                            color = Color(0xFFEEF2FF),
                            border = BorderStroke(1.dp, Color(0xFFC7D2FE))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.LocationOn, null, tint = PrimaryColor, modifier = Modifier.size(13.dp))
                                Text(suggestion.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryColor)
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        if (properties.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Card(
                    modifier = Modifier.widthIn(max = 440.dp).fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, CardBorderColor)
                ) {
                    Column(
                        modifier = Modifier.padding(36.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(PrimaryColor.copy(alpha = 0.08f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Business, null, tint = PrimaryColor, modifier = Modifier.size(24.dp))
                        }
                        Spacer(Modifier.height(16.dp))
                        Text("No properties listed yet", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "List your first room, studio, or space to start receiving inquiries.",
                            fontSize = 14.sp,
                            color = SubtitleColor,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(20.dp))
                        Button(
                            onClick = onAddProperty,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("+ Add Property", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        } else if (filteredProperties.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.SearchOff, null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("No properties match \"$searchQuery\"", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    Spacer(Modifier.height(4.dp))
                    Text("Try searching another place name or clear the search query.", fontSize = 13.sp, color = SubtitleColor)
                }
            }
        } else {
            val columns = if (isExpanded) 3 else if (isMedium) 2 else 1
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredProperties) { room ->
                    OwnerPropertyCardItem(room = room, onClick = { onViewProperty(room) })
                }
            }
        }
    }
}

@Composable
private fun OwnerPropertyCardItem(room: Room, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, CardBorderColor),
        shadowElevation = 1.dp
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(72.dp).clip(RoundedCornerShape(12.dp))) {
                KamelImage(
                    resource = { asyncPainterResource(room.firstImageUrl ?: "") },
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    onLoading = {
                        Box(Modifier.fillMaxSize().background(Color(0xFFF1F5F9)), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = PrimaryColor)
                        }
                    },
                    onFailure = {
                        Box(Modifier.fillMaxSize().background(Color(0xFFF1F5F9)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Error, null, tint = Color.LightGray, modifier = Modifier.size(24.dp))
                        }
                    }
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(room.title ?: "Property", fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1, color = Color(0xFF0F172A))
                Spacer(Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(3.dp))
                    Text(room.displayLocation, fontSize = 12.sp, color = SubtitleColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(room.formattedPrice, fontSize = 13.sp, color = PrimaryColor, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                    val vStatus = room.verificationStatus.uppercase()
                    Surface(
                        color = when (vStatus) {
                            "VERIFIED" -> Color(0xFFDCFCE7)
                            "REJECTED" -> Color(0xFFFEE2E2)
                            else -> Color(0xFFFEF3C7)
                        },
                        shape = CircleShape
                    ) {
                        Text(
                            text = when (vStatus) {
                                "VERIFIED" -> "VERIFIED"
                                "REJECTED" -> "REJECTED"
                                else -> "PENDING"
                            },
                            color = when (vStatus) {
                                "VERIFIED" -> Color(0xFF166534)
                                "REJECTED" -> Color(0xFF991B1B)
                                else -> Color(0xFF92400E)
                            },
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.LightGray, modifier = Modifier.size(18.dp))
        }
    }
}

// TAB 2: Requests Tab View
@Composable
private fun RequestsTabView(
    bookings: List<Booking>,
    onAcceptBooking: (Booking) -> Unit,
    onRejectBooking: (Booking) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("Pending") }
    val filters = listOf("All", "Pending", "Approved", "Declined")

    val filteredBookings = remember(bookings, selectedFilter) {
        when (selectedFilter) {
            "Pending" -> bookings.filter { it.status == "PENDING" }
            "Approved" -> bookings.filter { it.status == "ACCEPTED" || it.status == "APPROVED" || it.status == "CONFIRMED" }
            "Declined" -> bookings.filter { it.status == "REJECTED" || it.status == "DECLINED" }
            else -> bookings
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text("Pending Booking Requests", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            filters.forEach { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        if (filteredBookings.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Card(
                    modifier = Modifier.widthIn(max = 440.dp).fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, CardBorderColor)
                ) {
                    Column(
                        modifier = Modifier.padding(36.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(PrimaryColor.copy(alpha = 0.08f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Bookmark, null, tint = PrimaryColor, modifier = Modifier.size(24.dp))
                        }
                        Spacer(Modifier.height(16.dp))
                        Text("No pending booking requests", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "New tenant viewing or booking requests will appear here.",
                            fontSize = 14.sp,
                            color = SubtitleColor,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredBookings) { booking ->
                    BookingRequestCardItem(booking = booking, onAccept = { onAcceptBooking(booking) }, onReject = { onRejectBooking(booking) })
                }
            }
        }
    }
}

@Composable
private fun BookingRequestCardItem(booking: Booking, onAccept: () -> Unit, onReject: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, CardBorderColor),
        shadowElevation = 1.dp
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(44.dp).clip(CircleShape).background(PrimaryColor.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, null, tint = PrimaryColor, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(booking.userName ?: "Guest Tenant", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))
                        if ("VIEWING".equals(booking.bookingType, ignoreCase = true)) {
                            Surface(color = Color(0xFFEFF6FF), shape = RoundedCornerShape(6.dp)) {
                                Text("👀 UKAGUZI", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB), modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                            }
                        }
                    }
                    Text(booking.roomTitle ?: "Property", fontSize = 12.sp, color = SubtitleColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if ("VIEWING".equals(booking.bookingType, ignoreCase = true)) {
                        Text("Muda: ${booking.viewingTimeSlot ?: "Asubuhi"} • Eneo: ${booking.meetingPoint ?: "Kituoni"}", fontSize = 11.sp, color = Color(0xFF1D4ED8), fontWeight = FontWeight.SemiBold)
                    } else {
                        Text("100% Direct Rent (No broker deduction)", fontSize = 11.sp, color = SuccessColor, fontWeight = FontWeight.SemiBold)
                    }
                }
                val rentVal = if (booking.rentAmount > 0) booking.rentAmount else booking.totalPrice
                Text(if ("VIEWING".equals(booking.bookingType, ignoreCase = true)) "FREE" else "TZS ${rentVal.toLong()}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PrimaryColor)
            }
            if (booking.status == "PENDING") {
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onAccept,
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessColor)
                    ) {
                        Text("Accept", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onReject,
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFDC2626))
                    ) {
                        Text("Reject", fontSize = 13.sp, color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// TAB 3: Messages Tab View (Split view on Expanded >= 840.dp)
@Composable
private fun MessagesTabView(
    conversations: List<Conversation>,
    user: User,
    isExpanded: Boolean,
    onConversationClick: (Conversation) -> Unit
) {
    var selectedConversation by remember { mutableStateOf<Conversation?>(conversations.firstOrNull()) }

    if (conversations.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Card(
                modifier = Modifier.widthIn(max = 440.dp).fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, CardBorderColor)
            ) {
                Column(
                    modifier = Modifier.padding(36.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(PrimaryColor.copy(alpha = 0.08f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Message, null, tint = PrimaryColor, modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("No messages received yet", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Conversations with potential tenants will appear here.",
                        fontSize = 14.sp,
                        color = SubtitleColor,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    } else if (isExpanded) {
        // Split view: 38% list, 62% thread panel
        Row(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // Left List (38%)
            Surface(
                modifier = Modifier.weight(0.38f).fillMaxHeight(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = BorderStroke(1.dp, CardBorderColor)
            ) {
                LazyColumn(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        Text("Conversations", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A), modifier = Modifier.padding(8.dp))
                    }
                    items(conversations) { conv ->
                        val isSelected = selectedConversation?.conversationId == conv.conversationId
                        Surface(
                            modifier = Modifier.fillMaxWidth().clickable { selectedConversation = conv },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) PrimaryColor.copy(alpha = 0.08f) else Color.Transparent
                        ) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(40.dp).clip(CircleShape).background(PrimaryColor.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                                    Text(conv.otherPartyName.take(1).uppercase(), color = PrimaryColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(conv.otherPartyName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F172A))
                                    Text(conv.lastMessage, fontSize = 12.sp, color = SubtitleColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }
            }

            // Right Thread Panel (62%)
            Surface(
                modifier = Modifier.weight(0.62f).fillMaxHeight(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = BorderStroke(1.dp, CardBorderColor)
            ) {
                if (selectedConversation != null) {
                    ChatScreen(
                        currentUser = user,
                        otherUserName = selectedConversation!!.otherPartyName,
                        otherUserId = selectedConversation!!.otherPartyId,
                        roomId = selectedConversation!!.roomId,
                        roomTitle = selectedConversation!!.roomTitle,
                        onBack = {}
                    )
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Select a conversation to view messages", color = SubtitleColor, fontSize = 14.sp)
                    }
                }
            }
        }
    } else {
        // Compact full screen list
        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(conversations) { conv ->
                Surface(
                    modifier = Modifier.fillMaxWidth().clickable { onConversationClick(conv) },
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, CardBorderColor)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(44.dp).clip(CircleShape).background(PrimaryColor.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Chat, null, tint = PrimaryColor, modifier = Modifier.size(22.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(conv.otherPartyName.ifBlank { "Tenant" }, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))
                            Spacer(Modifier.height(2.dp))
                            Text(conv.lastMessage.ifBlank { "New inquiry" }, fontSize = 12.sp, color = SubtitleColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.LightGray, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
