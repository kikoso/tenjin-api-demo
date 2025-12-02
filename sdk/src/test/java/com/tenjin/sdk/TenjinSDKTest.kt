package com.tenjin.sdk

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class TenjinSDKTest {

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var config: TenjinSDKConfig

    @Mock
    private lateinit var eventDao: EventDao

    @Mock
    private lateinit var userDao: UserDao

    @Mock
    private lateinit var workManager: WorkManager

    private lateinit var sdk: TenjinSDK
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = CoroutineScope(testDispatcher)

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        sdk = TenjinSDK(context, config, eventDao, userDao, workManager, testScope)
        sdk.initialize()
    }

    @Test
    fun `sendEvent should insert event into dao`() = runTest {
        whenever(eventDao.getEventsCount()).thenReturn(0)
        whenever(config.eventQueueSizeLimit).thenReturn(100)
        val eventName = "test_event"
        sdk.sendEvent(eventName)
        verify(eventDao).insert(Event(eventName = eventName))
    }

    @Test
    fun `setUsername should set username in dao`() = runTest {
        val username = "test_user"
        sdk.setUsername(username)
        verify(userDao).setUsername(User(username = username))
    }

    @Test
    fun `removeUsername should remove username from dao`() = runTest {
        sdk.removeUsername()
        verify(userDao).removeUsername()
    }
}