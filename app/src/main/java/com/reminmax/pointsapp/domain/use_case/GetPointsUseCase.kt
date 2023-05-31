package com.reminmax.pointsapp.domain.use_case

//import com.reminmax.pointsapp.data.SResult
//import com.reminmax.pointsapp.data.emptyResult
//import com.reminmax.pointsapp.data.errorResult
//import com.reminmax.pointsapp.data.loading
//import com.reminmax.pointsapp.data.successResult
//import com.reminmax.pointsapp.domain.model.Point
//import com.reminmax.pointsapp.domain.repository.IPointsApiDataSource
//import io.reactivex.Observable
//import io.reactivex.ObservableOnSubscribe
//import io.reactivex.schedulers.Schedulers
//import javax.inject.Inject
//
//interface IGetPointsUseCase {
//    operator fun invoke(count: Int): Observable<SResult<List<Point>>>
//}
//
//class GetPointsUseCase @Inject constructor(
//    private val dataSource: IPointsApiDataSource
//) : IGetPointsUseCase {
//
//    override operator fun invoke(count: Int): Observable<SResult<List<Point>>> {
//        return Observable.create(ObservableOnSubscribe<SResult<List<Point>>> { emitter ->
//            emitter.onNext(loading())
//
//            try {
//                when (val response = dataSource.getPoints(count = count)) {
//                    is NetworkResult.Success -> {
//                        val body = response.data.points.map { pointDto ->
//                            pointDto.convertTo()
//                        }
//                        if (body.isNotEmpty()) {
//                            emitter.onNext(successResult(data = body))
//                        } else {
//                            emitter.onNext(emptyResult())
//                        }
//                        emitter.onComplete()
//                    }
//                    is NetworkResult.Error -> {
//                        emitter.onNext(errorResult(response.code, response.message))
//                        emitter.onComplete()
//                    }
//                    is NetworkResult.Exception -> {
//                        emitter.onNext(errorResult(0, response.e.localizedMessage))
//                        emitter.onComplete()
//                    }
//
//                }
//            } catch (ex: Exception) {
//                emitter.onNext(errorResult(0, ex.localizedMessage))
//                emitter.onComplete()
//            }
//        }).subscribeOn(Schedulers.io())
//    }
//}