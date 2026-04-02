package com.app.data.model

data class ChatRequest(
    val model: String = "gpt-4o-mini",
    val messages: List<Message>,
    val max_tokens: Int = 50,
)

data class Message(
    val role: String,
    val content: String,
)