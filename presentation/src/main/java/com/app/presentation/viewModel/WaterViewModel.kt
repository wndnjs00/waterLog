package com.app.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.domain.model.WaterLog
import com.app.domain.repository.TimeProvider
import com.app.domain.usecase.AccountUseCase
import com.app.domain.usecase.WaterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WaterViewModel @Inject constructor(
    private val waterUseCase: WaterUseCase,
    private val accountUseCase: AccountUseCase,
    private val timeProvider: TimeProvider,
) : ViewModel() {

    private val _todayLog = MutableStateFlow<WaterLog?>(null)
    val todayLog: StateFlow<WaterLog?> = _todayLog

    private val _weeklyLogs = MutableStateFlow<List<WaterLog>>(emptyList())
    val weeklyLogs: StateFlow<List<WaterLog>> = _weeklyLogs

    private val _monthlyLogs = MutableStateFlow<List<WaterLog>>(emptyList())
    val monthlyLogs: StateFlow<List<WaterLog>> = _monthlyLogs

    fun loadToday() {
        viewModelScope.launch {
            val user = accountUseCase.getAccountInfo().value ?: return@launch
            val date = timeProvider.nowDateString()

            val log = waterUseCase.getToday(user.uid, date)
                ?: WaterLog(
                    date = date,
                    cups = 0,
                    targetCups = user.dailyGoal ?: 8,
                    totalMl = 0,
                    updatedAt = timeProvider.nowDateTimeString()
                )
            _todayLog.value = log
        }
    }

    fun addCup() {
        val log = _todayLog.value ?: return

        updateLog(
            log.copy(
                cups = log.cups + 1,
                totalMl = log.totalMl + 250,
                updatedAt = timeProvider.nowDateTimeString()
            )
        )
    }

    fun removeCup() {
        val log = _todayLog.value ?: return
        if (log.cups <= 0) return

        updateLog(
            log.copy(
                cups = log.cups - 1,
                totalMl = (log.totalMl - 250).coerceAtLeast(0),
                updatedAt = timeProvider.nowDateTimeString()
            )
        )
    }

    private fun updateLog(newLog: WaterLog) {
        viewModelScope.launch {
            val user = accountUseCase.getAccountInfo().value ?: return@launch
            waterUseCase.saveWithAchievement(user.uid, newLog)

            _todayLog.value = newLog
        }
    }

    fun loadWeekly() {
        viewModelScope.launch {
            val user = accountUseCase.getAccountInfo().value ?: return@launch
            val start = timeProvider.weekStart()
            val end = timeProvider.nowDateString()

            _weeklyLogs.value = waterUseCase.weekly(user.uid, start, end)
        }
    }

    fun loadMonthly() {
        viewModelScope.launch {
            val user = accountUseCase.getAccountInfo().value ?: return@launch
            val start = timeProvider.monthStart()
            val end = timeProvider.nowDateString()

            _monthlyLogs.value = waterUseCase.monthly(user.uid, start, end)
        }
    }
}