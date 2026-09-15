package org.com.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

private val MagicGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF6200EE), Color(0xFF03DAC5))
)

@Composable
fun FantasticBubble(
    isVisible: Boolean,
    onSearchFurniture: () -> Unit,
    onPostFurniture: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(1000)) + scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Surface(
                modifier = Modifier
                    .size(70.dp)
                    .shadow(16.dp, CircleShape)
                    .clip(CircleShape)
                    .background(MagicGradient)
                    .clickable { showDialog = true },
                color = Color.Transparent
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Furniture Magic",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
    }

    if (showDialog) {
        Dialog(
            onDismissRequest = { showDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(16.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(MagicGradient, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Chair, null, tint = Color.White, modifier = Modifier.size(40.dp))
                    }

                    Spacer(Modifier.height(24.dp))

                    Text(
                        "Karibu kwenye Roomify Furniture!",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF1A237E)
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        "Chagua unachotaka kufanya leo:",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(32.dp))

                    // Option 1: Search
                    Button(
                        onClick = {
                            showDialog = false
                            onSearchFurniture()
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
                    ) {
                        Icon(Icons.Default.Chair, null)
                        Spacer(Modifier.width(12.dp))
                        Text("Unatafuta thamani/vitu vya ndani?", fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(12.dp))

                    // Option 2: Post
                    OutlinedButton(
                        onClick = {
                            showDialog = false
                            onPostFurniture()
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(2.dp, Color(0xFF03DAC5))
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, null, tint = Color(0xFF018786))
                        Spacer(Modifier.width(12.dp))
                        Text("Namiliki bidhaa au nataka kupost", color = Color(0xFF018786), fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(24.dp))

                    TextButton(onClick = { showDialog = false }) {
                        Text("Baadaye", color = Color.Gray)
                    }
                }
            }
        }
    }
}
