package base.boudicca.fetcher

import io.github.oshai.kotlinlogging.KotlinLogging
import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.instrumentation.javahttpclient.JavaHttpClientTelemetry
import java.net.HttpURLConnection
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse.BodyHandlers
import java.time.Clock
import java.util.concurrent.Callable

private const val WAIT_TIME_FACTOR = 1.5F
private const val MIN_WAIT_TIME = 100L

/**
 * THIS CLASS IS NOT THREADSAFE!
 */
@Suppress("detekt:LongParameterList")
class Fetcher(
    private val manualSetDelay: Long? = null,
    private val userAgent: String = Constants.USER_AGENT,
    private val clock: Clock = Clock.systemDefaultZone(),
    private val sleeper: Sleeper = Sleeper { ms -> Thread.sleep(ms) },
    private val otel: OpenTelemetry = GlobalOpenTelemetry.get(),
    private val httpClient: HttpClientWrapper = createDefaultHttpClientWrapper(userAgent, otel),
    private val eventListeners: List<FetcherEventListener> = emptyList(),
    private val fetcherCache: FetcherCache = NoopFetcherCache,
    private val disableRetries: Boolean = false,
) {
    private val logger = KotlinLogging.logger {}
    private val tracer = otel.getTracer("fetcher")

    private var lastRequestEnd = 0L
    private var lastRequestDuration = 0L

    fun fetchUrl(url: String): String =
        fetch("GET", url, url, null) {
            httpClient.doGet(url)
        }

    fun fetchUrlPost(
        url: String,
        contentType: String,
        content: String,
    ): String =
        fetch("POST", "$url|$contentType|$content", url, content) {
            httpClient.doPost(url, contentType, content)
        }

    private fun fetch(
        httpMethod: String,
        cacheKey: String,
        url: String,
        content: String?,
        executeRequestAction: () -> Pair<Int, String>,
    ): String {
        val span =
            tracer
                .spanBuilder("fetcher")
                .setSpanKind(SpanKind.CLIENT)
                .setAttribute("http.request.method", httpMethod)
                .setAttribute("url.full", url)
                .startSpan()
        try {
            span.makeCurrent().use {
                if (fetcherCache.containsEntry(cacheKey)) {
                    logger.debug { "Using cached entry for Key: $cacheKey" }
                    span.setAttribute("from_cache", true)
                    return fetcherCache.getEntry(cacheKey)
                }
                span.setAttribute("from_cache", false)

                val response = executeRequest(span, url, content, executeRequestAction)

                fetcherCache.putEntry(cacheKey, response)
                logger.debug { "Added new entry to cache with Key: $cacheKey" }
                span.setStatus(StatusCode.OK)
                return response
            }
        } catch (e: Exception) {
            span.setStatus(StatusCode.ERROR)
            throw e
        } finally {
            span.end()
        }
    }

    private fun executeRequest(
        span: Span,
        url: String,
        content: String?,
        request: Callable<Pair<Int, String>>,
    ): String {
        doSleep(span)
        val response =
            retry {
                eventListeners.forEach { it.callStarted(url, content) }
                val start = clock.millis()

                val response =
                    try {
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

    private fun <T> retry(function: () -> T): T =
        if (disableRetries) {
            function()
        } else {
            retry(logger, sleeper, function)
        }

    private fun doSleep(span: Span) {
        if (manualSetDelay != null) {
            sleeper.sleep(manualSetDelay)
            span.addEvent("manual delay slept")
        } else {
            val waitTime = lastRequestEnd + calcWaitTime() - clock.millis()
            if (waitTime > 0) {
                sleeper.sleep(waitTime)
                span.addEvent("delay slept")
            }
        }
    }

    private fun calcWaitTime(): Long {
        if (lastRequestDuration == 0L) {
            return 0L
        }
        val waitTime = (lastRequestDuration.toDouble() * WAIT_TIME_FACTOR).toLong()
        if (waitTime <= MIN_WAIT_TIME) {
            return MIN_WAIT_TIME
        }
        return waitTime
    }
}

class FetcherException(
    reason: String,
) : RuntimeException(reason)

private fun createDefaultHttpClientWrapper(
    userAgent: String,
    otel: OpenTelemetry,
): HttpClientWrapper {
    val httpClient =
        JavaHttpClientTelemetry
            .builder(otel)
            .build()
            .newHttpClient(
                HttpClient
                    .newBuilder()
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build(),
            )

    return object : HttpClientWrapper {
        override fun doGet(url: String): Pair<Int, String> {
            val request =
                HttpRequest
                    .newBuilder(URI.create(url))
                    .GET()
                    .header("User-Agent", userAgent)
                    .build()
            return doRequest(request)
        }

        override fun doPost(
            url: String,
            contentType: String,
            content: String,
        ): Pair<Int, String> {
            val request =
                HttpRequest
                    .newBuilder(URI.create(url))
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
