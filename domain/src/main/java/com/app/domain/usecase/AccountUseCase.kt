package com.app.domain.usecase

import com.app.domain.model.UserInfo
import com.app.domain.repository.AccountRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class AccountUseCase @Inject constructor(
    private val accountRepository: AccountRepository
) {
    fun getAccountInfo(): StateFlow<UserInfo?> {
        return accountRepository.getAccountInfo()
    }

    suspend fun signIn(userInfo: UserInfo) {
        accountRepository.saveUserInfo(userInfo)
    }

    suspend fun logout(loginProvider: UserInfo.LoginProvider?) {
        accountRepository.logout(loginProvider)
    }

    suspend fun signUpWithEmail(email: String, password: String, name: String): Result<UserInfo> {
        return accountRepository.signUpWithEmail(email, password, name)
    }

    suspend fun signInWithEmail(email: String, password: String): Result<UserInfo> {
        return accountRepository.signInWithEmail(email, password)
    }
}