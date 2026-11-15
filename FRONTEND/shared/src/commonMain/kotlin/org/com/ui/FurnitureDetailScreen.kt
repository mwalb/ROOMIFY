package org.com.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import org.com.model.Furniture
import org.com.ui.components.AIRoomArrangerDialog

private val BrandNavy = Color(0xFF1A237E)
private val BrandIndigo = Color(0xFF3949AB)
private val BrandBlueLight = Color(0xFFEEF2FF)
private val BrandAmber = Color(0xFFFF9800)
private val BrandAmberLight = Color(0xFFFFF7ED)
private val BrandSlate = Color(0xFF1E293B)
private val BackgroundSurface = Color(0xFFF8FAFC)
private val BorderColor = Color(0xFFE2E8F0)
private val BrandGreen = Color(0xFF10B981)

@Composable
fun FurnitureDetailScreen(
    furniture: Furniture,
    onBack: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    val scrollState = rememberScrollState()
    var showAIArranger by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(BackgroundSurface)) {
        val isWideScreen = maxWidth >= 768.dp
        val horizontalPadding = if (isWideScreen) 48.dp else 20.dp

        Scaffold(
            containerColor = BackgroundSurface,
            bottomBar = {
                Surface(
                    tonalElevation = 10.dp,
                    shadowElevation = 20.dp,
                    color = Color.White,
                    modifier = Modifier.navigationBarsPadding()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = horizontalPadding, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 800.dp)
                                .align(Alignment.Center),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            if (!furniture.contactPhone.isNullOrBlank()) {
                                OutlinedButton(
                                    onClick = { uriHandler.openUri("tel:${furniture.contactPhone}") },
                                    modifier = Modifier.weight(1f).height(54.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.5.dp, BrandNavy),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandNavy)
                                ) {
                                    Icon(Icons.Default.Call, null, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Piga Simu", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }
                            }

                            if (!furniture.contactEmail.isNullOrBlank()) {
                                Button(
                                    onClick = { uriHandler.openUri("mailto:${furniture.contactEmail}") },
                                    modifier = Modifier.weight(1f).height(54.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandNavy),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                                ) {
                                    Icon(Icons.Default.Email, null, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Tuma Ujumbe", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }
                            }
                        }
                    }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Image Gallery Banner
                val imageUrls = furniture.fullImageUrls
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isWideScreen) 440.dp else 340.dp)
                ) {
                    if (imageUrls.isNotEmpty()) {
                        val lazyListState = rememberLazyListState()
                        val currentIndex = remember { derivedStateOf { lazyListState.firstVisibleItemIndex } }

                        LazyRow(
                            state = lazyListState,
                            modifier = Modifier.fillMaxSize(),
                            flingBehavior = rememberSnapFlingBehavior(lazyListState)
                        ) {
                            itemsIndexed(imageUrls) { index, url ->
                                KamelImage(
                                    resource = { asyncPainterResource(url) },
                                    contentDescription = "${furniture.title} $index",
                                    modifier = Modifier.fillParentMaxSize(),
                                    contentScale = ContentScale.Crop,
                                    onLoading = {
                                        Box(
                                            modifier = Modifier.fillMaxSize().background(Color(0xFFF1F5F9)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(color = BrandNavy, strokeWidth = 2.5.dp)
                                        }
                                    },
                                    onFailure = {
                                        Box(
                                            modifier = Modifier.fillMaxSize().background(Color(0xFFF1F5F9)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Icon(Icons.Default.BrokenImage, null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                                                Spacer(Modifier.height(4.dp))
                                                Text("Picha haipatikani", fontSize = 12.sp, color = Color.Gray)
                                            }
                                        }
                                    }
                                )
                            }
                        }

                        // Gradient Scrim Overlay at top for back button contrast
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent)
                                    )
                                )
                        )

                        // Badge / Index Counter
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp),
                            color = BrandSlate.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.PhotoLibrary, null, tint = BrandAmber, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "${currentIndex.value + 1} / ${imageUrls.size}",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        // Empty Image Placeholder
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(listOf(BrandBlueLight, Color(0xFFF1F5F9)))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Chair, null, tint = BrandNavy.copy(alpha = 0.4f), modifier = Modifier.size(72.dp))
                                Spacer(Modifier.height(8.dp))
                                Text("Roomify Furniture", fontWeight = FontWeight.Bold, color = BrandNavy)
                            }
                        }
                    }

                    // Floating Glassmorphic Back Button
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .statusBarsPadding()
                            .padding(16.dp)
                            .size(42.dp)
                            .background(Color.Black.copy(alpha = 0.35f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Rudi Nyuma",
                            tint = Color.White
                        )
                    }
                }

                // Content Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 840.dp)
                        .padding(horizontal = horizontalPadding, vertical = 22.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Category & Condition Tags
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = BrandNavy,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = furniture.category.ifBlank { "SAMANI" }.uppercase(),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.8.sp,
                                color = Color.White
                            )
                        }

                        if (furniture.condition.isNotBlank()) {
                            Surface(
                                color = BrandAmberLight,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, BrandAmber.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = furniture.condition,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFC2410C)
                                )
                            }
                        }
                    }

                    // Title and Price
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = furniture.title,
                            fontSize = if (isWideScreen) 30.sp else 24.sp,
                            fontWeight = FontWeight.Black,
                            color = BrandSlate,
                            lineHeight = 32.sp
                        )

                        Text(
                            text = furniture.formattedPrice,
                            fontSize = if (isWideScreen) 26.sp else 22.sp,
                            fontWeight = FontWeight.Black,
                            color = BrandNavy
                        )
                    }

                    // AI Room Arranger / Visualizer Banner (Powered by Gemini)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showAIArranger = true },
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFF6200EE).copy(alpha = 0.06f),
                        border = BorderStroke(1.5.dp, Color(0xFF6200EE).copy(alpha = 0.25f)),
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFF6200EE), Color(0xFF03DAC5))
                                        ),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AutoAwesome, null, tint = Color.White, modifier = Modifier.size(24.dp))
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "✨ Jaribu Chumbani Kwako (AI)",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.sp,
                                        color = Color(0xFF6200EE)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Surface(
                                        color = Color(0xFF6200EE),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            "GEMINI",
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    "Tazama jinsi '${furniture.title}' inavyokaa chumbani kabla ya kuinunua",
                                    fontSize = 12.sp,
                                    color = Color(0xFF475569)
                                )
                            }
                            Icon(Icons.Default.ChevronRight, null, tint = Color(0xFF6200EE), modifier = Modifier.size(20.dp))
                        }
                    }

                    // Description Card
                    if (furniture.description.isNotBlank()) {
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(18.dp),
                            border = BorderStroke(1.dp, BorderColor),
                            shadowElevation = 1.dp
                        ) {
                            Column(Modifier.padding(18.dp)) {
                                Text(
                                    text = "Maelezo ya Bidhaa",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandSlate
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = furniture.description,
                                    color = Color(0xFF475569),
                                    lineHeight = 22.sp,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    // Seller / Shop Information Card
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, BorderColor),
                        shadowElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFFEEF2FF), Color.White)
                                    )
                                )
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .background(BrandNavy, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (furniture.shopId != null) Icons.Default.Storefront else Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = if (furniture.shopId != null) "Duka / Karakana" else "Muuzaji Binafsi",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                                Text(
                                    text = furniture.ownerName ?: "Mfanyabiashara wa Roomify",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    color = BrandNavy
                                )
                                if (!furniture.contactPhone.isNullOrBlank()) {
                                    Text(
                                        text = furniture.contactPhone,
                                        fontSize = 12.sp,
                                        color = Color(0xFF475569)
                                    )
                                }
                            }
                            if (!furniture.contactPhone.isNullOrBlank()) {
                                IconButton(
                                    onClick = { uriHandler.openUri("tel:${furniture.contactPhone}") },
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(BrandGreen.copy(alpha = 0.15f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = "Piga Simu",
                                        tint = BrandGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Location Card with Google Maps action
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, BorderColor),
                        shadowElevation = 1.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Eneo Inapopatikana",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandSlate
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(BrandBlueLight, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.LocationOn, null, tint = BrandNavy, modifier = Modifier.size(18.dp))
                                }
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = furniture.address ?: "Dar es Salaam, Tanzania",
                                    color = Color(0xFF475569),
                                    fontSize = 14.sp
                                )
                            }

                            if (furniture.latitude != 0.0 && furniture.longitude != 0.0) {
                                Button(
                                    onClick = {
                                        uriHandler.openUri("https://www.google.com/maps/search/?api=1&query=${furniture.latitude},${furniture.longitude}")
                                    },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandNavy)
                                ) {
                                    Icon(Icons.Default.Map, null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Angalia kwenye Ramani (Google Maps)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(60.dp))
                }
            }
        }
    }

    if (showAIArranger) {
        AIRoomArrangerDialog(
            initialFurnitureName = furniture.title,
            onDismiss = { showAIArranger = false }
        )
    }
}
