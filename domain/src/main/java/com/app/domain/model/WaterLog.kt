package com.app.domain.model

data class WaterLog(
    val date: String,
    val cups: Int,
    val targetCups: Int = 8,
    val totalMl: Int,
    val updatedAt: String,
    val streak: Int = 0,
)