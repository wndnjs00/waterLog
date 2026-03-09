package com.app.presentation.ui.Screen.Main

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.presentation.ui.theme.MainBlue
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.font.FontWeight

@Composable
fun CircularWaterProgress (cups: Int, target: Int, streak: Int) {
    val progress = if(target > 0) {
        cups.coerceAtMost(target).toFloat() / target
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(600),
        label = "water_animation"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(200.dp)
        ){
            CircularProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.size(180.dp),
                color = MainBlue,
                strokeWidth = 12.dp,
                trackColor = Color(0xFFE0E0E0),
            )

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEAF2FF)),
                contentAlignment = Alignment.Center
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(animatedProgress)
                        .align(Alignment.BottomCenter)
                        .background(MainBlue.copy(alpha = 0.5f))
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "$cups", fontSize = 32.sp, color = MainBlue, fontWeight = FontWeight.Bold)
                    Text(text = "/ $target 잔")
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))

        if (cups >= target) {
            Text(
                text = "🎉목표 달성!",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }else {
            Text(
                text = "${target - cups}잔 더 마셔요!",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "${streak}일 연속",
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}

@Composable
@Preview(showBackground = true, name = "목표달성 못함")
fun CircularWaterProgressPreview() {
    CircularWaterProgress(
        cups = 3,
        target = 8,
        streak = 5
    )
}

@Composable
@Preview(showBackground = true, name = "목표달성 함")
fun CircularWaterProgressPreview2() {
    CircularWaterProgress(
        cups = 10,
        target = 8,
        streak = 6
    )
}