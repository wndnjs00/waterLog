package com.app.data.datasource

import com.app.domain.model.TempModel
import javax.inject.Inject

class TempDataSource @Inject constructor() {

    fun getTempModel() : TempModel {
        return TempModel("name")
    }
}