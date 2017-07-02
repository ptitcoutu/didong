package org.didong.didong.events

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.didong.didong.R
import android.widget.ArrayAdapter
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by Vincent Couturier on 04/06/2017.
 */
public class EventsRecyclerAdapter(val parentActivity: Activity) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val evtService = EventDetailService.instance
    var calendarEvents = evtService.getEvents(parentActivity)

    override fun onCreateViewHolder(viewGroup: ViewGroup?, i: Int): RecyclerView.ViewHolder {
        if (viewGroup != null) {
            val v: View = LayoutInflater.from(viewGroup.context).inflate(R.layout.event_card, viewGroup, false)
            var viewHolder = EventsViewHolder(parentActivity, v)
            return viewHolder
        } else {
            throw IllegalArgumentException("don't know how to manage null viewgroup")
        }
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder?, i: Int) {
        if (viewHolder is EventsViewHolder) {
            val evt : EventDetail = calendarEvents[i]
            val sdf = SimpleDateFormat("MM-dd hh:mm")
            viewHolder.evtDetail = evt
            viewHolder.itemTitle.setText(evt.title, TextView.BufferType.EDITABLE)
            val startTxt = if (evt.startTime!=null) sdf.format(Date(evt.startTime.toLong())) else ""
            viewHolder.startTime.setText(startTxt)
            val endTxt = if (evt.endTime!=null) sdf.format(Date(evt.endTime.toLong())) else ""
            viewHolder.endTime.setText(endTxt)
            val suggestions = arrayOf("Tortilla Chips", "Melted Cheese", "Salsa", "Guacamole", "Mexico", "Jalapeno")
            val adapter = ArrayAdapter(parentActivity, android.R.layout.simple_dropdown_item_1line, suggestions)
            viewHolder.tagNachos.setAdapter(adapter)
            if(evt.description != null) {
                viewHolder.tagNachos.setText(evt.description.tags)
                viewHolder.startStopButton.setText(if (evt.description.started?:false) "Stop" else "Start")
            } else {
                viewHolder.tagNachos.setText(emptyList())
                viewHolder.startStopButton.setText("Start")
            }

        } else {
            throw IllegalArgumentException("don't know how to bind on a null viewHolder")
        }
    }

    override fun getItemCount(): Int {
        return calendarEvents.size
    }

    fun reloadEvents() {
        calendarEvents = evtService.getEvents(parentActivity)
    }
}
