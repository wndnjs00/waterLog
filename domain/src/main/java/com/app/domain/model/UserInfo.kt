package com.app.domain.model

import com.app.domain.repository.TimeProvider


data class UserInfo(
    val uid: String,
    val name: String,
    val email: String?,
    val loginProvider: LoginProvider,
    val createdAt: String, //회원가입 날짜
    val lastDrinkDate: String?, //마지막으로 물마신 날짜
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
                lastDrinkDate = null,
                streakDays = null,
                dailyGoal = null,
                totalDays = null,
                chatLimit = null,
            )
        }
    }
}