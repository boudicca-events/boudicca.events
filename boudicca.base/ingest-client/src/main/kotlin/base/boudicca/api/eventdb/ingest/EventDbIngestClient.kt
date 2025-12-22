package base.boudicca.api.eventdb.ingest

import base.boudicca.eventdb.openapi.api.IngestionApi
import base.boudicca.model.Entry
import base.boudicca.model.Event
import base.boudicca.openapi.ApiClient
import base.boudicca.openapi.ApiException
import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.instrumentation.javahttpclient.JavaHttpClientTelemetry
import java.net.http.HttpClient
import java.util.*

class EventDbIngestClient(private val eventDbUrl: String, user: String, password: String, private val otel: OpenTelemetry = GlobalOpenTelemetry.get()) {
    private val ingestApi: IngestionApi

    init {
        if (eventDbUrl.isBlank()) {
            error("you need to pass an eventDbUrl!")
        }
        if (user.isBlank()) {
            error("you need to pass an user!")
        }
        if (password.isBlank()) {
            error("you need to pass an password!")
        }
        val apiClient =
            object : ApiClient() {
                override fun getHttpClient(): HttpClient? = JavaHttpClientTelemetry
                    .builder(otel)
                    .build()
                    .newHttpClient(super.getHttpClient())
            }
        apiClient.updateBaseUri(eventDbUrl)
        apiClient.setRequestInterceptor {
            it.header(
                "Authorization",
                "Basic " +
                    Base64.getEncoder()
                        .encodeToString(
                            ("$user:$password").encodeToByteArray(),
                        ),
            )
        }
        ingestApi = IngestionApi(apiClient)
    }

    fun ingestEvents(events: List<Event>) {
        ingestEntries(events.map { Event.toEntry(it) })
    }

    fun ingestEntries(entries: List<Entry>) {
        try {
            ingestApi.addEntries(entries)
        } catch (e: ApiException) {
            throw EventDBException("could not reach eventdb: $eventDbUrl", e)
        }
    }
}

class EventDBException(msg: String, e: ApiException) : RuntimeException(msg, e)
