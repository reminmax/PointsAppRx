package com.reminmax.pointsapp.data.network

import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class CallAdapterFactory private constructor() : CallAdapter.Factory() {

    companion object {
        @JvmStatic
        fun create(): CallAdapterFactory {
            return CallAdapterFactory()
        }
    }

    override fun get(returnType: Type, annotations: Array<Annotation>, retrofit: Retrofit): CallAdapter<*, *>? {
        if (getRawType(returnType) != Single::class.java) {
            return null
        }

        val responseType = getParameterUpperBound(0, returnType as ParameterizedType)
        val rawReturnType = getRawType(responseType)

        if (rawReturnType != NetworkResult::class.java) {
            return null
        }

        if (responseType !is ParameterizedType) {
            throw IllegalStateException("NetworkResult must have generic type.")
        }

        val successType = getParameterUpperBound(0, responseType)
        return RxJava2CallAdapter<Any>(successType)
    }

    private class RxJava2CallAdapter<R>(
        private val responseType: Type
    ) : CallAdapter<R, Single<out NetworkResult<R>>> {

        override fun responseType(): Type {
            return responseType
        }

        override fun adapt(call: Call<R>): Single<out NetworkResult<R>> {
            return Single.fromCallable {
                val response = call.execute()
                if (response.isSuccessful) {
                    val body = response.body()
                    NetworkResult.Success(body)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val error = Error(errorBody)
                    NetworkResult.Failure(error)
                }
            }.subscribeOn(Schedulers.io())
        }
    }
}