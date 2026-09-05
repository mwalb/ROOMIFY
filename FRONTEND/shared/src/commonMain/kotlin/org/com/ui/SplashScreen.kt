package org.com.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.alexzhirkevich.compottie.*
import kotlinx.coroutines.delay
import org.com.i18n.LocalRoomifyStrings
import org.jetbrains.compose.resources.ExperimentalResourceApi
import roomifytz.shared.generated.resources.Res

object AppColors {
    val SplashGradientStart = Color(0xFF1A237E)
    val SplashGradientEnd   = Color(0xFF3949AB)
    val AppNameColor        = Color(0xFFFFFFFF)
    val TaglineColor        = Color(0xB3FFFFFF)
    val LoadingTextColor    = Color(0x80FFFFFF)
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun SplashScreen(
    onLoadingComplete: () -> Unit = {}
) {
    val strings = LocalRoomifyStrings.current

    // Load Lottie composition
    val composition by rememberLottieComposition {
        LottieCompositionSpec.JsonString(
            Res.readBytes("files/city_splash.json").decodeToString()
        )
    }

    // Track if composition is ready
    var isReady by remember { mutableStateOf(false) }

    // Start timer ONLY after animation is loaded
    LaunchedEffect(composition) {
        if (composition != null) {
            isReady = true
            delay(3000) // 3 seconds of actual play time
            onLoadingComplete()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        AppColors.SplashGradientStart,
                        AppColors.SplashGradientEnd
                    )
                )
            )
    ) {
        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.25f)
                        )
                    )
                )
        )

        // Decorative Circle 1
        Box(
            modifier = Modifier
                .size(200.dp)
                .offset(x = (-100).dp, y = (-100).dp)
                .alpha(0.1f)
                .background(Color.White, shape = CircleShape)
                .align(Alignment.TopStart)
        )

        // Decorative Circle 2
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = 150.dp, y = 150.dp)
                .alpha(0.1f)
                .background(Color.White, shape = CircleShape)
                .align(Alignment.BottomEnd)
        )

        // Center content
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Lottie Animation - only show when loaded
            if (isReady) {
                Image(
                    painter = rememberLottiePainter(
                        composition = composition,
                        iterations = Compottie.IterateForever
                    ),
                    contentDescription = "City splash animation",
                    modifier = Modifier.size(280.dp)
                )
            } else {
                // Show a loading placeholder while Lottie loads
                Box(
                    modifier = Modifier.size(280.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "",
                        color = AppColors.AppNameColor,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // App Name
            Box(
                modifier = Modifier
                    .shadow(4.dp, shape = RoundedCornerShape(24.dp))
                    .background(
                        color = Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    text = strings.appName.lowercase().replaceFirstChar { it.uppercase() },
                    color = AppColors.AppNameColor,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif
                )
            }

            // Tagline
            Text(
                text = strings.tagline,
                color = AppColors.TaglineColor,
                fontSize = 16.sp,
                letterSpacing = 0.5.sp,
                fontFamily = FontFamily.SansSerif
            )
        }

        // Loading text
        Text(
            text = if (isReady) strings.loading else "",
            color = AppColors.LoadingTextColor,
            fontSize = 12.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp)
        )
    }
}
