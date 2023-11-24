package base.boudicca.enricher.service

import base.boudicca.model.Event
import org.springframework.stereotype.Service

@Service
class MusicBrainzArtistEnricher : Enricher {

    override fun enrich(e: Event): Event {
        return e //TODO
    }

}