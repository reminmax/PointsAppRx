package com.reminmax.pointsapp.data.repository

import com.reminmax.pointsapp.data.SResult
import com.reminmax.pointsapp.data.data_source.remote.PointsApiService
import com.reminmax.pointsapp.data.emptyResult
import com.reminmax.pointsapp.data.errorResult
import com.reminmax.pointsapp.data.network.NetworkResult
import com.reminmax.pointsapp.data.successResult
import com.reminmax.pointsapp.domain.model.Point
import com.reminmax.pointsapp.domain.repository.IPointsApiDataSource
import io.reactivex.Single
import javax.inject.Inject

class PointsApiDataSource @Inject constructor(
    private val apiService: PointsApiService
) : IPointsApiDataSource {

    override fun getPoints(count: Int): Single<SResult<List<Point>>> {
        return apiService.getPoints(count)
            .map { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        val response = result.data
                        if (response != null) {
                            successResult(
                                response.points.map {
                                    it.convertTo()
                                })
                        } else {
                            emptyResult()
                        }
                    }

                    is NetworkResult.Failure -> {
                        val error = result.error
                        errorResult(0, error.message)
                    }
                }
            }
    }
}