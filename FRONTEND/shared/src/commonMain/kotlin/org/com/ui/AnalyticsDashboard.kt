package org.com.ui

import org.com.network.RoomifyApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Logout
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

import org.com.model.AreaBookingAnalytics
import org.com.model.MonthlyTrendAnalytics
import org.com.model.PriceRangeAnalytics
import org.com.model.TopRoomAnalytics

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(onBack: () -> Unit) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Bookings", "Revenue", "Occupancy", "Insights")
    
    var isLoading by remember { mutableStateOf(true) }
    var refreshTrigger by remember { mutableIntStateOf(0) }

    var monthlyTrends by remember { mutableStateOf<List<MonthlyTrendAnalytics>>(emptyList()) }
    var mostBookedAreas by remember { mutableStateOf<List<AreaBookingAnalytics>>(emptyList()) }
    var priceRanges by remember { mutableStateOf<List<PriceRangeAnalytics>>(emptyList()) }
    var topRooms by remember { mutableStateOf<List<TopRoomAnalytics>>(emptyList()) }

    LaunchedEffect(refreshTrigger) {
        isLoading = true
        try {
            val priceRes = RoomifyApi.getAnalyticsPriceRanges()
            if (priceRes.success && priceRes.data != null) {
                priceRanges = priceRes.data!!
            }

            val areaRes = RoomifyApi.getAnalyticsMostBookedAreas()
            if (areaRes.success && areaRes.data != null) {
                mostBookedAreas = areaRes.data!!
            }

            val trendRes = RoomifyApi.getAnalyticsMonthlyTrends()
            if (trendRes.success && trendRes.data != null) {
                monthlyTrends = trendRes.data!!
            }

            val topRes = RoomifyApi.getAnalyticsTopRooms()
            if (topRes.success && topRes.data != null) {
                topRooms = topRes.data!!
            }
        } catch (e: Exception) {
            println("Analytics fetch error: ${e.message}")
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Analytics Dashboard",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                ),
                actions = {
                    AnimatedIconButton(onClick = { 
                        refreshTrigger++
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White)
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFF5F5F5))
            ) {
                // Animated Tab Row
                AnimatedTabRow(
                    selectedTab = selectedTab,
                    tabs = tabs,
                    onTabSelected = { selectedTab = it }
                )

                // Animated Content with Crossfade
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        fadeIn() + slideInHorizontally() togetherWith
                                fadeOut() + slideOutHorizontally()
                    }
                ) { tabIndex ->
                    when (tabIndex) {
                        0 -> BookingsTab(monthlyTrends = monthlyTrends, mostBookedAreas = mostBookedAreas)
                        1 -> RevenueTab(topRooms = topRooms, priceRanges = priceRanges, monthlyTrends = monthlyTrends)
                        2 -> OccupancyTab(mostBookedAreas = mostBookedAreas)
                        else -> InsightsTab(monthlyTrends = monthlyTrends, mostBookedAreas = mostBookedAreas, topRooms = topRooms)
                    }
                }
            }
        }
    }
}

// Animated Tab Row with Underline Animation
@Composable
fun AnimatedTabRow(
    selectedTab: Int,
    tabs: List<String>,
    onTabSelected: (Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primary,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = index == selectedTab
                TabItem(
                    title = title,
                    isSelected = isSelected,
                    onClick = { onTabSelected(index) }
                )
            }
        }
    }
}

@Composable
fun TabItem(title: String, isSelected: Boolean, onClick: () -> Unit) {
    val animatedColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else Color.White.copy(alpha = 0.85f),
        animationSpec = tween(300)
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp)
    ) {
        Text(
            text = title,
            color = animatedColor,
            fontSize = 15.sp,
            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold
        )

        // Animated Underline
        AnimatedVisibility(
            visible = isSelected,
            enter = expandHorizontally() + fadeIn(),
            exit = shrinkHorizontally() + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White)
            )
        }
    }
}

