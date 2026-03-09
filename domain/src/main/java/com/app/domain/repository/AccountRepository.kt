package com.app.domain.repository

import com.app.domain.model.UserInfo
import kotlinx.coroutines.flow.StateFlow

interface AccountRepository {
    fun getAccountInfo(): StateFlow<UserInfo?>

    suspend fun saveUserInfo(userInfo: UserInfo)

    suspend fun logout(loginProvider: UserInfo.LoginProvider?)

    /**
     * emailReauthPassword: 이메일 로그인일 때만 사용 (재인증용 비밀번호).
     */
    suspend fun deleteAccount(
        loginProvider: UserInfo.LoginProvider,
        emailReauthPassword: String? = null
    ): Result<Unit>

    suspend fun signUpWithEmail(
        email: String,
        password: String,
        name: String
    ): Result<UserInfo>

    suspend fun signInWithEmail(email: String, password: String): Result<UserInfo>
    
    suspend fun loadUserFromFireStore(): UserInfo?

    suspend fun saveFcmToken(token: String)

}