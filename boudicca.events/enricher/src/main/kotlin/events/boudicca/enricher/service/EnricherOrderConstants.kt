package events.boudicca.enricher.service

object EnricherOrderConstants {
    const val TYPE_ENRICHER_ORDER = -1 // should run before CategoryEnricher
    const val CATEGORY_ENRICHER_ORDER = 0
    const val LOCATION_ENRICHER_ORDER = 10
    const val OSM_ENRICHER_ORDER = 11 // runs after LocationEnricher (who might add OSM_IDs)
}
