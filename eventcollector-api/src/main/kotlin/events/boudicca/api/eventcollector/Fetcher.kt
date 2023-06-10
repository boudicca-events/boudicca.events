package events.boudicca.api.eventcollector

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
        val requestStartTime = System.currentTimeMillis()
        val response = newHttpClient.send(request, BodyHandlers.ofString())
        if (response.statusCode() != 200) {
            throw RuntimeException("request to $url failed with status code ${response.statusCode()}")
        }
        val requestTime = System.currentTimeMillis() - requestStartTime
        if (requestTime > 1000) {
            println("slow request detected. took $requestTime ms fetching $url")
        }
        lastRequest = System.currentTimeMillis()
        return response.body()
    }

}