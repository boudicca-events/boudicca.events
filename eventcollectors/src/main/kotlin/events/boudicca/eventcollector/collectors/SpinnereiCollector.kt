package events.boudicca.eventcollector.collectors

import events.boudicca.SemanticKeys
import events.boudicca.api.eventcollector.Event
import events.boudicca.api.eventcollector.Fetcher
import events.boudicca.api.eventcollector.TwoStepEventCollector
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class SpinnereiCollector : TwoStepEventCollector<Pair<String, Document>>("spinnerei") {

    override fun getAllUnparsedEvents(): List<Pair<String, Document>> {
        val fetcher = Fetcher()
        val events = mutableListOf<Pair<String, Document>>()
        val eventUrls = mutableSetOf<String>()

        val document = Jsoup.parse(fetcher.fetchUrl("https://spinnerei.kulturpark.at/programm/"))
        document.select("div.container-programm-uebersicht > a")
            .forEach { eventUrls.add(it.attr("href")) }

        eventUrls.forEach {
            events.add(Pair(it, Jsoup.parse(fetcher.fetchUrl(it))))
        }

        return events
    }

    override fun parseEvent(event: Pair<String, Document>): Event {
        val (url, doc) = event
        val data = mutableMapOf<String, String>()
        data[SemanticKeys.URL] = url

        var name = doc.select("div.vng-details div.vng-detail-content-titel").text()
        name += " " + doc.select("div.vng-details div.vng-detail-content-untertitel").text()

        val startDate = parseTypeAndDate(data, doc.select("div.vng-details div.vng-detail-content-beginn").text())
        data[SemanticKeys.DESCRIPTION] = doc.select("div.vng-details div.vng-detail-content-bodytext p:nth-child(3)").text()

        data[SemanticKeys.PICTUREURL] =
            "https://spinnerei.kulturpark.at" + parsePictureUrl(doc.select("div.vng-details div.bg-image").attr("style"))

        data[SemanticKeys.LOCATION_NAME] = "Spinnerei"
        data[SemanticKeys.LOCATION_URL] = "https://spinnerei.kulturpark.at/"
        data[SemanticKeys.LOCATION_CITY] = "Traun"
        data[SemanticKeys.ACCESSIBILITY_ACCESSIBLEENTRY] = "true"
        data[SemanticKeys.ACCESSIBILITY_ACCESSIBLESEATS] = "true"
        data[SemanticKeys.ACCESSIBILITY_ACCESSIBLETOILETS] = "true"

        return Event(name, startDate, data)
    }

    private fun parsePictureUrl(style: String): String {
        //looks like background-image:url('/media/47704/c-wolf-gruber_hypnotica_pr-foto.png?center=0.39316239316239315,0.22666666666666666&mode=crop&width=320&height=180');
        return style.substring(22, style.length - 3)
    }

    private fun parseTypeAndDate(data: MutableMap<String, String>, text: String): OffsetDateTime {
        val split = text.split(' ', ignoreCase = false,  limit = 4)
        if (split.size != 4) {
            throw IllegalStateException("could not parse type and date from $text")
        }
        data[SemanticKeys.TYPE] = split[3]

        val date = LocalDate.parse(split[1], DateTimeFormatter.ofPattern("dd.MM.uu"))
        val time = LocalTime.parse(split[2], DateTimeFormatter.ofPattern("kk:mm"))
        return date.atTime(time).atZone(ZoneId.of("CET")).toOffsetDateTime()
    }

}
