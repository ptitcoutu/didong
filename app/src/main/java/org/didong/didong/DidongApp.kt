package org.didong.didong

import android.app.Application
import com.github.salomonbrys.kodein.*
import org.didong.didong.event.CalendarService
import org.didong.didong.event.EventDetailService

/**
 * Created by vincent.couturier@gmail.com on 2017-12-29.
 */
class DidongApp : Application(), KodeinAware {
    override val kodein by Kodein.lazy {
        bind<CalendarService>() with singleton { CalendarService() }
        bind<EventDetailService>() with singleton { EventDetailService(instance()) }
    }
}
