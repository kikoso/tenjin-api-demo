package com.tenjin.sdk

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder

import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.mockito.kotlin.spy
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.Response

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class BatchEventWorkerTest {

    private lateinit var context: Context

    @Mock
    private lateinit var eventDao: EventDao

    @Mock
    private lateinit var userDao: UserDao

    @Mock
    private lateinit var tenjinApi: TenjinApi

    @Mock
    private lateinit var advertisingIdProvider: AdvertisingIdProvider

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        context = ApplicationProvider.getApplicationContext()
        val sharedPreferences = context.getSharedPreferences("tenjin_sdk_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit()
            .putString("api_key", "123")
            .putString("bundle_id", "com.tenjin.sdk.test")
            .commit()
    }

    @Test
    fun `doWork returns success when no events`() = runBlocking {
        whenever(eventDao.getEvents()).thenReturn(emptyList())
        val worker = createWorker()
        val result = worker.doWork()
        assertEquals(ListenableWorker.Result.success(), result)
    }

    @Test
    fun `doWork returns success when events are sent`() = runBlocking {
        val events = listOf(Event(id = 1, eventName = "test_event"))
        whenever(eventDao.getEvents()).thenReturn(events)
        whenever(advertisingIdProvider.getAdvertisingId()).thenReturn("test_aid")
        whenever(tenjinApi.sendEvent(any())).thenReturn(Response.success(Unit))
        whenever(eventDao.deleteEvent(any())).thenReturn(Unit)
        val worker = createWorker()
        val result = worker.doWork()
        assertEquals(ListenableWorker.Result.success(), result)
    }

    @Test
    fun `doWork returns retry when api fails`() = runBlocking {
        val events = listOf(Event(id = 1, eventName = "test_event"))
        whenever(eventDao.getEvents()).thenReturn(events)
        whenever(advertisingIdProvider.getAdvertisingId()).thenReturn("test_aid")
        whenever(tenjinApi.sendEvent(any())).thenReturn(Response.error(500, "".toResponseBody("application/json".toMediaTypeOrNull())))
        val worker = createWorker()
        val result = worker.doWork()
        assertEquals(ListenableWorker.Result.retry(), result)
    }

    @Test
    fun `doWork returns failure when api key is missing`() = runBlocking {
        val sharedPreferences = context.getSharedPreferences("tenjin_sdk_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().commit()
        val events = listOf(Event(id = 1, eventName = "test_event"))
        whenever(eventDao.getEvents()).thenReturn(events)
        whenever(advertisingIdProvider.getAdvertisingId()).thenReturn("test_aid")
        val worker = createWorker()
        val result = worker.doWork()
        assertEquals(ListenableWorker.Result.failure(), result)
    }

    private fun createWorker(): BatchEventWorker {
        val workerFactory = object : WorkerFactory() {
            override fun createWorker(
                appContext: Context,
                workerClassName: String,
                workerParameters: WorkerParameters
            ): ListenableWorker {
                return BatchEventWorker(appContext, workerParameters, eventDao, userDao, tenjinApi, advertisingIdProvider)
            }
        }
        return TestListenableWorkerBuilder<BatchEventWorker>(context)
            .setWorkerFactory(workerFactory)
            .build()
    }
}