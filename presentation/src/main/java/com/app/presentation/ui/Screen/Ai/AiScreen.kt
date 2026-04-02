package com.app.presentation.ui.Screen.Ai

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.app.presentation.R
import com.app.presentation.ui.event.UiEvent
import com.app.presentation.viewModel.AiViewModel
import kotlinx.coroutines.flow.collect
import retrofit2.http.Header

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AiScreen(
    viewModel: AiViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val messages by viewModel.message.collectAsState()
    val count by viewModel.count.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is UiEvent.ShowToast -> Toast.makeText(context, event.message, Toast.LENGTH_SHORT)
                    .show()

                else -> {}
            }
        }
    }

    AiScreenContent(
        messages = messages,
        count = count,
        isLoading = isLoading,
        onSend = { viewModel.send(it) }
    )
}

@Composable
fun AiScreenContent(
    messages: List<Pair<Boolean, String>>,
    count: Int,
    isLoading: Boolean,
    onSend: (String) -> Unit
) {
    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val recommendList = listOf(
        "하루 권장량", "물 마시는 시간", "수분 부족 증상", "운동 후 수분", "수분 섭취 팁"
    )

    // 메시지 추가될때 자동 스크롤
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {

        Header()

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            if (messages.isEmpty()) {
                item {
                    BotIntro()
                }

                item {
                    RecommendChips(
                        list = recommendList, onClick = { onSend(it) })
                }
            }

            items(messages) { (isUser, text) ->
                ChatItem(isUser, text)
            }

            // 로딩 ...애니메이션
            if (isLoading) {
                item { LoadingChatItem() }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
        ) {

            if (count >= 3) {
                Text(
                    text = "오늘 질문 횟수를 모두 사용했습니다",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    textAlign = TextAlign.Center
                )
            }

            InputBar(
                input = input,
                onChange = { input = it },
                onSend = {
                    onSend(input)
                    input = ""
                },
                enabled = input.isNotBlank() && count < 3 && !isLoading
            )
        }
    }
}


@Composable
fun Header() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Text(
            text = "AI 수분 섭취 도우미", style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "⚠ AI 질문은 하루 3번으로 제한됩니다.\nAI 답변은 정확하지 않을 수 있어요.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

@Composable
fun BotIntro() {
    Row(
        verticalAlignment = Alignment.Top
    ) {
        AiProfile()

        Spacer(modifier = Modifier.width(8.dp))

        Column {
            Text("위티", style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(12.dp)
            ) {
                Text(
                    "안녕하세요! 어떤 점이 궁금하세요?",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun RecommendChips(
    list: List<String>, onClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(start = 40.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 1.dp, color = Color.LightGray, shape = RoundedCornerShape(16.dp)
            )
            .background(Color.White)
            .padding(8.dp)
    ) {
        list.chunked(2).forEach { row ->
            Row {
                row.forEach {
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFE3F2FD))
                            .clickable { onClick(it) }
                            .padding(horizontal = 12.dp, vertical = 8.dp)) {
                        Text(it)
                    }
                }
            }
        }
    }
}

@Composable
fun ChatItem(isUser: Boolean, text: String) {

    if (isUser) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1976D2))
                    .padding(12.dp)
            ) {
                Text(
                    text,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            AiProfile()

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Text("위티", style = MaterialTheme.typography.labelMedium)

                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .padding(12.dp)
                ) {
                    Text(
                        text,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}


@Composable
fun LoadingChatItem() {
    val infiniteTransition = rememberInfiniteTransition()

    val dotCount by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        )
    )

    val dots = ".".repeat(dotCount.toInt())

    Row {
        AiProfile()

        Spacer(modifier = Modifier.width(6.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(12.dp)
        ) {
            Text(dots)
        }
    }
}

@Composable
fun InputBar(
    input: String, onChange: (String) -> Unit, onSend: () -> Unit, enabled: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .border(
                    width = 1.dp, color = Color.LightGray, shape = RoundedCornerShape(24.dp)
                )
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            if (input.isEmpty()) {
                Text(
                    text = "수분 섭취에 대해 질문해보세요.", color = Color.Gray
                )
            }
            BasicTextField(
                value = input,
                onValueChange = onChange,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
            onClick = onSend, enabled = enabled, modifier = Modifier
                .clip(CircleShape)
                .background(
                    if (enabled) Color(0xFF1976D2) else Color.LightGray
                )
        ) {
            Icon(
                imageVector = Icons.Default.Send, contentDescription = "send", tint = Color.White
            )
        }
    }
}

@Composable
fun AiProfile() {
    Image(
        painter = painterResource(id = R.drawable.ai_helper_img),
        contentDescription = "AI 이미지",
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape),
        contentScale = ContentScale.Crop
    )
}

// ================= Preview =================
@Preview(showBackground = true)
@Composable
fun AiScreenPreview() {

    val fakeMessages = listOf(
        false to "반가워요! 오늘도 충분한 수분 섭취를 해보세요 💧",
        true to "물은 몇잔마시는게 좋나요?",
        false to "물섭취량은 개인상태에 따라 다를 수 있지만, 일반적으로 성인이 하루에 2리터를 마시는게 권장됩니다.물섭취량은 개인상태에 따라 다를 수 있지만, 일반적으로 성인이 하루에 2리터를 마시는게 권장됩니다물섭취량은 개인상태에 따라 다를 수 있지만, 일반적으로 성인이 하루에 2리터를 마시는게 권장됩니다물섭취량은 개인상태에 따라 다를 수 있지만, 일반적으로 성인이 하루에 2리터를 마시는게 권장됩니다",
        true to "감사합니다",
        false to "ㅎㅎ",
        true to "ㅎㅎㅎ",
        false to "ㅎㅎㅎㅎㅎㅎ",
        true to "ㅎㅎㅎ",
        false to "ㅎㅎㅎㅎㅎㅎ",
        true to "ㅎㅎㅎ",
        false to "ㅎㅎㅎㅎㅎㅎ",
    )

    AiScreenContent(
        messages = fakeMessages,
        count = 1,
        onSend = {},
        isLoading = true,
    )
}

// ================= 질문 제한 상태 Preview =================
@Preview(showBackground = true)
@Composable
fun AiScreenLimitPreview() {

    val fakeMessages = listOf(
        false to "오늘 질문은 여기까지예요!"
    )

    AiScreenContent(
        messages = fakeMessages,
        count = 3,
        onSend = {},
        isLoading = false,
    )
}

// ================= 첫 진입 Preview =================
@Preview(showBackground = true)
@Composable
fun AiScreenWelcomePreview() {

    AiScreenContent(
        messages = emptyList(),
        count = 0,
        onSend = {},
        isLoading = false,
    )
}