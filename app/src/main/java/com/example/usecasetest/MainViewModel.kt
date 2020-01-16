package com.example.usecasetest

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel : ViewModel() {

    private val viewStateLiveData = MutableLiveData<ViewState>()
    private val uploadLiveData = MutableLiveData<Upload>()
    private val backgroundLiveData = MutableLiveData<Void>()

    fun viewState() = viewStateLiveData
    fun upload() = uploadLiveData
    fun backgroundTask() = backgroundLiveData

    private val uploadUseCase = UploadUseCase(viewModelScope.coroutineContext, Dispatchers.Main)
    private val taskUseCase = TaskUseCase(Dispatchers.IO, Dispatchers.Main)

    init {
        loadData()
        startBackgroundTask()
    }

    fun loadData(cancel: Boolean = false) {
        if (cancel || !uploadUseCase.isActive()) {
            uploadUseCase
                .doBefore { viewStateLiveData.value = ViewState.Loading }
                .onResult { uploadLiveData.value = it }
                .onCancel { viewStateLiveData.value = ViewState.Cancelled }
                .onError { viewStateLiveData.value = ViewState.Error(it) }
                .doAfter { viewStateLiveData.value = ViewState.Success }
                .invoke()
        }
    }

    private fun startBackgroundTask() {
        val time = System.currentTimeMillis()
        viewModelScope.launch {
            stuff()
            Log.d("Task", "isDone: ${System.currentTimeMillis() - time}")
        }

    }

    private suspend fun stuff() {
        var i = 0
        withContext(Dispatchers.IO) {
            repeat(1000000000) {
                i++
            }
        }
    }

    fun cancel() {
        uploadUseCase.cancel()
    }

    fun throwError() {
        uploadUseCase.throwError = true
    }

    sealed class ViewState {
        object Loading : ViewState()
        object Cancelled : ViewState()
        object Success : ViewState()
        class Error(val exception: Throwable?) : ViewState()
    }
}
