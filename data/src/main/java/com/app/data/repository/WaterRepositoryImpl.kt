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

            // water_log 저장
            transaction.set(logRef, WaterLogMapper.toDto(waterLog))

            // 목표 달성체크
            if (waterLog.cups >= dailyGoal) {

                val notiRef = userRef.collection("notifications").document()
                transaction.set(notiRef, mapOf(
                    "title" to "오늘 물섭취 목표 달성!",
                    "message" to "하루 목표인 $dailyGoal 잔을 모두 마셨어요 💧",
                    "type" to "goal_achieved",
                    "date" to timeProvider.nowDateTimeString(),
                    "isRead" to false
                ))

                // 하루 2L 뱃지
                createBadge(transaction, userRef, "day_2L",
                    "하루 2L 달성",
                    "하루에 8잔 달성!",
                    waterLog.date)
            }

            val yesterday = timeProvider.yesterdayString()

            val newStreak = when {
                lastDate == yesterday -> currentStreak + 1
                lastDate == waterLog.date -> currentStreak
                else -> 1
            }

            transaction.update(userRef, mapOf(
                "streakDays" to newStreak,
                "lastDrinkDate" to waterLog.date
            ))

            // 7일
            if (newStreak == 7)
                createBadge(transaction, userRef, "week_7days",
                    "7일 연속 달성",
                    "일주일 연속 성공!",
                    waterLog.date)

            // 30일
            if (newStreak == 30)
                createBadge(transaction, userRef, "month_30days",
                    "30일 연속 달성",
                    "한달 연속 성공!",
                    waterLog.date)

            // 180일
            if (newStreak == 180)
                createBadge(transaction, userRef, "king_6months",
                    "6개월 연속 달성",
                    "진정한 물왕!",
                    waterLog.date)
        }.await()
    }

    private fun createBadge(
        transaction: Transaction,
        userRef: DocumentReference,
        badgeId: String,
        name: String,
        description: String,
        date: String
    ) {
        val badgeRef = userRef.collection("badges").document(badgeId)

        transaction.set(badgeRef, mapOf(
            "name" to name,
            "description" to description,
            "acquired" to true,
            "acquiredDate" to date
        ))

        transaction.update(userRef, "badges.$badgeId", true)

        val notiRef = userRef.collection("notifications").document()
        transaction.set(notiRef, mapOf(
            "title" to "$name 획득!",
            "message" to description,
            "type" to "streak",
            "date" to timeProvider.nowDateTimeString(),
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