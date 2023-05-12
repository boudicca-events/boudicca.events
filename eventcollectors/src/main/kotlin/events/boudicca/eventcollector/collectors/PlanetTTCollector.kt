package events.boudicca.eventcollector.collectors

import events.boudicca.SemanticKeys
import events.boudicca.api.eventcollector.Event
import events.boudicca.api.eventcollector.TwoStepEventCollector
import it.skrape.core.htmlDocument
import it.skrape.fetcher.*
import it.skrape.selects.DocElement
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.*
import java.time.format.DateTimeFormatter


object CustomHttpFetcher : BlockingFetcher<Request> {
    override val requestBuilder: Request get() = Request()
    override fun fetch(request: Request): Result {
        val result = HttpClient.newHttpClient()
            .send(
                HttpRequest.newBuilder(URI.create(request.url)).GET().build(),
                HttpResponse.BodyHandlers.ofString(Charsets.UTF_8)
            )
        return Result(result.body(), Result.Status(result.statusCode(), ""), "", emptyMap(), request.url, emptyList())
    }
}

class PlanetTTCollector : TwoStepEventCollector<DocElement>("planettt") {

    override fun getAllUnparsedEvents(): List<DocElement> {
        val events = mutableListOf<DocElement>()
        val baseUrl = "http://planet.tt/index.php?article_id=173&start=0&limit=200&locat=Home"
        skrape(CustomHttpFetcher) {
            request {
                url = baseUrl
            }
            response {
                htmlDocument {
                    selection("article.event_articles") {
                        findAll {
                            events.addAll(this)
                        }
                    }
                }
            }
        }

        return events
    }

    override fun parseEvent(event: DocElement): Event? {
        var name: String? = null
        val startDate: OffsetDateTime? = parseDate(event)
        val data = mutableMapOf<String, String>()

        event.apply {
            data[SemanticKeys.URL] = "http://planet.tt/index.php?article_id=148&va=" + attribute("id")
            selection("div.eventkasten h6") {
                findFirst {
                    name = text
                }
            }
            selection("div.eventkasten>div:nth-child(2)>img") {
                findFirst {
                    data["pictureUrl"] = "http://planet.tt/" + attribute("src")
                }
            }
            selection("div.eventkasten>div.completeInfo") {
                findFirst {
                    data[SemanticKeys.DESCRIPTION] = this.children.dropLast(2).map { it.text }.joinToString("\n").trim()
                }
            }
        }

        data[SemanticKeys.TYPE] = "concert"
        mapLocation(data, event)

        if (name != null && startDate != null) {
            return Event(name!!, startDate!!, data)
        }
        return null
    }

    private fun mapLocation(data: MutableMap<String, String>, event: DocElement) {
        var location: String? = null
        event.apply {
            selection("header:nth-child(1) img") {
                findFirst {
                    location = attribute("src")
                }
            }
        }
        if (location!!.contains("simmcity")) {
            data[SemanticKeys.LOCATION_NAME] = "SiMMCity"
            data[SemanticKeys.LOCATION_URL] = "https://simmcity.at/"
            data[SemanticKeys.LOCATION_CITY] = "Wien"
        } else if (location!!.contains("szene")) {
            data[SemanticKeys.LOCATION_NAME] = "Szene"
            data[SemanticKeys.LOCATION_URL] = "https://szene.wien/"
            data[SemanticKeys.LOCATION_CITY] = "Wien"
        } else if (location!!.contains("planet")) {
            data[SemanticKeys.LOCATION_NAME] = "Gasometer"
            data[SemanticKeys.LOCATION_URL] = "https://www.gasometer.at/"
            data[SemanticKeys.LOCATION_CITY] = "Wien"
        } else {
            println("could not guess location from img src: $location")
        }
    }

    private fun parseDate(event: DocElement): OffsetDateTime? {
        var firstPart: String? = null
        var time: String? = null
        event.apply {
            selection("header>div:nth-child(1) p:nth-child(4)") {
                findFirst {
                    firstPart = text
                }
            }
            selection("header>div:nth-child(1) p:nth-child(6)") {
                findFirst {
                    time = text
                }
            }
        }
        val date = (firstPart!! + " " + time!!).replace("'","")

        return LocalDateTime.parse(date, DateTimeFormatter.ofPattern("dd.MM.uu kk:mm'Uhr'")).atZone(ZoneId.of("CET"))
            .toOffsetDateTime()
    }

}
