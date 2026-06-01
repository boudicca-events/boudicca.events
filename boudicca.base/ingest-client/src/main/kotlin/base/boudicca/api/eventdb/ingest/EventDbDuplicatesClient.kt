package base.boudicca.api.eventdb.ingest

import base.boudicca.eventdb.openapi.api.DuplicatesApi
import base.boudicca.openapi.ApiClient
import base.boudicca.openapi.ApiException
import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.instrumentation.javahttpclient.JavaHttpClientTelemetry
import java.net.http.HttpClient
import java.util.*

class EventDbDuplicatesClient(
    private val eventDbUrl: String,
    user: String,
    password: String,
    private val otel: OpenTelemetry = GlobalOpenTelemetry.get(),
) {
    private val duplicatesApi: DuplicatesApi

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
                override fun getHttpClient(): HttpClient? =
                    JavaHttpClientTelemetry
                        .builder(otel)
                        .build()
                        .wrap(super.getHttpClient())
            }
        apiClient.updateBaseUri(eventDbUrl)
        apiClient.setRequestInterceptor {
            it.header(
                "Authorization",
                "Basic " +
                    Base64
                        .getEncoder()
                        .encodeToString(
                            ("$user:$password").encodeToByteArray(),
                        ),
            )
        }
        duplicatesApi = DuplicatesApi(apiClient)
    }

    fun markDuplicates(duplicateIds: List<UUID>) {
        try {
            duplicatesApi.markDuplicates(duplicateIds)
        } catch (e: ApiException) {
            throw EventDBException("could not reach eventdb: $eventDbUrl", e)
        }
    }
}
