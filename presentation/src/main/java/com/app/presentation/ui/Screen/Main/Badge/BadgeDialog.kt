package com.app.presentation.ui.Screen.Main.Badge

import androidx.compose.animation.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.app.domain.constants.BadgeType
import com.app.presentation.R
import com.app.presentation.ui.components.WaterLogBaseDialog
import com.app.presentation.ui.theme.MainBlue

@Composable
fun BadgeDialog (
    badgeKey: String,
    onDismiss: () -> Unit
) {
    val badgeName = when (badgeKey) {
        BadgeType.DAY_2L -> "하루 2L 달성"
        BadgeType.WEEK_7DAYS -> "7일 연속 달성"
        BadgeType.MONTH_30DAYS -> "30일 연속 달성"
        BadgeType.KING_6MONTHS -> "6개월 달성"
        else -> "뱃지"
    }


    val badgeImage = when (badgeKey) {
        BadgeType.DAY_2L, BadgeType.WEEK_7DAYS,BadgeType.MONTH_30DAYS,BadgeType.KING_6MONTHS -> R.drawable.badge_active
        else -> R.drawable.badge_active
    }

    WaterLogBaseDialog(
        onDismissRequest = onDismiss
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 애니메이션
            val scale = remember { androidx.compose.animation.core.Animatable(0.5f) }

            LaunchedEffect(Unit) {
                scale.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(600)
                )
            }

            Image(
                painter = painterResource(id = badgeImage),
                contentDescription = "뱃지 이미지",
                modifier = Modifier
                    .size(120.dp)
                    .graphicsLayer {
                        scaleX = scale.value
                        scaleY = scale.value
                    }
            )

            Text(
                text = badgeName,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 12.dp)
            )

            Text(
                text = "획득 완료 🎉",
                color = MainBlue,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onDismiss
            ) {
                Text("확인")
            }
        }
    }
}