package com.app.presentation.ui.Screen.Main

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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

@Composable
fun CircularWaterProgress (cups: Int, target: Int) {
    val progress = if(target > 0) {
        cups.coerceAtMost(target).toFloat() / target
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(600),
        label = "water_animation"
    )

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
                Text(text = "$cups", fontSize = 32.sp, color = MainBlue)
                Text(text = "/ $target 잔")
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun CircularWaterProgressPreview() {
    CircularWaterProgress(
        cups = 3,
        target = 5,
    )
}