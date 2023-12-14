package base.boudicca.enricher.service.location

class LocationEnricherNoopUpdater : LocationEnricherUpdater {
    override fun updateData(): List<LocationData> {
        return emptyList()
    }
}
