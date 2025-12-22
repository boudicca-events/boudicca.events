package base.boudicca.api.eventcollector.util

import base.boudicca.api.eventcollector.collections.Collections
import base.boudicca.fetcher.Constants
import base.boudicca.fetcher.Fetcher
import base.boudicca.fetcher.FetcherCache
import base.boudicca.fetcher.FetcherEventListener
import base.boudicca.fetcher.NoopFetcherCache
import io.opentelemetry.api.OpenTelemetry

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

    fun newFetcher(
        manualSetDelay: Long? = null,
        userAgent: String = Constants.USER_AGENT,
    ): Fetcher =
        Fetcher(
            manualSetDelay = manualSetDelay,
            userAgent = userAgent,
            eventListeners = listOf(collectionFetcherEventListener),
            fetcherCache = defaultFetcherCache,
            disableRetries = disableRetries,
            otel = otel,
        )
}
