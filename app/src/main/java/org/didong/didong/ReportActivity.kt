package org.didong.didong

import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar

import android.support.v4.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemSelectedListener
import android.content.Context
import android.content.Intent
import android.support.v7.widget.ThemedSpinnerAdapter
import android.content.res.Resources.Theme
import android.support.design.widget.NavigationView
import android.support.design.widget.TextInputEditText
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.CardView
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.android.*
import com.github.salomonbrys.kodein.instance
import org.didong.didong.event.DateSelectionViewHolder
import org.didong.didong.event.EventDetailService
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

        val drawer = findViewById(R.id.report_drawer_layout) as DrawerLayout
        val toggle = ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        // Setup spinner
        val spinner = findViewById(R.id.spinner) as Spinner
        spinner.adapter = MyAdapter(
                toolbar.context,
                arrayOf("Day", "Week", "Month", "Year", "Specific Range"))

        spinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                // When the given dropdown item is selected, show its contents in the
                // container view.
                supportFragmentManager.beginTransaction()
                        .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
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
    class PlaceholderFragment : Fragment(), SupportFragmentInjector {
        override val injector: KodeinInjector = KodeinInjector()

        val evtService: EventDetailService by injector.instance()

        override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {
            initializeInjector()
            val arg = arguments.getInt(ARG_SECTION_NUMBER)
            val rootView = if (arg == 1) {
                val dayReport = inflater!!.inflate(R.layout.fragment_report_day, container, false)
                val itemList = dayReport.findViewById(R.id.tagActivityList) as ExpandableListView
                val tagsActivity = evtService.getTagsActivity(this.activity)
                itemList.setAdapter(ReportListAdapter(tagsActivity))
                tagsActivity.keys.forEachIndexed { index:Int, tag:String ->
                    itemList.expandGroup(index)
                }
                val dateCard = dayReport.findViewById(R.id.currentdate_card) as CardView
                val dateSelectionViewHolder = DateSelectionViewHolder(this.activity, injector, dateCard)
                evtService.listeners.add(
                        object : DataChangeEventListener {
                            override fun dataChange(evt: Any) {
                                if (evt is Date) {
                                    val tagsActivity = evtService.getTagsActivity(this@PlaceholderFragment.activity)
                                    itemList.setAdapter(ReportListAdapter(tagsActivity))
                                    tagsActivity.keys.forEachIndexed { index:Int, tag:String ->
                                        itemList.expandGroup(index)
                                    }
                                }
                            }
                        }
                )
                dayReport
            } else if (arg == 2) {
                val weekReport = inflater!!.inflate(R.layout.fragment_report_week, container, false)
                val itemList = weekReport.findViewById(R.id.tagActivityList) as ExpandableListView
                val cal = Calendar.getInstance()
                var year = cal.get(Calendar.YEAR)
                var week = cal.get(Calendar.WEEK_OF_YEAR)
                val tagsActivity = evtService.getWeekTagsActivity(this.activity, week, year)
                itemList.setAdapter(ReportListAdapter(tagsActivity))
                tagsActivity.keys.forEachIndexed { index:Int, tag:String ->
                    itemList.expandGroup(index)
                }
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
                            val tagsActivity = evtService.getWeekTagsActivity(this@PlaceholderFragment.activity, week, year)
                            itemList.setAdapter(ReportListAdapter(tagsActivity))
                            tagsActivity.keys.forEachIndexed { index:Int, tag:String ->
                                itemList.expandGroup(index)
                            }
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
                            week = yearStr.toInt()
                            val tagsActivity = evtService.getWeekTagsActivity(this@PlaceholderFragment.activity, week, year)
                            itemList.setAdapter(ReportListAdapter(tagsActivity))
                            tagsActivity.keys.forEachIndexed { index:Int, tag:String ->
                                itemList.expandGroup(index)
                            }
                        } catch(parseException: ParseException) {
                            // Do nothing because the week input could be edited and partial
                        }
                    }
                })
                weekReport
            } else {
                val notYetImplementedReport = inflater!!.inflate(R.layout.fragment_report, container, false)
                val textView = notYetImplementedReport.findViewById(R.id.section_label) as TextView
                textView.text = "Not Yet Implemented $arg"
                notYetImplementedReport
            }
            return rootView
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
            fun newInstance(sectionNumber: Int): PlaceholderFragment {
                val fragment = PlaceholderFragment()
                val args = Bundle()
                args.putInt(ARG_SECTION_NUMBER, sectionNumber)
                fragment.arguments = args
                return fragment
            }
        }
    }
}
