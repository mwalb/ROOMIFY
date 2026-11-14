package org.com.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import org.com.model.Furniture
import org.com.model.Shop

private val BrandNavy = Color(0xFF1A237E)
private val BrandIndigo = Color(0xFF3949AB)
private val BrandBlueLight = Color(0xFFEEF2FF)
private val BrandAmber = Color(0xFFFF9800)
private val BrandSlate = Color(0xFF1E293B)
private val BackgroundSurface = Color(0xFFF8FAFC)
private val BorderColor = Color(0xFFE2E8F0)

@Composable
fun ShopDetailScreen(
    shop: Shop,
    shopFurniture: List<Furniture>,
    onBack: () -> Unit,
    onViewFurniture: (Furniture) -> Unit
) {
    val uriHandler = LocalUriHandler.current
    val scrollState = rememberScrollState()

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(BackgroundSurface)) {
        val isWideScreen = maxWidth >= 768.dp
        val horizontalPadding = if (isWideScreen) 48.dp else 20.dp

        Scaffold(
            containerColor = BackgroundSurface,
            topBar = {
                // Branded Hero Top App Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.horizontalGradient(listOf(BrandNavy, BrandIndigo)))
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.White.copy(alpha = 0.18f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Rudi Nyuma",
                                tint = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "ROOMIFY STORE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.2.sp,
                                    color = BrandAmber
                                )
                                Spacer(Modifier.width(6.dp))
                                Surface(
                                    color = Color.White.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "${shopFurniture.size} BIDHAA",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Text(
                                text = shop.name,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                maxLines = 1
                            )
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
                // Header Image Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isWideScreen) 340.dp else 240.dp)
                ) {
                    if (shop.fullImageUrls.isNotEmpty()) {
                        KamelImage(
                            resource = { asyncPainterResource(shop.fullImageUrls.first()) },
                            contentDescription = shop.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                            onLoading = {
                                Box(Modifier.fillMaxSize().background(Color(0xFFF1F5F9)), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = BrandNavy, strokeWidth = 2.dp)
                                }
                            }
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.linearGradient(listOf(BrandBlueLight, Color(0xFFF1F5F9)))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Storefront, null, modifier = Modifier.size(64.dp), tint = BrandNavy.copy(alpha = 0.35f))
                        }
                    }

                    if (shop.hasVideo && !shop.videoUrl.isNullOrBlank()) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp)
                                .clickable { uriHandler.openUri(shop.getFullUrl(shop.videoUrl!!)) },
                            color = Color(0xFFEF4444),
                            shape = RoundedCornerShape(12.dp),
                            shadowElevation = 4.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("ANGALIA VIDEO", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }

                // Shop Information Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 840.dp)
                        .padding(horizontal = horizontalPadding, vertical = 22.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Title and Address Card
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, BorderColor),
                        shadowElevation = 1.dp
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = shop.name,
                                fontSize = if (isWideScreen) 28.sp else 22.sp,
                                fontWeight = FontWeight.Black,
                                color = BrandSlate
                            )

                            Spacer(Modifier.height(8.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .background(BrandBlueLight, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.LocationOn, null, tint = BrandNavy, modifier = Modifier.size(16.dp))
                                }
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = shop.address ?: "Dar es Salaam, Tanzania",
                                    color = Color(0xFF475569),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    // Description Card
                    if (shop.description.isNotBlank()) {
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(18.dp),
                            border = BorderStroke(1.dp, BorderColor),
                            shadowElevation = 1.dp
                        ) {
                            Column(Modifier.padding(18.dp)) {
                                Text(
                                    text = "Kuhusu Duka Hili",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandSlate
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = shop.description,
                                    color = Color(0xFF475569),
                                    lineHeight = 22.sp,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    // Contact Actions Card
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
                                text = "Mawasiliano na Eneo",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandSlate
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                if (!shop.contactPhone.isNullOrBlank()) {
                                    OutlinedButton(
                                        onClick = { uriHandler.openUri("tel:${shop.contactPhone}") },
                                        modifier = Modifier.weight(1f).height(50.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        border = BorderStroke(1.5.dp, BrandNavy),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandNavy)
                                    ) {
                                        Icon(Icons.Default.Call, null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("Piga Simu", fontWeight = FontWeight.Bold)
                                    }
                                }

                                if (shop.latitude != 0.0 && shop.longitude != 0.0) {
                                    Button(
                                        onClick = {
                                            uriHandler.openUri("https://www.google.com/maps/search/?api=1&query=${shop.latitude},${shop.longitude}")
                                        },
                                        modifier = Modifier.weight(1f).height(50.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = BrandNavy)
                                    ) {
                                        Icon(Icons.Default.Map, null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("Ramani", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // Products in Shop Section
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Bidhaa za Duka Hili",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = BrandSlate
                            )
                            Surface(
                                color = BrandBlueLight,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "${shopFurniture.size} zipo",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandNavy,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        if (shopFurniture.isEmpty()) {
                            Surface(
                                color = Color.White,
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, BorderColor),
                                modifier = Modifier.fillMaxWidth().height(140.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.Inventory2, null, tint = Color.LightGray, modifier = Modifier.size(40.dp))
                                        Spacer(Modifier.height(8.dp))
                                        Text("Bado hakuna bidhaa zilizopostiwa chini ya duka hili", color = Color.Gray, fontSize = 13.sp)
                                    }
                                }
                            }
                        } else {
                            shopFurniture.forEach { item ->
                                FurnitureCard(furniture = item, onClick = { onViewFurniture(item) })
                            }
                        }
                    }

                    Spacer(Modifier.height(60.dp))
                }
            }
        }
    }
}
