package com.karrar.movieapp.domain

sealed class ResultHandler<out T> {
    data class Success<T>(val data: T) : ResultHandler<T>()
    data class Error(val throwable: Throwable) : ResultHandler<Nothing>()
}

fun <T> T?.toResult(errorMessage: String = "Not Success"): ResultHandler<T> =
    if (this != null) ResultHandler.Success(this)
    else ResultHandler.Error(Throwable(errorMessage))

suspend fun <T> runCatchingResult(block: suspend () -> T?): ResultHandler<T> = try {
    block().toResult()
} catch (e: Throwable) {
    ResultHandler.Error(e)
}
