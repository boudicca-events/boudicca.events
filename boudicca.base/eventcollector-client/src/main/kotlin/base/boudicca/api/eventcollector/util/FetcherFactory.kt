package base.boudicca.api.eventcollector.util

import base.boudicca.api.eventcollector.collections.Collections
import base.boudicca.fetcher.Constants
import base.boudicca.fetcher.Fetcher
import base.boudicca.fetcher.FetcherCache
import base.boudicca.fetcher.FetcherEventListener
import base.boudicca.fetcher.NoopFetcherCache
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.trace.TracerProvider
import io.opentelemetry.context.propagation.ContextPropagators

object FetcherFactory {
    @Volatile
    var defaultFetcherCache: FetcherCache = NoopFetcherCache

    @Volatile
    var disableRetries: Boolean = false

    @Volatile
    var otel: OpenTelemetry = OpenTelemetry.noop()

    private val collectionFetcherEventListener =
        object : FetcherEventListener {
            override fun callStarted(
                url: String,
                content: String?,
            ) {
                Collections.startHttpCall(url, content)
            }

            override fun callEnded(responseCode: Int) {
                Collections.endHttpCall(responseCode)
            }
        }

    private val delegatingOpenTelemetry =
        object : OpenTelemetry {
            override fun getTracerProvider(): TracerProvider? = otel.tracerProvider

            override fun getPropagators(): ContextPropagators? = otel.propagators
        }

    private val delegatingFetcherCache =
        object : FetcherCache {
            override fun containsEntry(key: String): Boolean = defaultFetcherCache.containsEntry(key)

            override fun getEntry(key: String): String = defaultFetcherCache.getEntry(key)

            override fun putEntry(
                key: String,
                entry: String,
            ) = defaultFetcherCache.putEntry(key, entry)
        }

    fun newFetcher(
        manualSetDelay: Long? = null,
        userAgent: String = Constants.USER_AGENT,
    ): Fetcher =
        Fetcher(
            manualSetDelay = manualSetDelay,
            userAgent = userAgent,
            eventListeners = listOf(collectionFetcherEventListener),
            fetcherCache = delegatingFetcherCache,
            disableRetries = disableRetries,
            otel = delegatingOpenTelemetry,
        )
}
