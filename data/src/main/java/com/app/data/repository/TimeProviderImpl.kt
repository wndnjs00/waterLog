package com.app.data.repository

import com.app.domain.repository.TimeProvider
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
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

    // 마지막 기록날짜 (streak(연속 기록))이 어제인지 확인
    override fun yesterdayString(): String {
        return LocalDateTime.now().minusDays(1).format(dateFormatter)
    }

    // 주간 조회
    override fun weekStart(): String {
        val monday = LocalDate.now()
            .with(DayOfWeek.MONDAY)
        return monday.format(dateFormatter)
    }

    override fun monthStart(): String {
        val firstDay = LocalDate.now()
            .withDayOfMonth(1)
        return firstDay.format(dateFormatter)
    }
}