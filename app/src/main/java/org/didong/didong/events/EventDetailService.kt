package org.didong.didong.events

import android.app.Activity
import android.content.ContentValues
import android.database.Cursor
import android.provider.CalendarContract
import android.support.design.widget.Snackbar
import org.didong.didong.DataChangeEventListener
import org.didong.didong.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Vincent Couturier on 02/07/2017.
 */
class EventDetailService private constructor() {
    init {
        println("This ($this) is a singleton")
    }

    private object Holder {
        val INSTANCE = EventDetailService()
    }

    companion object {
        val instance: EventDetailService by lazy { Holder.INSTANCE }
    }

    val calendarService = CalendarService.instance

    // Projection array. Creating indices for this array instead of doing
    // dynamic lookups improves performance.
    val EVENT_PROJECTION = arrayOf(CalendarContract.Events._ID, // 0
            CalendarContract.Events.DTSTART, // 1
            CalendarContract.Events.DTEND, // 2
            CalendarContract.Events.TITLE, // 3
            CalendarContract.Events.DESCRIPTION, //4
            CalendarContract.Events.ALL_DAY, //5
            CalendarContract.Events.CALENDAR_ID //6
    )

    // The indices for the projection array above.
    val PROJECTION_ID_INDEX = 0
    val PROJECTION_DTSTART_INDEX = 1
    val PROJECTION_DTEND_INDEX = 2
    val PROJECTION_TITLE_INDEX = 3
    val PROJECTION_DESCRIPTION_INDEX = 4
    val PROJECTION_ALL_DAY_INDEX = 5
    val PROJECTION_CALENDAR_ID_INDEX = 6

    val CALENDAR_EVENTS_URI = CalendarContract.Events.CONTENT_URI
    val listeners: MutableList<DataChangeEventListener> = mutableListOf()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd")
    var currentDate: Date = dateFormat.parse(dateFormat.format(Date()))

    fun getEvents(parentActivity: Activity): List<EventDetail> {

        val evts = ArrayList<EventDetail>()

        val activityCalendar = calendarService.getActivityCalendar(parentActivity)
        if (activityCalendar.length > 0) {

            // Run query
            var cur: Cursor? = null
            val cr = parentActivity.contentResolver
            val uri = CALENDAR_EVENTS_URI
            val activityCalendarDetail = calendarService.getActivityCalendarDetail(parentActivity)
            if (activityCalendarDetail == null) {
                Snackbar.make(parentActivity.findViewById(R.id.drawer_layout), "Activity calendar must be selected in app settings", Snackbar.LENGTH_LONG)
                        .setAction("Close", null).show()
                return evts;
            }
            val selection = "(${CalendarContract.Events.CALENDAR_ID} = ? and ${CalendarContract.Events.DTSTART} >= ? and ${CalendarContract.Events.DTSTART} < ?)"
            //val selectionArgs = arrayOf(activityCalendar)
            val numberOfMillisInADay = 24 * 60 * 60 * 1000
            val currentDateTime = currentDate.time
            val nextDayAfterCurrentDateTime = currentDateTime + numberOfMillisInADay
            val selectionArgs = arrayOf(activityCalendarDetail.id.toString(), currentDateTime.toString(), nextDayAfterCurrentDateTime.toString())

            // Submit the query and get a Cursor object back.
            try {
                cur = cr.query(uri, EVENT_PROJECTION, selection, selectionArgs, "${CalendarContract.Events.DTSTART} ASC, ${CalendarContract.Events.CALENDAR_ID} ASC")
                while (cur.moveToNext()) {
                    // Get the field values
                    val evtID = cur.getLong(PROJECTION_ID_INDEX);
                    val dtStart = cur.getString(PROJECTION_DTSTART_INDEX)
                    //val dtStartLong = dtStart?.toLong()
                    //val startTime = if (dtStartLong != null) Date(dtStartLong) else Date()
                    val dtEnd = cur.getString(PROJECTION_DTEND_INDEX)
                    //val dtEndLong = dtStart?.toLong()
                    //val endTime = if (dtEndLong != null) Date(dtEndLong) else null

                    val title = cur.getString(PROJECTION_TITLE_INDEX)
                    val description = cur.getString(PROJECTION_DESCRIPTION_INDEX)
                    val alDay = cur.getString(PROJECTION_ALL_DAY_INDEX)
                    val calId = cur.getString(PROJECTION_CALENDAR_ID_INDEX)
                    evts.add(EventDetail(id = evtID, calendarId = calId, startTime = dtStart, endTime = dtEnd, title = title, description = EventDescription.fromJson(description)))
                }
            } catch (e: SecurityException) {
                Snackbar.make(parentActivity.findViewById(R.id.drawer_layout), "Error occurs ${e.message}", Snackbar.LENGTH_LONG)
                        .setAction("Close", null).show()
            } finally {
                if (cur != null) {
                    cur.close()
                }
            }
        } else {
            Snackbar.make(parentActivity.findViewById(R.id.drawer_layout), "You have to set a calendar in the app settings", Snackbar.LENGTH_LONG)
                    .setAction("Close", null).show()
        }
        return evts
    }