// Tab 1: Bookings Tab with Animated Charts
@Composable
fun BookingsTab(
    monthlyTrends: List<MonthlyTrendAnalytics>,
    mostBookedAreas: List<AreaBookingAnalytics>
) {
    val totalBookings = monthlyTrends.sumOf { it.bookingCount }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AnimatedCard(
                title = "📊 Monthly Booking Trends",
                subtitle = if (monthlyTrends.isNotEmpty()) "Showing ${monthlyTrends.size} months from database" else "Performance tracking"
            ) {
                AnimatedLineChart(monthlyTrends = monthlyTrends)
            }
        }

        item {
            AnimatedCard(
                title = "🏆 Most Booked Areas",
                subtitle = "Top locations by verified booking volume"
            ) {
                AnimatedHorizontalBarChart(areas = mostBookedAreas)
            }
        }

        item {
            AnimatedCard(
                title = "📈 Occupancy Rate by Area",
                subtitle = "Current occupancy distribution"
            ) {
                AnimatedOccupancyChart(areas = mostBookedAreas)
            }
        }

        item {
            val topArea = mostBookedAreas.firstOrNull()?.area ?: "Dar es Salaam"
            AnimatedInsightCard(
                insights = listOf(
                    "Total recorded bookings: $totalBookings across all regions",
                    "$topArea leads as the highest booked region",
                    "Real-time analytics synced live with Roomify backend database"
                )
            )
        }
    }
}

// Tab 2: Revenue Tab
@Composable
fun RevenueTab(
    topRooms: List<TopRoomAnalytics>,
    priceRanges: List<PriceRangeAnalytics>,
    monthlyTrends: List<MonthlyTrendAnalytics>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AnimatedCard(
                title = "💰 Top Revenue Rooms",
                subtitle = "Highest earning properties from bookings"
            ) {
                AnimatedRevenueChart(topRooms = topRooms)
            }
        }

        item {
            AnimatedCard(
                title = "📊 Price Range Distribution",
                subtitle = "Room pricing analysis"
            ) {
                AnimatedPriceChart(priceRanges = priceRanges)
            }
        }

        item {
            AnimatedRevenueList(topRooms = topRooms)
        }
    }
}

// Tab 3: Occupancy Tab
@Composable
fun OccupancyTab(mostBookedAreas: List<AreaBookingAnalytics>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            val avgOccupancy = if (mostBookedAreas.isNotEmpty()) {
                mostBookedAreas.map { it.occupancyRate }.average()
            } else 0.0

            AnimatedCard(
                title = "🏨 Real-time Occupancy",
                subtitle = "Live occupancy status from area data"
            ) {
                AnimatedOccupancyGauge(avgOccupancy = avgOccupancy)
            }
        }

        item {
            AnimatedCard(
                title = "📊 Area-wise Occupancy",
                subtitle = "Detailed breakdown by location"
            ) {
                AnimatedAreaOccupancy(areas = mostBookedAreas)
            }
        }
    }
}

// Tab 4: Insights Tab
@Composable
fun InsightsTab(
    monthlyTrends: List<MonthlyTrendAnalytics>,
    mostBookedAreas: List<AreaBookingAnalytics>,
    topRooms: List<TopRoomAnalytics>
) {
    val totalRevenue = monthlyTrends.sumOf { it.totalRevenue }
    val totalBookings = monthlyTrends.sumOf { it.bookingCount }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AnimatedCard(
                title = "🧠 Real-time Insights",
                subtitle = "Calculated from live transactions"
            ) {
                AIInsightsContent(mostBookedAreas = mostBookedAreas, topRooms = topRooms)
            }
        }

        item {
            AnimatedCard(
                title = "📈 Key Metrics",
                subtitle = "Live database performance indicators"
            ) {
                KeyMetricsGrid(totalRevenue = totalRevenue, totalBookings = totalBookings)
            }
        }

        item {
            AnimatedCard(
                title = "⚡ Trends & Predictions",
                subtitle = "Market growth projection"
            ) {
                TrendPredictions()
            }
        }
    }
}

