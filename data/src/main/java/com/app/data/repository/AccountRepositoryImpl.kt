package com.app.data.repository

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import com.app.domain.model.UserInfo
import com.app.domain.repository.AccountRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kakao.sdk.user.UserApiClient
import com.navercorp.nid.NidOAuth
import com.navercorp.nid.oauth.util.NidOAuthCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AccountRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val credentialManager: CredentialManager,
) : AccountRepository {
    private val accountInfoFlow = MutableStateFlow<UserInfo?>(null)

    override fun getAccountInfo(): StateFlow<UserInfo?> {
        return accountInfoFlow
    }

    override suspend fun signIn(userInfo: UserInfo) {
        firestore.collection("users")
            .document(userInfo.uid)
            .set(userInfo)
            .await()
        accountInfoFlow.emit(userInfo)
    }

    override suspend fun logout(loginProvider: UserInfo.LoginProvider?) {

        when (loginProvider) {
            UserInfo.LoginProvider.GOOGLE -> {
                FirebaseAuth.getInstance().signOut()
                runCatching {
                    credentialManager.clearCredentialState(ClearCredentialStateRequest())
                }
            }

            UserInfo.LoginProvider.KAKAO -> {
                UserApiClient.instance.logout {}
                FirebaseAuth.getInstance().signOut()
            }
            UserInfo.LoginProvider.NAVER -> {
                NidOAuth.logout(object : NidOAuthCallback {
                    override fun onSuccess() {}
                    override fun onFailure(errorCode: String, errorDesc: String) {}
                })
                FirebaseAuth.getInstance().signOut()
            }
            null -> FirebaseAuth.getInstance().signOut()
        }

        accountInfoFlow.emit(null)

    }
}