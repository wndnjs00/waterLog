package com.app.domain

import com.app.domain.usecase.StreakCalculator
import junit.framework.TestCase.assertEquals
import org.junit.Test

class StreakCalculatorTest {
    private val today = "2026-03-04"
    private val yesterday = "2026-03-03"

    // 첫 목표 달성
    @Test
    fun 첫목표_달성() {
        val result = StreakCalculator.calculate(
            lastGoalDate = null,
            today = today,
            yesterday = yesterday,
            currentStreak = 0,
            reachedGoalFirstTime = true,
        )
        assertEquals(1, result)
    }

    // 같은날 재달성 -> 증가하면 안됨
    @Test
    fun 같은날_재달성() {
        val result = StreakCalculator.calculate(
            lastGoalDate = yesterday,
            today = today,
            yesterday = yesterday,
            currentStreak = 1,
            reachedGoalFirstTime = false
        )
        assertEquals(1, result)
    }

    // 연속달성시, streak 증가
    @Test
    fun 연속달성() {
        val result = StreakCalculator.calculate(
            lastGoalDate = yesterday,
            today = today,
            yesterday = yesterday,
            currentStreak = 1,
            reachedGoalFirstTime = true
        )
        assertEquals(2, result)
    }

    // 하루 실패후 달성 (리셋됨)
    @Test
    fun 하루_실패후_달성() {
        val result = StreakCalculator.calculate(
            lastGoalDate = "2026-03-01",
            today = today,
            yesterday = yesterday,
            currentStreak = 5,
            reachedGoalFirstTime = true
        )
        assertEquals(1, result)
    }

    // 목표 미달성시, streak 유지
    @Test
    fun 목표_미달성() {
        val result = StreakCalculator.calculate(
            lastGoalDate = yesterday,
            today = today,
            yesterday = yesterday,
            currentStreak = 3,
            reachedGoalFirstTime = false
        )
        assertEquals(3, result)
    }

    // streak가 0일때(초기상태에), 첫연속 성공
    @Test
    fun 초기상태에_성공() {
        val result = StreakCalculator.calculate(
            lastGoalDate = yesterday,
            today = today,
            yesterday = yesterday,
            currentStreak = 0,
            reachedGoalFirstTime = true
        )
        assertEquals(1, result)
    }

    // 같은날 중복성공은 증가하지 않음
    @Test
    fun 같은날_중복성공() {
        val result = StreakCalculator.calculate(
            lastGoalDate = today,
            today = today,
            yesterday = yesterday,
            currentStreak = 3,
            reachedGoalFirstTime = true
        )
        assertEquals(3, result)
    }
}

/*
* 결과
* 처음 성공: 1
* 어제성공, 오늘 성공: +1
* 같은날 중복: 유지
* 하루 이상 비었음: 1 (리셋)
* 목표 미달성: 유지
* */