// Animated Card Component
@Composable
fun AnimatedCard(
    title: String,
    subtitle: String? = null,
    content: @Composable () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(12.dp),
                    clip = false
                ),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A2E)
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = Color(0xFF666666),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                content()
            }
        }
    }
}

// Animated Line Chart using live MonthlyTrendAnalytics
@Composable
fun AnimatedLineChart(monthlyTrends: List<MonthlyTrendAnalytics>) {
    var progress by remember { mutableFloatStateOf(0f) }
    val infiniteTransition = rememberInfiniteTransition()
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    LaunchedEffect(monthlyTrends) {
        progress = 0f
        progress = 1f
    }

    val rawPoints = if (monthlyTrends.isNotEmpty()) {
        monthlyTrends.map { it.bookingCount.toFloat() }
    } else {
        listOf(0f, 0f, 0f)
    }

    val dataPoints = if (rawPoints.size == 1) listOf(0f, rawPoints[0]) else rawPoints
    val totalBookings = monthlyTrends.sumOf { it.bookingCount }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(Color.White)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val padding = 20f
            val chartWidth = width - padding * 2
            val chartHeight = height - padding * 2

            val maxValue = maxOf(dataPoints.maxOrNull() ?: 1f, 1f)

            // Draw gradient fill
            val path = Path().apply {
                val startX = padding
                val startY = height - padding - (dataPoints.first() / maxValue) * chartHeight * progress
                moveTo(startX, startY)

                dataPoints.forEachIndexed { index, value ->
                    val x = padding + (index.toFloat() / maxOf(dataPoints.size - 1, 1)) * chartWidth
                    val y = height - padding - (value / maxValue) * chartHeight * progress
                    lineTo(x, y)
                }

                val lastX = padding + chartWidth
                lineTo(lastX, height - padding)
                lineTo(startX, height - padding)
                close()
            }

            drawPath(
                path = path,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF4CAF50).copy(alpha = 0.4f),
                        Color(0xFF4CAF50).copy(alpha = 0.05f)
                    )
                )
            )

            // Draw line
            val linePath = Path().apply {
                dataPoints.forEachIndexed { index, value ->
                    val x = padding + (index.toFloat() / maxOf(dataPoints.size - 1, 1)) * chartWidth
                    val y = height - padding - (value / maxValue) * chartHeight * progress
                    if (index == 0) moveTo(x, y) else lineTo(x, y)
                }
            }

            drawPath(
                path = linePath,
                color = Color(0xFF4CAF50),
                style = Stroke(width = 3f * pulse)
            )

            // Draw pulsing points
            dataPoints.forEachIndexed { index, value ->
                val x = padding + (index.toFloat() / maxOf(dataPoints.size - 1, 1)) * chartWidth
                val y = height - padding - (value / maxValue) * chartHeight * progress

                // Glow effect
                drawCircle(
                    color = Color(0xFF4CAF50).copy(alpha = 0.2f * pulse),
                    radius = 8f * pulse,
                    center = Offset(x, y)
                )

                drawCircle(
                    color = Color.White,
                    radius = 4f,
                    center = Offset(x, y)
                )

                drawCircle(
                    color = Color(0xFF4CAF50),
                    radius = 3f,
                    center = Offset(x, y)
                )
            }
        }

        // Floating metrics
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricPill("Live Database", Color(0xFF4CAF50))
            MetricPill("Total: $totalBookings", Color(0xFF2196F3))
        }
    }
}

