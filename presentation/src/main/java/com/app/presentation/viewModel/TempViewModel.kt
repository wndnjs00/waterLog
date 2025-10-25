package com.app.presentation.viewModel

import androidx.lifecycle.ViewModel
import com.app.domain.model.TempModel
import com.app.domain.usecase.TempUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TempViewModel @Inject constructor(private val useCase: TempUseCase) : ViewModel(){

    fun getTempModel(): TempModel{
        return useCase.getTempMode()
    }
}