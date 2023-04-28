package events.boudicca.eventcollector

import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import events.boudicca.api.eventcollector.Event
import events.boudicca.api.eventcollector.EventCollector
import java.net.URL
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class TechnologiePlauscherlFetcher : EventCollector {
    override fun getName(): String {
        return "technologieplauscherl"
    }


    override fun collectEvents(): List<events.boudicca.api.eventcollector.Event> {
        val url = URL("https://technologieplauscherl.at/feed")
        val input = SyndFeedInput()
        val feed = input.build(XmlReader(url))

        val events = feed.entries.map { entry ->

            val titleComponents = entry.title.split("|", "@")
            val nameString = titleComponents[0].trim()
            val dateString = titleComponents[1].trim()
            val locationString = titleComponents[2].trim()

            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            val dateTime = LocalDateTime.parse(dateString, formatter)
            val zonedDateTime = ZonedDateTime.of(dateTime, ZoneId.of("UTC"))

            val locationTag = "start.location.name"

            Event(
                nameString, zonedDateTime.toOffsetDateTime(),
                mapOf(
                    locationTag to locationString,
                    "tags" to listOf("TechCommunity", "Afterwork", "Socializing", "Networking").toString(),
                    "url" to entry.link
                )
            )
        }


        events.forEach {
            println(it)
        }

        return events
    }
}
