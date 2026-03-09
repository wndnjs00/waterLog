package com.app.presentation.ui.Screen.Auth

import android.content.Context
import com.app.domain.model.UserInfo
import com.app.presentation.viewModel.MainViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.messaging.FirebaseMessaging
import com.navercorp.nid.NidOAuth
import com.navercorp.nid.oauth.util.NidOAuthCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


fun naverLogin(
    context: Context,
    viewModel: MainViewModel,
    onLoginSuccess: () -> Unit
) {
    val nidOAthCallback = object : NidOAuthCallback {
        override fun onSuccess() {
            val accessToken = NidOAuth.getAccessToken()
            if (accessToken != null) {
                loginWithFirebaseCustomTokenFromNaver(
                    accessToken = accessToken,
                    viewModel = viewModel,
                    onLoginSuccess = onLoginSuccess
                )
            } else {
                viewModel.showOAuthError(Exception("AccessToken is null"))
            }
        }

        override fun onFailure(errorCode: String, errorDesc: String) {
            viewModel.showOAuthError(Exception("$errorCode: $errorDesc"))
        }

    }

    // 최신 로그인 호출
    NidOAuth.requestLogin(context, nidOAthCallback)
}


private fun loginWithFirebaseCustomTokenFromNaver(
    accessToken: String,
    viewModel: MainViewModel,
    onLoginSuccess: () -> Unit
) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val functions = FirebaseFunctions.getInstance()
            val auth = FirebaseAuth.getInstance()

            val result = functions
                .getHttpsCallable("createCustomTokenWithNaver")
                .call(mapOf("accessToken" to accessToken))
                .await()

            val data = result.data as Map<*, *>

            auth.signInWithCustomToken(data["customToken"] as String).await()

            viewModel.saveUser(
                UserInfo.userInfoCreate(
                    uid = data["uid"] as String,
                    name = data["nickname"] as String,
                    email = data["email"] as String,
                    loginProvider = UserInfo.LoginProvider.NAVER,
                    timeProvider = viewModel.getTimeProvider(),
                )
            )

            // FCM Token 저장
            FirebaseMessaging.getInstance().token
                .addOnSuccessListener { token ->
                    viewModel.saveFcmToken(token)
                }

            launch(Dispatchers.Main){
                onLoginSuccess()
            }

        } catch (e: Exception) {
            viewModel.showOAuthError(e)
        }
    }
}