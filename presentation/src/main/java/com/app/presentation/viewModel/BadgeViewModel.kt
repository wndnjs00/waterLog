package com.app.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.domain.model.Badge
import com.app.domain.repository.BadgeShownStore
import com.app.domain.usecase.AccountUseCase
import com.app.domain.usecase.BadgeUseCase
import com.app.presentation.ui.event.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BadgeViewModel @Inject constructor(
    private val badgeUseCase: BadgeUseCase,
    private val accountUseCase: AccountUseCase,
    private val badgeShownStore: BadgeShownStore,
): ViewModel() {

    private val _badges = MutableStateFlow<Map<String, Badge>>(emptyMap())
    val badges: StateFlow<Map<String, Badge>> = _badges

    private val _event = MutableSharedFlow<UiEvent>()
    val event = _event.asSharedFlow()

    init {
        observeBadge()
    }

    private fun observeBadge() {
        viewModelScope.launch {
            val user = accountUseCase.getAccountInfo().value ?: return@launch

            badgeUseCase.observe(user.uid).collect { map ->
                _badges.value = map

                val shownSet = badgeShownStore.shownBadges.first()

                map.keys.forEach { key ->
                    if (!shownSet.contains(key)) {
                        // 신규뱃지 -> 다이얼로그
                        _event.emit(UiEvent.ShowBadgeDialog(key))
                        badgeShownStore.saveBadge(key)
                    }
                }
            }
        }
    }
}