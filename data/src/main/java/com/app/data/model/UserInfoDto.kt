package com.app.data.model


data class UserInfoDto (
    var uid: String? = null,
    var name: String? = null,
    var email: String? = null,
    var loginProvider: String? = null,
    var createdAt: String? = null,
    var lastDrinkDate: String? = null,
    var lastGoalAchievedDate: String? = null,
    var streakDays: Int? = null,
    var dailyGoal: Int? = null,
    var totalDays: Int? = null,
    var chatLimit: Int? = null
)