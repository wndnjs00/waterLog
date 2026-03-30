package com.app.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.domain.model.Badge
import com.app.domain.usecase.AccountUseCase
import com.app.domain.usecase.BadgeUseCase
import com.app.presentation.ui.event.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BadgeViewModel @Inject constructor(
    private val badgeUseCase: BadgeUseCase,
    private val accountUseCase: AccountUseCase,
): ViewModel() {

    private val _badges = MutableStateFlow<Map<String, Badge>>(emptyMap())
    val badges: StateFlow<Map<String, Badge>> = _badges

    private val _event = MutableSharedFlow<UiEvent>()
    val event = _event.asSharedFlow()

    private var shownBadges = mutableSetOf<String>()

    init {
        observeBadge()
    }

    private fun observeBadge(){
        viewModelScope.launch {
            val user = accountUseCase.getAccountInfo().value ?: return@launch

            badgeUseCase(user.uid).collect { map ->
                _badges.value = map

                map.keys.forEach { key ->
                    if (!shownBadges.contains(key)) {
                        shownBadges.add(key)
                        _event.emit(UiEvent.ShowBadgeDialog(key))
                    }
                }
            }
        }
    }
}