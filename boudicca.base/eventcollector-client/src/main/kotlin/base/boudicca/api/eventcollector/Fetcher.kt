package base.boudicca.api.eventcollector

import base.boudicca.api.eventcollector.collections.Collections
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse.BodyHandlers
import java.time.Clock
import java.util.concurrent.Callable
import java.util.function.Consumer

class Fetcher(
    private val clock: Clock,
    private val sleeper: Consumer<Long>,
    private val httpClient: HttpClientWrapper
) {
    constructor() : this(Clock.systemDefaultZone(), { Thread.sleep(it) }, createHttpClientWrapper())

    private val LOG = LoggerFactory.getLogger(this::class.java)

    private var lastRequestEnd = 0L
    private var lastRequestDuration = 0L

    fun fetchUrl(url: String): String {
        Collections.startHttpCall(url)
        return doRequest(url) { httpClient.doGet(url) }
    }

    fun fetchUrlPost(url: String, contentType: String, content: ByteArray): String {
        Collections.startHttpCall(url, String(content)) //TODO what if this is not string content?
        return doRequest(url) { httpClient.doPost(url, contentType, content) }
    }

    private fun doRequest(url: String, request: Callable<Pair<Int, String>>): String {
        val waitTime = lastRequestEnd + calcWaitTime() - clock.millis()
        if (waitTime > 0) {
            sleeper.accept(waitTime)
        }
        var start = 0L
        val response = try {
            retry(LOG, sleeper) {
                Collections.resetHttpTiming()
                start = clock.millis()
                val response = request.call()
                if (response.first != 200) {
                    throw RuntimeException("request to $url failed with status code ${response.first}")
                }
                response
            }
        } catch (e: Exception) {
            Collections.endHttpCall(-1)
            throw e
        } finally {
            val end = clock.millis()
            lastRequestEnd = end
            lastRequestDuration = end - start
        }
        Collections.endHttpCall(response.first)
        return response.second
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

private fun createHttpClientWrapper(): HttpClientWrapper {
    val httpClient = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build()
    return object : HttpClientWrapper {
        override fun doGet(url: String): Pair<Int, String> {
            val request = HttpRequest.newBuilder(URI.create(url))
                .GET()
                .header("User-Agent", "boudicca.events collector")
                .build()

            return doRequest(request)
        }

        override fun doPost(url: String, contentType: String, content: ByteArray): Pair<Int, String> {
            val request = HttpRequest.newBuilder(URI.create(url))
                .POST(BodyPublishers.ofByteArray(content))
                .header("User-Agent", "boudicca.events collector")
                .header("Content-Type", contentType)
                .build()

            return doRequest(request)
        }

        private fun doRequest(request: HttpRequest): Pair<Int, String> {
            val response = httpClient.send(request, BodyHandlers.ofString())
            return Pair(response.statusCode(), response.body())
        }

    }
}

interface HttpClientWrapper {
    fun doGet(url: String): Pair<Int, String>
    fun doPost(url: String, contentType: String, content: ByteArray): Pair<Int, String>
}
