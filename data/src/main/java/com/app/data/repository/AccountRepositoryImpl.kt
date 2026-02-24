package com.app.data.repository

import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import com.app.domain.model.UserInfo
import com.app.domain.repository.AccountRepository
import com.app.domain.repository.TimeProvider
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
    private val timeProvider: TimeProvider
) : AccountRepository {
    private val accountInfoFlow = MutableStateFlow<UserInfo?>(null)
    private val auth = FirebaseAuth.getInstance()

    override fun getAccountInfo(): StateFlow<UserInfo?> {
        return accountInfoFlow
    }

    // Firestore에 저장
    override suspend fun saveUserInfo(userInfo: UserInfo) {
        firestore.collection("users")
            .document(userInfo.uid)
            .set(userInfo)
            .await()
        accountInfoFlow.emit(userInfo)
    }

    override suspend fun logout(loginProvider: UserInfo.LoginProvider?) {

        when (loginProvider) {
            UserInfo.LoginProvider.GOOGLE -> {
                auth.signOut()
                runCatching {
                    credentialManager.clearCredentialState(ClearCredentialStateRequest())
                }
            }

            UserInfo.LoginProvider.KAKAO -> {
                UserApiClient.instance.logout {}
                auth.signOut()
            }

            UserInfo.LoginProvider.NAVER -> {
                NidOAuth.logout(object : NidOAuthCallback {
                    override fun onSuccess() {}
                    override fun onFailure(errorCode: String, errorDesc: String) {}
                })
                auth.signOut()
            }

            UserInfo.LoginProvider.EMAIL -> {
                auth.signOut()
            }

            null -> auth.signOut()
        }

        accountInfoFlow.emit(null)

    }

    override suspend fun signUpWithEmail(
        email: String,
        password: String,
        name: String
    ): Result<UserInfo> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("User is null")

            val userInfo = UserInfo.UserInfoCreate(
                uid = user.uid,
                name = name,
                email = user.email,
                loginProvider = UserInfo.LoginProvider.EMAIL,
                timeProvider = timeProvider,
            )

            // Firestore에 저장
            saveUserInfo(userInfo)
            Result.success(userInfo)

        } catch (e: Exception) {
            Result.failure(e)
        }

    }

    override suspend fun signInWithEmail(email: String, password: String): Result<UserInfo> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("User is null")

            val snapshot = firestore.collection("users")
                .document(user.uid)
                .get()
                .await()

            val userInfo = snapshot.toObject(UserInfo::class.java) ?: throw Exception("Firestore user not found")

            accountInfoFlow.emit(userInfo)
            Result.success(userInfo)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}