package events.boudicca.api.eventcollector

import events.boudicca.api.eventcollector.collections.Collections
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse.BodyHandlers

/* how long we want to wait between request as to not overload the target server */
const val DEFAULT_MIN_WAIT_TIME_IN_MS = 100L

class Fetcher(waitTimeInMs: Long = DEFAULT_MIN_WAIT_TIME_IN_MS) {

    private val realWaitTimeInMs = Math.max(DEFAULT_MIN_WAIT_TIME_IN_MS, waitTimeInMs)
    private val newHttpClient = HttpClient.newHttpClient()

    private var lastRequest = 0L

    fun fetchUrl(url: String): String {
        val request = HttpRequest.newBuilder(URI.create(url))
            .GET()
            .header("User-Agent", "boudicca.events collector")
            .build()

        return doRequest(request, url)
    }

    fun fetchUrlPost(url: String, contentType: String, content: ByteArray): String {
        val request = HttpRequest.newBuilder(URI.create(url))
            .POST(BodyPublishers.ofByteArray(content))
            .header("User-Agent", "boudicca.events collector")
            .header("Content-Type", contentType)
            .build()

        return doRequest(request, url)
    }

    private fun doRequest(request: HttpRequest, url: String): String {
        val waitTime = lastRequest + realWaitTimeInMs - System.currentTimeMillis()
        if (waitTime > 0) {
            Thread.sleep(waitTime)
        }
        Collections.startHttpCall(url)
        val response = newHttpClient.send(request, BodyHandlers.ofString())
        if (response.statusCode() != 200) {
            Collections.endHttpCall(response.statusCode())
            throw RuntimeException("request to $url failed with status code ${response.statusCode()}")
        }
        lastRequest = System.currentTimeMillis()
        Collections.endHttpCall(response.statusCode())
        return response.body()
    }

}