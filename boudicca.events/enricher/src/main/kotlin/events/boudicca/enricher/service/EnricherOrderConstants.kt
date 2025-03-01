package events.boudicca.enricher.service

object EnricherOrderConstants {
    const val TypeEnricherOrder = -1  //should run before CategoryEnricher
    const val CategoryEnricherOrder = 0
    const val LocationEnricherOrder = 10
    const val OsmEnricherOrder = 11 // runs after LocationEnricher (who might add OSM_IDs)
}
