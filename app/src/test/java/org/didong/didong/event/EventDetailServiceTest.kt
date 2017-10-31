package org.didong.didong.event

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import org.junit.After
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*

/**
 * EventDetail Service Unit Tests
 */
class EventDetailServiceTest {
    val evtDetailService = EventDetailService.instance;

    @Before
    fun setUp() {
        //EventDetailService.Holder =
        /*whenever(CalendarService.instance.getActivityCalendar).thenReturn(mock())
        evtDetailService.calendarService =
        evtDetailService.getEvents()*/
    }

    @After
    fun tearDown() {
    }

    @Test
    fun getCurrentDate() {
    }

    @Test
    fun setCurrentDate() {
    }

    @Test
    fun getNumberOfMillisInADay() {
    }

    @Test
    fun getEventsOfWeek() {
    }

    @Test
    fun getEvents() {
    }

    @Test
    fun getEvents1() {
    }

    @Test
    fun createEvent() {
    }

    @Test
    fun cloneEvent() {
    }

    @Test
    fun updateEvent() {
    }

    @Test
    fun getTagsActivity() {
    }

    @Test
    fun getWeekTagsActivity() {
    }

    @Test
    fun computeEventToGetTags() {
    }

    @Test
    fun refreshCurrentEventList() {
    }

}