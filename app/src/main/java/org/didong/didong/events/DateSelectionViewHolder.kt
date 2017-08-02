package org.didong.didong.events

import android.app.Activity
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.util.EventLog
import android.view.View
import android.widget.*
import org.didong.didong.R
import com.hootsuite.nachos.NachoTextView
import org.didong.didong.DataChangeEventListener
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by Vincent Couturier on 04/06/2017.
 */
class DateSelectionViewHolder : RecyclerView.ViewHolder {
    val evtService = EventDetailService.instance

    constructor(parentActivity: Activity, itemView: View?) : super(itemView) {
        if (itemView != null) {
            val currentDate = itemView.findViewById(R.id.currentDate) as EditText
            val dateFormat = SimpleDateFormat("yyyy-MM-dd")
            initEditTextWithTodaysDate(currentDate, dateFormat)
            currentDate.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(newText: CharSequence?, startPos: Int, endPos: Int, length: Int) {
                    val currentDateStr = newText.toString()
                    try {
                        val selectedCurrentDate = dateFormat.parse(currentDateStr)
                        if (evtService.currentDate != selectedCurrentDate) {
                            evtService.currentDate = selectedCurrentDate
                            evtService.refreshCurrentEventList(parentActivity)
                        }
                    } catch(parseException: ParseException) {
                        // Do nothing because the date input could be edited and partial
                        // if the input is finished thus the date is changed and the change is apply
                    }
                }
            })

            evtService.listeners.add(object : DataChangeEventListener {
                override fun dataChange(evt: Any) {
                    if (evt is Date) {
                        // Check if the date is really 'changed'
                        val newCurrentDateStr = dateFormat.format(evt.time)
                        if (currentDate.text.toString() != newCurrentDateStr) {
                            currentDate.setText(newCurrentDateStr, TextView.BufferType.EDITABLE)
                        }
                    }
                }
            })

            val currentDatePicker = itemView.findViewById(R.id.currentDatePicker) as DatePicker


            val chooseCurrentDate = itemView.findViewById(R.id.chooseCurrentDate) as Button
            chooseCurrentDate.setOnClickListener {
                if (currentDatePicker.visibility == View.VISIBLE) {
                    currentDatePicker.visibility = View.GONE
                } else {
                    // Init date picker to date of today
                    // TODO : Should init with the value of current date field
                    val today = Calendar.getInstance()
                    currentDatePicker.init(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH), { _, year, monthOfYear, dayOfMonth ->
                        currentDate.setText("$year-${monthOfYear + 1}-$dayOfMonth")
                    })
                    currentDatePicker.visibility = View.VISIBLE
                }
            }

            val goToday = itemView.findViewById(R.id.goToday)
            goToday.setOnClickListener {
                initEditTextWithTodaysDate(currentDate, dateFormat)
            }
        }
    }

    private fun initEditTextWithTodaysDate(editText: EditText, dateFormat: SimpleDateFormat) {
        val today = Calendar.getInstance()
        editText.setText(dateFormat.format(today.time), TextView.BufferType.EDITABLE)
    }
}