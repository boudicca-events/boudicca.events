import at.cnoize.boudicca.model.Event
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import io.quarkus.scheduler.Scheduled
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
    fun fetchRss() {
        try {
            val url = URL(rssUrl)
            val input = SyndFeedInput()
            val feed = input.build(XmlReader(url))

            val events = feed.entries.map { entry ->
//                println("Title: ${entry.title}")
//                println("Link: ${entry.link}")
//                println("Description: ${entry.description.value}")


                val titleComponents = entry.title.split("|","@")
                val nameString = titleComponents[0].trim()
                val dateString = titleComponents[1].trim()
                val locationString = titleComponents[2].trim()

                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                val dateTime = LocalDateTime.parse(dateString, formatter)
                val zonedDateTime = ZonedDateTime.of(dateTime, ZoneId.of("UTC"))

                val locationTag = "start.location.name"

                Event().name(nameString).startDate(zonedDateTime.toOffsetDateTime()).data(
                    mapOf(
                        locationTag to locationString
                    )
                )
            }


            events.forEach {
                println(it)
                // TODO: sent to api here
            }

        } catch (e: Exception) {
            System.err.println("Failed to fetch RSS feed: ${e.message}")
        }
    }
}