package com.app.domain.repository

import com.app.domain.model.Badge
import kotlinx.coroutines.flow.Flow

interface BadgeRepository {
    fun observeBadges(uid: String): Flow<Map<String, Badge>>
}