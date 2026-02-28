package com.app.data.model

data class WaterLogDto (
    var date: String? = null,
    var cups: Int? = null,
    var targetCups: Int? = 8,
    var totalMl: Int? = null,
    var updatedAt: String? = null
)