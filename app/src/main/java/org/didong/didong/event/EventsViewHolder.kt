package org.didong.didong.event

import android.app.Activity
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.*
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.instance
import org.didong.didong.R
import com.hootsuite.nachos.NachoTextView
import java.util.*


/**
 * Created by Vincent Couturier on 04/06/2017.
 */
class EventsViewHolder : androidx.recyclerview.widget.RecyclerView.ViewHolder {
    val injector : KodeinInjector
    val itemTitle : EditText
    val tagNachos : NachoTextView
    val startTime : TextView
    val endTime : TextView
    val startStopButton : Button
    val cloneButton : Button
    var started : Boolean = false
    var evtDetail : EventDetail? = null
    val evtDetailService: EventDetailService

    constructor(parentActivity: Activity, parentInjector: KodeinInjector, itemView: View) : super(itemView){
        injector = parentInjector
        evtDetailService = injector.instance<EventDetailService>().value
        itemTitle = itemView.findViewById(R.id.item_title) as EditText
        tagNachos = itemView.findViewById(R.id.nacho_text_view) as NachoTextView
        startTime = itemView.findViewById(R.id.start_time) as TextView
        endTime = itemView.findViewById(R.id.end_time) as TextView
        startStopButton = itemView.findViewById(R.id.startstop_event) as Button
        cloneButton = itemView.findViewById(R.id.clone_event) as Button
        itemTitle.setOnFocusChangeListener { _, _ ->
            val newTitle = itemTitle.text.toString()
            if (newTitle != evtDetail?.title) {
                evtDetail?.title = itemTitle.text.toString()
                evtDetailService.updateEvent(parentActivity, evtDetail!!)
            }
        }
        tagNachos.setOnFocusChangeListener { _, _ ->
            val newTags = tagNachos.chipAndTokenValues
            if(newTags != evtDetail?.description?.tags) {
                evtDetail?.description = EventDescription(tags = tagNachos.chipAndTokenValues, started = evtDetail?.description?.started ?: false)
                evtDetailService.updateEvent(parentActivity, evtDetail!!)
            }
        }
        startStopButton.setOnClickListener { _ ->
            if (evtDetail?.description?.started?:false) {
                // Stop the event
                startStopButton.setText(parentActivity.resources.getString(R.string.event_start))
                if (evtDetail != null) {
                    val eventDetail : EventDetail = evtDetail as EventDetail
                    eventDetail.title = itemTitle.text.toString()
                    eventDetail.description = EventDescription(tags = tagNachos.chipAndTokenValues, started = false)
                    evtDetailService.updateEvent(parentActivity, EventDetail(eventDetail.id,eventDetail.calendarId,eventDetail.title,eventDetail.description,eventDetail.startTime,Date().time.toString()))
                }
            } else {
                // Start the event
                if (evtDetail != null) {
                    val eventDetail : EventDetail = evtDetail as EventDetail
                    eventDetail.title = itemTitle.text.toString()
                    eventDetail.description = EventDescription(tags = tagNachos.chipAndTokenValues, started = true)
                    val now = Date().time.toString()
                    evtDetailService.updateEvent(parentActivity, EventDetail(eventDetail.id,eventDetail.calendarId,eventDetail.title,eventDetail.description, now, now))
                }
                startStopButton.setText(parentActivity.resources.getString(R.string.event_stop))
            }
            started = !started
        }
        cloneButton.setOnClickListener { _ ->
            if (evtDetail != null) {
                evtDetailService.cloneEvent(parentActivity, evtDetail as EventDetail)
            }
        }
    }
}