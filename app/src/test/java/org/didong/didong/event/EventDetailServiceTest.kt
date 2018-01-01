package org.didong.didong.event

import android.app.Activity
import android.content.ContentResolver
import android.database.Cursor
import android.support.design.widget.Snackbar
import com.github.salomonbrys.kodein.KodeinInjector
import com.nhaarman.mockito_kotlin.*
import org.didong.didong.R
import org.didong.didong.calendar.CalendarDetail
import org.didong.didong.gui.UIService
import org.junit.After
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*
import java.util.*

/**
 * EventDetail Service Unit Tests
 */
class EventDetailServiceTest {
    var uiService: UIService = mock()
    var calService: CalendarService = mock()
    var evtDetailService: EventDetailService = EventDetailService(calService, uiService)
    var contentResolver: ContentResolver = mock()

    var parentActivity: Activity = mock()
    val beginningDate = 61474978800000L; // 2017-12-25 (Monday)
    val endingDate = 61475324400000L; // 2017-12-29 (Friday)

    @Before
    fun setUp() {
        // We ensure each mock are 'clean'
        uiService = mock()
        calService = mock()
        evtDetailService = EventDetailService(calService, uiService)

        // drawer layout is mocked
        given(parentActivity.findViewById(R.id.drawer_layout)).willReturn(mock())
        contentResolver = mock();
        given(parentActivity.contentResolver).willReturn(contentResolver)
    }

    @After
    fun tearDown() {
        // Nothing to do for the moment as there's no real state change :-)
    }

    @Test
    fun `Default current date should be today`() {
        // Given: Today's calendar and new instance of event detail service
        // as 'old' android api doesn't support date API we have to use the old java api
        // today calendar
        val todayCalendar = Calendar.getInstance()
        // with time set to 00:00:00
        val timeFields = arrayOf(Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND, Calendar.MILLISECOND)
        timeFields.forEach { todayCalendar.set(it, 0) }

        // When: we get current date from event detail service
        val currentDate = evtDetailService.currentDate

        // Then: the current date is today
        assertEquals(currentDate, todayCalendar.time)
    }

    @Test
    fun `getEvents should fail if no calendar has been defined by the user`() {
        // Given: new instance of event detail service with a calendar service mock with no user defined 'calendar'
        given(calService.getActivityCalendar(any())).willReturn("")

        // When: we retrieve event from event detail service
        val evts = evtDetailService.getEvents(parentActivity, beginningDate, endingDate)

        // Then: the returned list should be empty and a message should be displayed
        assertEquals(evts, emptyList<EventDetail>())
        verify(uiService, times(1)).showMessage(anyOrNull(), eq(evtDetailService.NO_CALENDAR_SELECTED))
        verify(uiService, never()).showError(anyOrNull(), anyOrNull())
    }

    //@Test
    fun `getEvents between two dates should retrieve events from android calendar to prepare didong data`() {
        // Given: new instance of event detail service with a user calendar
        given(calService.getActivityCalendar(eq(parentActivity))).willReturn("test")
        given(calService.getActivityCalendarDetail(eq(parentActivity))).willReturn(CalendarDetail(1, "Test", "FR"))
        val returnedCursor: Cursor = mock()
        given(contentResolver.query(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())).willReturn(returnedCursor)
        var calIter = true;
        given(returnedCursor.moveToNext()).willReturn {
            if (calIter) {
                calIter = false
                return@willReturn true
            }
            return@willReturn false
        }
        given(returnedCursor.getString(CalendarService.PROJECTION_ID_INDEX)).willReturn("1")
        given(returnedCursor.getString(CalendarService.PROJECTION_TIMEZONE_INDEX)).willReturn("1")
        given(returnedCursor.getString(CalendarService.PROJECTION_DISPLAY_NAME_INDEX)).willReturn("2")

        // When: we retrieve event from event detail service
        val evts = evtDetailService.getEvents(parentActivity, beginningDate, endingDate)

        // Then: no should be displayed and the list of events should contains the one from calendar
        assertNotNull(evts)
        verify(uiService, never()).showMessage(anyOrNull(), anyOrNull())
        verify(uiService, never()).showError(anyOrNull(), anyOrNull())
    }
}