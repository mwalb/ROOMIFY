package org.com.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.serialization.Serializable
import org.com.model.User

private val PrimaryColor = Color(0xFF1A237E)
private val PrimaryLight = Color(0xFF3949AB)

@Serializable
data class Message(
    val id: String,
    val senderId: Long,
    val content: String,
    val timestamp: Long
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    currentUser: User,
    otherUserName: String,
    onBack: () -> Unit
) {
    var messageText by remember { mutableStateOf("") }
    val messages = remember {
        mutableStateListOf(
            Message("1", 0L, "Hi, is this room still available?", 0L),
            Message("2", currentUser.id, "Yes, it is! Would you like to schedule a viewing?", 0L)
        )
    }
    val listState = rememberLazyListState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(PrimaryColor, PrimaryLight))),
        contentAlignment = Alignment.Center
    ) {
        // Main Centered Card
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .widthIn(max = 480.dp)
                .fillMaxHeight(0.85f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = PrimaryColor, modifier = Modifier.size(20.dp))
                    }
                    
                    Spacer(Modifier.width(8.dp))
                    
                    Box(
                        Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(PrimaryColor.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(otherUserName.take(1).uppercase(), fontWeight = FontWeight.Black, color = PrimaryColor, fontSize = 14.sp)
                    }
                    
                    Spacer(Modifier.width(12.dp))
                    
                    Column {
                        Text(otherUserName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1A1A1A))
                        Text("Online", fontSize = 11.sp, color = Color(0xFF4CAF50), fontWeight = FontWeight.Medium)
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // Chat Messages Area
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages) { msg ->
                        val isMine = msg.senderId == currentUser.id
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = if (isMine) Alignment.CenterEnd else Alignment.CenterStart
                        ) {
                            Surface(
                                color = if (isMine) PrimaryColor else Color(0xFFF0F2F5),
                                contentColor = if (isMine) Color.White else Color.Black,
                                shape = RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isMine) 16.dp else 4.dp,
                                    bottomEnd = if (isMine) 4.dp else 16.dp
                                )
                            ) {
                                Text(
                                    text = msg.content,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                // Input Area - Reference style (compact)
                HorizontalDivider(color = Color(0xFFF0F0F0))
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        placeholder = { Text("Type a message...", fontSize = 13.sp) },
                        modifier = Modifier.weight(1f).heightIn(min = 44.dp, max = 100.dp),
                        shape = RoundedCornerShape(22.dp),
                        maxLines = 4,
                        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryColor,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            cursorColor = PrimaryColor
                        )
                    )
                    
                    Spacer(Modifier.width(8.dp))
                    
                    IconButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                messages.add(Message(messages.size.toString(), currentUser.id, messageText, 0L))
                                messageText = ""
                            }
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(PrimaryColor)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
