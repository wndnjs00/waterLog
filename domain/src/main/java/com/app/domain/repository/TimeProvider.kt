package com.app.domain.repository

interface TimeProvider {
    fun nowDateTimeString(): String
    fun nowDateString(): String
    fun yesterdayString(): String
    fun weekStart(): String
    fun monthStart(): String
    fun formatNotificationTime(dateTime: String): Result<String>
}