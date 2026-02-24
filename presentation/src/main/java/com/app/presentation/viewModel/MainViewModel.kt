package com.app.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.domain.model.UserInfo
import com.app.domain.repository.TimeProvider
import com.app.domain.usecase.AccountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.log

@HiltViewModel
class MainViewModel @Inject constructor(
    private val accountUseCase: AccountUseCase,
    private val timeProvider: TimeProvider
) : ViewModel() {

    val userInfo = accountUseCase.getAccountInfo()

    private val _signUpState = MutableStateFlow<EmailAuthState>(EmailAuthState.Idle)
    val signUpState: StateFlow<EmailAuthState> = _signUpState.asStateFlow()

    private val _signInState = MutableStateFlow<EmailAuthState>(EmailAuthState.Idle)
    val signInState: StateFlow<EmailAuthState> = _signInState.asStateFlow()

    fun signIn(userInfo: UserInfo) {
        viewModelScope.launch {
            accountUseCase.signIn(userInfo)
        }
    }

    fun logout(loginProvider: UserInfo.LoginProvider) {
        viewModelScope.launch {
            accountUseCase.logout(loginProvider)
        }
    }

    fun signUpWithEmail(
        email: String,
        password: String,
        name: String,
    ) {
        viewModelScope.launch {
            _signUpState.value = EmailAuthState.Loading
            val result = accountUseCase.signUpWithEmail(email, password, name)
            result.fold(
                onSuccess = {_signUpState.value = EmailAuthState.Success},
                onFailure = {_signUpState.value = EmailAuthState.Error(it.message ?: "회원가입 실패")},
            )
        }
    }

    fun signInWithEmail(
        email: String,
        password: String,
    ) {
        viewModelScope.launch {
            _signInState.value = EmailAuthState.Loading
            val result = accountUseCase.signInWithEmail(email, password)
            result.fold(
                onSuccess = {_signInState.value = EmailAuthState.Success},
                onFailure = { _signInState.value = EmailAuthState.Error(it.message ?: "로그인 실패")}
            )
        }
    }

    fun resetSignUpState() {
        _signUpState.value = EmailAuthState.Idle
    }

    fun resetSignInState() {
        _signInState.value = EmailAuthState.Idle
    }

    fun getTimeProvider(): TimeProvider = timeProvider
}

sealed class EmailAuthState {
    object Idle: EmailAuthState()
    object Loading: EmailAuthState()
    object Success: EmailAuthState()
    data class Error(val message: String): EmailAuthState()
}