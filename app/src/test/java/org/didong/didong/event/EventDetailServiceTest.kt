package org.didong.didong.event

import android.app.Activity
import android.content.ContentResolver
import android.database.Cursor
import android.support.v4.widget.DrawerLayout
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.amshove.kluent.`should equal`
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
    var uiService: UIService = mockk(relaxed = true)
    var calService: CalendarService = mockk()
    var evtDetailService: EventDetailService = EventDetailService(calService, uiService)
    var contentResolver: ContentResolver = mockk()

    var parentActivity: Activity = mockk()
    val beginningDate = 61474978800000L; // 2017-12-25 (Monday)
    val endingDate = 61475324400000L; // 2017-12-29 (Friday)

    @Before
    fun setUp() {
        // We ensure each mock are 'clean'
        uiService = mockk(relaxed = true)
        calService = mockk()
        evtDetailService = EventDetailService(calService, uiService)

        // drawer layout is mocked
        every { parentActivity.findViewById(R.id.drawer_layout) as DrawerLayout } returns mockk()
        contentResolver = mockk();
        every { parentActivity.contentResolver } returns contentResolver
    }

    @After
    fun tearDown() {
        // Nothing to do for the moment as there's no real state change :-)
    }

    @Test
    fun `Default current date should be today`() {
        // GIVEN: Today's calendar and new instance of event detail service
        // as 'old' android api doesn't support date API we have to use the old java api
        // today calendar
        val todayCalendar = Calendar.getInstance()
        // with time set to 00:00:00
        val timeFields = arrayOf(Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND, Calendar.MILLISECOND)
        timeFields.forEach { todayCalendar.set(it, 0) }

        // WHEN: we get current date from event detail service
        val currentDate = evtDetailService.currentDate

        // THEN: the current date is today
        assertEquals(currentDate, todayCalendar.time)
    }

    @Test
    fun `getEvents should fail if no calendar has been defined by the user`() {
        // Given: new instance of event detail service with a calendar service mock with no user defined 'calendar'
        every { calService.getActivityCalendar(any()) } returns ""

        // When: we retrieve event from event detail service
        val evts = evtDetailService.getEvents(parentActivity, beginningDate, endingDate)

        // Then: the returned list should be empty and a message should be displayed
        assertEquals(evts, emptyList<EventDetail>())
        verify(exactly = 1) { uiService.showMessage(any(), eq(evtDetailService.NO_CALENDAR_SELECTED)) }
        verify(exactly = 0) { uiService.showError(any(), any()) }
    }

    //@Test
    fun `getEvents between two dates should retrieve events from android calendar to prepare didong data`() {
        // Given: new instance of event detail service with a user calendar
        every { calService.getActivityCalendar(eq(parentActivity)) } returns "test"
        every { calService.getActivityCalendarDetail(eq(parentActivity)) } returns CalendarDetail(1, "Test", "FR")
        val returnedCursor: Cursor = mockk()
        every { contentResolver.query(any(), any(), any(), any(), any()) } returns returnedCursor
        var calIter = true;
        every { returnedCursor.moveToNext() } answers {
            if (calIter) {
                calIter = false
                true
            } else {
                false
            }
        }
        every {returnedCursor.getString(CalendarService.PROJECTION_ID_INDEX)} returns "1"
        every {returnedCursor.getString(CalendarService.PROJECTION_TIMEZONE_INDEX)} returns "1"
        every {returnedCursor.getString(CalendarService.PROJECTION_DISPLAY_NAME_INDEX)} returns "1"

        // When: we retrieve event from event detail service
        val evts = evtDetailService.getEvents(parentActivity, beginningDate, endingDate)

        // Then: no should be displayed and the list of events should contains the one from calendar
        assertNotNull(evts)
        verify(exactly = 0) { uiService.showMessage(any(), any()) }
        verify(exactly = 0) { uiService.showError(any(), any()) }
    }

    @Test
    fun `computeEventToGetTags should give total duration of events spreaded by tag`() {
        val evts = listOf(EventDetail(id = 1, calendarId = "1",title = "test 1",
                description = EventDescription(tags = listOf("dev","play"), started = false),
                startTime = "1523819378026", endTime = "1523819978026"),
                EventDetail(id = 2, calendarId = "1",title = "test 2",
                        description = EventDescription(tags = listOf("test","play"), started = false),
                        startTime = "1523824378026", endTime = "1523829378026"))
        val res = evtDetailService.computeEventToGetTags(evts)
        res["dev"] `should equal` 600000L
        res["play"] `should equal` 5600000L
        res["test"] `should equal` 5000000L
    }

}