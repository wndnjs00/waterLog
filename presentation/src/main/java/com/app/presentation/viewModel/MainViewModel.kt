package com.app.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.domain.model.UserInfo
import com.app.domain.repository.TimeProvider
import com.app.domain.usecase.AccountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val accountUseCase: AccountUseCase,
    private val timeProvider: TimeProvider
) : ViewModel() {

    val userInfo = accountUseCase.getAccountInfo()

    fun signInGoogle(userInfo: UserInfo) {
        viewModelScope.launch {
            accountUseCase.signInGoogle(userInfo)
        }
    }

    fun logoutGoogle() {
        viewModelScope.launch {
            accountUseCase.logoutGoogle()
        }
    }

    fun getTimeProvider(): TimeProvider = timeProvider
}