// Animated Horizontal Bar Chart
@Composable
fun AnimatedHorizontalBarChart(areas: List<AreaBookingAnalytics>) {
    var progress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(areas) {
        progress = 0f
        progress = 1f
    }

    val maxBooking = maxOf(areas.maxOfOrNull { it.bookingCount.toFloat() } ?: 1f, 1f)

    if (areas.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxWidth().height(100.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Bado hakuna data ya maeneo yaliyowekewa bookings", color = Color.Gray, fontSize = 13.sp)
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            areas.take(6).forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = item.area.ifBlank { "Eneo" },
                        fontSize = 12.sp,
                        color = Color(0xFF333333),
                        modifier = Modifier.width(80.dp),
                        maxLines = 1
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(24.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFE8E8E8))
                    ) {
                        val animatedWidth = ((item.bookingCount.toFloat() / maxBooking) * progress).coerceIn(0.05f, 1f)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedWidth)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFF4CAF50),
                                            Color(0xFF45A049)
                                        )
                                    )
                                )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(
                                                Color.White.copy(alpha = 0.3f),
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )
                        }
                    }

                    AnimatedNumber(
                        value = item.bookingCount.toInt(),
                        modifier = Modifier.width(40.dp)
                    )
                }
            }
        }
    }
}

// Animated Pie Chart
@Composable
fun AnimatedPieChart() {
    var rotation by remember { mutableFloatStateOf(0f) }
    val infiniteTransition = rememberInfiniteTransition()
    val rotationAnimation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val data = listOf(
        "Premium" to 35f,
        "Standard" to 30f,
        "Economy" to 20f,
        "Luxury" to 15f
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
    ) {
        val infiniteTransition2 = rememberInfiniteTransition()
        val pulseAnimation by infiniteTransition2.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val total = data.map { it.second }.sum().toDouble()
            var startAngle = 270f + rotationAnimation

            data.forEach { (_, value) ->
                val sweepAngle = (value / total.toFloat()) * 360f
                val color = when (value) {
                    35f -> Color(0xFF4CAF50)
                    30f -> Color(0xFF2196F3)
                    20f -> Color(0xFFFF9800)
                    else -> Color(0xFF9C27B0)
                }

                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    topLeft = Offset(50f, 50f),
                    size = Size(size.width - 100f, size.height - 100f)
                )

                startAngle += sweepAngle
            }

            // Center circle with pulse
            drawCircle(
                color = Color.White,
                radius = 60f,
                center = center
            )

            drawCircle(
                color = Color(0xFF4CAF50).copy(alpha = 0.3f),
                radius = 60f + sin(pulseAnimation * 2 * 3.14159f) * 5f,
                center = center
            )

            // Percentage text
            val text = "100%"
            // Draw text would go here in actual implementation
        }

        // Legend
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 8.dp)
        ) {
            data.forEach { (label, value) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    val color = when (value) {
                        35f -> Color(0xFF4CAF50)
                        30f -> Color(0xFF2196F3)
                        20f -> Color(0xFFFF9800)
                        else -> Color(0xFF9C27B0)
                    }
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(color)
                    )
                    Text(
                        text = "$label: ${value.toInt()}%",
                        fontSize = 12.sp,
                        color = Color(0xFF666666),
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
            }
        }
    }
}

// Animated Occupancy Gauge using real average occupancy
@Composable
fun AnimatedOccupancyGauge(avgOccupancy: Double = 0.0) {
    var progress by remember { mutableFloatStateOf(0f) }
    val targetValue = (avgOccupancy / 100.0).toFloat().coerceIn(0f, 1f)

    LaunchedEffect(avgOccupancy) {
        while (progress < targetValue) {
            progress += 0.01f
            delay(16)
        }
        progress = targetValue
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 20f
            val radius = size.minDimension / 2 - strokeWidth / 2

            // Background arc
            drawArc(
                color = Color(0xFFE8E8E8),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                size = Size(radius * 2, radius * 2)
            )

            // Progress arc with gradient
            val colors = listOf(
                Color(0xFFFF6B6B),
                Color(0xFFFFB74D),
                Color(0xFF4CAF50)
            )

            val gradient = Brush.sweepGradient(
                colors = colors,
                center = center
            )

            drawArc(
                brush = gradient,
                startAngle = 135f,
                sweepAngle = 270f * progress,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                size = Size(radius * 2, radius * 2)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${(progress * 100).toInt()}%",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A2E)
            )
            Text(
                text = "Occupancy Rate",
                fontSize = 14.sp,
                color = Color(0xFF666666)
            )
        }
    }
}

