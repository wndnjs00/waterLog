package com.app.domain.repository

import kotlinx.coroutines.flow.Flow

interface ChatLimitStore {
    val countFlow: Flow<Int>
    suspend fun increase(today: String)
}

