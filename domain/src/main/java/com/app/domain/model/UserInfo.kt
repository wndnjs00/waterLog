package com.app.domain.model

import com.app.domain.repository.TimeProvider


data class UserInfo(
    val uid: String,
    val name: String,
    val email: String?,
    val loginProvider: LoginProvider,
    val createdAt: String,
    val lastDrinkDate: String,
    val streakDays: Int?,
    val dailyGoal: Int?,
    val totalDays: Int?,
    val chatLimit: Int?,
) {
    enum class LoginProvider {
        GOOGLE,
    }

    companion object {
        fun UserInfoCreate(
            uid: String,
            name: String,
            email: String?,
            loginProvider: LoginProvider,
            timeProvider: TimeProvider
        ): UserInfo {
            return UserInfo(
                uid = uid,
                name = name,
                email = email,
                loginProvider = loginProvider,
                createdAt = timeProvider.nowDateTimeString(),
                lastDrinkDate = timeProvider.nowDateString(),
                streakDays = null,
                dailyGoal = null,
                totalDays = null,
                chatLimit = null,
            )
        }
    }
}