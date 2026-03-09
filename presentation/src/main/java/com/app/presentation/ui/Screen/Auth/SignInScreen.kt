package com.app.presentation.ui.Screen.Auth


import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.presentation.ui.event.UiEvent
import com.app.presentation.viewModel.EmailAuthState
import com.app.presentation.viewModel.MainViewModel

@Composable
fun SignInScreen(
    viewModel: MainViewModel,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val signInState by viewModel.signInState.collectAsStateWithLifecycle()

    // 에러,안내 토스트 수신
    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is UiEvent.ShowToast -> Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                is UiEvent.NavigateToLogin -> { }
            }
        }
    }

    // 로그인 성공시 화면이동
    LaunchedEffect(signInState) {
        if (signInState is EmailAuthState.Success) {
            onLoginSuccess()
            viewModel.resetSignInState()
        }
    }

    // 유효성 검사
    val isValidEmail = remember(email) {
        Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    val isValidPassword = remember(password) {
        // 8자 이상, 영문,숫자,특수문자 포함
        Regex("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#\$%^&*()_+=-]).{8,}\$").matches(password)
    }
    val isFormValid =
        isValidEmail && isValidPassword && email.isNotBlank() && password.isNotBlank()
    val isLoading = signInState is EmailAuthState.Loading

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "이메일로 로그인",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold
            ),
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "워터로그와 함께 건강한 수분 섭취를 시작하세요",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(80.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("이메일") },
            placeholder = { Text("example@email.com") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        if (email.isNotBlank() && !isValidEmail) {
            Text(
                text = "이메일 주소형식에 맞게 입력해주세요",
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("비밀번호") },
            placeholder = { Text("8자 이상, 영문+숫자+특수문자 포함") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        if (password.isNotBlank() && !isValidPassword) {
            Text(
                text = "비밀번호는 8자 이상이며, 영문/숫자/특수문자를 모두 포함해야 합니다",
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(80.dp))

        Button(
            onClick = {
                viewModel.signInWithEmail(email, password)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = isFormValid && !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isFormValid && !isLoading) {
                    Color(0xFF4E7CF0)
                } else {
                    Color.LightGray
                }
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
            } else {
                Text(text = "로그인 하기", color = Color.White, fontSize = 16.sp)
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun SignInScreenPreview() {
    SignInScreen(
        viewModel = hiltViewModel(),
        onLoginSuccess = {},
    )
}
