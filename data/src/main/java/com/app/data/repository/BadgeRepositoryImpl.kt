package com.app.data.repository

import com.app.data.model.BadgeDto
import com.app.domain.model.Badge
import com.app.domain.repository.BadgeRepository
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class BadgeRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
) : BadgeRepository {

    override fun observeBadges(uid: String) = callbackFlow {
        val cacheMap = mutableMapOf<String, Badge>()
        var isFirstSnapshot = true

        val listener = firestore
            .collection("users")
            .document(uid)
            .collection("badges")
            .addSnapshotListener { snapshot, _ ->

                if (snapshot == null) return@addSnapshotListener

                if (isFirstSnapshot) {
                    snapshot.documents.forEach { doc ->
                        val dto = doc.toObject(BadgeDto::class.java)
                        val badge = dto?.toDomain()

                        if (badge != null) {
                            cacheMap[doc.id] = badge
                        }
                    }
                    isFirstSnapshot = false
                    trySend(cacheMap.toMap())
                    return@addSnapshotListener
                }

                for (change in snapshot.documentChanges) {
                    val dto = change.document.toObject(BadgeDto::class.java)
                    val badge = dto.toDomain()
                    val key = change.document.id

                    if (badge != null) {
                        when (change.type) {
                            DocumentChange.Type.ADDED,
                            DocumentChange.Type.MODIFIED -> cacheMap[key] = badge
                            DocumentChange.Type.REMOVED -> cacheMap.remove(key)
                        }
                    }
                }
                trySend(cacheMap.toMap())
            }
        awaitClose { listener.remove() }
    }
}