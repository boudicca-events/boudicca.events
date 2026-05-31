package base.boudicca.api.eventdb.publisher

import base.boudicca.eventdb.openapi.api.PublisherApi
import base.boudicca.model.Entry
import base.boudicca.model.Event
import base.boudicca.model.toEvent
import base.boudicca.openapi.ApiClient
import base.boudicca.openapi.ApiException
import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.instrumentation.javahttpclient.JavaHttpClientTelemetry
import java.net.http.HttpClient
import kotlin.jvm.optionals.getOrNull

class DefaultEventDbPublisherClient(
    private val eventDbUrl: String,
    private val otel: OpenTelemetry = GlobalOpenTelemetry.get(),
) : EventDbPublisherClient {
    private val publisherApi: PublisherApi

    init {
        if (eventDbUrl.isBlank()) {
            error("you need to pass an eventDbUrl!")
        }
        val apiClient =
            object : ApiClient() {
                override fun getHttpClient(): HttpClient? =
                    JavaHttpClientTelemetry
                        .builder(otel)
                        .build()
                        .newHttpClient(super.getHttpClient())
            }
        apiClient.updateBaseUri(eventDbUrl)
        publisherApi = PublisherApi(apiClient)
    }

    override fun getAllEvents(): Set<Event> = getAllEntries().mapNotNull { it.toEvent().getOrNull() }.toSet()

    override fun getAllEntries(): Set<Entry> {
        try {
            return publisherApi.all()
        } catch (e: ApiException) {
            throw EventDBException("could not reach eventdb: $eventDbUrl", e)
        }
    }
}

class EventDBException(
    msg: String,
    e: ApiException,
) : RuntimeException(msg, e)
