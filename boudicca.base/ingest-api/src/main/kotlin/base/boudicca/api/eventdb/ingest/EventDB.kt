package base.boudicca.api.eventdb.ingest

import base.boudicca.Entry
import base.boudicca.Event
import events.boudicca.openapi.ApiClient
import events.boudicca.openapi.api.IngestionResourceApi
import java.util.*

class EventDB(eventDbUrl: String, user: String, password: String) {

    private val ingestApi: IngestionResourceApi

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
        ingestApi = IngestionResourceApi(apiClient)
    }

    fun ingestEvents(events: List<Event>) {
        ingestEntries(events.map { Event.toEntry(it) })
    }

    fun ingestEntries(entries: List<Entry>) {
        ingestApi.addEntries(entries)
    }

}