package events.boudicca.api.eventcollector

import events.boudicca.api.eventcollector.collections.Collections
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse.BodyHandlers

class Fetcher {

    private val newHttpClient = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build()

    private var lastRequestEnd = 0L
    private var lastRequestDuration = 0L

    fun fetchUrl(url: String): String {
        val request = HttpRequest.newBuilder(URI.create(url))
            .GET()
            .header("User-Agent", "boudicca.events collector")
            .build()

        Collections.startHttpCall(url)
        return doRequest(request, url)
    }

    fun fetchUrlPost(url: String, contentType: String, content: ByteArray): String {
        val request = HttpRequest.newBuilder(URI.create(url))
            .POST(BodyPublishers.ofByteArray(content))
            .header("User-Agent", "boudicca.events collector")
            .header("Content-Type", contentType)
            .build()

        Collections.startHttpCall(url, String(content)) //TODO what if this is not string content?
        return doRequest(request, url)
    }

    private fun doRequest(request: HttpRequest, url: String): String {
        val waitTime = lastRequestEnd + calcWaitTime() - System.currentTimeMillis()
        if (waitTime > 0) {
            Thread.sleep(waitTime)
        }
        val start = System.currentTimeMillis()
        val response = try {
            newHttpClient.send(request, BodyHandlers.ofString())
        } catch (e: Exception) {
            Collections.endHttpCall(-1)
            throw e
        } finally {
            val end = System.currentTimeMillis()
            lastRequestEnd = end
            lastRequestDuration = end - start
        }
        Collections.endHttpCall(response.statusCode())
        if (response.statusCode() != 200) {
            throw RuntimeException("request to $url failed with status code ${response.statusCode()}")
        }
        return response.body()
    }

    private fun calcWaitTime(): Long {
        if (lastRequestDuration == 0L) {
            return 0L
        }
        val waitTime = (lastRequestDuration.toDouble() * 0.8F).toLong()
        if (waitTime <= 100L) {
            return 100L
        }
        return waitTime
    }

}