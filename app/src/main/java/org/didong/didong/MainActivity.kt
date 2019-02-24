package org.didong.didong

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.Toolbar
import org.didong.didong.event.EventsRecyclerAdapter
import android.content.Intent
import android.view.*
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.android.AppCompatActivityInjector
import com.github.salomonbrys.kodein.instance
import org.didong.didong.event.EventDetailService


class MainActivity : AppCompatActivity(), DataChangeEventListener, AppCompatActivityInjector {
    override val injector: KodeinInjector = KodeinInjector()
    var evtRecyclerAdapter: EventsRecyclerAdapter? = null
    val evtService: EventDetailService by injector.instance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeInjector()
        setContentView(R.layout.activity_main)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR), 0)
        }

        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        val fab = findViewById(R.id.fab) as FloatingActionButton
        fab.setOnClickListener { _ ->
            evtService.createEvent(this)
        }

        val drawer = findViewById(R.id.drawer_layout) as androidx.drawerlayout.widget.DrawerLayout
        val toggle = ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        val navigationView = findViewById(R.id.nav_view) as NavigationView
        navigationView.setNavigationItemSelectedListener(MainNavigationListener(this, drawer))

        val eventsView = findViewById(R.id.events_view) as androidx.recyclerview.widget.RecyclerView
        evtRecyclerAdapter = EventsRecyclerAdapter(this, injector)
        eventsView.adapter = evtRecyclerAdapter
        eventsView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        evtService.listeners.add(this)
    }

    override fun dataChange(newObject: Any) {
        evtRecyclerAdapter?.reloadEvents()
        evtRecyclerAdapter?.notifyDataSetChanged()
    }

    override fun onBackPressed() {
        val drawer = findViewById(R.id.drawer_layout) as androidx.drawerlayout.widget.DrawerLayout
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> {
                val prefIntent = Intent(this, SettingsActivity::class.java)
                startActivity(prefIntent)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        destroyInjector()
        super.onDestroy()
    }

}
