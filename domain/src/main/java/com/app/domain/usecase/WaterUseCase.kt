package com.app.domain.usecase

import com.app.domain.model.WaterLog
import com.app.domain.repository.WaterRepository
import javax.inject.Inject

class WaterUseCase @Inject constructor(
    private val repository: WaterRepository
){
    // 오늘 날짜 waterLog 조회
    suspend fun getToday(uid: String, date: String) =
        repository.getTodayLog(uid, date)

    suspend fun saveWithAchievement(uid: String, log: WaterLog) =
        repository.saveWithAchievement(uid, log)

    suspend fun weekly(uid: String, start: String, end: String): List<WaterLog> =
        repository.getWeeklyLogs(uid, start, end)

    suspend fun monthly(uid: String, start: String, end: String): List<WaterLog> =
        repository.getMonthlyLogs(uid, start, end)
}