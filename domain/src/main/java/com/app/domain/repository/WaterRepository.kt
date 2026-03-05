package com.app.domain.repository

import com.app.domain.model.WaterLog

interface WaterRepository {
    suspend fun getTodayLog(uid: String, date: String): WaterLog?
    suspend fun saveWithAchievement(uid: String, waterLog: WaterLog): Result<Unit>
    suspend fun getWeeklyLogs(uid: String, start: String, end: String): List<WaterLog>
    suspend fun getMonthlyLogs(uid: String, start: String, end: String): List<WaterLog>
}