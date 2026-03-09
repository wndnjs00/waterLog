package com.app.domain.usecase

object StreakCalculator {

    fun calculate(
        lastGoalDate: String?,
        today: String,
        yesterday: String,
        currentStreak: Int,
        reachedGoalFirstTime: Boolean
    ): Int{

        if(!reachedGoalFirstTime) return currentStreak

        return when {
            lastGoalDate == null -> 1
            lastGoalDate == today -> currentStreak   // 같은 날 중복 방지
            lastGoalDate == yesterday -> currentStreak + 1
            else -> 1
        }
    }
}