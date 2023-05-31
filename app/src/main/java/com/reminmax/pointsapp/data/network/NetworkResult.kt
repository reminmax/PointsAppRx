package com.reminmax.pointsapp.data.network

sealed class NetworkResult<out T>  {
    data class Success<out T>(val data: T?) : NetworkResult<T>()
    data class Failure(val error: Error) : NetworkResult<Nothing>()
}
