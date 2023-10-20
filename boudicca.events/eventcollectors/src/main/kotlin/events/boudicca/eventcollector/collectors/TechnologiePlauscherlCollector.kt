package events.boudicca.eventcollector.collectors

import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.Event
import base.boudicca.api.eventcollector.EventCollector
import base.boudicca.api.eventcollector.Fetcher
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
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
            val zonedDateTime = ZonedDateTime.of(dateTime, ZoneId.of("UTC"))

            //TODO filter for only events in the future?
            Event(
                nameString, zonedDateTime.toOffsetDateTime(),
                mapOf(

                    SemanticKeys.LOCATION_NAME to locationString,
                    SemanticKeys.TAGS to listOf("TechCommunity", "Afterwork", "Socializing", "Networking").toString(),
                    SemanticKeys.URL to entry.link,
                    SemanticKeys.TYPE to "techmeetup", //TODO not sure if this works well
                    SemanticKeys.DESCRIPTION to entry.description.value,
                    SemanticKeys.REGISTRATION to "free"
                )
            )
        }

        return events
    }
}
