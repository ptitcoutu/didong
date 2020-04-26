package org.didong.didong.event

import com.winterbe.expekt.should
import io.mockk.mockk
import org.amshove.kluent.`should be`
import org.amshove.kluent.shouldContain
import org.didong.didong.calendar.CalendarService
import org.didong.didong.gui.UIService
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

/**
 *
 */
class EventDetailServiceSpek : Spek({
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
        on("computeEventToGetTags") {
            it("should group and sum event by tags") {
                // given: a liste of three events related to 4 different tags
                val events = listOf(
                        EventDetail(id = 1, title = "t1", calendarId = "c1",
                                startTime = "1517475600000" /* 2018-02-01T10:00:00*/,
                                endTime = "1517476800000" /* 2018-02-01T10:20:00*/,
                                description = EventDescription(
                                        started = false,
                                        tags = listOf("p1", "a1"))),
                        EventDetail(id = 2, title = "t2", calendarId = "c1",
                                startTime = "1517476800000" /* 2018-02-01T10:20:00*/,
                                endTime = "1517479200000" /* 2018-02-01T11:00:00*/,
                                description = EventDescription(
                                        started = false,
                                        tags = listOf("p1", "a2"))),
                        EventDetail(id = 1, title = "t3", calendarId = "c1",
                                startTime = "1517479200000" /* 2018-02-01T11:00:00*/,
                                endTime = "1517482200000" /* 2018-02-01T11:50:00*/,
                                description = EventDescription(
                                        started = false,
                                        tags = listOf("p2", "a1")))
                )
                // when: compute event to get tags
                val tags = eventDetailService.computeEventToGetTags(events)

                // then: the tag should be p1 -> 1h, p2 -> 50mn, a1 -> 70mn, a2 -> 40mn
                tags.size `should be` 4
                tags.shouldContain("p1" to 3_600_000L /* 1h */)
                tags.shouldContain("p2" to 3_000_000L /* 50mn */)
                tags.shouldContain("a1" to 4_200_000L /* 70mn */)
                tags.shouldContain("a2" to 2_400_000L /* 40mn */)
            }
            it("should group and sum event by 'normalized' tags") {
                // given: a list of three events related to 4 different tags
                val events = listOf(
                        EventDetail(id = 1, title = "t1", calendarId = "c1",
                                startTime = "1517475600000" /* 2018-02-01T10:00:00*/,
                                endTime = "1517476800000" /* 2018-02-01T10:20:00*/,
                                description = EventDescription(
                                        started = false,
                                        tags = listOf("P1 ", " a1"))),
                        EventDetail(id = 2, title = "t2", calendarId = "c1",
                                startTime = "1517476800000" /* 2018-02-01T10:20:00*/,
                                endTime = "1517479200000" /* 2018-02-01T11:00:00*/,
                                description = EventDescription(
                                        started = false,
                                        tags = listOf("p1", " A2"))),
                        EventDetail(id = 1, title = "t3", calendarId = "c1",
                                startTime = "1517479200000" /* 2018-02-01T11:00:00*/,
                                endTime = "1517482200000" /* 2018-02-01T11:50:00*/,
                                description = EventDescription(
                                        started = false,
                                        tags = listOf("P2 ", "A1")))
                )
                // when: compute event to get tags
                val tags = eventDetailService.computeEventToGetTags(events)

                // then: the tag should be p1 -> 1h, p2 -> 50mn, a1 -> 70mn, a2 -> 40mn
                tags.size `should be` 4
                tags.shouldContain("p1" to 3_600_000L /* 1h */)
                tags.shouldContain("p2" to 3_000_000L /* 50mn */)
                tags.shouldContain("a1" to 4_200_000L /* 70mn */)
                tags.shouldContain("a2" to 2_400_000L /* 40mn */)
            }
        }
    }
})