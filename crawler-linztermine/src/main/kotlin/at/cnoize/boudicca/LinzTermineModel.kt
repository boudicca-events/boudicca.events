package at.cnoize.boudicca

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import java.time.LocalDateTime

@JacksonXmlRootElement(localName = "events")
data class LinzTermineEvents(
    @JacksonXmlProperty(localName = "event")
    val eventList: List<LinzTermineEvent>,
)

data class LinzTermineEvent(
    @JacksonXmlProperty(localName = "id", isAttribute = true)
    val id: Int,

    @JacksonXmlProperty(localName = "title")
    val title: String,

    @JacksonXmlProperty(localName = "description")
    val description: String,

    @JacksonXmlProperty(localName = "url")
    val url: String,

    @JacksonXmlProperty(localName = "location")
    val location: String,

    @JacksonXmlProperty(localName = "firstdate", isAttribute = true)
    val start: LocalDateTime,

    @JacksonXmlProperty(localName = "lastdate", isAttribute = true)
    val end: LocalDateTime,

    @JacksonXmlProperty(localName = "category")
    val category: String,
)


