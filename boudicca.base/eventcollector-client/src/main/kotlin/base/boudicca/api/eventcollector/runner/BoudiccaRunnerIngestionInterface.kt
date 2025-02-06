package base.boudicca.api.eventcollector.runner

import base.boudicca.api.eventdb.ingest.EventDbIngestClient
import base.boudicca.model.Event

class BoudiccaRunnerIngestionInterface(private val ingestClient: EventDbIngestClient) : RunnerIngestionInterface {
    override fun ingestEvents(events: List<Event>) {
        ingestClient.ingestEvents(events)
    }
}
