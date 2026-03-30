package com.app.data.repository

import com.app.domain.model.Badge
import com.app.domain.repository.BadgeRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class BadgeRepositoryImpl @Inject constructor(
   private val firestore: FirebaseFirestore,
): BadgeRepository {

    override fun observeBadges(uid: String) = callbackFlow {
        val listener = firestore
            .collection("users")
            .document(uid)
            .collection("badges")
            .addSnapshotListener { snapshot, _ ->

                val map = mutableMapOf<String, Badge>()

                snapshot?.documents?.forEach { doc ->
                    val badge = doc.toObject(Badge::class.java)
                    if (badge != null) {
                        map[doc.id] = badge
                    }
                }
                trySend(map)
            }

        awaitClose { listener.remove() }
    }
}