package org.didong.didong.event

import android.annotation.SuppressLint
import android.app.Activity
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.didong.didong.R
import android.widget.ArrayAdapter
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.instance
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by Vincent Couturier on 04/06/2017.
 */
class EventsRecyclerAdapter(val parentActivity: Activity, val injector: KodeinInjector) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val evtService: EventDetailService by injector.instance()
    private var calendarEvents = evtService.getEvents(parentActivity)
    val EVENT_DETAIL = 0
    val DATE_SELECTION = 1

    override fun getItemViewType(position: Int): Int {
        // The first card is the date selection card
        return if (position == 0 || position == calendarEvents.size + 1) DATE_SELECTION else EVENT_DETAIL
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            DATE_SELECTION -> {
                val v: View = LayoutInflater.from(viewGroup.context).inflate(R.layout.date_selection, viewGroup, false)
                DateSelectionViewHolder(parentActivity, injector, v)
            }
            EVENT_DETAIL -> {
                val v: View = LayoutInflater.from(viewGroup.context).inflate(R.layout.event_card, viewGroup, false)
                EventsViewHolder(parentActivity, injector, v)
            }
            else -> {
                throw IllegalArgumentException("unknown viewType: $viewType")
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, i: Int) {
        if (viewHolder is EventsViewHolder) {
            val evt: EventDetail = calendarEvents[i - 1]
            val sdf = SimpleDateFormat("MM-dd HH:mm")
            viewHolder.evtDetail = evt
            viewHolder.itemTitle.setText(evt.title, TextView.BufferType.EDITABLE)
            val startTxt = if (evt.startTime != null) sdf.format(Date(evt.startTime.toLong())) else ""
            viewHolder.startTime.text = startTxt
            val endTxt = if (evt.endTime != null) sdf.format(Date(evt.endTime.toLong())) else ""
            viewHolder.endTime.text = endTxt
            val suggestions = evtService.getTagsActivity(parentActivity).keys.toTypedArray()
            val adapter = ArrayAdapter(parentActivity, android.R.layout.simple_dropdown_item_1line, suggestions)
            viewHolder.tagNachos.setAdapter(adapter)
            val startLabel = parentActivity.resources.getString(R.string.event_start)
            val stopLabel = parentActivity.resources.getString(R.string.event_stop)
            viewHolder.tagNachos.setText(evt.description.tags)
            viewHolder.startStopButton.text = if (evt.description.started) stopLabel else startLabel

        } else if (viewHolder is DateSelectionViewHolder) {
            // Nothing to do because data is shared from event detail service
        } else {
            throw IllegalArgumentException("don't know how to bind on a null viewHolder")
        }
    }

    override fun getItemCount(): Int {
        if (calendarEvents.isNotEmpty()) {
            // there's the events of the day + 2 date selectors
            return calendarEvents.size + 2
        } else {
            return 1
        }

    }

    fun reloadEvents() {
        calendarEvents = evtService.getEvents(parentActivity)
    }
}
