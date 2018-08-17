package org.didong.didong

import android.app.Application
import com.github.salomonbrys.kodein.*
import org.didong.didong.event.CalendarService
import org.didong.didong.event.EventDetailService
import org.didong.didong.gui.UIService

/**
 * Created by vincent.couturier@gmail.com on 2017-12-29.
 */
class DidongApp : Application(), KodeinAware {
    override val kodein by Kodein.lazy {
        bind<UIService>() with singleton { UIService() }
        bind<CalendarService>() with singleton { CalendarService(instance()) }
        bind<EventDetailService>() with singleton { EventDetailService(instance(), instance()) }
    }
}
