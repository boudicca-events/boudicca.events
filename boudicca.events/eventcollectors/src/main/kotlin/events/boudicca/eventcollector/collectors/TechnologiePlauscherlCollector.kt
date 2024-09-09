package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.EventCollector
import base.boudicca.api.eventcollector.Fetcher
import base.boudicca.format.UrlUtils
import base.boudicca.model.Registration
import base.boudicca.model.structured.StructuredEvent
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter


class TechnologiePlauscherlCollector : EventCollector {
    override fun getName(): String {
        return "technologieplauscherl"
    }

    override fun collectStructuredEvents(): List<StructuredEvent> {
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

            StructuredEvent
                .builder(nameString, offsetDateTime)
                .withProperty(SemanticKeys.LOCATION_NAME_PROPERTY, locationString)
                .withProperty(
                    SemanticKeys.TAGS_PROPERTY,
                    listOf("TechCommunity", "Afterwork", "Socializing", "Networking")
                )
                .withProperty(SemanticKeys.URL_PROPERTY, UrlUtils.parse(entry.link))
                .withProperty(SemanticKeys.TYPE_PROPERTY, "techmeetup") //TODO not sure if this works well
                .withProperty(SemanticKeys.DESCRIPTION_TEXT_PROPERTY, entry.description.value)
                .withProperty(SemanticKeys.REGISTRATION_PROPERTY, Registration.FREE)
                .withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(entry.link))
                .build()
        }

        return events
    }
}
