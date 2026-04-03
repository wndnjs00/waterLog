package com.app.data.repository

import com.app.data.model.BadgeDto
import com.app.domain.model.Badge
import com.app.domain.repository.BadgeRepository
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class BadgeRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
) : BadgeRepository {

    override fun observeBadges(uid: String) = callbackFlow {
        val badgesRef = firestore
            .collection("users")
            .document(uid)
            .collection("badges")

        // 스냅샷 리스너만으로는 오프라인 시 캐시로 조용히 동작해 error 콜백이 오지 않는 경우가 많음.
        // 서버 1회 조회로 네트워크/권한 실패를 확실히 전달한다.
        badgesRef.get(Source.SERVER).await()

        val cacheMap = mutableMapOf<String, Badge>()
        var isFirstSnapshot = true

        val listener = badgesRef.addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }

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