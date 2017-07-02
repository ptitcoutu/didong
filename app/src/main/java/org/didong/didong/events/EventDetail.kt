package org.didong.didong.events

import java.util.*

/**
 * Created by Vincent Couturier on 11/06/2017.
 */
class EventDetail (var id: Long, val calendarId: String, var title: String?, var description: EventDescription, val startTime : String?, val endTime: String?) {
}