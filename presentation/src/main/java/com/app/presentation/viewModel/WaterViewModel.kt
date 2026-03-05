package com.app.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.domain.model.WaterLog
import com.app.domain.repository.TimeProvider
import com.app.domain.usecase.AccountUseCase
import com.app.domain.usecase.WaterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

@HiltViewModel
class WaterViewModel @Inject constructor(
    private val waterUseCase: WaterUseCase,
    private val accountUseCase: AccountUseCase,
    private val timeProvider: TimeProvider,
) : ViewModel() {

    private val mutex = Mutex()

    private val _todayLog = MutableStateFlow<WaterLog?>(null)
    val todayLog: StateFlow<WaterLog?> = _todayLog

    private val _isUpdating = MutableStateFlow(false)
    val isUpdating: StateFlow<Boolean> = _isUpdating

    @OptIn(ExperimentalCoroutinesApi::class)
    val weeklyLogs: StateFlow<List<WaterLog>> =
        _todayLog
            .filterNotNull()
            .flatMapLatest {
                flow {
                    val user = accountUseCase.getAccountInfo().value ?: return@flow
                    val start = timeProvider.weekStart()
                    val end = timeProvider.nowDateString()
                    emit(waterUseCase.weekly(user.uid, start, end))
                }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    val monthlyLogs: StateFlow<List<WaterLog>> =
        _todayLog
            .filterNotNull()
            .flatMapLatest {
                flow {
                    val user = accountUseCase.getAccountInfo().value ?: return@flow
                    val start = timeProvider.monthStart()
                    val end = timeProvider.nowDateString()
                    emit(waterUseCase.monthly(user.uid, start, end))
                }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    init {
        loadToday()
    }

    fun loadToday() {
        viewModelScope.launch {
            val user = accountUseCase.getAccountInfo().value ?: return@launch
            val date = timeProvider.nowDateString()

            val log = waterUseCase.getToday(user.uid, date)
                ?: WaterLog(
                    date = date,
                    cups = 0,
                    targetCups = user.dailyGoal,
                    totalMl = 0,
                    updatedAt = timeProvider.nowDateTimeString(),
                )
            _todayLog.value = log
        }
    }

    fun addCup() {
        val log = _todayLog.value ?: return
        if (_isUpdating.value) return

        val newLog = log.copy(
            cups = log.cups + 1,
            totalMl = log.totalMl + 250,
            updatedAt = timeProvider.nowDateTimeString()
        )

        updateLog(newLog)
    }

    fun removeCup() {
        val log = _todayLog.value ?: return
        if (_isUpdating.value || log.cups <= 0) return

        val newLog = log.copy(
            cups = log.cups - 1,
            totalMl = (log.totalMl - 250).coerceAtLeast(0),
            updatedAt = timeProvider.nowDateTimeString()
        )

        updateLog(newLog)
    }

    private fun updateLog(newLog: WaterLog) {
        viewModelScope.launch {

            mutex.withLock {
                val user = accountUseCase.getAccountInfo().value ?: return@launch
                _isUpdating.value = true

                try {
                    val result = waterUseCase.saveWithAchievement(user.uid, newLog)

                    result.onSuccess {
                        _todayLog.value = newLog
                    }.onFailure {
                        // TODO: 실패시 (토스트 메세지 등 추가)
                    }
                } finally {
                    // 무조건 실행
                    _isUpdating.value = false
                }
            }
        }
    }
}