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
        accountRepository.signIn(userInfo)
    }

    suspend fun logout(loginProvider: UserInfo.LoginProvider?) {
        accountRepository.logout(loginProvider)
    }
}