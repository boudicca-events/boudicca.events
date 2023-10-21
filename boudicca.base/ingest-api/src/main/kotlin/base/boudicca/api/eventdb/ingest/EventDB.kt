package base.boudicca.api.eventdb.ingest

import base.boudicca.Event
import events.boudicca.openapi.ApiClient
import events.boudicca.openapi.api.EventIngestionResourceApi
import java.util.*

class EventDB(eventDbUrl: String, user: String, password: String) {

    private val ingestApi: EventIngestionResourceApi

    init {
        if (eventDbUrl.isBlank()) {
            throw IllegalStateException("you need to pass an eventDbUrl!")
        }
        if (user.isBlank()) {
            throw IllegalStateException("you need to pass an user!")
        }
        if (password.isBlank()) {
            throw IllegalStateException("you need to pass an password!")
        }
        val apiClient = ApiClient()
        apiClient.updateBaseUri(eventDbUrl)
        apiClient.setRequestInterceptor {
            it.header(
                "Authorization",
                "Basic " + Base64.getEncoder()
                    .encodeToString(
                        ("$user:$password").encodeToByteArray()
                    )
            )
        }
        ingestApi = EventIngestionResourceApi(apiClient)
    }

    fun ingestEvents(events: List<Event>) {
        for (event in events) {
            ingestApi.ingestAddPost(mapToRemoteEvent(event))
        }
    }

    private fun mapToRemoteEvent(event: Event): events.boudicca.openapi.model.Event {
        return events.boudicca.openapi.model.Event()
            .name(event.name)
            .startDate(event.startDate)
            .data(event.data)
    }
}