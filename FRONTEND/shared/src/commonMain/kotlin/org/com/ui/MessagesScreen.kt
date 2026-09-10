package org.com.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.com.model.Conversation

private val PrimaryColor = Color(0xFF1A237E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(
    conversations: List<Conversation> = emptyList(),
    onBack: () -> Unit,
    onConversationClick: (Conversation) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Messages", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = PrimaryColor,
                    navigationIconContentColor = PrimaryColor
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8F9FA))
        ) {
            if (conversations.isEmpty()) {
                EmptyMessagesState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(conversations) { conversation ->
                        ConversationItem(conversation) { onConversationClick(conversation) }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConversationItem(conversation: Conversation, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(PrimaryColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = conversation.otherPartyName.take(1).uppercase(),
                    color = PrimaryColor,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = conversation.otherPartyName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF111111)
                    )
                    Text(
                        text = conversation.lastMessageTime,
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
                
                Spacer(Modifier.height(4.dp))
                
                Text(
                    text = conversation.lastMessage,
                    fontSize = 14.sp,
                    color = if (conversation.unreadCount > 0) Color.Black else Color.Gray,
                    fontWeight = if (conversation.unreadCount > 0) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            if (conversation.unreadCount > 0) {
                Spacer(Modifier.width(8.dp))
                Surface(
                    color = PrimaryColor,
                    shape = CircleShape,
                    modifier = Modifier.size(20.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = conversation.unreadCount.toString(),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyMessagesState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = PrimaryColor.copy(alpha = 0.05f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Email, null, tint = PrimaryColor, modifier = Modifier.size(40.dp))
                }
            }
            Spacer(Modifier.height(24.dp))
            Text("No messages yet", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111111))
            Spacer(Modifier.height(8.dp))
            Text(
                "Your conversations with property owners and agents will appear here.",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 40.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
