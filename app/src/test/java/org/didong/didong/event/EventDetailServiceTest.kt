package org.didong.didong.event

import android.app.Activity
import com.github.salomonbrys.kodein.KodeinInjector
import com.nhaarman.mockito_kotlin.*
import org.junit.After
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*
import java.util.*

/**
 * EventDetail Service Unit Tests
 */
class EventDetailServiceTest {
    var calService: CalendarService = mock()
    var evtDetailService: EventDetailService = mock()
    var parentActivity: Activity = mock()

    @Before
    fun setUp() {
        calService = mock()
        given(calService.getActivityCalendar(any())).willReturn("test")

        evtDetailService = EventDetailService(calService)
    }

    @After
    fun tearDown() {
        // Nothing to do for the moment as there's no real state change :-)
    }

    @Test
    fun `Default current date should be today`() {
        // Given
        // as 'old' android api doesn't support date API we have to use the old java api
        val currentTimeMillis = Date().time
        val numberOfMillisByDay = 24 /* days */ * 60 /* minutes */ * 60 /* seconds */ * 1000 /* milliseconds */
        val todayDate = Date(currentTimeMillis - (currentTimeMillis % numberOfMillisByDay))
        // When
        val currentDate = evtDetailService.currentDate
        // Then
        assertEquals(currentDate.time, todayDate.time)
    }

}