package org.didong.didong.event

import android.app.Activity
import android.support.v4.view.GestureDetectorCompat
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.*
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.instance
import org.didong.didong.R
import org.didong.didong.DataChangeEventListener
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by Vincent Couturier on 04/06/2017.
 */
class DateSelectionViewHolder : RecyclerView.ViewHolder {
    val injector: KodeinInjector
    val evtService: EventDetailService

    constructor(parentActivity: Activity, parentInjector: KodeinInjector, itemView: View?) : super(itemView) {
        injector = parentInjector
        evtService = injector.instance<EventDetailService>().value
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
                    val currentDateCal = Calendar.getInstance()
                    currentDateCal.time = evtService.currentDate
                    currentDatePicker.init(currentDateCal.get(Calendar.YEAR), currentDateCal.get(Calendar.MONTH), currentDateCal.get(Calendar.DAY_OF_MONTH), { _, year, monthOfYear, dayOfMonth ->
                        currentDate.setText("$year-${monthOfYear + 1}-$dayOfMonth")
                    })
                    currentDatePicker.visibility = View.VISIBLE
                }
            }

            val goToday = itemView.findViewById(R.id.goToday)
            goToday.setOnClickListener {
                initEditTextWithTodaysDate(currentDate, dateFormat)
            }
            val flingListener = object : GestureDetector.SimpleOnGestureListener() {
                override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float,
                                     velocityY: Float): Boolean {
                    Log.println(Log.DEBUG, "app", "$e1 $e2 $velocityX $velocityY")
                    if (Math.abs(velocityX) > 700 && Math.abs(velocityY) < 500) {
                        val sign : Long = if (velocityX>0) 1 else -1
                        val dayInMilliseconds : Long = 24 /*hours*/ * 3_600_000 /*milliseconds*/
                        evtService.currentDate = Date(evtService.currentDate.time + sign* dayInMilliseconds)
                        evtService.refreshCurrentEventList(parentActivity)
                        return true;
                    }
                    return false
                }

                override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
                    Log.println(Log.DEBUG, "app", "$e1 $e2 $distanceX $distanceY")
                    return super.onScroll(e1, e2, distanceX, distanceY)
                }

                override fun onDown(e: MotionEvent?): Boolean {
                    Log.println(Log.DEBUG, "app", "$e")
                    return super.onDown(e)
                }
            }
            val gestDetector = GestureDetectorCompat(parentActivity, flingListener)

            itemView.setOnTouchListener(object: View.OnTouchListener {
                override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                    gestDetector.onTouchEvent(event)
                    return true
                }
            })
        }
    }



    private fun initEditTextWithTodaysDate(editText: EditText, dateFormat: SimpleDateFormat) {
        val today = Calendar.getInstance()
        editText.setText(dateFormat.format(today.time), TextView.BufferType.EDITABLE)
    }
}