    fun createEvent(parentActivity: Activity) {
        try {
            val cr = parentActivity.contentResolver
            val eventData = ContentValues(6)
            val calendarDetail = calendarService.getActivityCalendarDetail(parentActivity)
            if (calendarDetail != null) {
                eventData.put(CalendarContract.Events.DTSTART, currentDate.time)
                eventData.put(CalendarContract.Events.DTEND, currentDate.time)
                eventData.put(CalendarContract.Events.TITLE, "")
                eventData.put(CalendarContract.Events.DESCRIPTION, EventDescription.EMPTY_DESCRIPTION_JSON)
                eventData.put(CalendarContract.Events.EVENT_TIMEZONE, calendarDetail?.timeZone)
                eventData.put(CalendarContract.Events.CALENDAR_ID, calendarDetail?.id)
                val evtURI = cr.insert(CALENDAR_EVENTS_URI, eventData)
                notifyChange(evtURI)
            } else {
                Snackbar.make(parentActivity.findViewById(R.id.drawer_layout), "A calendar must be selected in app settings", Snackbar.LENGTH_LONG)
                        .setAction("Close", null).show()
            }
        } catch (e: Exception) {
            Snackbar.make(parentActivity.findViewById(R.id.drawer_layout), "Error occurs ${e.message}", Snackbar.LENGTH_LONG)
                    .setAction("Close", null).show()
        }
    }

    fun cloneEvent(parentActivity: Activity, evtDetail: EventDetail) {
        try {
            val cr = parentActivity.contentResolver
            val calendarDetail = calendarService.getActivityCalendarDetail(parentActivity)
            val eventData = ContentValues(6)
            eventData.put(CalendarContract.Events.DTSTART, evtDetail.startTime)
            eventData.put(CalendarContract.Events.DTEND, evtDetail.endTime)
            eventData.put(CalendarContract.Events.TITLE, evtDetail.title)
            eventData.put(CalendarContract.Events.DESCRIPTION, evtDetail.description?.toJson() ?: EventDescription.EMPTY_DESCRIPTION_JSON)
            eventData.put(CalendarContract.Events.EVENT_TIMEZONE, calendarDetail?.timeZone)
            eventData.put(CalendarContract.Events.CALENDAR_ID, calendarDetail?.id)
            val evtURI = cr.insert(CALENDAR_EVENTS_URI, eventData)
            notifyChange(evtURI)
        } catch (e: Exception) {
            Snackbar.make(parentActivity.findViewById(R.id.drawer_layout), "Error occurs ${e.message}", Snackbar.LENGTH_LONG)
                    .setAction("Close", null).show()
        }
    }

    fun updateEvent(parentActivity: Activity, evtDetail: EventDetail) {
        try {
            val cr = parentActivity.contentResolver
            val eventData = ContentValues(4)
            eventData.put(CalendarContract.Events.DTSTART, evtDetail.startTime)
            eventData.put(CalendarContract.Events.DTEND, evtDetail.endTime)
            eventData.put(CalendarContract.Events.TITLE, evtDetail.title)
            eventData.put(CalendarContract.Events.DESCRIPTION, evtDetail.description?.toJson() ?: EventDescription.EMPTY_DESCRIPTION_JSON)
            eventData.put(CalendarContract.Events.CALENDAR_ID, evtDetail.calendarId)
            val evtURI = cr.update(CALENDAR_EVENTS_URI, eventData, "(${CalendarContract.Events._ID} = ?)", arrayOf(evtDetail.id.toString()))
            notifyChange(evtURI)
        } catch (e: Exception) {
            Snackbar.make(parentActivity.findViewById(R.id.drawer_layout), "Error occurs ${e.message}", Snackbar.LENGTH_LONG)
                    .setAction("Close", null).show()
        }
    }

    /**
     * Get for each tag of a day the sum of all seconds spent in all events with this tag
     */
    fun getTagsActivity(parentActivity: Activity): Map<String, Long> {
        val events = getEvents(parentActivity)
        val tags = events.flatMap { it.description.tags }.distinct()
        val tagsActivity = tags.map { tag ->
            tag to events.filter { it.description.tags.contains(tag) }.fold(0L) { sumOfActivity : Long, eventDetail ->
                if(eventDetail.startTime != null && eventDetail.startTime != ""&& (eventDetail.description.started || eventDetail.startTime != eventDetail.endTime) ) {
                    val startTime = eventDetail.startTime.toLong()
                    // If the event is not terminated the endTime is evaluated to now otherwise to the saved endTime of the event
                    val endTime : Long = if(eventDetail.description?.started) Date().time else eventDetail.endTime!!.toLong()
                    val elapse = endTime-startTime
                    sumOfActivity+elapse
                } else {
                    sumOfActivity
                }
            }
        }.toMap()
        return tagsActivity
    }

    private fun notifyChange(data: Any) {
        listeners.forEach { it.dataChange(data) }
    }

    fun refreshCurrentEventList(parentActivity: Activity) {
        notifyChange(currentDate)
    }
}