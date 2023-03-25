import at.cnoize.boudicca.api.Event
import at.cnoize.boudicca.api.EventApi
import io.quarkus.scheduler.Scheduled
import it.skrape.core.htmlDocument
import it.skrape.fetcher.HttpFetcher
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import it.skrape.selects.DocElement
import it.skrape.selects.html5.a
import it.skrape.selects.html5.div
import net.fortuna.ical4j.data.CalendarBuilder
import net.fortuna.ical4j.model.Calendar
import net.fortuna.ical4j.model.component.VEvent
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.net.URL
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject


@ApplicationScoped
class RssFetcher {

    @Inject
    @ConfigProperty(name = "rss.url")
    lateinit var rssUrl: String

    @Scheduled(every = "30s")
    fun scrapeJkuEvents() {
        val eventUrls = mutableSetOf<String>()
        val icsUrls = mutableSetOf<String>()
        val baseUrl = "https://www.jku.at"
        skrape(HttpFetcher) {
            request {
                url = "${baseUrl}/studium/studieninteressierte/messen-events/"
            }
            response {
                htmlDocument {
                    div {
                        withClass="news_list_item"
                        findAll{
                            a {
                                findAll {
                                    forEach {
                                        eventUrls.add(it.attribute("href"))
                                    }
                                }
                            }
                        }
                    }
                    // parsed Doc is available here
                }

            }
        }

        eventUrls.forEach { eventUrl ->
            skrape(HttpFetcher) {
                request {
                    url = "${baseUrl}${eventUrl}"
                }
                response {
                    htmlDocument {
                        a {
                            findAll {
                                forEach {
                                    val href = it.attribute("href")
                                    if (href.endsWith(".ics")) {
                                        icsUrls.add(href)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        val events = mutableListOf<Event>()

        icsUrls.forEach {

            val fullUrl = "${baseUrl}${it}"
            println(fullUrl)
            val url = URL(fullUrl)
            events.addAll(parseEventFromIcs(url))
        }
        println("parsed ${events.size} events")

        val api = EventApi()
        events.forEach {
            println(it)
            api.add(it)
        }
    }

    fun parseEventFromIcs(url: URL): List<Event> {
        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")
        val daylongEventFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
        url.openStream().use {
            val builder = CalendarBuilder()
            val calendar: Calendar = builder.build(it)
            val components = calendar.components.filterIsInstance<VEvent>()

            return components.map {
                val title = it.summary.value
                println(it.startDate.value)

                val eventName = title
                val eventStartDate = if (it.isDaylongEvent()) {
                    val dateTime = LocalDate.parse(it.startDate.value, daylongEventFormatter)
                    val zonedDateTime = ZonedDateTime.of(dateTime.atTime(0,0), ZoneId.of("UTC"))
                    zonedDateTime.toOffsetDateTime()
                } else {
                    val dateTime = LocalDateTime.parse(it.startDate.value, formatter)
                    val zonedDateTime = ZonedDateTime.of(dateTime, ZoneId.of("UTC"))
                    zonedDateTime.toOffsetDateTime()
                }

                Event(eventName, eventStartDate)
            }
        }
    }

    private fun VEvent.isDaylongEvent(): Boolean {
        return this.startDate.toString().indexOf("VALUE=DATE") != -1
    }

    fun List<DocElement>.printAll() {
        this.forEach {
            println(it.text)
        }
    }
}