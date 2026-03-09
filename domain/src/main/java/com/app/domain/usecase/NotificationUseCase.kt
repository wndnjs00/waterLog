package com.app.domain.usecase

import com.app.domain.model.Notification
import com.app.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NotificationUseCase @Inject constructor(
    private val repository: NotificationRepository
){
    fun observe(uid: String): Flow<List<Notification>> {
        return repository.observeNotifications(uid)
    }

    suspend fun markRead(uid: String, id: String) {
        repository.markAsRead(uid, id)
    }
}