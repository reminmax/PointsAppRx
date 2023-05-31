package com.reminmax.pointsapp.domain.repository

import com.reminmax.pointsapp.data.SResult
import com.reminmax.pointsapp.domain.model.Point
import io.reactivex.Single

interface IPointsApiDataSource {

    fun getPoints(count: Int): Single<SResult<List<Point>>>

}