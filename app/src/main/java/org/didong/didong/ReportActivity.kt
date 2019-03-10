package org.didong.didong

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemSelectedListener
import android.content.Context
import android.content.Intent
import androidx.appcompat.widget.ThemedSpinnerAdapter
import android.content.res.Resources.Theme
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textfield.TextInputEditText
import androidx.appcompat.app.ActionBarDrawerToggle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.fragment.app.FragmentActivity
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.android.*
import com.github.salomonbrys.kodein.instance
import org.didong.didong.event.EventDetailService
import org.didong.didong.gui.expandFirstLevelChildren
import java.text.ParseException
import java.util.*

class ReportActivity : AppCompatActivity(),AppCompatActivityInjector {
    override val injector: KodeinInjector = KodeinInjector()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeInjector()
        setContentView(R.layout.activity_report)

        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        val drawer = findViewById(R.id.report_drawer_layout) as androidx.drawerlayout.widget.DrawerLayout
        val toggle = ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        // Setup spinner
        val spinner = findViewById(R.id.spinner) as Spinner
        spinner.adapter = MyAdapter(
                toolbar.context,
                arrayOf(this.resources.getString(R.string.toolbar_day), this.resources.getString(R.string.toolbar_week), this.resources.getString(R.string.toolbar_month), this.resources.getString(R.string.toolbar_year), this.resources.getString(R.string.toolbar_specific_range )))

        spinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                // When the given dropdown item is selected, show its contents in the
                // container view.
                supportFragmentManager.beginTransaction()
                        .replace(R.id.container, PlaceholderFragment.newInstance(this@ReportActivity, position + 1))
                        .commit()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        val navigationView = findViewById(R.id.nav_view) as NavigationView
        navigationView.setNavigationItemSelectedListener(MainNavigationListener(this, drawer))

    }

    override fun onDestroy() {
        destroyInjector()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_report, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId


        if (id == R.id.action_settings) {
            val prefIntent = Intent(this, SettingsActivity::class.java)
            startActivity(prefIntent)
            return true
        }

        return super.onOptionsItemSelected(item)
    }


    private class MyAdapter(context: Context, objects: Array<String>) : ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, objects), ThemedSpinnerAdapter {
        private val mDropDownHelper: ThemedSpinnerAdapter.Helper

        init {
            mDropDownHelper = ThemedSpinnerAdapter.Helper(context)
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view: View

            if (convertView == null) {
                // Inflate the drop down using the helper's LayoutInflater
                val inflater = mDropDownHelper.dropDownViewInflater
                view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false)
            } else {
                view = convertView
            }

            val textView = view.findViewById(android.R.id.text1) as TextView
            textView.text = getItem(position)

            return view
        }

        override fun getDropDownViewTheme(): Theme? {
            return mDropDownHelper.dropDownViewTheme
        }

        override fun setDropDownViewTheme(theme: Theme?) {
            mDropDownHelper.dropDownViewTheme = theme
        }
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    class PlaceholderFragment() : Fragment(), SupportFragmentInjector {

        override val injector: KodeinInjector = KodeinInjector()

        var parentActivity: ReportActivity? = null
        val evtService: EventDetailService by injector.instance()

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {
            initializeInjector()
            val currActivity = (parentActivity ?: this.activity!!) as ReportActivity
            val arg = arguments?.getInt(ARG_SECTION_NUMBER)
            val rootView = if (arg == 1) {
                val dayReport = inflater.inflate(R.layout.fragment_report_day, container, false)
                val itemList = dayReport.findViewById(R.id.tagActivityList) as ExpandableListView
                val tagsActivity = evtService.getTagsActivity(currActivity)
                itemList.setAdapter(ReportListAdapter(evtService, tagsActivity))
                itemList.expandFirstLevelChildren()
                evtService.listeners.add(
                        object : DataChangeEventListener {
                            override fun dataChange(newObject: Any?) {
                                if (newObject is Date) {
                                    val currentTagsActivity = evtService.getTagsActivity(currActivity)
                                    itemList.setAdapter(ReportListAdapter(evtService, currentTagsActivity))
                                    itemList.expandFirstLevelChildren()
                                }
                            }
                        }
                )
                dayReport
            } else if (arg == 2) {
                val weekReport = inflater.inflate(R.layout.fragment_report_week, container, false)
                val itemList = weekReport.findViewById(R.id.tagActivityList) as ExpandableListView
                val cal = Calendar.getInstance()
                var year = cal.get(Calendar.YEAR)
                var week = cal.get(Calendar.WEEK_OF_YEAR)
                val tagsActivity = evtService.getWeekTagsActivity(currActivity, week, year)
                itemList.setAdapter(ReportListAdapter(evtService, tagsActivity))
                itemList.expandFirstLevelChildren()
                val weekInput = weekReport.findViewById(R.id.week) as TextInputEditText
                weekInput.setText(week.toString(), TextView.BufferType.EDITABLE)
                weekInput.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(p0: Editable?) {
                    }

                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    }

                    override fun onTextChanged(newText: CharSequence?, startPos: Int, endPos: Int, length: Int) {
                        val weekStr = if (newText != null) newText.toString() else ""
                        try {
                            week = weekStr.toInt()
                            processWeek(week, year, itemList)
                        } catch(parseException: ParseException) {
                            // Do nothing because the week input could be edited and partial
                        }
                    }
                })
                val yearInput = weekReport.findViewById(R.id.year) as TextInputEditText
                yearInput.setText(year.toString(), TextView.BufferType.EDITABLE)
                yearInput.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(p0: Editable?) {
                    }

                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    }

                    override fun onTextChanged(newText: CharSequence?, startPos: Int, endPos: Int, length: Int) {
                        val yearStr = newText.toString()
                        try {
                            year = yearStr.toInt()
                            processWeek(week, year, itemList)
                        } catch(parseException: ParseException) {
                            // Do nothing because the week input could be edited and partial
                        }
                    }
                })
                val nextWeekButton = weekReport.findViewById(R.id.nextWeek) as Button
                nextWeekButton.setOnClickListener { _ ->
                    week++
                    if(week > evtService.getLastWeekNumber(year)) {
                        week = 1
                        year++
                        yearInput.setText(year.toString(), TextView.BufferType.EDITABLE)
                    }
                    weekInput.setText(week.toString(), TextView.BufferType.EDITABLE)
                    processWeek(week, year, itemList)
                }
                val previousWeekButton = weekReport.findViewById(R.id.previousWeek) as Button
                previousWeekButton.setOnClickListener { _ ->
                    week--
                    if (week<=0) {
                        year--
                        week = evtService.getLastWeekNumber(year)
                        yearInput.setText(year.toString(), TextView.BufferType.EDITABLE)
                    }
                    weekInput.setText(week.toString(), TextView.BufferType.EDITABLE)
                    processWeek(week, year, itemList)
                }
                weekReport
            } else {
                val notYetImplementedReport = inflater.inflate(R.layout.fragment_report, container, false)
                val textView = notYetImplementedReport.findViewById(R.id.section_label) as TextView
                textView.text = this.resources.getString(R.string.coming_soon)
                notYetImplementedReport
            }
            return rootView
        }

        fun processWeek(week : Int, year: Int, itemList: ExpandableListView) {
            val currActivity : FragmentActivity = this@PlaceholderFragment.parentActivity ?: activity!!
            val tagsActivity = evtService.getWeekTagsActivity(currActivity, week, year)
            itemList.setAdapter(ReportListAdapter(evtService, tagsActivity))
            itemList.expandFirstLevelChildren()
        }

        override fun onDestroy() {
            destroyInjector()
            super.onDestroy()
        }

        companion object {
            /**
             * The fragment argument representing the section number for this
             * fragment.
             */
            private val ARG_SECTION_NUMBER = "section_number"

            /**
             * Returns a new instance of this fragment for the given section
             * number.
             */
            fun newInstance(parentActivity : ReportActivity, sectionNumber: Int): PlaceholderFragment {
                val fragment = PlaceholderFragment()
                fragment.parentActivity = parentActivity
                val args = Bundle()
                args.putInt(ARG_SECTION_NUMBER, sectionNumber)
                fragment.arguments = args
                return fragment
            }
        }
    }
}
