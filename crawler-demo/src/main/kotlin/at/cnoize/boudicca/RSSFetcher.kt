import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import io.quarkus.scheduler.Scheduled
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.net.URL
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

            for (entry in feed.entries) {
                println("Title: ${entry.title}")
                println("Link: ${entry.link}")
                println("Description: ${entry.description.value}")
            }
        } catch (e: Exception) {
            System.err.println("Failed to fetch RSS feed: ${e.message}")
        }
    }
}