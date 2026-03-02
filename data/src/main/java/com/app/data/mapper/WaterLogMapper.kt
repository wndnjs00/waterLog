package com.app.data.mapper

import com.app.data.model.WaterLogDto
import com.app.domain.model.WaterLog
import kotlinx.serialization.json.internal.decodeByReader

object WaterLogMapper {
    fun toDomain(dto: WaterLogDto) : WaterLog {
        return WaterLog(
            date = dto.date ?: "",
            cups = dto.cups ?: 0,
            targetCups = dto.targetCups ?: 8,
            totalMl = dto.totalMl ?: 0,
            updatedAt = dto.updatedAt ?: "",
        )
    }
    
    fun toDto(domain: WaterLog) : WaterLogDto {
        return WaterLogDto(
            date = domain.date,
            cups = domain.cups,
            targetCups = domain.targetCups,
            totalMl = domain.totalMl,
            updatedAt = domain.updatedAt,
        )
    }
}