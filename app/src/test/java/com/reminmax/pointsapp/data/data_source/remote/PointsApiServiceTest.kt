package com.reminmax.pointsapp.data.data_source.remote

import JvmUnitTestFakeAssetManager
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.reminmax.pointsapp.data.entity.GetPointsResponse
import com.reminmax.pointsapp.data.entity.PointDto
import com.reminmax.pointsapp.data.network.CallAdapterFactory
import com.reminmax.pointsapp.data.network.NetworkResult
import com.reminmax.pointsapp.util.CoroutineTestRule
import com.reminmax.pointsapp.util.GET_POINTS_RESPONSE_ASSET
import io.reactivex.Single
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subscribers.TestSubscriber
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.buffer
import okio.source
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import retrofit2.Retrofit


@RunWith(JUnit4::class)
class PointsApiServiceTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    var coroutineTestRule = CoroutineTestRule()

    private lateinit var apiService: PointsApiService
    private lateinit var mockServer: MockWebServer
    private val client = OkHttpClient.Builder().build()

    private val pointCount = 20

    @Before
    fun setUp() {
        mockServer = MockWebServer()

        val contentType = "application/json".toMediaType()
        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }

        apiService = Retrofit.Builder()
            .baseUrl(mockServer.url(""))
            .client(client)
            .addCallAdapterFactory(CallAdapterFactory.create())
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(PointsApiService::class.java)
    }

    @After
    fun tearDown() {
        mockServer.shutdown()
    }

    private fun enqueueMockResponse() {
        val mockResponse = MockResponse()
        mockResponse.apply {
            setBody("{\"points\":[{\"x\":-3.93,\"y\":-14.00},{\"x\":-30.48,\"y\":63.38},]}")
            setResponseCode(200)
        }
        mockServer.enqueue(mockResponse)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `successful response`() = runTest {

        val scheduler = TestScheduler()
        enqueueMockResponse()
        mockServer.takeRequest()

        val getPoints = apiService.getPoints(count = pointCount)
        val getPointsTest = getPoints.test()

        scheduler.triggerActions()
        getPointsTest.assertComplete()
        getPointsTest.assertNoErrors()


//            .test()
//            .assertValue(
//                NetworkResult.Success<GetPointsResponse>(
//                    GetPointsResponse(
//                        points = listOf(
//                            PointDto(x = -3.93f, y = -14.00f),
//                            PointDto(x = -30.48f, y = 63.38f),
//                        )
//                    )
//                )
//            )


//        val testObserver: TestObserver<NetworkResult<GetPointsResponse>> = response.test()
//        testObserver.assertSubscribed()
//        testObserver.assertComplete()
//
//        testObserver.assertValue(1)
//
//        assertThat(result).isNotNull()
//        assertThat(request.path).isEqualTo("/$POINTS_PATH?count=$pointCount")
//        assertThat(result?.size).isEqualTo(pointCount)
//
//        assertThat(result?.first()?.x).isEqualTo(-3.93f)
//        assertThat(result?.first()?.y).isEqualTo(-14.00f)
    }

}