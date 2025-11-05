package com.app.data.repository

import com.app.domain.model.UserInfo
import com.app.domain.repository.AccountRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AccountRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : AccountRepository {
    private val accountInfoFlow = MutableStateFlow<UserInfo?>(null)

    override fun getAccountInfo(): StateFlow<UserInfo?> {
        return accountInfoFlow
    }

    override suspend fun signInGoogle(userInfo: UserInfo) {
        firestore.collection("users")
            .document(userInfo.uid)
            .set(userInfo)
            .await()
        accountInfoFlow.emit(userInfo)
    }

    override suspend fun logoutGoogle() {
        accountInfoFlow.emit(null)
        FirebaseAuth.getInstance().signOut()
    }
}