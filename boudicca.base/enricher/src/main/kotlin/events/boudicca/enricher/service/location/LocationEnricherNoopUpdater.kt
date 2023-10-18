package events.boudicca.enricher.service.location

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

class LocationEnricherNoopUpdater : LocationEnricherUpdater {
    override fun updateData(): List<LocationData> {
        return emptyList()
    }
}

@Configuration
@ConditionalOnMissingBean(LocationEnricherUpdater::class)
class LocationEnricherNoopUpdaterConfiguration {
    @Bean
    fun noopUpdater(): LocationEnricherUpdater {
        return LocationEnricherNoopUpdater()
    }
}