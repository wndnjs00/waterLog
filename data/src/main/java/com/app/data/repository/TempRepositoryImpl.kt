package com.app.data.repository

import com.app.data.datasource.TempDataSource
import com.app.domain.model.TempModel
import com.app.domain.repository.TempRepository
import javax.inject.Inject

class TempRepositoryImpl @Inject constructor(private val dataSource: TempDataSource) : TempRepository {

    override fun getTempModel(): TempModel {
        return dataSource.getTempModel()
    }
}