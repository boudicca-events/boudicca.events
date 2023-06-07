package events.boudicca.api.eventcollector

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers

/* how long we want to wait between request as to not overload the target server */
const val MIN_WAIT_TIME_IN_MS = 100

class Fetcher {

    private val newHttpClient = HttpClient.newHttpClient()

    private var lastRequest = 0L

    fun fetchUrl(url: String): String {
        val waitTime = lastRequest + MIN_WAIT_TIME_IN_MS - System.currentTimeMillis()
        if (waitTime > 0) {
            Thread.sleep(waitTime)
        }
        val request = HttpRequest.newBuilder(URI.create(url))
            .GET()
            .header("User-Agent", "boudicca.events collector")
            .build()
        val response = newHttpClient.send(request, BodyHandlers.ofString())
        lastRequest = System.currentTimeMillis()
        return response.body()
    }

}