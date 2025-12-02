package com.tenjin.sdk

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.kotlin.argThat
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class TenjinSDKTest {

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

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `sendEvent should insert event into dao`() = runTest {
        val sdk = TenjinSDK(context, config, eventDao, userDao, workManager, this)
        sdk.initialize()
        whenever(eventDao.getEventsCount()).thenReturn(0)
        whenever(config.eventQueueSizeLimit).thenReturn(100)
        val eventName = "test_event"
        sdk.sendEvent(eventName)
        advanceUntilIdle()
        verify(eventDao).insert(argThat { this.eventName == eventName })
    }

    @Test
    fun `setUsername should set username in dao`() = runTest {
        val sdk = TenjinSDK(context, config, eventDao, userDao, workManager, this)
        sdk.initialize()
        val username = "test_user"
        sdk.setUsername(username)
        advanceUntilIdle()
        verify(userDao).setUsername(User(username = username))
    }

    @Test
    fun `removeUsername should remove username from dao`() = runTest {
        val sdk = TenjinSDK(context, config, eventDao, userDao, workManager, this)
        sdk.initialize()
        sdk.removeUsername()
        advanceUntilIdle()
        verify(userDao).removeUsername()
    }
}