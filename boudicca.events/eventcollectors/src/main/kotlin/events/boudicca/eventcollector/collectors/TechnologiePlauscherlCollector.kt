package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.EventCollector
import base.boudicca.api.eventcollector.Fetcher
import base.boudicca.model.Event
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter


class TechnologiePlauscherlCollector : EventCollector {
    override fun getName(): String {
        return "technologieplauscherl"
    }

    override fun collectEvents(): List<Event> {
        val url = "https://technologieplauscherl.at/feed"
        val contentAsInputStream = Fetcher().fetchUrl(url).byteInputStream()
        val feed = SyndFeedInput().build(XmlReader(contentAsInputStream))

        val events = feed.entries.map { entry ->

            val titleComponents = entry.title.split("|", "@")
            val nameString = titleComponents[0].trim()
            val dateString = titleComponents[1].trim()
            val locationString = titleComponents[2].trim()

            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            val dateTime = LocalDateTime.parse(dateString, formatter)
            val offsetDateTime = dateTime.atZone(ZoneId.of("UTC")).toOffsetDateTime()

            Event(
                nameString, offsetDateTime,
                mapOf(
                    SemanticKeys.LOCATION_NAME to locationString,
                    SemanticKeys.TAGS to listOf("TechCommunity", "Afterwork", "Socializing", "Networking").toString(),
                    SemanticKeys.URL to entry.link,
                    SemanticKeys.TYPE to "techmeetup", //TODO not sure if this works well
                    SemanticKeys.DESCRIPTION to entry.description.value,
                    SemanticKeys.REGISTRATION to "free",
                    SemanticKeys.SOURCES to entry.link,
                )
            )
        }

        return events
    }
}
