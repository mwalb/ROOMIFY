package org.com.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import org.com.model.Furniture
import org.com.model.Shop

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopDetailScreen(
    shop: Shop,
    shopFurniture: List<Furniture>,
    onBack: () -> Unit,
    onViewFurniture: (Furniture) -> Unit
) {
    val uriHandler = LocalUriHandler.current
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(shop.name, fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
        ) {
            // Header Image
            Box(Modifier.fillMaxWidth().height(250.dp)) {
                if (shop.fullImageUrls.isNotEmpty()) {
                    KamelImage(
                        resource = { asyncPainterResource(shop.fullImageUrls.first()) },
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(Modifier.fillMaxSize().background(Color(0xFFF0F2F5)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Storefront, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                    }
                }

                if (shop.hasVideo) {
                    Surface(
                        modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp).clickable {
                            if (shop.videoUrl != null) uriHandler.openUri(shop.getFullUrl(shop.videoUrl))
                        },
                        color = Color.Red,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("WATCH DEMO", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }

            Column(Modifier.padding(24.dp)) {
                Text(shop.name, fontSize = 28.sp, fontWeight = FontWeight.Black)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                    Icon(Icons.Default.LocationOn, null, tint = Color(0xFF6200EE), modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(shop.address ?: "Dar es Salaam", color = Color.Gray, fontSize = 14.sp)
                }

                Spacer(Modifier.height(24.dp))
                Text("Kuhusu Duka Hili", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(shop.description, color = Color.DarkGray, lineHeight = 24.sp)

                Spacer(Modifier.height(24.dp))
                Text("Mawasiliano", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Row(Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { uriHandler.openUri("tel:${shop.contactPhone}") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Call, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Piga Simu")
                    }
                    Button(
                        onClick = { uriHandler.openUri("https://www.google.com/maps/search/?api=1&query=${shop.latitude},${shop.longitude}") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                    ) {
                        Icon(Icons.Default.Map, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Ramani")
                    }
                }

                Spacer(Modifier.height(32.dp))
                Text("Bidhaa za Duka Hili", fontSize = 20.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(16.dp))

                if (shopFurniture.isEmpty()) {
                    Box(Modifier.fillMaxWidth().height(150.dp).background(Color(0xFFF8F9FA), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                        Text("Bado hakuna bidhaa zilizopostiwa", color = Color.Gray)
                    }
                } else {
                    shopFurniture.forEach { item ->
                        FurnitureCard(item, onClick = { onViewFurniture(item) })
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}
