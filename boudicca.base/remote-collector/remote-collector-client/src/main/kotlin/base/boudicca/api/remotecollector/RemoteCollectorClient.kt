package base.boudicca.api.remotecollector

import base.boudicca.api.remotecollector.model.EventCollection
import base.boudicca.api.remotecollector.model.HttpCall
import base.boudicca.model.Event
import base.boudicca.openapi.ApiClient
import base.boudicca.openapi.ApiException
import base.boudicca.remote_collector.openapi.api.RemoteCollectorApi

class RemoteCollectorClient(private val remoteCollectorUrl: String) {
    private val remoteCollectorApi: RemoteCollectorApi

    init {
        if (remoteCollectorUrl.isBlank()) {
            error("you need to pass an remoteCollectorUrl!")
        }
        val apiClient = ApiClient()
        apiClient.updateBaseUri(remoteCollectorUrl)

        remoteCollectorApi = RemoteCollectorApi(apiClient)
    }

    fun collectEvents(): EventCollection {
        try {
            return toApi(remoteCollectorApi.collectEvents())
        } catch (e: ApiException) {
            throw RemoteCollectorException("could not reach remote collector: $remoteCollectorUrl", e)
        }
    }

    private fun toApi(eventCollection: base.boudicca.remote_collector.openapi.model.EventCollection): EventCollection = EventCollection(
        eventCollection.events?.map { toApi(it) } ?: emptyList(),
        eventCollection.httpCalls?.map { toApi(it) },
        eventCollection.logLines,
        eventCollection.warningCount,
        eventCollection.errorCount,
    )

    private fun toApi(event: base.boudicca.remote_collector.openapi.model.Event): Event = Event(event.name!!, event.startDate!!, event.data ?: emptyMap())

    private fun toApi(httpCall: base.boudicca.remote_collector.openapi.model.HttpCall): HttpCall = HttpCall(
        httpCall.url!!,
        httpCall.responseCode!!,
        httpCall.startTime!!,
        httpCall.endTime!!,
        httpCall.postParams,
    )
}

class RemoteCollectorException(msg: String, e: ApiException) : RuntimeException(msg, e)
