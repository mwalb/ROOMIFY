package org.com.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlin.math.roundToInt

private val PrimaryColor = Color(0xFF1A237E)
private val AccentColor = Color(0xFF3949AB)
private val GlassColor = Color.White.copy(alpha = 0.85f)

enum class FurnitureType(val label: String, val icon: ImageVector, val width: Dp, val height: Dp, val color: Color) {
    BED("Bed", Icons.Default.Bed, 130.dp, 170.dp, Color(0xFF5C6BC0)),
    SOFA("Sofa", Icons.Default.Chair, 150.dp, 75.dp, Color(0xFF66BB6A)),
    TV("TV/Entertainment", Icons.Default.Tv, 100.dp, 40.dp, Color(0xFF424242)),
    DESK("Work Desk", Icons.Default.DesktopMac, 110.dp, 65.dp, Color(0xFF8D6E63)),
    WARDROBE("Wardrobe", Icons.Default.Window, 95.dp, 55.dp, Color(0xFF78909C)),
    TABLE("Dining Table", Icons.Default.TableRestaurant, 85.dp, 85.dp, Color(0xFFFFCA28)),
    PLANT("Indoor Plant", Icons.Default.Yard, 50.dp, 50.dp, Color(0xFF43A047)),
    LAMP("Floor Lamp", Icons.Default.Light, 40.dp, 40.dp, Color(0xFFFFEB3B)),
    RUG("Area Rug", Icons.Default.Layers, 180.dp, 120.dp, Color(0xFFBDBDBD))
}

data class PlacedFurniture(
    val id: Long,
    val type: FurnitureType,
    var position: Offset,
    var rotation: Float = 0f,
    var zIndex: Int = 0
)

@Composable
fun SpacePlannerDialog(
    roomArea: Double,
    roomImageUrl: String? = null,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            SpacePlannerContent(roomArea, roomImageUrl, onDismiss)
        }
    }
}

