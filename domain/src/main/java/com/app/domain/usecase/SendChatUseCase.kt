package com.app.domain.usecase

import com.app.domain.repository.ChatRepository
import javax.inject.Inject

class SendChatUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(question: String): String {
        return repository.sendQuestion(question)
    }
}