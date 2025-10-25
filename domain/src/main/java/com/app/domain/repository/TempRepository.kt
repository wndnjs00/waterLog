package com.app.domain.repository

import com.app.domain.model.TempModel

interface TempRepository {
    fun getTempModel() : TempModel
}