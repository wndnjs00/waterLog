package com.app.domain.repository

interface ChatRepository {
    suspend fun sendQuestion(question: String): String
}