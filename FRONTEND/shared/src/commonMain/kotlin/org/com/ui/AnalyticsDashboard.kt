package org.com.ui

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(onBack: () -> Unit) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Bookings", "Revenue", "Occupancy", "Insights")
    
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(isLoading) {
        if (isLoading) {
            // Simulate fetching analytics data independently
            delay(1500)
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Analytics Dashboard",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
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
                        isLoading = true
                        // Simulate refresh
                        // LaunchedEffect will handle the reset
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
                        0 -> BookingsTab()
                        1 -> RevenueTab()
                        2 -> OccupancyTab()
                        else -> InsightsTab()
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
        targetValue = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
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
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
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
fun BookingsTab() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AnimatedCard(
                title = "📊 Monthly Booking Trends",
                subtitle = "Last 12 months performance"
            ) {
                AnimatedLineChart()
            }
        }

        item {
            AnimatedCard(
                title = "🏆 Most Booked Areas",
                subtitle = "Top locations by booking volume"
            ) {
                AnimatedHorizontalBarChart()
            }
        }

        item {
            AnimatedCard(
                title = "📈 Occupancy Rate by Area",
                subtitle = "Current occupancy distribution"
            ) {
                AnimatedOccupancyChart()
            }
        }

        item {
            AnimatedInsightCard(
                insights = listOf(
                    "Total bookings increased by 23% this month",
                    "Downtown area leads with 45% occupancy rate",
                    "Weekend bookings up 15% compared to last month"
                )
            )
        }
    }
}

// Tab 2: Revenue Tab
@Composable
fun RevenueTab() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AnimatedCard(
                title = "💰 Top Revenue Rooms",
                subtitle = "Highest earning properties"
            ) {
                AnimatedRevenueChart()
            }
        }

        item {
            AnimatedCard(
                title = "💳 Revenue Breakdown",
                subtitle = "Distribution by category"
            ) {
                AnimatedPieChart()
            }
        }

        item {
            AnimatedCard(
                title = "📊 Price Range Distribution",
                subtitle = "Room pricing analysis"
            ) {
                AnimatedPriceChart()
            }
        }

        item {
            AnimatedRevenueList()
        }
    }
}

// Tab 3: Occupancy Tab
@Composable
fun OccupancyTab() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AnimatedCard(
                title = "🏨 Real-time Occupancy",
                subtitle = "Live occupancy status"
            ) {
                AnimatedOccupancyGauge()
            }
        }

        item {
            AnimatedCard(
                title = "📊 Area-wise Occupancy",
                subtitle = "Detailed breakdown by location"
            ) {
                AnimatedAreaOccupancy()
            }
        }

        items(occupancyData) { data ->
            AnimatedOccupancyItem(data)
        }
    }
}

// Tab 4: Insights Tab
@Composable
fun InsightsTab() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AnimatedCard(
                title = "🧠 AI Insights",
                subtitle = "Powered by machine learning"
            ) {
                AIInsightsContent()
            }
        }

        item {
            AnimatedCard(
                title = "📈 Key Metrics",
                subtitle = "Performance indicators"
            ) {
                KeyMetricsGrid()
            }
        }

        item {
            AnimatedCard(
                title = "⚡ Trends & Predictions",
                subtitle = "What's coming next"
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

// Animated Line Chart with Pulse Effect
@Composable
fun AnimatedLineChart() {
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

    LaunchedEffect(Unit) {
        progress = 1f
    }

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

            val dataPoints = listOf(30f, 45f, 28f, 55f, 42f, 70f, 60f, 80f, 65f, 90f, 75f, 95f)
            val maxValue = dataPoints.maxOrNull() ?: 100f

            // Draw gradient fill
            val path = Path().apply {
                val startX = padding
                val startY = height - padding - (dataPoints.first() / maxValue) * chartHeight * progress
                moveTo(startX, startY)

                dataPoints.forEachIndexed { index, value ->
                    val x = padding + (index.toFloat() / (dataPoints.size - 1)) * chartWidth
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
                    val x = padding + (index.toFloat() / (dataPoints.size - 1)) * chartWidth
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
                val x = padding + (index.toFloat() / (dataPoints.size - 1)) * chartWidth
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
            MetricPill("↑ 23%", Color(0xFF4CAF50))
            MetricPill("Total: 1,284", Color(0xFF2196F3))
        }
    }
}

// Animated Horizontal Bar Chart
@Composable
fun AnimatedHorizontalBarChart() {
    var progress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        progress = 1f
    }

    val data = listOf(
        "Downtown" to 85f,
        "Uptown" to 72f,
        "Eastside" to 60f,
        "Westside" to 55f,
        "Southside" to 40f
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        data.forEach { (label, value) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    color = Color(0xFF333333),
                    modifier = Modifier.width(70.dp)
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(24.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE8E8E8))
                ) {
                    // Animated bar with gradient
                    val animatedWidth = (value / 100f) * progress
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
                        // Shine effect
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
                    value = value.toInt(),
                    modifier = Modifier.width(40.dp)
                )
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

// Animated Occupancy Gauge
@Composable
fun AnimatedOccupancyGauge() {
    var progress by remember { mutableFloatStateOf(0f) }
    val targetValue = 0.78f // 78% occupancy

    LaunchedEffect(Unit) {
        while (progress < targetValue) {
            progress += 0.01f
            delay(16)
        }
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

            // Center text
            val percentage = (progress * 100).toInt()
            // Draw text would go here in actual implementation
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

// Additional Animated Charts

@Composable
fun AnimatedRevenueChart() {
    var progress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        progress = 1f
    }

    val data = listOf("Room 101" to 85f, "Room 205" to 72f, "Room 310" to 68f, "Room 410" to 55f, "Room 520" to 45f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        data.forEach { (label, value) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    color = Color(0xFF333333),
                    modifier = Modifier.width(70.dp)
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(20.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFE8E8E8))
                ) {
                    val animatedWidth = (value / 100f) * progress
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
                    text = "$${value.toInt()}K",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1A1A2E),
                    modifier = Modifier.width(45.dp)
                )
            }
        }
    }
}

