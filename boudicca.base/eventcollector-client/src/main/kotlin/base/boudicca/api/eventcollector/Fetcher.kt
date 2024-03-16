package base.boudicca.api.eventcollector

import base.boudicca.api.eventcollector.collections.Collections
import base.boudicca.api.eventcollector.fetcher.FetcherCache
import base.boudicca.api.eventcollector.fetcher.HttpClientWrapper
import base.boudicca.api.eventcollector.fetcher.NoopFetcherCache
import base.boudicca.api.eventcollector.util.Sleeper
import base.boudicca.api.eventcollector.util.retry
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse.BodyHandlers
import java.time.Clock
import java.util.concurrent.Callable

class Fetcher(
    private val clock: Clock,
    private val sleeper: Sleeper,
    private val httpClient: HttpClientWrapper,
    private val manualSetDelay: Long? = null
) {
    companion object {
        @Volatile
        var fetcherCache: FetcherCache = NoopFetcherCache
    }

    constructor(manualSetDelay: Long? = null) : this(
        Clock.systemDefaultZone(),
        Sleeper { ms -> Thread.sleep(ms) },
        createHttpClientWrapper(),
        manualSetDelay
    )

    private val LOG = LoggerFactory.getLogger(this::class.java)

    private var lastRequestEnd = 0L
    private var lastRequestDuration = 0L

    fun fetchUrl(url: String): String {
        if (fetcherCache.containsEntry(url)) {
            return fetcherCache.getEntry(url)
        }
        Collections.startHttpCall(url)
        val response = doRequest(url) { httpClient.doGet(url) }
        fetcherCache.putEntry(url, response)
        return response
    }

    fun fetchUrlPost(url: String, contentType: String, content: ByteArray): String {
        val cacheKey = "$url|${String(content)}"
        if (fetcherCache.containsEntry(cacheKey)) {
            return fetcherCache.getEntry(cacheKey)
        }
        Collections.startHttpCall(url, String(content)) //TODO what if this is not string content?
        val response = doRequest(url) { httpClient.doPost(url, contentType, content) }
        fetcherCache.putEntry(cacheKey, response)
        return response
    }

    private fun doRequest(url: String, request: Callable<Pair<Int, String>>): String {
        doSleep()
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

    private fun doSleep() {
        if (manualSetDelay != null) {
            sleeper.sleep(manualSetDelay)
        } else {
            val waitTime = lastRequestEnd + calcWaitTime() - clock.millis()
            if (waitTime > 0) {
                sleeper.sleep(waitTime)
            }
        }
    }

    private fun calcWaitTime(): Long {
        if (lastRequestDuration == 0L) {
            return 0L
        }
        val waitTime = (lastRequestDuration.toDouble() * 1.5F).toLong()
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

