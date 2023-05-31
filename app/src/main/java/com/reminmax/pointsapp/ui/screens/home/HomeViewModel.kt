package com.reminmax.pointsapp.ui.screens.home

import com.reminmax.pointsapp.R
import com.reminmax.pointsapp.common.util.MAX_POINT_COUNT
import com.reminmax.pointsapp.common.util.MIN_POINT_COUNT
import com.reminmax.pointsapp.data.SResult
import com.reminmax.pointsapp.domain.helpers.INetworkUtils
import com.reminmax.pointsapp.domain.repository.IPointsApiDataSource
import com.reminmax.pointsapp.domain.resource_provider.IResourceProvider
import com.reminmax.pointsapp.domain.validation.IValidator
import com.reminmax.pointsapp.domain.validation.rules.IsInRangeValidationRule
import com.reminmax.pointsapp.domain.validation.rules.IsIntegerValidationRule
import com.reminmax.pointsapp.domain.validation.rules.NotEmptyValidationRule
import com.reminmax.pointsapp.domain.validation.rules.OnlyNumbersValidationRule
import com.reminmax.pointsapp.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val dataSource: IPointsApiDataSource,
    private val resourceProvider: IResourceProvider,
    private val validator: IValidator,
    private val networkUtils: INetworkUtils,
) : BaseViewModel() {

    private val _uiState = BehaviorSubject.createDefault(HomeUiState())
    val uiState: Observable<HomeUiState> = _uiState.hide()

    private val mDisposable = CompositeDisposable()

    fun dispatch(action: HomeAction) {
        when (action) {
            HomeAction.GetPoints -> {
                getPoints()
            }

            is HomeAction.PointCountValueChanged -> {
                onPointCountValueChanged(action.value)
            }
        }
    }

    private fun getPoints() {
        if (!networkUtils.hasNetworkConnection()) {
            showErrorMessage(
                message = resourceProvider.getString(R.string.noInternetConnection)
            )
            return
        }

        validatePointCountValue()

        _uiState.value?.let {
            if (it.isPointCountValid) {
                updatePoints(count = it.pointCount.toInt())
            }
        }
    }

    private fun updatePoints(count: Int) {
        startLoading()

        mDisposable.add(
            dataSource.getPoints(count = count)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { result ->
                    when (result) {
                        SResult.Empty -> {
                            stopLoading()
                        }

                        is SResult.Error -> {
                            stopLoading()
                            showErrorMessage(message = result.message)
                        }

                        is SResult.Success -> {
                            stopLoading()
                            sendEvent(
                                HomeScreenEvent.NavigateToChartScreen(
                                    points = result.data
                                )
                            )
                        }
                    }
                }
        )
    }

    private fun validatePointCountValue() {
        val pointCount = _uiState.value?.pointCount ?: ""

        validator
            .addValidationRules(
                NotEmptyValidationRule(
                    errorMsg = resourceProvider.getString(R.string.valueCantBeEmptyError)
                ),
                IsIntegerValidationRule(
                    errorMsg = resourceProvider.getString(R.string.onlyIntegerValuesAcceptedError)
                ),
                OnlyNumbersValidationRule(
                    errorMsg = resourceProvider.getString(
                        R.string.shouldNotContainAnyAlphabetCharactersError
                    )
                ),
                IsInRangeValidationRule(
                    startRange = MIN_POINT_COUNT,
                    endRange = MAX_POINT_COUNT,
                    errorMsg = resourceProvider.getString(
                        R.string.valueOutOfRangeError,
                        MIN_POINT_COUNT,
                        MAX_POINT_COUNT
                    )
                )
            )
            .addSuccessCallback {
                _uiState.value?.copy(errorMessage = null)?.let { _uiState.onNext(it) }
            }
            .addErrorCallback { errorMsg ->
                _uiState.value?.copy(errorMessage = errorMsg)?.let { _uiState.onNext(it) }
            }
            .validate(pointCount)
    }

    private fun stopLoading() {
        _uiState.value?.copy(isLoading = false)?.let { _uiState.onNext(it) }
    }

    private fun startLoading() {
        _uiState.value?.copy(isLoading = true)?.let { _uiState.onNext(it) }
    }

    private fun showErrorMessage(message: String? = null) {
        _uiState.value?.copy(
            errorMessage = message
                ?: resourceProvider.getString(R.string.anUnexpectedErrorOccurred)
        )?.let { _uiState.onNext(it) }
    }

    private fun onPointCountValueChanged(value: String) {
        _uiState.value?.copy(pointCount = value)?.let { _uiState.onNext(it) }
    }

    override fun onCleared() {
        super.onCleared()
        mDisposable.clear()
    }
}