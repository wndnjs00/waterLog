@file:Suppress("UNREACHABLE_CODE")

package com.app.presentation.ui.Screen.Auth

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.presentation.viewModel.EmailAuthState
import com.app.presentation.viewModel.MainViewModel

@Composable
fun SignUpScreen(
    viewModel: MainViewModel,
    onSignUpComplete: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var allChecked by remember { mutableStateOf(false) }
    var termsChecked by remember { mutableStateOf(false) }
    var privacyChecked by remember { mutableStateOf(false) }

    val signUpState by viewModel.signUpState.collectAsStateWithLifecycle()

    // 회원가입 성공시 화면이동
    LaunchedEffect(signUpState) {
        if(signUpState is EmailAuthState.Success) {
            onSignUpComplete()
            viewModel.resetSignUpState()
        }
    }

    // 체크상태 자동동기화
    LaunchedEffect(termsChecked, privacyChecked) {
        allChecked = termsChecked && privacyChecked
    }

    // 유효성 검사
    val isValidEmail = remember(email) {
        Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    val isValidPassword = remember(password) {
        // 8자 이상, 영문,숫자,특수문자 포함
        Regex("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#\$%^&*()_+=-]).{8,}\$").matches(password)
    }

    val isPassWordMatch = password == confirmPassword && password.isNotBlank()
    val isFormValid =
        isValidEmail && isValidPassword && termsChecked && privacyChecked && isPassWordMatch
    val isLoading = signUpState is EmailAuthState.Loading

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "이메일로 회원가입",
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

        Spacer(modifier = Modifier.height(40.dp))

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

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("비밀번호 확인") },
            placeholder = { Text("비밀번호를 한번더 입력해주세요") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        if (!isPassWordMatch && confirmPassword.isNotBlank()) {
            Text(
                text = "비밀번호가 일치하지 않습니다",
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = allChecked,
                onCheckedChange = { checked ->
                    allChecked = checked
                    termsChecked = checked
                    privacyChecked = checked
                }
            )
            Text(text = "전체 동의", color = Color.Black, fontSize = 16.sp)
        }

        Divider(color = Color.LightGray)

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = termsChecked,
                    onCheckedChange = { termsChecked = it }
                )
                Text(text = "(필수) 서비스 이용약관", fontSize = 14.sp)
                Spacer(modifier = Modifier.weight(1f))
                Text(text = "보기", color = Color.Gray, fontSize = 14.sp)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = privacyChecked,
                    onCheckedChange = { privacyChecked = it }
                )
                Text(text = "(필수) 개인정보 처리방침", fontSize = 14.sp)
                Spacer(modifier = Modifier.weight(1f))
                Text(text = "보기", color = Color.Gray, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(60.dp))


        Button(
            onClick = { viewModel.signUpWithEmail(
                email = email,
                password = password,
                name = email.substringBefore("@"), //TODO: 입력받는걸로 수정하기?
            ) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = isFormValid && !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isFormValid) {
                    Color(0xFF4E7CF0)
                } else {
                    Color.LightGray
                }
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
            } else {
                Text(text = "가입하기", color = Color.White, fontSize = 16.sp)
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun SignUpScreenPreview() {
    SignUpScreen (
        viewModel = hiltViewModel(),
        onSignUpComplete = {}
    )
}
