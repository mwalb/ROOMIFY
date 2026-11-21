package org.com.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import org.com.ui.theme.RoomifyColors

/* ============================================================
   SHIMMER / SKELETON LOADERS
   ============================================================ */

/** Animated shimmer background used for skeleton placeholders. */
fun Modifier.shimmer(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val x by transition.animateFloat(
        initialValue = -400f,
        targetValue = 1400f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmerX"
    )
    background(
        Brush.linearGradient(
            colors = listOf(
                RoomifyColors.Skeleton,
                RoomifyColors.SkeletonHighlight,
                RoomifyColors.Skeleton
            ),
            start = Offset(x, 0f),
            end = Offset(x + 400f, 0f)
        )
    )
}

@Composable
fun SkeletonBox(modifier: Modifier = Modifier, cornerRadius: Int = 12) {
    Box(modifier.clip(RoundedCornerShape(cornerRadius.dp)).shimmer())
}

/** Placeholder that mimics a property card while rooms are loading. */
@Composable
fun SkeletonRoomCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            SkeletonBox(Modifier.fillMaxWidth().height(200.dp), cornerRadius = 0)
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SkeletonBox(Modifier.fillMaxWidth(0.6f).height(18.dp), 6)
                SkeletonBox(Modifier.fillMaxWidth(0.4f).height(12.dp), 6)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SkeletonBox(Modifier.width(70.dp).height(12.dp), 6)
                    SkeletonBox(Modifier.width(70.dp).height(12.dp), 6)
                }
            }
        }
    }
}

@Composable
fun SkeletonRoomList(count: Int = 3) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        repeat(count) { SkeletonRoomCard() }
    }
}

/* ============================================================
   EMPTY / ERROR STATES
   ============================================================ */

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(RoomifyColors.Primary.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = RoomifyColors.Primary, modifier = Modifier.size(34.dp))
        }
        Text(
            title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = RoomifyColors.TextStrong,
            textAlign = TextAlign.Center
        )
        if (subtitle != null) {
            Text(
                subtitle,
                fontSize = 13.sp,
                color = RoomifyColors.TextMuted,
                textAlign = TextAlign.Center
            )
        }
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(4.dp))
            Button(
                onClick = onAction,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RoomifyColors.Primary)
            ) {
                Text(actionLabel, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/* ============================================================
   IMAGES (shimmer while loading, fade-in, graceful failure)
   ============================================================ */

@Composable
fun RoomifyImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    KamelImage(
        resource = { asyncPainterResource(url) },
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        animationSpec = tween(durationMillis = 450),
        onLoading = { Box(Modifier.fillMaxSize().shimmer()) },
        onFailure = {
            Box(
                Modifier.fillMaxSize().background(RoomifyColors.Skeleton),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.BrokenImage,
                    contentDescription = "Image unavailable",
                    tint = RoomifyColors.TextMuted,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    )
}

/* ============================================================
   BEFORE / AFTER COMPARISON SLIDER
   ============================================================ */

@Composable
fun BeforeAfterSlider(
    beforeUrl: String,
    afterUrl: String,
    modifier: Modifier = Modifier
) {
    var fraction by remember { mutableStateOf(0.5f) }

    BoxWithConstraints(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(RoomifyColors.Skeleton)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    fraction = (offset.x / size.width).coerceIn(0f, 1f)
                }
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, _ ->
                    fraction = (change.position.x / size.width).coerceIn(0f, 1f)
                }
            }
    ) {
        val handleX = maxWidth * fraction

        // AFTER (full width, underneath)
        RoomifyImage(
            url = afterUrl,
            contentDescription = "After",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // BEFORE (clipped to the left of the handle)
        Box(
            Modifier
                .fillMaxSize()
                .drawWithContent {
                    clipRect(right = size.width * fraction) {
                        this@drawWithContent.drawContent()
                    }
                }
        ) {
            RoomifyImage(
                url = beforeUrl,
                contentDescription = "Before",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Divider line + knob
        Box(
            Modifier
                .offset(x = handleX - 1.dp)
                .width(2.dp)
                .fillMaxSize()
                .background(Color.White)
        )
        Surface(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = handleX - 20.dp)
                .size(40.dp),
            shape = CircleShape,
            color = Color.White,
            shadowElevation = 6.dp,
            border = BorderStroke(1.dp, RoomifyColors.Primary.copy(alpha = 0.2f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.UnfoldMore,
                    contentDescription = "Drag to compare",
                    tint = RoomifyColors.Primary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Labels
        LabelChip("BEFORE", Modifier.align(Alignment.TopStart).padding(12.dp))
        LabelChip("AFTER", Modifier.align(Alignment.TopEnd).padding(12.dp))
    }
}

@Composable
private fun LabelChip(text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = Color.Black.copy(alpha = 0.55f),
        shape = RoundedCornerShape(50)
    ) {
        Text(
            text,
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

/* ============================================================
   BRAND AMBIENT BACKGROUND & MOTION MODIFIERS
   ============================================================ */

/**
 * Applies the signature Roomify soft indigo-pearl gradient background with
 * gently breathing ambient light orbs in the corners so every screen feels
 * alive while keeping cards crisp and readable.
 */
fun Modifier.roomifyAmbientBackground(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "ambientBg")
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambientDrift"
    )
    this
        .background(RoomifyColors.PageBackgroundBrush)
        .drawWithContent {
            val topGlowCenter = Offset(
                x = size.width * (0.85f - 0.08f * drift),
                y = size.height * (0.10f + 0.05f * drift)
            )
            val bottomGlowCenter = Offset(
                x = size.width * (0.12f + 0.08f * drift),
                y = size.height * (0.88f - 0.05f * drift)
            )
            val radius = size.minDimension * 0.65f.coerceAtLeast(1f)
            if (radius > 10f) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(RoomifyColors.AmbientGlowIndigo, Color.Transparent),
                        center = topGlowCenter,
                        radius = radius
                    ),
                    radius = radius,
                    center = topGlowCenter
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(RoomifyColors.AmbientGlowAmber, Color.Transparent),
                        center = bottomGlowCenter,
                        radius = radius * 0.85f
                    ),
                    radius = radius * 0.85f,
                    center = bottomGlowCenter
                )
            }
            drawContent()
        }
}

/**
 * Smooth entrance fade + upward slide when a composable first enters the composition.
 */
@Composable
fun AnimatedEntrance(
    delayMillis: Int = 0,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        if (delayMillis > 0) kotlinx.coroutines.delay(delayMillis.toLong())
        visible = true
    }
    androidx.compose.animation.AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = androidx.compose.animation.fadeIn(tween(380)) +
                androidx.compose.animation.slideInVertically(tween(380)) { it / 14 }
    ) {
        content()
    }
}

