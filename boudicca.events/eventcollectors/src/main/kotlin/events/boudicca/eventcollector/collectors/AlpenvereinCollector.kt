package events.boudicca.eventcollector.collectors

import base.boudicca.SemanticKeys
import base.boudicca.TextProperty
import base.boudicca.api.eventcollector.Fetcher
import base.boudicca.api.eventcollector.TwoStepEventCollector
import base.boudicca.format.UrlUtils
import base.boudicca.model.structured.StructuredEvent
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.URI
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class AlpenvereinCollector : TwoStepEventCollector<String>("alpenverein") {

    private val fetcher = Fetcher(12 * 1000) //they have a crawl-delay of 12 seconds...

    override fun getAllUnparsedEvents(): List<String> {

        val mainSearchPage = Jsoup.parse(fetcher.fetchUrl("https://www.alpenverein.at/portal/termine/suche.php"))

        val allSearchPages = mainSearchPage.select("div.pager a")
            .map { normalizeUrl(it.attr("href")) }
            .distinct()
            .filter { !it.endsWith("=1") }
            .map { Jsoup.parse(fetcher.fetchUrl(it)) }
            .plus(mainSearchPage)

        val allLinks = allSearchPages.flatMap { doc ->
            doc.select("div.elementList h2.listEntryTitle a")
                .map { normalizeUrl(it.attr("href")) }
        }.distinct()

        return allLinks
    }

    override fun parseStructuredEvent(event: String): StructuredEvent {
        val eventSite = Jsoup.parse(fetcher.fetchUrl(event))

        val name = eventSite.select("div.elementBoxSheet h2").text()

        val (startDate, endDate) = parseDates(eventSite)

        return StructuredEvent
            .builder(name, startDate)
            .withProperty(SemanticKeys.NAME_PROPERTY, name)
            .withProperty(SemanticKeys.ENDDATE_PROPERTY, endDate)
            .withProperty(SemanticKeys.URL_PROPERTY, UrlUtils.parse(event))
            .withProperty(SemanticKeys.SOURCES_PROPERTY, listOf(event))
            .withProperty(SemanticKeys.TYPE_PROPERTY, "sport")
            .withProperty(TextProperty("sport.participation"), "active")
            .withProperty(
                SemanticKeys.DESCRIPTION_TEXT_PROPERTY,
                eventSite.select("div.elementBoxSheet div.elementText").text()
            )
            .withProperty(SemanticKeys.PICTURE_URL_PROPERTY, getPictureUrl(eventSite))
            .withProperty(SemanticKeys.LOCATION_CITY_PROPERTY, getLocationCity(eventSite))
            .build()
    }

    private fun getLocationCity(eventSite: Document): String? {
        val locationElements = eventSite.select("div.elementBoxSheet dl#officeveranstaltung_ort1")
        if (locationElements.isNotEmpty()) {
            return locationElements.text().removePrefix("Ort: ")
        }
        return null
    }

    private fun getPictureUrl(eventSite: Document): URI? {
        val imageElements = eventSite.select("div.elementBoxSheet dl#officeveranstaltung_bild1 img")
        if (imageElements.isNotEmpty()) {
            val pictureUrl = imageElements.first()!!.attr("src")
            if (!pictureUrl.isNullOrEmpty()) {
                return UrlUtils.parse(normalizeUrl(pictureUrl))
            }
        }
        return null
    }

    private fun parseDates(eventSite: Document): Pair<OffsetDateTime, OffsetDateTime?> {
        val times = eventSite.select("div.elementBoxSheet dl#officeveranstaltung_zeitraum1 h3")
        val timeText = if (times.isNotEmpty()) {
            times.text().removePrefix("Termin: ")
        } else {
            eventSite.select("div.elementBoxSheet div.elementText > p.subline").text()
        }

        if (timeText.contains("-")) {
            //03.05.2024 19:00 - 17:00
            //03.05.2024 19:00 - 04.05.2024
            //03.05.2024 19:00 - 04.05.2024 17:00
            val split = timeText.split("-")
            if (!split[1].trim().contains(" ") && split[1].trim().contains(":")) {
                //03.05.2024 19:00 - 17:00
                val dateText = split[0].split(" ")[0]
                return Pair(parseDate(split[0].trim()), parseDate(dateText.trim() + " " + split[1].trim()))
            } else {
                //03.05.2024 19:00 - 04.05.2024
                //03.05.2024 19:00 - 04.05.2024 17:00
                return Pair(parseDate(split[0].trim()), parseDate(split[1].trim()))
            }
        } else {
            //03.05.2024
            //03.05.2024 19:00
            return Pair(parseDate(timeText.trim()), null)
        }
    }

    private fun parseDate(fullDateTimeText: String): OffsetDateTime {
        var timeText: String? = null
        val dateText = if (fullDateTimeText.contains(" ")) {
            //03.05.2024 19:00
            val split = fullDateTimeText.split(" ")
            timeText = split[1].trim()
            split[0]
        } else {
            //03.05.2024
            fullDateTimeText
        }

        val date = LocalDate.parse(dateText, DateTimeFormatter.ofPattern("dd.MM.uuuu"))
        val dateTime = if (timeText != null) {
            date.atTime(LocalTime.parse(timeText, DateTimeFormatter.ofPattern("kk:mm")))
        } else {
            date.atStartOfDay()
        }

        return dateTime.atZone(ZoneId.of("Europe/Vienna")).toOffsetDateTime()
    }

    private fun normalizeUrl(url: String): String {
        val secureUrl = url.replace("http://", "https://")
        return if (secureUrl.startsWith("https://")) {
            secureUrl
        } else {
            "https://www.alpenverein.at$secureUrl"
        }
    }

}
