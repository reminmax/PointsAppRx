package com.reminmax.pointsapp.ui.screens.chart

import androidx.lifecycle.SavedStateHandle
import com.reminmax.pointsapp.data.fake.FakeAndroidResourceProvider
import com.reminmax.pointsapp.domain.model.LinearChartStyle
import com.reminmax.pointsapp.domain.model.Point
import com.reminmax.pointsapp.ui.navigation.NavigationParams
import com.reminmax.pointsapp.util.CoroutineTestRule
import com.reminmax.pointsapp.util.RxImmediateSchedulerRule
import io.reactivex.schedulers.TestScheduler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.MockitoAnnotations

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(JUnit4::class)
class ChartViewModelTest {

    @get:Rule
    var coroutineTestRule = CoroutineTestRule()

    @get:Rule
    val schedulers = RxImmediateSchedulerRule()

    private lateinit var viewModel: ChartViewModel
    private lateinit var testScheduler: TestScheduler
    private val resourceProvider = FakeAndroidResourceProvider()

    private val points = "[{\"x\": -3.93,\"y\": -14.00},{\"x\": -30.48,\"y\": 63.38}]"
    private val savedStateHandle: SavedStateHandle = SavedStateHandle().apply {
        this[NavigationParams.POINTS] = points
    }

    private val pointList = Json.decodeFromString<List<Point>>(points)

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        testScheduler = TestScheduler()

        viewModel = ChartViewModel(
            savedStateHandle = savedStateHandle,
            resourceProvider = resourceProvider
        )
    }

    @Test
    fun `savedStateHandle test`() = runTest {
        val testObserver = viewModel.uiState.test()
        testObserver.assertValue {
            it.points == pointList
        }
        testObserver.assertNoErrors()
    }

    @Test
    fun `ChartStyleSelected test`() = runTest {
        val expectedValue = LinearChartStyle.SMOOTH
        viewModel.dispatch(ChartAction.ChartStyleSelected(expectedValue))

        val testObserver = viewModel.uiState.test()
        testObserver.assertValue {
            it.chartStyle == expectedValue
        }
        testObserver.assertNoErrors()
    }

    @Test
    fun `ShowUserMessage test`() = runTest {
        val messageToShow = "some text"

        viewModel.dispatch(
            ChartAction.ShowUserMessage(
                messageToShow = messageToShow,
                actionLabel = null,
                onActionPerformed = {}
            )
        )

        val testObserver = viewModel.userMessages.test()
        testObserver.assertValue { msgList ->
            msgList.size == 1 && msgList.first().message == messageToShow
        }
        testObserver.assertNoErrors()
    }

    @Test
    fun `UserMessageShown test`() = runTest {
        viewModel.dispatch(
            ChartAction.ShowUserMessage(
                messageToShow = "some text",
                actionLabel = null,
                onActionPerformed = {}
            )
        )

        viewModel.dispatch(
            ChartAction.UserMessageShown(
                messageId = viewModel.userMessages.blockingFirst().first().id
            )
        )

        val testObserver = viewModel.userMessages.test()
        testObserver.assertValue { msgList ->
            msgList.isEmpty()
        }
        testObserver.assertNoErrors()
    }
}