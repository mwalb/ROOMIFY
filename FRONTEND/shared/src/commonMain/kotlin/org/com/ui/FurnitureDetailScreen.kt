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
                color = Color.White
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { uriHandler.openUri("tel:${furniture.contactPhone}") },
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Call, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Piga Simu")
                    }
                    Button(
                        onClick = { uriHandler.openUri("mailto:${furniture.contactEmail}") },
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
                    ) {
                        Icon(Icons.Default.Email, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Tuma Ujumbe")
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
            // Image Gallery
            Box(Modifier.fillMaxWidth().height(350.dp)) {
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
                                    Box(Modifier.fillMaxSize().background(Color(0xFFF0F2F5)), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(color = Color(0xFF6200EE), strokeWidth = 2.dp)
                                    }
                                },
                                onFailure = {
                                    Box(Modifier.fillMaxSize().background(Color(0xFFF0F2F5)), contentAlignment = Alignment.Center) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(Icons.Default.BrokenImage, null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                                            Text("Failed to load image", fontSize = 12.sp, color = Color.Gray)
                                        }
                                    }
                                }
                            )
                        }
                    }

                    // Counter
                    Surface(
                        modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                        color = Color.Black.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "${currentIndex.value + 1} / ${imageUrls.size}",
                            color = Color.White,
                            modifier = Modifier.padding(8.dp, 4.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                IconButton(
                    onClick = onBack,
                    modifier = Modifier.padding(16.dp).background(Color.Black.copy(alpha = 0.3f), CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                }
            }

            Column(Modifier.padding(24.dp)) {
                Surface(color = Color(0xFF6200EE).copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                    Text(
                        furniture.category.uppercase(),
                        modifier = Modifier.padding(8.dp, 4.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF6200EE)
                    )
                }

                Spacer(Modifier.height(12.dp))

                Text(furniture.title, fontSize = 28.sp, fontWeight = FontWeight.Black)
                Text(furniture.formattedPrice, fontSize = 24.sp, color = Color(0xFF6200EE), fontWeight = FontWeight.Black)

                Spacer(Modifier.height(24.dp))

                Text("Maelezo ya Bidhaa", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(furniture.description, color = Color.DarkGray, lineHeight = 24.sp)

                Spacer(Modifier.height(24.dp))

                // Seller Info
                Surface(
                    color = Color(0xFFF8F9FA),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color(0xFFEEEEEE))
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(48.dp).background(Color.White, CircleShape).border(1.dp, Color(0xFFEEEEEE), CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, null, tint = Color.Gray)
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text(furniture.ownerName ?: "Muuzaji", fontWeight = FontWeight.Bold)
                            Text("Imepostiwa hivi karibuni", fontSize = 12.sp, color = Color.Gray)
                        }
                        IconButton(onClick = { uriHandler.openUri("tel:${furniture.contactPhone}") }) {
                            Icon(Icons.Default.Phone, null, tint = Color(0xFF2E7D32))
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                Text("Eneo", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                    Icon(Icons.Default.LocationOn, null, tint = Color(0xFF6200EE))
                    Spacer(Modifier.width(8.dp))
                    Text(furniture.address ?: "Dar es Salaam", color = Color.Gray)
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = { uriHandler.openUri("https://www.google.com/maps/search/?api=1&query=${furniture.latitude},${furniture.longitude}") },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                ) {
                    Icon(Icons.Default.Map, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Angalia kwenye Ramani")
                }
            }
        }
    }
}
