package com.app.data.repository

import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import com.app.data.mapper.UserInfoMapper
import com.app.data.model.UserInfoDto
import com.app.domain.model.UserInfo
import com.app.domain.repository.AccountRepository
import com.app.domain.repository.TimeProvider
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kakao.sdk.user.UserApiClient
import com.navercorp.nid.NidOAuth
import com.navercorp.nid.oauth.util.NidOAuthCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

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

        val dto = UserInfoMapper.toDto(userInfo)

        firestore.collection("users")
            .document(userInfo.uid)
            .set(dto)
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

            UserInfo.LoginProvider.EMAIL -> auth.signOut()

            null -> auth.signOut()
        }

        accountInfoFlow.emit(null)

    }

    // 회원 탈퇴
    override suspend fun deleteAccount(
        provider: UserInfo.LoginProvider,
        emailReauthPassword: String?
    ): Result<Unit> {

        return runCatching {
            val user = auth.currentUser ?: throw Exception("user not logged in")
            val uid = user.uid

            when (provider) {
                UserInfo.LoginProvider.KAKAO -> {
                    suspendCancellableCoroutine { cont ->
                        UserApiClient.instance.unlink { error ->
                            if (error != null) {
                                cont.resumeWithException(error)
                            } else {
                                cont.resume(Unit)
                            }
                        }
                    }
                }

                UserInfo.LoginProvider.NAVER -> {
                    suspendCancellableCoroutine { cont ->
                        NidOAuth.disconnect(object : NidOAuthCallback {
                            override fun onSuccess() {
                                cont.resume(Unit)
                            }
                            override fun onFailure(errorCode: String, errorDesc: String) {
                                cont.resumeWithException(Exception("네이버 회원탈퇴 실패: $errorCode - $errorDesc"))
                            }
                        })
                    }
                }

                UserInfo.LoginProvider.GOOGLE -> {
                    credentialManager.clearCredentialState(
                        ClearCredentialStateRequest()
                    )
                }

                UserInfo.LoginProvider.EMAIL -> {
                    // 비밀번호가 있으면 재인증 후 탈퇴, 없으면 바로 탈퇴 시도 (최근 로그인 시 성공할 수 있음)
                    emailReauthPassword?.let { password ->
                        val email = user.email ?: throw Exception("이메일 정보가 없습니다")
                        val credential = EmailAuthProvider.getCredential(email, password)
                        user.reauthenticate(credential).await()
                    }
                }
            }

            firestore.collection("users")
                .document(uid)
                .delete()
                .await()

            user.delete().await()

            accountInfoFlow.emit(null)
        }
    }


    override suspend fun signUpWithEmail(
        email: String,
        password: String,
        name: String
    ): Result<UserInfo> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("User is null")

            val userInfo = UserInfo.userInfoCreate(
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
            auth.signInWithEmailAndPassword(email, password).await()

            val userInfo = loadUserFromFireStore()
                ?: throw Exception("Firestore user not found")

            Result.success(userInfo)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 자동 로그인
    override suspend fun loadUserFromFireStore(): UserInfo? {
        val firebaseUser = auth.currentUser ?: return null

        val snapshot = firestore.collection("users")
            .document(firebaseUser.uid)
            .get()
            .await()

        val dto = snapshot.toObject(UserInfoDto::class.java) ?: return null
        val domain = UserInfoMapper.toDomain(dto)

        accountInfoFlow.emit(domain)

        return domain
    }
}