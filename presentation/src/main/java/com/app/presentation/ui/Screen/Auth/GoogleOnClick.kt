package com.app.presentation.ui.Screen.Auth

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.app.domain.model.UserInfo
import com.app.domain.repository.TimeProvider
import com.app.presentation.R
import com.app.presentation.viewModel.MainViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

suspend fun GoogleOnClick(
    viewModel: MainViewModel,
    credentialManager: CredentialManager,
    activity: Activity,
    onLoginSuccess: (String) -> Unit,
) {
    try {
        // 1) Google ID 옵션설정
        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(activity.getString(R.string.default_web_client_id))
            .setFilterByAuthorizedAccounts(false)
            .build()

        // 2) Credential 요청생성
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        // 3) 요청실행
        val response = credentialManager.getCredential(
            request = request,
            context = activity
        )
        handleSignInCredential(
            credential = response.credential,
            viewModel = viewModel,
            activity = activity,
            onLoginSuccess = onLoginSuccess,
            timeProvider = viewModel.getTimeProvider()
        )


    } catch (e: Exception) {
        Log.e("GoogleOnClick", "Google 로그인 취소 : ${e.localizedMessage}")
        Toast.makeText(activity, "구글 로그인 중 취소되었습니다.", Toast.LENGTH_SHORT).show()
    }
}

private fun handleSignInCredential(
    credential: Credential?,
    viewModel: MainViewModel,
    activity: Activity,
    onLoginSuccess: (String) -> Unit,
    timeProvider: TimeProvider
) {
    if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        try {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val idToken = googleIdTokenCredential.idToken

            if (idToken != null) {
                // firestore 인증
                val auth = FirebaseAuth.getInstance()
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)

                auth.signInWithCredential(firebaseCredential)
                    .addOnCompleteListener(activity) { task ->
                        // 로그인 성공
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            user?.let {
                                viewModel.signIn(
                                    UserInfo.UserInfoCreate(
                                        uid = it.uid,
                                        name = it.displayName ?: "닉네임없음",
                                        email = it.email ?: "",
                                        loginProvider = UserInfo.LoginProvider.GOOGLE,
                                        timeProvider = timeProvider
                                    )
                                )
                                Toast.makeText(
                                    activity,
                                    "로그인유저: ${it.displayName}",
                                    Toast.LENGTH_LONG
                                ).show()
                                onLoginSuccess(it.displayName ?: "") // MainScreen으로 이동
                            }
                        } else {
                            // 로그인 실패
                            viewModel.logout()
                            Toast.makeText(activity, "로그인 실패", Toast.LENGTH_SHORT).show()
                            Log.e("로그인 실패", "로그인 실패: ${task.exception}")
                        }
                    }
            } else {
                Log.d("idToken null", "idToken is null")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}