@Composable
private fun SpacePlannerContent(roomArea: Double, roomImageUrl: String?, onDismiss: () -> Unit) {
    var placedFurniture by remember { mutableStateOf(emptyList<PlacedFurniture>()) }
    var idCounter by remember { mutableStateOf(0L) }
    var containerSize by remember { mutableStateOf(Offset.Zero) }
    var maxZIndex by remember { mutableStateOf(0) }

    val density = LocalDensity.current

    Column(modifier = Modifier.fillMaxSize()) {
        // Advanced Header
        Surface(
            tonalElevation = 12.dp,
            shadowElevation = 12.dp,
            color = PrimaryColor
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss, modifier = Modifier.background(Color.White.copy(alpha = 0.15f), CircleShape)) {
                    Icon(Icons.Default.Close, null, tint = Color.White)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Interactive Room Planner", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SquareFoot, null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("${roomArea.toInt()} m² Floor Space", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                
                OutlinedButton(
                    onClick = { placedFurniture = emptyList(); maxZIndex = 0 },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("RESET", fontSize = 12.sp, fontWeight = FontWeight.Black)
                }
            }
        }

        // Planner Canvas with Background
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFF121212))
                .onGloballyPositioned { coordinates ->
                    containerSize = Offset(coordinates.size.width.toFloat(), coordinates.size.height.toFloat())
                }
        ) {
            // Room Image Background
            if (!roomImageUrl.isNullOrBlank()) {
                KamelImage(
                    resource = { asyncPainterResource(roomImageUrl) },
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().graphicsLayer { alpha = 0.4f },
                    contentScale = ContentScale.Crop
                )
            }

            // Grid Background Overlay
            Canvas(modifier = Modifier.fillMaxSize()) {
                val gridSize = 32.dp.toPx()
                for (x in 0..(size.width / gridSize).toInt()) {
                    drawLine(Color.White.copy(alpha = 0.1f), Offset(x * gridSize, 0f), Offset(x * gridSize, size.height))
                }
                for (y in 0..(size.height / gridSize).toInt()) {
                    drawLine(Color.White.copy(alpha = 0.1f), Offset(0f, y * gridSize), Offset(size.width, y * gridSize))
                }
            }

            if (placedFurniture.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(Icons.Default.AddHome, null, tint = Color.White, modifier = Modifier.size(56.dp))
                            Spacer(Modifier.height(16.dp))
                            Text("Virtual Room Ready", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("Select furniture below to arrange your space", color = Color.White.copy(alpha = 0.6f), textAlign = TextAlign.Center, fontSize = 13.sp)
                        }
                    }
                }
            }

            // Placed Furniture with Z-Index Support
            placedFurniture.sortedBy { it.zIndex }.forEach { item ->
                FurnitureItemView(
                    item = item,
                    onMove = { newOffset ->
                        placedFurniture = placedFurniture.map {
                            if (it.id == item.id) it.copy(position = newOffset) else it
                        }
                    },
                    onRotate = {
                        placedFurniture = placedFurniture.map {
                            if (it.id == item.id) it.copy(rotation = (it.rotation + 45f) % 360f) else it
                        }
                    },
                    onRemove = {
                        placedFurniture = placedFurniture.filter { it.id != item.id }
                    },
                    onBringToFront = {
                        maxZIndex++
                        placedFurniture = placedFurniture.map {
                            if (it.id == item.id) it.copy(zIndex = maxZIndex) else it
                        }
                    }
                )
            }
        }

        // Advanced Furniture Toolbar
        Surface(
            tonalElevation = 24.dp,
            shadowElevation = 24.dp,
            color = Color.White,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
        ) {
            Column(Modifier.padding(top = 20.dp, bottom = 32.dp, start = 20.dp, end = 20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(4.dp, 16.dp).background(PrimaryColor, CircleShape))
                    Spacer(Modifier.width(8.dp))
                    Text("FURNITURE LIBRARY", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color.Gray, letterSpacing = 1.sp)
                }
                Spacer(Modifier.height(16.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(FurnitureType.values()) { type ->
                        ToolbarFurnitureItem(type) {
                            val centerX = (containerSize.x / 2) - (with(density) { type.width.toPx() } / 2)
                            val centerY = (containerSize.y / 2) - (with(density) { type.height.toPx() } / 2)
                            maxZIndex++
                            placedFurniture = placedFurniture + PlacedFurniture(
                                id = idCounter++,
                                type = type,
                                position = Offset(centerX, centerY),
                                zIndex = maxZIndex
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FurnitureItemView(
    item: PlacedFurniture,
    onMove: (Offset) -> Unit,
    onRotate: () -> Unit,
    onRemove: () -> Unit,
    onBringToFront: () -> Unit
) {
    var isDragging by remember { mutableStateOf(false) }

    val rotationAnim by animateFloatAsState(item.rotation, animationSpec = spring(stiffness = Spring.StiffnessLow))
    val scale by animateFloatAsState(if (isDragging) 1.15f else 1.0f)
    val alpha by animateFloatAsState(if (isDragging) 0.8f else 1.0f)
    val elevation by animateDpAsState(if (isDragging) 24.dp else 4.dp)

    // Entrance Animation
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    AnimatedVisibility(
        visible = isVisible,
        enter = scaleIn(initialScale = 0.5f) + fadeIn(),
        modifier = Modifier.offset { IntOffset(item.position.x.roundToInt(), item.position.y.roundToInt()) }
    ) {
        Box(
            modifier = Modifier
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { 
                            isDragging = true
                            onBringToFront()
                        },
                        onDragEnd = { isDragging = false },
                        onDragCancel = { isDragging = false },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            onMove(item.position + dragAmount)
                        }
                    )
                }
                .graphicsLayer {
                    rotationZ = rotationAnim
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha
                }
                .shadow(elevation, RoundedCornerShape(12.dp))
                .background(item.type.color.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                .background(GlassColor, RoundedCornerShape(12.dp))
                .border(2.dp, if (isDragging) item.type.color else Color.Transparent, RoundedCornerShape(12.dp))
                .width(item.type.width)
                .height(item.type.height),
            contentAlignment = Alignment.Center
        ) {
            // Realistic Furniture Representation
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(4.dp)
            ) {
                Icon(
                    item.type.icon, 
                    null, 
                    tint = item.type.color, 
                    modifier = Modifier.size(if (item.type.width < 60.dp) 20.dp else 32.dp)
                )
                if (item.type.width > 60.dp) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        item.type.label, 
                        fontSize = 9.sp, 
                        fontWeight = FontWeight.Black, 
                        color = item.type.color,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Interactive Controls (visible when not dragging)
            if (!isDragging) {
                // Top Actions
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 10.dp, y = (-10).dp)
                ) {
                    ActionCircle(Icons.Default.Delete, Color.Red) { onRemove() }
                }
                
                // Bottom Actions
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 10.dp, y = 10.dp)
                ) {
                    ActionCircle(Icons.Default.RotateRight, PrimaryColor) { onRotate() }
                }
            }
        }
    }
}

@Composable
private fun ActionCircle(icon: ImageVector, color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .shadow(4.dp, CircleShape)
            .background(color, CircleShape)
            .border(1.5.dp, Color.White, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun ToolbarFurnitureItem(type: FurnitureType, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(90.dp)
    ) {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(20.dp),
            color = type.color.copy(alpha = 0.08f),
            border = BorderStroke(1.dp, type.color.copy(alpha = 0.15f)),
            modifier = Modifier.fillMaxWidth().aspectRatio(1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(type.icon, null, tint = type.color, modifier = Modifier.size(32.dp))
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(type.label, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color.DarkGray)
    }
}
