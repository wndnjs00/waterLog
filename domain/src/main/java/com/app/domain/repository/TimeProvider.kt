package com.app.domain.repository

interface TimeProvider {
    fun nowDateTimeString(): String
    fun nowDateString(): String
}