package com.app.presentation.ui.Screen.Auth

import android.annotation.SuppressLint
import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.app.domain.model.UserInfo
import com.app.presentation.R
import com.app.presentation.ui.Screens
import com.app.presentation.ui.theme.MainBlue
import com.app.presentation.viewModel.MainViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlin.math.log

@Composable
fun LoginScreen(
    viewModel: MainViewModel,
    credentialManager: CredentialManager,
    navController: NavController,
    onLoginSuccess: () -> Unit,
    onSignUpClick: () -> Unit,
    onSignInClick: () -> Unit,
    onKaKaoClick: () -> Unit,
    onNaverClick: () -> Unit,
) {
    val context = LocalContext.current
    val activity = context as? Activity ?: return
    val coroutineScope = rememberCoroutineScope()
    val firebaseAuth by lazy{FirebaseAuth.getInstance()}

    // 로그인되어있는 사용자 확인
    // 사용자 정보 가져오기(자동로그인 위함)
    LaunchedEffect(Unit) {
        if(firebaseAuth.currentUser != null){
            firebaseAuth.currentUser?.let{
                viewModel.signInGoogle(
                    UserInfo.UserInfoCreate(
                        uid = it.uid,
                        name = it.displayName ?: "",
                        email = it.email,
                        loginProvider = UserInfo.LoginProvider.GOOGLE,
                        timeProvider = viewModel.getTimeProvider()
                    )
                )

                // 바로 MainScreen으로 이동
                navController.navigate(Screens.Main.route) {
                    popUpTo(Screens.Login.route) { inclusive = true }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MainBlue),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "워터로그",
            color = Color.White,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.ExtraBold
            )
        )
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "매일 수분섭취를 기록하세요",
            color = Color.White.copy(alpha = 0.8f),
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(48.dp))

        Image(
            painter = painterResource(id = R.drawable.kakao_login_large_wide),
            contentDescription = "카카오톡 로그인 이미지",
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth(0.65f)
                .clickable {
                    onKaKaoClick()
                }
        )
        Spacer(modifier = Modifier.height(8.dp))

        // 네이버 로그인 버튼
        NaverLoginButton(onClick = onNaverClick)
        Spacer(modifier = Modifier.height(8.dp))

        // 구글 로그인 버튼클릭 동작구현
        GoogleLoginButton(
            onClick = {
                activity.let { activity ->
                    coroutineScope.launch {
                        GoogleOnClick(
                            viewModel = viewModel,
                            credentialManager = credentialManager,
                            activity = activity,
                            onLoginSuccess = {
                                onLoginSuccess()
                            }
                        )
                    }
                }
            })
        Spacer(modifier = Modifier.height(8.dp))

        // 이메일 회원가입 버튼
        EmailSignUpButton(onClick = onSignUpClick)
        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "이미 회원이신가요?",
            color = Color.White.copy(alpha = 0.8f),
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "기존 계정으로 로그인하기",
            color = Color.White,
            style = MaterialTheme.typography.bodySmall.copy(
                textDecoration = TextDecoration.Underline
            ),
            modifier = Modifier
                .clickable {
                    onSignInClick()
                }
        )
    }
}

@Composable
fun NaverLoginButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.65f)
            .height(40.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF03C75A))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_naver_logo),
            contentDescription = "네이버 로고",
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 4.dp)
                .size(32.dp)
        )
        Text(
            text = "네이버 로그인",
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(start = 12.dp)
        )
    }
}

@Composable
fun GoogleLoginButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.65f)
            .height(40.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFFF2F2F2))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_google_logo),
            contentDescription = "구글 로고",
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 4.dp)
                .size(32.dp)
        )
        Text(
            text = "Google 계정으로 로그인",
            color = Color.Black,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(start = 12.dp)
        )
    }
}

@Composable
fun EmailSignUpButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.65f)
            .height(40.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFFB4B4B4))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "이메일로 회원가입",
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(start = 12.dp)
        )
    }
}


@SuppressLint("ViewModelConstructorInComposable")
@Composable
@Preview
fun LoginScreenPreView() {
    val context = LocalContext.current
    val fakeCredentialManager = CredentialManager.create(context)

    val fakeViewModel = MainViewModel(
        accountUseCase = TODO(),
        timeProvider = TODO(),
    )

    LoginScreen(
        onLoginSuccess = {},
        onSignUpClick = {},
        onSignInClick = {},
        onKaKaoClick = {},
        onNaverClick = {},
        viewModel = fakeViewModel,
        credentialManager = fakeCredentialManager,
        navController = NavController(context)
    )
}
