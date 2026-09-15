package org.com.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import org.com.ui.AddressResult
import org.com.ui.PlatformLocationMap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerModal(
    currentLat: String?,
    currentLng: String?,
    initialSearch: String? = null,
    onConfirmed: (AddressResult) -> Unit,
    onDismiss: () -> Unit
) {
    var tempResult by remember { mutableStateOf<AddressResult?>(null) }
    val primaryColor = Color(0xFF1A237E)

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth().fillMaxHeight(),
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(0.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(Modifier.fillMaxSize()) {
                // Header
                Surface(tonalElevation = 4.dp, shadowElevation = 4.dp, color = Color.White) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Select Location", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null) }
                    }
                }

                // Map Area
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    PlatformLocationMap(
                        latitude = if (!currentLat.isNullOrBlank()) currentLat else null,
                        longitude = if (!currentLng.isNullOrBlank()) currentLng else null,
                        modifier = Modifier.fillMaxSize(),
                        initialSearch = initialSearch,
                        onLocationSelected = { result ->
                            tempResult = result
                        },
                        onLocationConfirmed = { result ->
                            tempResult = result
                            onConfirmed(result)
                        },
                        onDismiss = onDismiss
                    )
                }

                // Footer
                Surface(tonalElevation = 8.dp, shadowElevation = 12.dp, color = Color.White) {
                    Column(Modifier.fillMaxWidth().padding(20.dp)) {
                        Text("Selected location:", fontSize = 12.sp, color = Color.Gray)
                        Text(
                            text = tempResult?.address ?: "Click on the map to pin the location",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = primaryColor,
                            maxLines = 1
                        )
                        Text(
                            text = tempResult?.formattedAddress ?: "",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            maxLines = 1
                        )

                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { tempResult?.let { onConfirmed(it) } },
                            enabled = tempResult != null,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                        ) {
                            Text("CONFIRM LOCATION", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
