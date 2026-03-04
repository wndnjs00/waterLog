@file:Suppress("UNREACHABLE_CODE")

package com.app.data.mapper

import com.app.data.model.UserInfoDto
import com.app.domain.model.UserInfo

object UserInfoMapper {

    // DTO -> Domain
    fun toDomain(dto: UserInfoDto): UserInfo {
        return UserInfo(
            uid = dto.uid ?: throw IllegalArgumentException("uid is null"),
            name = dto.name ?: "",
            email = dto.email,
            loginProvider = dto.loginProvider.toLoginProvider(),
            createdAt = dto.createdAt ?: "",
            lastDrinkDate = dto.lastDrinkDate,
            lastGoalAchievedDate = dto.lastGoalAchievedDate,
            streakDays = dto.streakDays,
            dailyGoal = dto.dailyGoal ?: 8,
            totalDays = dto.totalDays,
            chatLimit = dto.chatLimit,
        )
    }

    // Domain → DTO
    fun toDto(domain: UserInfo): UserInfoDto {
        return UserInfoDto(
            uid = domain.uid,
            name = domain.name,
            email = domain.email,
            loginProvider = domain.loginProvider.name,
            createdAt = domain.createdAt,
            lastDrinkDate = domain.lastDrinkDate,
            lastGoalAchievedDate = domain.lastGoalAchievedDate,
            streakDays = domain.streakDays,
            dailyGoal = domain.dailyGoal,
            totalDays = domain.totalDays,
            chatLimit = domain.chatLimit
        )
    }

    private fun String?.toLoginProvider(): UserInfo.LoginProvider {
        return try {
            UserInfo.LoginProvider.valueOf(this ?: "")
        } catch (e: Exception) {
            UserInfo.LoginProvider.EMAIL
        }
    }
}