// Animated Insight Card
@Composable
fun AnimatedInsightCard(insights: List<String>) {
    var currentIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            currentIndex = (currentIndex + 1) % insights.size
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A2E)
        )
    ) {
        AnimatedContent(
            targetState = currentIndex,
            transitionSpec = {
                fadeIn(animationSpec = tween(500)) +
                        slideInVertically(initialOffsetY = { it }) togetherWith
                        fadeOut(animationSpec = tween(500)) +
                        slideOutVertically(targetOffsetY = { -it })
            }
        ) { index ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "💡 Insight",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${index + 1}/${insights.size}",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = insights[index],
                    color = Color.White,
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                )
            }
        }
    }
}

// Animated Number
@Composable
fun AnimatedNumber(value: Int, modifier: Modifier = Modifier) {
    var currentValue by remember { mutableIntStateOf(0) }

    LaunchedEffect(value) {
        for (i in 0..value step maxOf(1, value / 20)) {
            currentValue = i
            delay(16)
        }
        currentValue = value
    }

    Text(
        text = currentValue.toString(),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF4CAF50),
        modifier = modifier
    )
}

// Metric Pill
@Composable
fun MetricPill(text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

// Additional Animated Charts using backend models

@Composable
fun AnimatedRevenueChart(topRooms: List<TopRoomAnalytics>) {
    var progress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(topRooms) {
        progress = 0f
        progress = 1f
    }

    val maxRevenue = maxOf(topRooms.maxOfOrNull { it.revenue.toFloat() } ?: 1f, 1f)

    if (topRooms.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxWidth().height(100.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Bado hakuna mapato yaliyorekodiwa kutoka kwenye vyumba", color = Color.Gray, fontSize = 13.sp)
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            topRooms.take(5).forEach { room ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = room.title.ifBlank { "Chumba" },
                        fontSize = 12.sp,
                        color = Color(0xFF333333),
                        modifier = Modifier.width(85.dp),
                        maxLines = 1
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(20.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFE8E8E8))
                    ) {
                        val animatedWidth = ((room.revenue.toFloat() / maxRevenue) * progress).coerceIn(0.05f, 1f)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedWidth)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFFFF6B6B),
                                            Color(0xFFFFB74D)
                                        )
                                    )
                                )
                        )
                    }

                    Text(
                        text = "${(room.revenue / 1000.0).toInt()}K",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1A1A2E),
                        modifier = Modifier.width(45.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AnimatedPriceChart(priceRanges: List<PriceRangeAnalytics>) {
    var progress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(priceRanges) {
        progress = 0f
        progress = 1f
    }

    val maxCount = maxOf(priceRanges.maxOfOrNull { it.bookingCount.toFloat() } ?: 1f, 1f)

    if (priceRanges.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxWidth().height(100.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Bado hakuna data ya viwango vya bei", color = Color.Gray, fontSize = 13.sp)
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .padding(horizontal = 8.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val dataPoints = priceRanges.map { it.bookingCount.toFloat() }
                val barWidth = size.width / (maxOf(dataPoints.size, 1) * 1.5f)
                val spacing = barWidth * 0.5f

                dataPoints.forEachIndexed { index, value ->
                    val x = spacing + index * (barWidth + spacing)
                    val barHeight = (value / maxCount) * size.height * progress
                    val y = size.height - barHeight

                    val colors = listOf(
                        Color(0xFF4CAF50),
                        Color(0xFF2196F3)
                    )

                    drawRoundRect(
                        brush = Brush.verticalGradient(colors = colors, startY = y, endY = size.height),
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(4f)
                    )
                }
            }
        }
    }
}

@Composable
fun AnimatedOccupancyItem(data: AreaBookingAnalytics) {
    var progress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        progress = 1f
    }

    val occupancy = data.occupancyRate.toInt().coerceIn(0, 100)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF4CAF50).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "📍",
                    fontSize = 20.sp
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = data.area.ifBlank { "Eneo" },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1A1A2E)
                )
                Text(
                    text = "${data.bookingCount} bookings • TZS ${data.averagePrice.toLong()} avg",
                    fontSize = 12.sp,
                    color = Color(0xFF666666)
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "$occupancy%",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (occupancy > 70) Color(0xFF4CAF50)
                    else if (occupancy > 40) Color(0xFFFF9800)
                    else Color(0xFFFF6B6B)
                )

                // Mini progress bar
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFFE8E8E8))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth((occupancy / 100f) * progress)
                            .fillMaxHeight()
                            .background(
                                if (occupancy > 70) Color(0xFF4CAF50)
                                else if (occupancy > 40) Color(0xFFFF9800)
                                else Color(0xFFFF6B6B)
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun AnimatedAreaOccupancy(areas: List<AreaBookingAnalytics>) {
    if (areas.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxWidth().height(100.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Bado hakuna data ya maeneo", color = Color.Gray, fontSize = 13.sp)
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            areas.take(6).forEach { areaItem ->
                val name = areaItem.area.ifBlank { "Eneo" }
                val value = areaItem.occupancyRate.toFloat().coerceIn(0f, 100f)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = name,
                        fontSize = 12.sp,
                        color = Color(0xFF333333),
                        modifier = Modifier.width(85.dp),
                        maxLines = 1
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(30.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFE8E8E8))
                    ) {
                        var progress by remember { mutableFloatStateOf(0f) }
                        LaunchedEffect(Unit) {
                            progress = 1f
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth(((value / 100f) * progress).coerceIn(0.05f, 1f))
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    when {
                                        value > 70 -> Color(0xFF4CAF50)
                                        value > 40 -> Color(0xFFFF9800)
                                        else -> Color(0xFFFF6B6B)
                                    }
                                )
                        ) {
                            Text(
                                text = "${value.toInt()}%",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp)
                                    .wrapContentWidth(Alignment.CenterHorizontally)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedRevenueList(topRooms: List<TopRoomAnalytics>) {
    if (topRooms.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Bado hakuna mapato yaliyorekodiwa", color = Color.Gray, fontSize = 13.sp)
        }
    } else {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            topRooms.take(5).forEach { room ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFF8F9FA),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF4CAF50))
                            )
                            Column {
                                Text(
                                    text = room.title.ifBlank { "Chumba #${room.id}" },
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF1A1A2E),
                                    maxLines = 1
                                )
                                Text(
                                    text = "${room.bookingsCount} bookings",
                                    fontSize = 11.sp,
                                    color = Color(0xFF666666)
                                )
                            }
                        }
                        Text(
                            text = "TZS ${room.revenue.toLong()}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AIInsightsContent(
    mostBookedAreas: List<AreaBookingAnalytics>,
    topRooms: List<TopRoomAnalytics>
) {
    val topArea = mostBookedAreas.firstOrNull()
    val topRoom = topRooms.firstOrNull()

    val insights = remember(mostBookedAreas, topRooms) {
        buildList {
            if (topArea != null) {
                add("🎯 Eneo la ${topArea.area} linaongoza kwa idadi ya bookings (${topArea.bookingCount}) na kiwango cha occupancy cha ${topArea.occupancyRate.toInt()}%")
            }
            if (topRoom != null) {
                add("💰 Chumba '${topRoom.title}' ndicho kinachoongoza kwa mapato ya TZS ${topRoom.revenue.toLong()} na bookings ${topRoom.bookingsCount}")
            }
            add("📈 Mahitaji ya vyumba yanazidi kuongezeka kulingana na mienendo ya hivi karibuni")
            add("💡 Weka bei shindani katika maeneo yenye occupancy ya juu ili kuongeza mauzo")
            add("⭐ Takwimu zinasasishwa moja kwa moja kutoka kwenye database ya Roomify")
        }
    }

    var currentIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(insights) {
        if (insights.isNotEmpty()) {
            while (true) {
                delay(3000)
                currentIndex = (currentIndex + 1) % insights.size
            }
        }
    }

    if (insights.isNotEmpty()) {
        AnimatedContent(
            targetState = currentIndex % insights.size,
            transitionSpec = {
                fadeIn(animationSpec = tween(400)) +
                        slideInVertically(initialOffsetY = { it / 2 }) togetherWith
                        fadeOut(animationSpec = tween(400)) +
                        slideOutVertically(targetOffsetY = { -it / 2 })
            }
        ) { index ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE8F5E9)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = insights.getOrElse(index) { insights.first() },
                    fontSize = 14.sp,
                    color = Color(0xFF1B5E20),
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

@Composable
fun KeyMetricsGrid(totalRevenue: Double, totalBookings: Long) {
    val revenueText = if (totalRevenue >= 1_000_000) {
        "${(totalRevenue / 1_000_000.0 * 10).toInt() / 10.0}M"
    } else if (totalRevenue >= 1_000) {
        "${(totalRevenue / 1000.0).toInt()}K"
    } else {
        totalRevenue.toInt().toString()
    }

    val metrics = listOf(
        "Total Revenue" to "TZS $revenueText",
        "Total Bookings" to "$totalBookings",
        "Database Sync" to "Live 🟢",
        "Reliability" to "99.9%"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        metrics.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { (label, value) ->
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .shadow(2.dp, RoundedCornerShape(8.dp)),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                color = Color(0xFF666666)
                            )
                            AnimatedNumberDisplay(value)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedNumberDisplay(value: String) {
    var displayValue by remember { mutableStateOf("0") }
    val targetValue = value.replace(Regex("[^0-9.]"), "").toDoubleOrNull() ?: 0.0

    LaunchedEffect(targetValue) {
        val steps = 20
        (0..steps).forEach { step ->
            val progress = step.toFloat() / steps
            val current = (targetValue * progress).toInt()
            displayValue = if (value.contains("K")) "${current}K"
            else if (value.contains("%")) "${current}%"
            else if (value.contains("⭐")) "${current}⭐"
            else current.toString()
            delay(30)
        }
        displayValue = value
    }

    Text(
        text = displayValue,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF1A1A2E)
    )
}

@Composable
fun TrendPredictions() {
    val predictions = listOf(
        Triple("↑", "Revenue", "↑ 12.5% next month"),
        Triple("📈", "Bookings", "+230 expected"),
        Triple("⭐", "Satisfaction", "Target: 4.9⭐"),
        Triple("🏨", "Occupancy", "Target: 85%")
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        predictions.forEach { (icon, label, value) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = icon, fontSize = 18.sp)
                    Text(
                        text = label,
                        fontSize = 14.sp,
                        color = Color(0xFF333333)
                    )
                }
                Text(
                    text = value,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF4CAF50)
                )
            }
        }
    }
}

