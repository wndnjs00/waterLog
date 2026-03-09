package com.app.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.domain.model.UserInfo
import com.app.domain.repository.TimeProvider
import com.app.domain.usecase.AccountUseCase
import com.app.presentation.ui.event.UiEvent
import com.app.presentation.ui.util.AuthErrorMapper
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

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

    private val _event = MutableSharedFlow<UiEvent>()
    val event = _event.asSharedFlow()

    init {
        autoLogin()
    }

    fun autoLogin() {
        viewModelScope.launch {
            try {
                accountUseCase.loadUser()
            } catch (e: Exception) {
                _event.emit(UiEvent.ShowToast(AuthErrorMapper.map(e)))
            }
        }
    }

    fun saveFcmToken(token: String) {
        viewModelScope.launch {
            try {
                accountUseCase.saveFcmToken(token)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /** OAuth(Google/Kakao/Naver) 로그인 실패 시 토스트 메시지 */
    fun showOAuthError(throwable: Throwable) {
        viewModelScope.launch {
            _event.emit(UiEvent.ShowToast(AuthErrorMapper.mapForOAuth(throwable)))
        }
    }

    fun saveUser(userInfo: UserInfo) {
        viewModelScope.launch {
            try {
                accountUseCase.saveUser(userInfo)
            } catch (e: Exception) {
                _event.emit(UiEvent.ShowToast(AuthErrorMapper.map(e)))
            }
        }
    }

    fun logout(loginProvider: UserInfo.LoginProvider) {
        viewModelScope.launch {
            try {
                accountUseCase.logout(loginProvider)
                _event.emit(UiEvent.ShowToast("로그아웃 되었습니다"))
            } catch (e: Exception) {
                _event.emit(UiEvent.ShowToast(AuthErrorMapper.map(e)))
            }
        }
    }

    // 회원 탈퇴 (이메일/OAuth 모두 적용, 성공 시 로그인 화면으로 이동)
    // 이메일 로그인일 때만 emailReauthPassword 필수
    fun deleteAccount(provider: UserInfo.LoginProvider, emailReauthPassword: String? = null) {
        viewModelScope.launch {
            accountUseCase.deleteAccount(provider, emailReauthPassword)
                .fold(
                    onSuccess = {
                        _event.emit(UiEvent.ShowToast("회원탈퇴 완료"))
                        _event.emit(UiEvent.NavigateToLogin)
                    },
                    onFailure = { _event.emit(UiEvent.ShowToast("회원탈퇴 실패")) }
                )
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
                onSuccess = {

                    try {
                        val token = FirebaseMessaging.getInstance().token.await()
                        accountUseCase.saveFcmToken(token)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    _signUpState.value = EmailAuthState.Success
                },
                onFailure = {
                    val msg = AuthErrorMapper.map(it)
                    _signUpState.value = EmailAuthState.Error(msg)
                    _event.emit(UiEvent.ShowToast(msg))
                }
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
                onSuccess = { _signInState.value = EmailAuthState.Success },
                onFailure = {
                    val msg = AuthErrorMapper.map(it)
                    _signInState.value = EmailAuthState.Error(msg)
                    _event.emit(UiEvent.ShowToast(msg))
                }
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