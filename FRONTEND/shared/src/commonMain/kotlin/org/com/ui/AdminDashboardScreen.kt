package org.com.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.com.model.User

private val PrimaryColor = Color(0xFF1A237E)
private val PrimaryLight = Color(0xFF3949AB)

@Composable
fun AdminDashboardScreen(
    user: User,
    onLogout: () -> Unit,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit
) {
    val isSuperAdmin = user.role.equals("SUPER_ADMIN", ignoreCase = true)
    var selectedSection by remember { mutableStateOf("Overview") }

    Row(modifier = Modifier.fillMaxSize()) {
        // Sidebar
        Column(
            modifier = Modifier
                .width(260.dp)
                .fillMaxHeight()
                .background(PrimaryColor)
                .padding(24.dp)
        ) {
            Text(
                text = if (isSuperAdmin) "SUPER ADMIN" else "ADMIN PANEL",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black
            )
            
            Spacer(Modifier.height(32.dp))

            val menuItems = remember(isSuperAdmin) {
                mutableStateListOf(
                    MenuItem("Overview", Icons.Default.Dashboard),
                    MenuItem("Property Verification", Icons.Default.Verified),
                    MenuItem("Users", Icons.Default.People),
                    MenuItem("Owners", Icons.Default.Store),
                    MenuItem("Dalalis", Icons.Default.Handshake),
                    MenuItem("Tenants", Icons.Default.Person),
                    MenuItem("Bookings", Icons.Default.BookOnline),
                    MenuItem("Reports", Icons.Default.Assessment),
                    MenuItem("Suspended Properties", Icons.Default.Report)
                ).apply {
                    if (isSuperAdmin) {
                        add(MenuItem("Administrators", Icons.Default.AdminPanelSettings))
                    }
                }
            }

            menuItems.forEach { item ->
                AdminMenuItem(
                    item = item,
                    isSelected = selectedSection == item.title,
                    onClick = { selectedSection = item.title }
                )
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Logout, null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("Logout", color = Color.White)
            }
        }

        // Main Content
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(Color(0xFFF5F7FB))
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(Color.White)
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedSection,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryColor
                )
                
                Spacer(Modifier.weight(1f))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(user.name, fontWeight = FontWeight.Bold)
                        Text(user.role, fontSize = 12.sp, color = Color.Gray)
                    }
                    Spacer(Modifier.width(16.dp))
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(PrimaryColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(user.name.take(1), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Content Area
            Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                when (selectedSection) {
                    "Overview" -> AdminOverviewContent()
                    "Administrators" -> if (isSuperAdmin) AdministratorsContent()
                    else -> Text("Content for $selectedSection coming soon...")
                }
            }
        }
    }
}

data class MenuItem(val title: String, val icon: ImageVector)

@Composable
fun AdminMenuItem(item: MenuItem, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        color = if (isSelected) Color.White.copy(alpha = 0.15f) else Color.Transparent,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(item.icon, null, tint = Color.White, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(16.dp))
            Text(item.title, color = Color.White, fontSize = 14.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
        }
    }
}

@Composable
fun AdminOverviewContent() {
    Column {
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            StatCard("Total Properties", "1,284", Icons.Default.Home, Color(0xFF2196F3))
            StatCard("Active Users", "8,492", Icons.Default.People, Color(0xFF4CAF50))
            StatCard("Total Bookings", "432", Icons.Default.BookOnline, Color(0xFFFF9800))
            StatCard("Revenue", "TZS 4.2M", Icons.Default.Payments, Color(0xFFE91E63))
        }
        
        Spacer(Modifier.height(32.dp))
        
        Text("Recent Activities", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        // Add a list or table here
    }
}

@Composable
fun AdministratorsContent() {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Administrators Management", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Button(
                onClick = { /* Open create admin dialog */ },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("Create Admin")
            }
        }
        
        Spacer(Modifier.height(24.dp))
        
        // Admin List
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(Modifier.padding(16.dp)) {
                AdminListRow("Name", "Email", "Status", isHeader = true)
                HorizontalDivider()
                AdminListRow("Raphael Frank", "raphaelfrank02@gmail.com", "Active")
                AdminListRow("System Admin", "admin@roomify.com", "Active")
                AdminListRow("Helper Admin", "helper@roomify.com", "Disabled")
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, icon: ImageVector, color: Color) {
    Card(
        modifier = Modifier.width(220.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color)
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, color = Color.Gray, fontSize = 12.sp)
                Text(value, fontWeight = FontWeight.Black, fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun AdminListRow(name: String, email: String, status: String, isHeader: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(name, modifier = Modifier.weight(1f), fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal)
        Text(email, modifier = Modifier.weight(1.5f), color = if (isHeader) Color.Black else Color.Gray)
        Text(status, modifier = Modifier.weight(0.5f), color = if (status == "Active") Color(0xFF4CAF50) else if (isHeader) Color.Black else Color.Red)
        if (!isHeader) {
            Row {
                IconButton(onClick = {}) { Icon(Icons.Default.Edit, null, tint = Color.Gray) }
                IconButton(onClick = {}) { Icon(Icons.Default.Delete, null, tint = Color.Red) }
            }
        } else {
            Spacer(Modifier.width(96.dp))
        }
    }
}
