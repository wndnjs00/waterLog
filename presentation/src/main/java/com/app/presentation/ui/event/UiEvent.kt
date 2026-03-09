package com.app.presentation.ui.event

sealed class UiEvent {
    data class ShowToast(val message: String) : UiEvent()
    object NavigateToLogin : UiEvent()
}