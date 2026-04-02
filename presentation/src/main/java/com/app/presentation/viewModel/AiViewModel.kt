package com.app.presentation.viewModel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.domain.repository.ChatLimitStore
import com.app.domain.usecase.SendChatUseCase
import com.app.presentation.ui.event.UiEvent
import com.app.presentation.ui.util.AuthErrorMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class AiViewModel @Inject constructor(
    private val sendChatUseCase: SendChatUseCase,
    private val chatLimitStore: ChatLimitStore,
): ViewModel() {
    private val _message = MutableStateFlow<List<Pair<Boolean, String>>>(emptyList())
    val message: StateFlow<List<Pair<Boolean, String>>> = _message

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _event = MutableSharedFlow<UiEvent>()
    val event = _event.asSharedFlow()

    val count = chatLimitStore.countFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        0
    )

    @RequiresApi(Build.VERSION_CODES.O)
    fun send(question: String) {
        viewModelScope.launch {
            val today = LocalDate.now().toString()
            if (count.value >= 3) return@launch

            _message.value += true to question
            _isLoading.value = true

            try {
                val answer = sendChatUseCase(question)
                _message.value += false to answer
                chatLimitStore.increase(today)
            } catch (e: Exception) {
                _event.emit(
                    UiEvent.ShowToast(AuthErrorMapper.map(e))
                )
            } finally {
                _isLoading.value = false
            }
        }
    }
}