@Composable
fun AnimatedPriceChart() {
    var progress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        progress = 1f
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(horizontal = 8.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val dataPoints = listOf(10f, 25f, 45f, 60f, 50f, 35f, 20f)
            val maxValue = 100f
            val barWidth = size.width / (dataPoints.size * 1.5f)
            val spacing = barWidth * 0.5f

            dataPoints.forEachIndexed { index, value ->
                val x = spacing + index * (barWidth + spacing)
                val barHeight = (value / maxValue) * size.height * progress
                val y = size.height - barHeight

                // Gradient bar
                val colors = listOf(
                    Color(0xFF4CAF50),
                    Color(0xFF2196F3)
                )

                drawRect(
                    brush = Brush.verticalGradient(
                        colors = colors,
                        startY = y,
                        endY = size.height
                    ),
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    style = Stroke(width = 0f)
                )

                // Rounded top
                drawRoundRect(
                    color = Color(0xFF4CAF50),
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(4f)
                )
            }
        }
    }
}

@Composable
fun AnimatedOccupancyItem(data: OccupancyData) {
    var progress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        progress = 1f
    }

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
                    .background(data.color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = data.icon,
                    fontSize = 20.sp
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = data.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1A1A2E)
                )
                Text(
                    text = "${data.bookings} bookings • ${data.revenue} revenue",
                    fontSize = 12.sp,
                    color = Color(0xFF666666)
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${data.occupancy}%",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (data.occupancy > 70) Color(0xFF4CAF50)
                    else if (data.occupancy > 40) Color(0xFFFF9800)
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
                            .fillMaxWidth((data.occupancy / 100f) * progress)
                            .fillMaxHeight()
                            .background(
                                if (data.occupancy > 70) Color(0xFF4CAF50)
                                else if (data.occupancy > 40) Color(0xFFFF9800)
                                else Color(0xFFFF6B6B)
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun AnimatedAreaOccupancy() {
    val areas = listOf(
        "Downtown" to 85f,
        "Uptown" to 72f,
        "Eastside" to 60f,
        "Westside" to 55f
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        areas.forEach { (name, value) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = name,
                    fontSize = 12.sp,
                    color = Color(0xFF333333),
                    modifier = Modifier.width(70.dp)
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
                            .fillMaxWidth((value / 100f) * progress)
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

@Composable
fun AnimatedRevenueList() {
    val rooms = listOf(
        "Penthouse Suite" to 15000,
        "Executive Room" to 12000,
        "Deluxe Room" to 8500,
        "Standard Room" to 5500
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        rooms.forEach { (name, revenue) ->
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
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF4CAF50))
                        )
                        Text(
                            text = name,
                            fontSize = 14.sp,
                            color = Color(0xFF1A1A2E)
                        )
                    }
                    Text(
                        text = "$${revenue}K",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                }
            }
        }
    }
}

@Composable
fun AIInsightsContent() {
    val insights = listOf(
        "📈 Bookings projected to increase 15% next quarter",
        "🎯 Downtown area shows highest growth potential",
        "💡 Weekend promotions driving 20% more revenue",
        "⭐ Guest satisfaction score up 12% this month"
    )

    var currentIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(2500)
            currentIndex = (currentIndex + 1) % insights.size
        }
    }

    AnimatedContent(
        targetState = currentIndex,
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
                text = insights[index],
                fontSize = 14.sp,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

@Composable
fun KeyMetricsGrid() {
    val metrics = listOf(
        "Revenue" to "$45.2K",
        "Bookings" to "1,284",
        "Occupancy" to "78%",
        "Satisfaction" to "4.8⭐"
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

// Data Classes
data class OccupancyData(
    val id: Int,
    val name: String,
    val icon: String,
    val color: Color,
    val bookings: Int,
    val revenue: String,
    val occupancy: Int
)

// Sample Data
val occupancyData = listOf(
    OccupancyData(1, "Downtown", "🏙️", Color(0xFF4CAF50), 342, "$45.2K", 85),
    OccupancyData(2, "Uptown", "🌆", Color(0xFF2196F3), 289, "$38.7K", 72),
    OccupancyData(3, "Eastside", "🌅", Color(0xFFFF9800), 240, "$32.1K", 60),
    OccupancyData(4, "Westside", "🌄", Color(0xFF9C27B0), 220, "$29.5K", 55),
    OccupancyData(5, "Southside", "🌇", Color(0xFFE91E63), 160, "$21.3K", 40)
)

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
fun AnimatedOccupancyChart() {
    var progress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        progress = 1f
    }

    val data = listOf("Downtown" to 85f, "Uptown" to 72f, "Eastside" to 60f, "Westside" to 55f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(horizontal = 8.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val maxValue = 100f
            val barWidth = size.width / (data.size * 1.8f)
            val spacing = barWidth * 0.8f

            data.forEachIndexed { index, (_, value) ->
                val x = spacing + index * (barWidth + spacing)
                val barHeight = (value / maxValue) * size.height * progress
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