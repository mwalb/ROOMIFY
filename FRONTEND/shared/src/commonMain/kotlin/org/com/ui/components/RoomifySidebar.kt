package org.com.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.com.model.User
import org.com.model.initials

private val PrimaryColor = Color(0xFF1A237E)
private val BackgroundGray = Color(0xFFF8F9FA)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomifySidebar(
    user: User?,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    onSearch: (type: String?, area: String?, maxPrice: Double?, status: String?) -> Unit,
    onClose: () -> Unit
) {
    var areaQuery by remember { mutableStateOf("") }

    ModalDrawerSheet(
        drawerContainerColor = Color.White,
        drawerTonalElevation = 0.dp,
        modifier = Modifier.width(320.dp).fillMaxHeight(),
        drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Header Section
            SidebarHeader(onClose)
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                // 2. Explore Roomify
                Text(
                    "Explore Roomify",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ExploreActionCard(
                        title = "Rentals",
                        subtitle = "Find a room",
                        icon = Icons.Default.Home,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("map"); onClose() }
                    )
                    ExploreActionCard(
                        title = "Furniture",
                        subtitle = "Shop hub",
                        icon = Icons.Default.Chair,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("furniture_dashboard"); onClose() }
                    )
                }
                
                Spacer(Modifier.height(32.dp))
                
                // 3. Quick Search
                Text(
                    "Quick Search",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Icon(Icons.Default.Search, null, tint = PrimaryColor, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        BasicTextField(
                            value = areaQuery,
                            onValueChange = { query ->
                                areaQuery = query
                                onSearch(null, query.ifBlank { null }, null, null)
                            },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            decorationBox = { inner ->
                                if (areaQuery.isEmpty()) Text("Where to?", color = Color.Gray, fontSize = 14.sp)
                                inner()
                            }
                        )
                    }
                }
            }

            // 4. Footer (Login / Register)
            HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                if (user == null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onNavigate("login"); onClose() }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Login, null, tint = PrimaryColor, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Login / Register", fontWeight = FontWeight.Bold, color = PrimaryColor, fontSize = 15.sp)
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onNavigate("profile"); onClose() }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(PrimaryColor.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(user.initials, color = PrimaryColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(user.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                            Text("Account Settings", fontSize = 11.sp, color = Color.Gray)
                        }
                        IconButton(onClick = onLogout) {
                            Icon(Icons.AutoMirrored.Filled.Logout, null, tint = Color.Red, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SidebarHeader(onClose: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(listOf(PrimaryColor, PrimaryColor.copy(alpha = 0.85f))),
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
            )
            .padding(top = 40.dp, bottom = 30.dp, start = 24.dp, end = 24.dp)
    ) {
        Column {
            Text(
                text = "Roomify",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "Your living space partner",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
        
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 12.dp, y = (-20).dp)
                .size(32.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.15f))
        ) {
            Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun ExploreActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(20.dp),
        color = BackgroundGray,
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(icon, null, tint = PrimaryColor, modifier = Modifier.size(28.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.Black)
                Text(subtitle, fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}
