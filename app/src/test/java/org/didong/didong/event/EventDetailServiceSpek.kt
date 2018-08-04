package org.didong.didong.event

import com.winterbe.expekt.should
import io.mockk.mockk
import org.didong.didong.gui.UIService
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

/**
 *
 */
class EventDetailServiceSpek : Spek ({
    describe("EventDetailService ") {
        val calService = mockk<CalendarService>()
        val uiService = mockk<UIService>()
        val eventDetailService = EventDetailService(calService, uiService)

        on("fromMilliSecondsToStr") {
            it("should process 10000 ms to return label with 1s") {
                val label = eventDetailService.fromMilliSecondsToStr(1000.0)
                label.should.equal("1 s  / 0,02 mn / 0,00 h / 0,00 d")
            }
            it("should process 60000 ms to return label with 1mn") {
                val label = eventDetailService.fromMilliSecondsToStr(60000.0)
                label.should.equal("60 s  / 1,00 mn / 0,02 h / 0,00 d")
            }
            it("should process 3 600 000 ms to return label with 1h") {
                val label = eventDetailService.fromMilliSecondsToStr(3600000.0)
                // the result should indicate 0.13 d because we use a day of 8 hours
                label.should.equal("3600 s  / 60,00 mn / 1,00 h / 0,13 d")
            }
            it("should process 36 000 000 ms to return label with 10h") {
                val label = eventDetailService.fromMilliSecondsToStr(36000000.0)
                label.should.equal("36000 s  / 600,00 mn / 10,00 h / 1,25 d")
            }
        }
    }
})