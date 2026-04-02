package com.app.data.remote

import com.app.data.model.ChatRequest
import com.app.data.model.ChatResponse
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface OpenAiApi {
    @Headers("Content-Type: application/json")
    @POST("v1/chat/completions")
    suspend fun chat(
        @Body request: ChatRequest
    ) : ChatResponse
}