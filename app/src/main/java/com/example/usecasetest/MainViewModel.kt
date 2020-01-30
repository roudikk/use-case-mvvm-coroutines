package com.example.usecasetest

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers

class MainViewModel : ViewModel() {

    private val viewStateLiveData = MutableLiveData<ViewState<Upload>>()

    fun viewState() = viewStateLiveData

    private val uploadUseCase = UploadUseCase(Dispatchers.IO, viewModelScope.coroutineContext)

    init {
        loadData()
    }

    fun loadData(cancel: Boolean = false) {
        if (cancel || !uploadUseCase.isActive()) {
            uploadUseCase
                .loadWithLiveData(viewStateLiveData)
                .invoke(UploadUseCase.Params("test"))
        }
    }

    fun cancel() {
        uploadUseCase.cancel()
    }

    fun throwError() {
        uploadUseCase.throwError = true
    }


}
