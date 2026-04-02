package com.app.data.repository

import com.app.data.model.ChatRequest
import com.app.data.model.Message
import com.app.data.remote.OpenAiApi
import com.app.domain.repository.ChatRepository
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val api: OpenAiApi
): ChatRepository {
    override suspend fun sendQuestion(question: String): String {

        val response = api.chat(
            ChatRequest(
                messages = listOf(
                    Message(
                        "system",
                        "너는 수분 섭취 전문가이자 이를 도와주는 AI야. " +
                                "물, 커피, 음료 등 수분 섭취와 관련된 질문에만 답해. " +
                                "수분 섭취와 관련 없는 질문이면 " +
                                "\"수분 섭취와 관련된 질문만 도와드릴 수 있어요 💧\"라고 답해. " +
                                "답변은 반드시 40자 이내로 간결하게 작성해. " +
                                "불필요한 설명은 하지 마. " +
                                "카페인 음료는 과다 섭취 시 주의하도록 안내해."
                    ),
                    Message("user", question)
                )
            )
        )
        return response.choices.firstOrNull()?.message?.content ?: "답변을 가져올 수 없습니다"
    }
}