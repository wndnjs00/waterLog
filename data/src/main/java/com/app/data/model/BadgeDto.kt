package com.app.data.model

import com.app.domain.model.Badge

data class BadgeDto(
    var name: String? = null,
    var createdAt: String? = null
) {
    fun toDomain(): Badge {
        return Badge(
            name = name ?: "",
            createdAt = createdAt ?: ""
        )
    }
}