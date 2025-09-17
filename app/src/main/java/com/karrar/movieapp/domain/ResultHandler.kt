package com.karrar.movieapp.domain

sealed class ResultHandler<out T> {
    data class Success<T>(val data: T) : ResultHandler<T>()
    data class Error(val throwable: Throwable) : ResultHandler<Nothing>()
}
