import at.cnoize.boudicca.model.Event
import at.cnoize.boudicca.crawlerapi.IngestionApi
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import io.quarkus.scheduler.Scheduled
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient
import org.eclipse.microprofile.rest.client.inject.RestClient
import java.net.URL
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject
import javax.ws.rs.Path

@RegisterRestClient(configKey = "ingestion-api")
@ApplicationScoped
@Path("/ingest")
interface RssIngestionApi: IngestionApi

@ApplicationScoped
class RssFetcher {

    @Inject
    @ConfigProperty(name = "rss.url")
    private lateinit var rssUrl: String

    @Inject
    @RestClient
    lateinit var ingestionApi: RssIngestionApi

    @Scheduled(every = "5m")
    fun fetchRss() {
        try {
            val url = URL(rssUrl)
            val input = SyndFeedInput()
            val feed = input.build(XmlReader(url))

            val events = feed.entries.map { entry ->
//                println("Title: ${entry.title}")
//                println("Link: ${entry.link}")
//                println("Description: ${entry.description.value}")


                val titleComponents = entry.title.split("|", "@")
                val nameString = titleComponents[0].trim()
                val dateString = titleComponents[1].trim()
                val locationString = titleComponents[2].trim()

                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                val dateTime = LocalDateTime.parse(dateString, formatter)
                val zonedDateTime = ZonedDateTime.of(dateTime, ZoneId.of("UTC"))

                val locationTag = "start.location.name"

                Event(nameString, zonedDateTime,
                        mapOf(
                                locationTag to locationString,
                                "tags" to listOf("TechCommunity", "Afterwork", "Socializing", "Networking").toString(),
                                "url" to entry.link
                        )
                )
            }

            events.forEach {
                println(it)
                ingestionApi.add(it)
            }

        } catch (e: Exception) {
            System.err.println("Failed to fetch RSS feed: ${e.message}")
        }
    }
}
