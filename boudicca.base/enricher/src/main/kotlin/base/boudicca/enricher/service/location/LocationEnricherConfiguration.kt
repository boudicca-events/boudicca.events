package base.boudicca.enricher.service.location

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class LocationEnricherConfiguration {
    @Bean
    @ConditionalOnProperty(
        "boudicca.enricher.location.googleCredentialsPath",
        "boudicca.enricher.location.spreadsheetId"
    )
    fun googleUpdater(
        @Value("\${boudicca.enricher.location.googleCredentialsPath:}") googleCredentialsPath: String,
        @Value("\${boudicca.enricher.location.spreadsheetId:}") spreadsheetId: String
    ): LocationEnricherUpdater {
        return LocationEnricherGoogleSheetsUpdater(googleCredentialsPath, spreadsheetId)
    }

    @Bean
    @ConditionalOnMissingBean(LocationEnricherUpdater::class)
    fun noopUpdater(): LocationEnricherUpdater {
        return LocationEnricherNoopUpdater()
    }
}
