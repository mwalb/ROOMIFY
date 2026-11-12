package org.com.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import org.com.model.Furniture

private val PrimaryColor = Color(0xFF1A237E)
private val PrimaryLight = Color(0xFF3949AB)
private val AccentColor = Color(0xFF00897B)
private val TextDark = Color(0xFF0F172A)
private val TextMuted = Color(0xFF475569)

@Composable
fun FurnitureDetailScreen(
    furniture: Furniture,
    onBack: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    val scrollState = rememberScrollState()

    Scaffold(
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 16.dp,
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                        .navigationBarsPadding(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    OutlinedButton(
                        onClick = { uriHandler.openUri("tel:${furniture.contactPhone}") },
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.5.dp, PrimaryColor),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryColor)
                    ) {
                        Icon(Icons.Default.Call, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Piga Simu", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Button(
                        onClick = { uriHandler.openUri("mailto:${furniture.contactEmail}") },
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                    ) {
                        Icon(Icons.Default.Email, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Tuma Ujumbe", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
        ) {
            // Image Gallery Header
            Box(Modifier.fillMaxWidth().height(360.dp)) {
                val imageUrls = furniture.fullImageUrls
                if (imageUrls.isNotEmpty()) {
                    val lazyListState = rememberLazyListState()
                    val currentIndex = remember { derivedStateOf { lazyListState.firstVisibleItemIndex } }

                    LazyRow(
                        state = lazyListState,
                        modifier = Modifier.fillMaxSize(),
                        flingBehavior = rememberSnapFlingBehavior(lazyListState)
                    ) {
                        items(imageUrls) { url ->
                            KamelImage(
                                resource = { asyncPainterResource(url) },
                                contentDescription = null,
                                modifier = Modifier.fillParentMaxSize(),
                                contentScale = ContentScale.Crop,
                                onLoading = {
                                    Box(Modifier.fillMaxSize().background(Color(0xFFF1F5F9)), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(color = PrimaryColor, strokeWidth = 2.dp, modifier = Modifier.size(32.dp))
                                    }
                                },
                                onFailure = {
                                    Box(Modifier.fillMaxSize().background(Color(0xFFF1F5F9)), contentAlignment = Alignment.Center) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(Icons.Default.BrokenImage, null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                                            Spacer(Modifier.height(8.dp))
                                            Text("Picha haipatikani", fontSize = 12.sp, color = TextMuted)
                                        }
                                    }
                                }
                            )
                        }
                    }

                    // Dots & Number Indicator
                    Surface(
                        modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                        color = Color.Black.copy(alpha = 0.65f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Text(
                                "${currentIndex.value + 1} / ${imageUrls.size}",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    Box(Modifier.fillMaxSize().background(Color(0xFFF1F5F9)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Chair, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                    }
                }

                // Enhanced Contrast Back Button
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .padding(16.dp)
                        .size(42.dp)
                        .background(Color.White.copy(alpha = 0.92f), CircleShape)
                        .border(1.dp, Color(0xFFE2E8F0), CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Rudi", tint = PrimaryColor, modifier = Modifier.size(20.dp))
                }
            }

            Column(Modifier.padding(24.dp)) {
                // Category & Condition Tags
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = PrimaryColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            furniture.category.uppercase(),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = PrimaryColor
                        )
                    }

                    Surface(
                        color = if (furniture.status == "SOLD") Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            if (furniture.status == "SOLD") "IMEUZWA" else furniture.condition.ifBlank { "MPYA" },
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (furniture.status == "SOLD") Color(0xFFD32F2F) else Color(0xFF2E7D32)
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Title & Price
                Text(furniture.title, fontSize = 24.sp, fontWeight = FontWeight.Black, color = TextDark)
                Spacer(Modifier.height(4.dp))
                Text(furniture.formattedPrice, fontSize = 22.sp, color = PrimaryColor, fontWeight = FontWeight.Black)

                Spacer(Modifier.height(20.dp))
                HorizontalDivider(color = Color(0xFFF1F5F9))
                Spacer(Modifier.height(20.dp))

                // Description
                Text("Maelezo ya Bidhaa", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Spacer(Modifier.height(6.dp))
                Text(
                    furniture.description.ifBlank { "Hakuna maelezo ya ziada yaliyowekwa kwa bidhaa hii." },
                    color = TextMuted,
                    lineHeight = 22.sp,
                    fontSize = 14.sp
                )

                Spacer(Modifier.height(24.dp))

                // Seller Information Card
                Surface(
                    color = Color(0xFFF8FAFC),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .background(PrimaryColor.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, null, tint = PrimaryColor, modifier = Modifier.size(24.dp))
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(furniture.ownerName ?: "Muuzaji / Fundi", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextDark)
                            Text("Muuza Samani & Vifaa Roomify", fontSize = 12.sp, color = TextMuted)
                        }
                        IconButton(
                            onClick = { uriHandler.openUri("tel:${furniture.contactPhone}") },
                            modifier = Modifier.background(Color(0xFFE8F5E9), CircleShape).size(38.dp)
                        ) {
                            Icon(Icons.Default.Phone, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Location Details
                Text("Eneo & Ramani", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = PrimaryColor, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(furniture.address ?: "Dar es Salaam, Tanzania", color = TextMuted, fontSize = 14.sp)
                }

                Spacer(Modifier.height(14.dp))

                Button(
                    onClick = { uriHandler.openUri("https://www.google.com/maps/search/?api=1&query=${furniture.latitude},${furniture.longitude}") },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                ) {
                    Icon(Icons.Default.Map, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Fungua Ramani ya Eneo", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                // Bottom Padding Spacer to prevent obstruction by sticky bottom bar
                Spacer(Modifier.height(96.dp))
            }
        }
    }
}
