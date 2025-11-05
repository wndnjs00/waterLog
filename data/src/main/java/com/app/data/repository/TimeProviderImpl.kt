package com.app.data.repository

import com.app.domain.repository.TimeProvider
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import javax.inject.Inject


class TimeProviderImpl @Inject constructor() : TimeProvider {
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")

    override fun nowDateTimeString(): String {
        return LocalDateTime.now().format(dateTimeFormatter)
    }

    override fun nowDateString(): String {
        return LocalDateTime.now().format(dateFormatter)
    }
}