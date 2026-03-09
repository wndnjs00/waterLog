package com.app.data.repository

import com.app.domain.model.Notification
import com.app.domain.repository.NotificationRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
): NotificationRepository{

    override fun observeNotifications(uid: String): Flow<List<Notification>> = callbackFlow {
        val ref = firestore.collection("users")
            .document(uid)
            .collection("notifications")

        val listener = ref.addSnapshotListener { snapshot, _ ->

            val list = snapshot?.documents?.mapNotNull {

                Notification(
                    id = it.id,
                    title = it.getString("title") ?: "",
                    message = it.getString("message") ?: "",
                    createdAt = it.getString("createdAt") ?: "",
                    isRead = it.getBoolean("isRead") ?: false,
                    type = it.getString("type") ?: ""
                )
            } ?: emptyList()

            trySend(list).isSuccess
        }
        awaitClose {listener.remove()}
    }

    override suspend fun markAsRead(uid: String, notificationId: String) {

        firestore.collection("users")
            .document(uid)
            .collection("notifications")
            .document(notificationId)
            .update("isRead", true)
    }
}