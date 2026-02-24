package com.app.domain.repository

import com.app.domain.model.UserInfo
import kotlinx.coroutines.flow.StateFlow

interface AccountRepository {
    fun getAccountInfo(): StateFlow<UserInfo?>

    suspend fun saveUserInfo(userInfo: UserInfo)

    suspend fun logout(loginProvider: UserInfo.LoginProvider?)

    suspend fun signUpWithEmail(
        email: String,
        password: String,
        name: String
    ): Result<UserInfo>

    suspend fun signInWithEmail(email: String, password: String): Result<UserInfo>
    
    suspend fun loadUserFromFireStore(): UserInfo?
}