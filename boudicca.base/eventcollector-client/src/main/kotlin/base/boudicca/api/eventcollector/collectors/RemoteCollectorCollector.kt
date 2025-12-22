package base.boudicca.api.eventcollector.collectors

import base.boudicca.api.eventcollector.EventCollector
import base.boudicca.api.eventcollector.collections.Collections
import base.boudicca.api.remotecollector.RemoteCollectorClient
import base.boudicca.api.remotecollector.model.HttpCall
import base.boudicca.model.Event
import java.net.HttpURLConnection

/**
 * EventCollector implementation which will collect events from a remote EventCollector via HTTP.
 * see the RemoteCollectorApi interface for a http interface description
 */
class RemoteCollectorCollector(
    private val url: String,
    private val name: String? = null,
) : EventCollector {
    override fun getName(): String = name ?: "remote collector: $url"

    override fun collectEvents(): List<Event> {
        Collections.startHttpCall(url)
        val eventCollection =
            try {
                val eventCollection = RemoteCollectorClient(url).collectEvents()
                Collections.endHttpCall(HttpURLConnection.HTTP_OK)
                eventCollection
            } catch (e: Exception) {
                Collections.endHttpCall(-1)
                throw e
            }

        val currentSingleCollection = Collections.getCurrentSingleCollection()
        if (currentSingleCollection != null) {
            currentSingleCollection.logLines.addAll(eventCollection.logLines ?: emptyList())
            currentSingleCollection.httpCalls.addAll(
                eventCollection.httpCalls?.map { toCollectionsHttpCall(it) } ?: emptyList(),
            )
            currentSingleCollection.errorCount = eventCollection.errorCount ?: 0
            currentSingleCollection.warningCount = eventCollection.warningCount ?: 0
        }

        return eventCollection.events
    }

    private fun toCollectionsHttpCall(httpCall: HttpCall): base.boudicca.api.eventcollector.collections.HttpCall =
        base.boudicca.api.eventcollector.collections.HttpCall(
            httpCall.startTime.toInstant().toEpochMilli(),
            httpCall.endTime.toInstant().toEpochMilli(),
            httpCall.url,
            httpCall.postParams,
            httpCall.responseCode,
        )
}
