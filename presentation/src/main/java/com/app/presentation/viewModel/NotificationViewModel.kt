package com.app.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.domain.model.Notification
import com.app.domain.repository.TimeProvider
import com.app.domain.usecase.AccountUseCase
import com.app.domain.usecase.NotificationUseCase
import com.app.presentation.ui.event.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationUseCase: NotificationUseCase,
    private val accountUseCase: AccountUseCase,
    private val timeProvider: TimeProvider,
): ViewModel(){

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications

    private val _event = MutableSharedFlow<UiEvent>()
    val event = _event.asSharedFlow()

    init {
        observeNotifications()
    }

    private fun observeNotifications() {
        viewModelScope.launch {
            try {
                val user = accountUseCase.getAccountInfo().value ?: return@launch
                notificationUseCase.observe(user.uid).collect{
                    _notifications.value = it.sortedByDescending { it.createdAt}
                }
            } catch (e:Exception) {
                _event.emit(
                    UiEvent.ShowToast("알림을 불러오지 못했습니다")
                )
            }
        }
    }

    fun markAsRead(id: String) {
        viewModelScope.launch {
            try {
                val user = accountUseCase.getAccountInfo().value ?: return@launch
                notificationUseCase.markRead(user.uid, id)
            } catch (e: Exception) {
                _event.emit(
                    UiEvent.ShowToast("알림 읽기 처리 실패")
                )
            }
        }
    }

    fun formatTime(dateTime: String): String {
        val result = timeProvider.formatNotificationTime(dateTime)

        return result.getOrElse {
            viewModelScope.launch {
                _event.emit(
                    UiEvent.ShowToast("시간을 불러올 수 없습니다")
                )
            }
            "시간을 불러올 수 없습니다"
        }
    }
}