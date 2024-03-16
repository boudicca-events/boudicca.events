package base.boudicca.api.eventcollector.runner

import base.boudicca.api.eventcollector.Configuration
import base.boudicca.api.eventdb.ingest.EventDbIngestClient
import base.boudicca.model.Event

fun interface RunnerIngestionInterface {

    companion object {
        fun createFromConfiguration(): RunnerIngestionInterface {
            val eventDbUrl = Configuration.getProperty("boudicca.eventdb.url")
            if (eventDbUrl.isNullOrBlank()) {
                throw IllegalStateException("you need to specify the boudicca.eventdb.url property!")
            }
            val userAndPassword = Configuration.getProperty("boudicca.ingest.auth")
                ?: throw IllegalStateException("you need to specify the boudicca.ingest.auth property!")
            val user = userAndPassword.split(":")[0]
            val password = userAndPassword.split(":")[1]
            val eventDb = EventDbIngestClient(eventDbUrl, user, password)
            return BoudiccaRunnerIngestionInterface(eventDb)
        }
    }

    fun ingestEvents(events: List<Event>)
}