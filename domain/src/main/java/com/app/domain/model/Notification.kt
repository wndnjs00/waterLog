package com.app.domain.model

data class Notification(
    val id: String,
    val title: String,
    val message: String,
    val createdAt: String,
    val isRead: Boolean,
    val type: String,
)