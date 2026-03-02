package com.app.presentation.ui.Screen.Main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.presentation.R
import com.app.presentation.ui.theme.MainBlue

@Composable
fun WaterControlSection(
    cups: Int,
    onAdd: () -> Unit,
    onRemove: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Text(
            text = "물 한 잔 마셨나요?",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            RoundedButton(
                backgroundColor = Color(0xFFE0E0E0),
                contentColor = Color.DarkGray,
                text = "-",
                onClick = onRemove
            )

            Box(
                modifier = Modifier
                    .width(150.dp)
                    .height(110.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFEAF2FF)),
                contentAlignment = Alignment.Center
            ) {

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    Image(
                        painter = painterResource(id = R.drawable.water_drop_img),
                        contentDescription = "water_image",
                        modifier = Modifier.size(28.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "${cups * 250} ml",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.DarkGray
                    )
                }
            }

            RoundedButton(
                backgroundColor = MainBlue,
                contentColor = Color.White,
                text = "+",
                onClick = onAdd
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "한 잔 = 250ml 기준",
            fontSize = 13.sp,
            color = Color.Black
        )
    }
}


@Composable
fun RoundedButton(
    backgroundColor: Color,
    contentColor: Color,
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(10.dp)
            .size(72.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
    }
}

@Composable
@Preview(showBackground = true)
fun WaterControlSectionPreview() {
    WaterControlSection(
        cups = 3,
        onAdd = {},
        onRemove = {}
    )
}