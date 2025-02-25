package base.boudicca.fetcher

import io.github.oshai.kotlinlogging.KotlinLogging
import java.net.HttpURLConnection
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse.BodyHandlers
import java.time.Clock
import java.util.concurrent.Callable

/**
 * THIS CLASS IS NOT THREADSAFE!
 */
class Fetcher(
    private val manualSetDelay: Long? = null,
    private val userAgent: String = Constants.USER_AGENT,
    private val clock: Clock = Clock.systemDefaultZone(),
    private val sleeper: Sleeper = Sleeper { ms -> Thread.sleep(ms) },
    private val httpClient: HttpClientWrapper = createDefaultHttpClientWrapper(userAgent),
    private val eventListeners: List<FetcherEventListener> = emptyList(),
    private val fetcherCache: FetcherCache = NoopFetcherCache
) {
    private val logger = KotlinLogging.logger {}

    private var lastRequestEnd = 0L
    private var lastRequestDuration = 0L

    fun fetchUrl(url: String): String {
        return fetch(url, url, null) {
            httpClient.doGet(url)
        }
    }

    fun fetchUrlPost(url: String, contentType: String, content: String): String {
        return fetch("$url|${contentType}|${content}", url, content) {
            httpClient.doPost(url, contentType, content)
        }
    }

    private fun fetch(
        cacheKey: String,
        url: String,
        content: String?,
        executeRequestAction: () -> Pair<Int, String>
    ): String {
        if (fetcherCache.containsEntry(cacheKey)) {
            logger.debug { "Using cached entry for Key: $cacheKey" }
            return fetcherCache.getEntry(cacheKey)
        }

        val response = executeRequest(url, content, executeRequestAction)

        fetcherCache.putEntry(cacheKey, response)
        logger.debug { "Added new entry to cache with Key: $cacheKey" }
        return response
    }

    private fun executeRequest(url: String, content: String?, request: Callable<Pair<Int, String>>): String {
        doSleep()
        val response = retry(logger, sleeper) {
            eventListeners.forEach { it.callStarted(url, content) }
            val start = clock.millis()

            @Suppress("detekt.TooGenericExceptionCaught") //it will be rethrown
            val response = try {
                request.call()
            } catch (e: Exception) {
                eventListeners.forEach { it.callEnded(-1) }
                throw e
            } finally {
                val end = clock.millis()
                lastRequestEnd = end
                lastRequestDuration = end - start
            }
            eventListeners.forEach { it.callEnded(response.first) }
            if (response.first != HttpURLConnection.HTTP_OK) {
                throw FetcherException("request to $url failed with status code ${response.first}")
            }
            response
        }
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

class FetcherException(reason: String) : RuntimeException(reason)

private fun createDefaultHttpClientWrapper(userAgent: String): HttpClientWrapper {
    val httpClient = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build()
    return object : HttpClientWrapper {
        override fun doGet(url: String): Pair<Int, String> {
            val request = HttpRequest.newBuilder(URI.create(url))
                .GET()
                .header("User-Agent", userAgent)
                .build()
            return doRequest(request)
        }

        override fun doPost(url: String, contentType: String, content: String): Pair<Int, String> {
            val request = HttpRequest.newBuilder(URI.create(url))
                .POST(BodyPublishers.ofString(content))
                .header("User-Agent", userAgent)
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
