package com.app.domain.usecase

import com.app.domain.model.TempModel
import com.app.domain.repository.TempRepository
import javax.inject.Inject

class TempUseCase @Inject constructor(private val repository: TempRepository) {

    fun getTempMode() : TempModel{
        return repository.getTempModel()
    }
}