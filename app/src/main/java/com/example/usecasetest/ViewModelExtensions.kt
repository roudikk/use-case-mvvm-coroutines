package com.example.usecasetest

import androidx.lifecycle.MutableLiveData


fun <T, Params> BaseUseCase<T, Params>.loadWithLiveData(viewStateLiveData: MutableLiveData<ViewState<T>>): BaseUseCase<T, Params> {
    doBefore {
        viewStateLiveData.value = ViewState.Loading()
    }
    onResult {
        viewStateLiveData.value = ViewState.Result(it)
    }
    onCancel {
        viewStateLiveData.value = ViewState.Cancelled()
    }
    onError {
        viewStateLiveData.value = ViewState.Error(it)
    }
    doAfter {
        val result = (viewStateLiveData.value as? ViewState.Result?)?.result
        viewStateLiveData.value = ViewState.Success(result)
    }
    return this
}
