package com.app.presentation.ui.Screen.Auth

import android.content.Context
import android.util.Log
import com.app.domain.model.UserInfo
import com.app.presentation.viewModel.MainViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

fun kakaoLogin(
    context: Context,
    viewModel: MainViewModel,
    onLoginSuccess: () -> Unit,
) {

    val kakaoCallback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
        when {
            error != null -> {
                Log.e("kakao login", "카카오 계정 로그인 실패", error)
            }

            token != null -> {
                loginWithFirebaseCustomToken(
                    accessToken = token.accessToken,
                    viewModel = viewModel,
                    onLoginSuccess = onLoginSuccess
                )
            }
        }
    }

    if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {

        UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->

            if (error != null) {
                Log.e("kakao login", "카카오톡 로그인 실패", error)
            }

            if (error is ClientError &&
                error.reason == ClientErrorCause.Cancelled
            ) {
                return@loginWithKakaoTalk
            }

            UserApiClient.instance.loginWithKakaoAccount(
                context,
                callback = kakaoCallback
            )
        }

    } else {
        UserApiClient.instance.loginWithKakaoAccount(
            context,
            callback = kakaoCallback
        )
    }
}

private fun loginWithFirebaseCustomToken(
    accessToken: String,
    viewModel: MainViewModel,
    onLoginSuccess: () -> Unit
) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val functions = FirebaseFunctions.getInstance()
            val auth = FirebaseAuth.getInstance()

            val result = functions
                .getHttpsCallable("createCustomTokenWithKakao")
                .call(mapOf("accessToken" to accessToken))
                .await()

            val data = result.data as Map<*, *>

            // Firebase 로그인
            auth.signInWithCustomToken(data["customToken"] as String).await()

            // Firestore 저장
            viewModel.signIn(
                UserInfo.UserInfoCreate(
                    uid = data["uid"] as String,
                    name = data["nickname"] as String,
                    email = data["email"] as String?,
                    loginProvider = UserInfo.LoginProvider.KAKAO,
                    timeProvider = viewModel.getTimeProvider()
                )
            )

            launch(Dispatchers.Main) {
                onLoginSuccess()
            }

        } catch (e: Exception) {
            Log.e("FIREBASE_CUSTOM_LOGIN", "로그인 실패", e)
        }
    }
}