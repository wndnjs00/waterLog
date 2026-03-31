package com.app.domain.repository

import kotlinx.coroutines.flow.Flow

interface BadgeShownStore {
    val shownBadges: Flow<Set<String>>
    suspend fun saveBadge(key: String)
}

