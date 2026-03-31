package com.app.data.repository

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
                        val badge = doc.toObject(Badge::class.java)
                        if (badge != null) {
                            cacheMap[doc.id] = badge
                        }
                    }
                    isFirstSnapshot = false
                    trySend(cacheMap.toMap())
                    return@addSnapshotListener
                }

                for (change in snapshot.documentChanges) {
                    val badge = change.document.toObject(Badge::class.java)
                    val key = change.document.id

                    when (change.type) {
                        DocumentChange.Type.ADDED,
                        DocumentChange.Type.MODIFIED -> cacheMap[key] = badge
                        DocumentChange.Type.REMOVED -> cacheMap.remove(key)
                    }
                }
                trySend(cacheMap.toMap())
            }
        awaitClose { listener.remove() }
    }
}