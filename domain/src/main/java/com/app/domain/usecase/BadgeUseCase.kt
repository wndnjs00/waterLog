package com.app.domain.usecase

import com.app.domain.repository.BadgeRepository
import javax.inject.Inject

class BadgeUseCase @Inject constructor(
    private val repository: BadgeRepository
){
    operator fun invoke(uid: String) = repository.observeBadges(uid)
}