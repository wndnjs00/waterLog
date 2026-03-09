package com.app.domain.repository

import com.app.domain.model.Notification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun observeNotifications(uid: String): Flow<List<Notification>>
    suspend fun markAsRead(uid: String, notificationId: String)
}