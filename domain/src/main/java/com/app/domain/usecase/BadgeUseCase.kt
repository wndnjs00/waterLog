package com.app.domain.usecase

import com.app.domain.repository.BadgeRepository
import javax.inject.Inject

class BadgeUseCase @Inject constructor(
    private val repository: BadgeRepository
){
    fun observe(uid: String) = repository.observeBadges(uid)
}