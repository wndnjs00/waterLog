package com.app.data.repository

import android.util.Log
import com.app.data.mapper.WaterLogMapper
import com.app.data.model.WaterLogDto
import com.app.domain.constants.BadgeType
import com.app.domain.exception.NetworkUnavailableException
import com.app.domain.model.WaterLog
import com.app.domain.repository.NetworkMonitor
import com.app.domain.repository.TimeProvider
import com.app.domain.repository.WaterRepository
import com.app.domain.usecase.StreakCalculator
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Transaction
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class WaterRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val timeProvider: TimeProvider,
    private val networkMonitor: NetworkMonitor
) : WaterRepository {
    override suspend fun getTodayLog(uid: String, date: String): WaterLog? {
        val snapshot = firestore.collection("users")
            .document(uid)
            .collection("water_logs")
            .document(date)
            .get()
            .await()

        val dto = snapshot.toObject(WaterLogDto::class.java)
        return dto?.let { WaterLogMapper.toDomain(it) }
    }

    override suspend fun saveWithAchievement(
        uid: String,
        waterLog: WaterLog
    ): Result<Unit> {
        if (!networkMonitor.isConnected()) {
            return Result.failure(NetworkUnavailableException())
        }

        return try {
            val userRef = firestore.collection("users").document(uid)
            val logRef = userRef.collection("water_logs").document(waterLog.date)

            firestore.runTransaction { transaction ->
                // Firestore 트랜잭션: 모든 읽기를 쓰기보다 먼저 수행
                val userSnap = transaction.get(userRef)
                val existingLogSnap = transaction.get(logRef)

                val dailyGoal = userSnap.getLong("dailyGoal")?.toInt() ?: 8
                val currentStreak = userSnap.getLong("streakDays")?.toInt() ?: 0
                val lastGoalDate = userSnap.getString("lastGoalAchievedDate")
                val totalDays = userSnap.getLong("totalDays")?.toInt() ?: 0

                val goalAchievedDate = userSnap.getString("goalAchievedDate")
                val isFirstRecordToday = !existingLogSnap.exists()
                val yesterday = timeProvider.yesterdayString()

                val reachedGoalFirstTime =
                    waterLog.cups >= dailyGoal && goalAchievedDate != waterLog.date

                Log.d("DEBUG", "---------------------------")
                Log.d("DEBUG", "waterLog.date = ${waterLog.date}")
                Log.d("DEBUG", "goalAchievedDate = $goalAchievedDate")
                Log.d("DEBUG", "dailyGoal = $dailyGoal")
                Log.d("DEBUG", "cups = ${waterLog.cups}")
                Log.d("DEBUG", "reachedGoalFirstTime = $reachedGoalFirstTime")
                Log.d("DEBUG", "---------------------------")

                val newStreak = StreakCalculator.calculate(
                    lastGoalDate = lastGoalDate,
                    today = waterLog.date,
                    yesterday = yesterday,
                    currentStreak = currentStreak,
                    reachedGoalFirstTime = reachedGoalFirstTime
                )

                // === 이하 쓰기만 수행 ===
                // water_log 저장
                transaction.set(logRef, WaterLogMapper.toDto(waterLog))

                // user 업데이트
                val updates = mutableMapOf<String, Any>(
                    "streakDays" to newStreak,
                    "lastDrinkDate" to waterLog.date
                )

                // totalDays는 오늘 처음 기록한 경우에만 증가
                if (isFirstRecordToday) {
                    updates["totalDays"] = totalDays + 1
                }

                // 목표 최초 달성시, 날짜기록
                if (reachedGoalFirstTime) {
                    updates["goalAchievedDate"] = waterLog.date
                    updates["lastGoalAchievedDate"] = waterLog.date
                }

                transaction.update(userRef, updates)

                // 목표 달성체크
                if (reachedGoalFirstTime) {
                    createGoalNotification(transaction, userRef, dailyGoal)
                    createGoalBadge(transaction, userRef, waterLog.date)
                }

                // streak 기반 배지 처리
                handleStreakBadges(
                    transaction,
                    userRef,
                    newStreak,
                    waterLog.date
                )
            }.await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 목표 달성 배지
    private fun createGoalBadge(
        transaction: Transaction,
        userRef: DocumentReference,
        date: String
    ) {
        val badgeRef = userRef.collection("badges").document(BadgeType.DAY_2L)

        transaction.set(
            badgeRef,
            mapOf(
                "name" to "하루 2L 달성",
                "description" to "하루에 8잔 달성!",
                "acquired" to true,
                "acquiredDate" to date
            )
        )
    }

    // streak 배지 처리
    private fun handleStreakBadges(
        transaction: Transaction,
        userRef: DocumentReference,
        newStreak: Int,
        date: String
    ) {
        when (newStreak) {
            7 -> createBadge(
                transaction,
                userRef,
                BadgeType.WEEK_7DAYS,
                "7일 연속 달성",
                "7일 동안 꾸준히 물을 마셨습니다!",
                date
            )

            30 -> createBadge(
                transaction,
                userRef,
                BadgeType.MONTH_30DAYS,
                "30일 연속 달성",
                "한달 동안 꾸준히 물을 마셨습니다!",
                date
            )

            180 -> createBadge(
                transaction,
                userRef,
                BadgeType.KING_6MONTHS,
                "6개월 꾸준함의 왕",
                "6개월 동안 꾸준히 물을 섭취했습니다!",
                date
            )
        }
    }


    // 배지가 없을 때만 쓰기
    private fun createBadge(
        transaction: Transaction,
        userRef: DocumentReference,
        badgeId: String,
        name: String,
        description: String,
        date: String
    ) {
        val badgeRef = userRef.collection("badges").document(badgeId)

        transaction.set(
            badgeRef, mapOf(
                "name" to name,
                "description" to description,
                "acquired" to true,
                "acquiredDate" to date
            )
        )
    }

    private fun createGoalNotification(
        transaction: Transaction,
        userRef: DocumentReference,
        dailyGoal: Int
    ) {
        val notificationRef = userRef.collection("notifications").document()

        transaction.set(
            notificationRef, mapOf(
                "title" to "오늘 물 목표 달성 💧",
                "message" to "하루 목표 8잔을 모두 마셨어요!",
                "type" to "goal_achieved",
                "createdAt" to timeProvider.nowDateTimeString(),
                "isRead" to false
            )
        )
    }


    override suspend fun getWeeklyLogs(uid: String, start: String, end: String): List<WaterLog> {
        val snapshot = firestore.collection("users")
            .document(uid)
            .collection("water_logs")
            .whereGreaterThanOrEqualTo("date", start)
            .whereLessThanOrEqualTo("date", end)
            .get()
            .await()

        return snapshot.documents.mapNotNull {
            it.toObject(WaterLogDto::class.java)?.let(WaterLogMapper::toDomain)
        }
    }

    override suspend fun getMonthlyLogs(uid: String, start: String, end: String): List<WaterLog> {
        return getWeeklyLogs(uid, start, end)
    }
}