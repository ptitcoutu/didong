package org.didong.didong.calendar

import android.app.Activity
import android.content.ContentResolver
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.preference.PreferenceManager
import android.provider.CalendarContract
import android.provider.CalendarContract.Calendars
import org.didong.didong.gui.UIService
import java.util.*


/**
 * Created by Vincent Couturier on 02/07/2017.
 */
class CalendarService(val uiService: UIService) {

    companion object {
        const val ACTIVITY_CALENDAR_PREF_NAME = "activity_calendar"

        // The indices for the projection array above.
        const val PROJECTION_ID_INDEX = 0
        const val PROJECTION_TIMEZONE_INDEX = 1
        //val PROJECTION_ACCOUNT_NAME_INDEX = 2
        const val PROJECTION_DISPLAY_NAME_INDEX = 2
        //val PROJECTION_OWNER_ACCOUNT_INDEX = 4
        //val PROJECTION_ACCOUNT_TYPE_INDEX = 5
        //val PROJECTION_IS_PRIMARY_INDEX = 6
        //val PROJECTION_LOCATION_INDEX = 7
        const val PROJECTION_ACCESS_INDEX = 3
    }

    val CALENDAR_EVENTS_URI = CalendarContract.Events.CONTENT_URI!!
    val CALENDARS_URI = Calendars.CONTENT_URI!!
    // Projection array. Creating indices for this array instead of doing
    // dynamic lookups improves performance.
    val CAL_PROJECTION = arrayOf(Calendars._ID, // 0
            Calendars.CALENDAR_TIME_ZONE, // 1
            //CalendarContract.Calendars.ACCOUNT_NAME, // 2
            Calendars.CALENDAR_DISPLAY_NAME, // 3
            //CalendarContract.Calendars.OWNER_ACCOUNT, // 4
            //CalendarContract.Calendars.ACCOUNT_TYPE, //5
            //CalendarContract.Calendars.IS_PRIMARY, //6
            //CalendarContract.Calendars.CALENDAR_LOCATION, //7
            Calendars.CALENDAR_ACCESS_LEVEL//8
    )

    fun getActivityCalendar(parentActivity: Activity): String {
        return PreferenceManager.getDefaultSharedPreferences(parentActivity).getString(ACTIVITY_CALENDAR_PREF_NAME, "")!!
    }

    fun getActivityCalendarDetail(parentActivity: Activity): CalendarDetail? {
        val activityCalendar = getActivityCalendar(parentActivity)
        // Run query
        var cur: Cursor? = null
        val cr = parentActivity.contentResolver
        val selection = "(${Calendars.CALENDAR_DISPLAY_NAME} = ?)"
        val selectionArgs = arrayOf(activityCalendar)
        // Submit the query and get a Cursor object back.
        try {
            cur = cr.query(CALENDARS_URI, CAL_PROJECTION, selection, selectionArgs, null)
            while (cur.moveToNext()) {
                // Get the field values
                val calID = cur.getLong(PROJECTION_ID_INDEX)
                val calTimeZone = cur.getString(PROJECTION_TIMEZONE_INDEX) ?: "UTC"
                val displayName = cur.getString(PROJECTION_DISPLAY_NAME_INDEX)
                return CalendarDetail(id = calID, timeZone = calTimeZone, displayName = displayName)
            }
        } catch (e: SecurityException) {
            uiService.showError(parentActivity, e)
        } finally {
            if (cur != null) {
                cur.close()
            }
        }
        return null
    }
    fun getActivityCalendarId(parentActivity: Activity): Long? {
        return getActivityCalendarDetail(parentActivity)?.id
    }

    fun getCalendars(parentActivity: Activity): List<String> {
        val calendars = ArrayList<String>()

        // Run query
        var cur: Cursor? = null
        val cr = parentActivity.contentResolver
        val selection = null
        val selectionArgs = null
        // Submit the query and get a Cursor object back.
        try {
            cur = cr.query(CALENDARS_URI, CAL_PROJECTION, selection, selectionArgs, null)
            while (cur.moveToNext()) {
                // Get the field values
                val displayName = cur.getString(PROJECTION_DISPLAY_NAME_INDEX)
                val calAccess = cur.getInt(PROJECTION_ACCESS_INDEX)
                if (calAccess == Calendars.CAL_ACCESS_OWNER) {
                    calendars.add(displayName)
                }
            }
        } catch (e: SecurityException) {
            uiService.showError(parentActivity, e)
        } finally {
            if (cur != null) {
                cur.close()
            }
        }
        return calendars
    }



    fun addDefaultDidongCalendar(parentActivity: Activity): Uri? {
        val cr = parentActivity.contentResolver
        try {
            val cv = buildDefaultDidongCalendarContentValues()
            val calUri: Uri = buildDefaultDidongCalendarUri()
            return cr.insert(calUri, cv)
        } catch (e: Exception) {
            uiService.showError(parentActivity, e)
            return null
        }
    }

    val ACCOUNT_NAME = "private"
    private fun buildDefaultDidongCalendarUri(): Uri {
        return Calendars.CONTENT_URI
                .buildUpon()
                .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(Calendars.ACCOUNT_NAME, ACCOUNT_NAME)
                .appendQueryParameter(Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL)
                .build()
    }

    private fun buildDefaultDidongCalendarContentValues(): ContentValues {
        val dispName = "Didong"
        val intName = "private_$dispName"
        val cv = ContentValues()
        cv.put(Calendars.ACCOUNT_NAME, ACCOUNT_NAME)
        cv.put(Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL)
        cv.put(Calendars.NAME, intName)
        cv.put(Calendars.CALENDAR_DISPLAY_NAME, dispName)
        cv.put(Calendars.CALENDAR_COLOR, 0x0066CC)
        cv.put(Calendars.CALENDAR_ACCESS_LEVEL, Calendars.CAL_ACCESS_OWNER)
        cv.put(Calendars.OWNER_ACCOUNT, ACCOUNT_NAME)
        cv.put(Calendars.VISIBLE, 1)
        cv.put(Calendars.SYNC_EVENTS, 1)
        return cv
    }
}