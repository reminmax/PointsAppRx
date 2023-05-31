package com.reminmax.pointsapp.ui.screens.home

import com.reminmax.pointsapp.data.SResult
import com.reminmax.pointsapp.data.fake.FakeAndroidResourceProvider
import com.reminmax.pointsapp.domain.helpers.INetworkUtils
import com.reminmax.pointsapp.domain.model.Point
import com.reminmax.pointsapp.domain.repository.IPointsApiDataSource
import com.reminmax.pointsapp.domain.validation.*
import com.reminmax.pointsapp.util.CoroutineTestRule
import com.reminmax.pointsapp.util.RxImmediateSchedulerRule
import io.reactivex.Single
import io.reactivex.schedulers.TestScheduler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import java.util.concurrent.TimeUnit

@RunWith(JUnit4::class)
class HomeViewModelTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    var coroutineTestRule = CoroutineTestRule()

    @get:Rule
    val schedulers = RxImmediateSchedulerRule()

    @Mock
    private lateinit var networkUtils: INetworkUtils

    @Mock
    private lateinit var dataSource: IPointsApiDataSource

    private lateinit var viewModel: HomeViewModel
    private lateinit var testScheduler: TestScheduler

    private val resourceProvider = FakeAndroidResourceProvider()
    private val validator: IValidator = Validator()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        testScheduler = TestScheduler()

        viewModel = HomeViewModel(
            dataSource = dataSource,
            resourceProvider = resourceProvider,
            validator = validator,
            networkUtils = networkUtils
        )
    }

    @Test
    fun pointCountValueChanged_test() {
        val pointCount = "20"
        viewModel.dispatch(HomeAction.PointCountValueChanged(pointCount))

        val testObserver = viewModel.uiState.test()

        testScheduler.triggerActions()
        testObserver.assertValueCount(1)
        testObserver.assertValue {
            it.pointCount == pointCount
        }
        testObserver.assertNoErrors()
    }

    @Test
    fun getPoints_test() {
        val pointCount = "1"
        val pointList = listOf(Point(10f, 15f))
        `when`(networkUtils.hasNetworkConnection()).thenReturn(true)
        `when`(dataSource.getPoints(pointCount.toInt())).thenReturn(
            Single.just(
                SResult.Success(pointList)
            )
        )

        val event = HomeScreenEvent.NavigateToChartScreen(points = pointList)
        val mViewModel = spy(viewModel)
        mViewModel.dispatch(HomeAction.PointCountValueChanged(pointCount))
        mViewModel.dispatch(HomeAction.GetPoints)
        testScheduler.triggerActions()

        verify(mViewModel).sendEvent(event)
    }
}