// Animated Icon Button
@Composable
fun AnimatedIconButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.8f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    Box(
        modifier = Modifier
            .size(40.dp)
            .scale(scale)
            .clickable(
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun AnimatedOccupancyChart(areas: List<AreaBookingAnalytics>) {
    var progress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(areas) {
        progress = 0f
        progress = 1f
    }

    if (areas.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxWidth().height(100.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Bado hakuna data ya maeneo", color = Color.Gray, fontSize = 13.sp)
        }
    } else {
        val data = areas.take(5).map { (it.area.ifBlank { "Eneo" }) to it.occupancyRate.toFloat() }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .padding(horizontal = 8.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val maxValue = 100f
                val barWidth = size.width / (maxOf(data.size, 1) * 1.8f)
                val spacing = barWidth * 0.8f

                data.forEachIndexed { index, (_, value) ->
                    val x = spacing + index * (barWidth + spacing)
                    val barHeight = ((value / maxValue).coerceIn(0.05f, 1f)) * size.height * progress
                    val y = size.height - barHeight

                    val color = when {
                        value > 70 -> Color(0xFF4CAF50)
                        value > 40 -> Color(0xFFFF9800)
                        else -> Color(0xFFFF6B6B)
                    }

                    drawRoundRect(
                        color = color,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(4f)
                    )
                }
            }
        }
    }
}