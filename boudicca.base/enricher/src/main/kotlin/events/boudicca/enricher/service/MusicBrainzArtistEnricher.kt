package events.boudicca.enricher.service

import events.boudicca.enricher.model.Event
import org.springframework.stereotype.Service

@Service
class MusicBrainzArtistEnricher : Enricher {

    override fun enrich(e: Event): Event {
        return e //TODO
    }

}