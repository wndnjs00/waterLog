package com.app.presentation.ui.Screen.Auth

import android.content.Context
import android.util.Log
import com.app.domain.model.UserInfo
import com.app.presentation.viewModel.MainViewModel
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient

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
                loginWithKakaoNickName(
                    token = token,
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

// 카카오계정으로 로그인했을대, 이름만 가져오는 함수 따로 빼기
private fun loginWithKakaoNickName(
    token: OAuthToken,
    viewModel: MainViewModel,
    onLoginSuccess: () -> Unit
) {

    UserApiClient.instance.me { user, error ->

        when {
            error != null -> {
                Log.e("kakao", "카카오 닉네임 가져오기 실패", error)
            }

            user != null -> {

                viewModel.signIn(
                    UserInfo.UserInfoCreate(
                        uid = user.id?.toString().orEmpty(),
                        name = user.properties?.get("nickname").orEmpty(),
                        email = user.kakaoAccount?.email,
                        loginProvider = UserInfo.LoginProvider.KAKAO,
                        timeProvider = viewModel.getTimeProvider()
                    )
                )

                onLoginSuccess()
            }
        }
    }
}