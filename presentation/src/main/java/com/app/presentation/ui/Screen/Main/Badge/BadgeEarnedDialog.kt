package com.app.presentation.ui.Screen.Main.Badge

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.app.presentation.R
import com.app.presentation.ui.components.WaterLogBaseDialog

@Composable
fun BadgeEarnedDialog(
    badgeKey: String,
    badgeName: String,
    onDismiss: () -> Unit
) {

    val scale = remember { Animatable(0.7f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            1.1f,
            animationSpec = tween(250)
        )
        scale.animateTo(
            1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy
            )
        )
    }

    WaterLogBaseDialog(
        onDismissRequest = onDismiss
    ) {

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .scale(scale.value),
            shape = androidx.compose.material3.MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "🎉 새로운 뱃지 획득!",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(20.dp))

                Image(
                    painter = painterResource(getBadgeImage(badgeKey)),
                    contentDescription = null,
                    modifier = Modifier.size(150.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = badgeName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "획득 완료",
                    color = Color(0xFF4CAF50)
                )
            }
        }
    }
}

fun getBadgeImage(key: String): Int {
    return when (key) {
        "day_2L" -> R.drawable.badge_active
        "week_7days" -> R.drawable.badge_active
        "month_30days" -> R.drawable.badge_active
        "king_6months" -> R.drawable.badge_active
        else -> R.drawable.badge_inactive
    }
}
