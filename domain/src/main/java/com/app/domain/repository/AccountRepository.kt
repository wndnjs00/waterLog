package com.app.domain.repository

import com.app.domain.model.UserInfo
import kotlinx.coroutines.flow.StateFlow

interface AccountRepository {
    fun getAccountInfo(): StateFlow<UserInfo?>

    suspend fun signIn(userInfo: UserInfo)

    suspend fun logout(loginProvider: UserInfo.LoginProvider?)
}