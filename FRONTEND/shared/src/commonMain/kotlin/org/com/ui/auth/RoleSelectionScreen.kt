package org.com.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.com.ui.AppColors

@Composable
fun RoleSelectionScreen(
    selectedRole: String,
    onRoleSelected: (String) -> Unit,
    onBack: () -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        AppColors.SplashGradientStart,
                        AppColors.SplashGradientEnd
                    )
                )
            )
            .padding(24.dp)
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = "Welcome to Roomify",
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Text(
                text = "Choose how you want to continue",
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 16.sp
            )

            Spacer(
                modifier = Modifier.height(32.dp)
            )

            RoleButton(
                title = "Tenant",
                selected = selectedRole == "tenant",
                onClick = {
                    onRoleSelected("tenant")
                }
            )

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            RoleButton(
                title = "Property Owner",
                selected = selectedRole == "owner",
                onClick = {
                    onRoleSelected("owner")
                }
            )

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            RoleButton(
                title = "Dalali / Agent",
                selected = selectedRole == "dalali",
                onClick = {
                    onRoleSelected("dalali")
                }
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.15f),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Back")
            }
        }
    }
}


@Composable
private fun RoleButton(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {

    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor =
                if (selected) {
                    Color.White
                } else {
                    Color.White.copy(alpha = 0.15f)
                },
            contentColor =
                if (selected) {
                    AppColors.SplashGradientStart
                } else {
                    Color.White
                }
        )
    ) {

        Text(
            text = title,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}