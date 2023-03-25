package at.cnoize.boudicca.api

import at.cnoize.boudicca.api.util.Http
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class EventApi(private val baseUrl: String = "http://localhost:8081/") {
    private val eventUrl = calcEventUrl()
    private val http = Http()

    fun list(): Set<Event> {
        return convertToEvents(http.httpGet(eventUrl))
    }

    fun add(event: Event) {
        http.httpPost(eventUrl, Json.encodeToString(event))
    }

    fun search(searchDTO: SearchDTO): Set<Event> {
        return convertToEvents(http.httpPost("$eventUrl/search", Json.encodeToString(searchDTO)))
    }

    private fun convertToEvents(response: String?): Set<Event> {
        if (response == null) {
            return emptySet()
        }
        return Json.decodeFromString(response)
    }

    private fun calcEventUrl(): String {
        var eventUrl = baseUrl;
        if (!baseUrl.endsWith("/")) {
            eventUrl += "/";
        }
        return eventUrl + "event/"
    }
}