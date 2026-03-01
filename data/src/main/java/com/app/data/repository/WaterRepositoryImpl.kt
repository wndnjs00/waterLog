package com.app.data.repository

import com.app.data.mapper.WaterLogMapper
import com.app.data.model.WaterLogDto
import com.app.domain.constants.BadgeType
import com.app.domain.model.WaterLog
import com.app.domain.repository.TimeProvider
import com.app.domain.repository.WaterRepository
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Transaction
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.math.log

class WaterRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val timeProvider: TimeProvider
): WaterRepository{
    override suspend fun getTodayLog(uid: String, date: String): WaterLog? {
        val snapshot = firestore.collection("users")
            .document(uid)
            .collection("water_logs")
            .document(date)
            .get()
            .await()

        val dto = snapshot.toObject(WaterLogDto::class.java)
        return dto?.let{WaterLogMapper.toDomain(it)}
    }

    override suspend fun saveWaterLog(uid: String, waterLog: WaterLog) {
        firestore.collection("users")
            .document(uid)
            .collection("water_logs")
            .document(waterLog.date)
            .set(WaterLogMapper.toDto(waterLog))
            .await()
    }

    override suspend fun saveWithAchievement(uid: String, waterLog: WaterLog) {

        val userRef = firestore.collection("users").document(uid)
        val logRef = userRef.collection("water_logs").document(waterLog.date)

        firestore.runTransaction { transaction ->

            val userSnap = transaction.get(userRef)
            val dailyGoal = userSnap.getLong("dailyGoal")?.toInt() ?: 8
            val currentStreak = userSnap.getLong("streakDays")?.toInt() ?: 0
            val lastDate = userSnap.getString("lastDrinkDate")
            val totalDays = userSnap.getLong("totalDays")?.toInt() ?: 0

            val yesterday = timeProvider.yesterdayString()

            val newStreak = when {
                lastDate == yesterday -> currentStreak + 1
                lastDate == waterLog.date -> currentStreak
                else -> 1
            }

            // water_log 저장
            val updateLog = waterLog.copy(streak = newStreak)
            transaction.set(logRef, WaterLogMapper.toDto(updateLog))

            // user 업데이트
            transaction.update(
                userRef, mapOf(
                    "streakDays" to newStreak,
                    "lastDrinkDate" to waterLog.date,
                    "totalDays" to totalDays + 1
                )
            )

            // 목표 달성체크
            if (waterLog.cups >= dailyGoal) {
                createGoalNotification(transaction, userRef, dailyGoal)
                createBadgeIfNotExist(
                    transaction, userRef,
                    BadgeType.DAY_2L,
                    "하루 2L 달성",
                    "하루에 8잔 달성!",
                    waterLog.date
                )
            }

            if (newStreak == 7)
                createBadgeIfNotExist(
                    transaction, userRef,
                    BadgeType.WEEK_7DAYS,
                    "7일 연속 달성",
                    "일주일 연속 성공!",
                    waterLog.date
                )

            if (newStreak == 30)
                createBadgeIfNotExist(
                    transaction, userRef,
                    BadgeType.MONTH_30DAYS,
                    "30일 연속 달성",
                    "한달 연속 성공!",
                    waterLog.date
                )

            if (newStreak == 180)
                createBadgeIfNotExist(
                    transaction, userRef,
                    BadgeType.KING_6MONTHS,
                    "6개월 연속 달성",
                    "진정한 물왕!",
                    waterLog.date
                )
        }.await()
    }


    private fun createBadgeIfNotExist(
        transaction: Transaction,
        userRef: DocumentReference,
        badgeId: String,
        name: String,
        description: String,
        date: String
    ) {
        val badgeRef = userRef.collection("badges").document(badgeId)
        val snapshot = transaction.get(badgeRef)

        if (!snapshot.exists()) {
            transaction.set(badgeRef, mapOf(
                "name" to name,
                "description" to description,
                "acquired" to true,
                "acquiredDate" to date
            ))

            transaction.update(userRef, "badges.$badgeId", true)
        }
    }

    private fun createGoalNotification(
        transaction: Transaction,
        userRef: DocumentReference,
        dailyGoal: Int
    ) {
        val notificationRef = userRef.collection("notifications").document()

        transaction.set(notificationRef, mapOf(
            "title" to "오늘 물 목표 달성 💧",
            "message" to "하루 목표 ${dailyGoal}잔을 모두 마셨어요!",
            "type" to "goal_achieved",
            "createdAt" to timeProvider.nowDateTimeString(),
            "isRead" to false
        ))
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