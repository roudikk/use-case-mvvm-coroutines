package com.example.usecasetest

@Suppress("unused")
sealed class ViewState<T> {
    class Loading<T> : ViewState<T>()
    class Cancelled<T> : ViewState<T>()
    class Result<T>(val result: T) : ViewState<T>()
    class Success<T>(val result: T? = null) : ViewState<T>()
    class Error<T>(val throwable: Throwable?) : ViewState<T>()
}