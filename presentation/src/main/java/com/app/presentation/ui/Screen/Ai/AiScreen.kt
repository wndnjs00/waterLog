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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.app.presentation.ui.event.UiEvent
import com.app.presentation.viewModel.AiViewModel
import kotlinx.coroutines.flow.collect

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
            when(event) {
                is UiEvent.ShowToast -> Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
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

    Column(modifier = Modifier.fillMaxSize()) {

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages) { (isUser, text) ->
                ChatItem(isUser, text)
            }

            // 로딩 ...애니메이션
            if (isLoading) {
                item {
                    LoadingChatItem()
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("물섭취에 대해 질문해보세요") }
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    onSend(input)
                    input = ""
                },
                enabled = input.isNotBlank() && count < 3 && !isLoading
            ) {
                Text("전송")
            }
        }

        if (count >= 3) {
            Text(
                text = "오늘 질문 횟수를 모두 사용했습니다 (3/3)",
                modifier = Modifier.padding(8.dp),
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun ChatItem(isUser: Boolean, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            tonalElevation = 2.dp
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(12.dp)
            )
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

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Surface(tonalElevation = 2.dp) {
            Text(
                text = dots,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}


// ================= Preview =================
@Preview(showBackground = true)
@Composable
fun AiScreenPreview() {

    val fakeMessages = listOf(
        false to "반가워요! 오늘도 충분한 수분 섭취를 해보세요 💧",
        true to "물은 몇잔마시는게 좋나요?",
        false to "물섭취량은 개인상태에 따라 다를 수 있지만, 일반적으로 성인이 하루에 2리터를 마시는게 권장됩니다",
        true to "감사합니다",
        false to "ㅎㅎ",
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