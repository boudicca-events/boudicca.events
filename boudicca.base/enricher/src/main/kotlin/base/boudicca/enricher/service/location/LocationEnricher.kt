package base.boudicca.enricher.service.location

import base.boudicca.SemanticKeys
import base.boudicca.TextProperty
import base.boudicca.enricher.service.Enricher
import base.boudicca.enricher.service.ForceUpdateEvent
import base.boudicca.model.structured.StructuredEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock


@Service
class LocationEnricher @Autowired constructor(
    private val updater: LocationEnricherUpdater
) : Enricher {

    private val updateLock = ReentrantLock()
    private var data = emptyList<LocationData>()

    override fun enrich(e: StructuredEvent): StructuredEvent {
        for (locationData in data) {
            if (matches(e, locationData)) {
                val builder = e.toBuilder()
                for (locationDatum in locationData) {
                    builder.withProperty(TextProperty(locationDatum.key), locationDatum.value.first())
                }
                return builder.build()
            }
        }
        return e
    }

    private fun matches(event: StructuredEvent, locationData: Map<String, List<String>>): Boolean {
        for (property in listOf(SemanticKeys.LOCATION_NAME_PROPERTY, SemanticKeys.LOCATION_ADDRESS_PROPERTY)) {
            val locationDatumValue = locationData[property.getKey().name]
            if (locationDatumValue != null) {
                val eventData = event.getProperty(property)
                for (eventDatum in eventData) {
                    for (locationDatumLine in locationDatumValue) {
                        if (eventDatum.second == locationDatumLine) {
                            return true
                        }
                    }
                }
            }
        }
        return false
    }

    @EventListener
    fun onEventsUpdate(event: ForceUpdateEvent) {
        updateData()
    }

    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.HOURS)
    fun update() {
        updateData()
    }

    private fun updateData() {
        updateLock.lock()
        try {
            data = updater.updateData()
        } finally {
            updateLock.unlock()
        }
    }
}

interface LocationEnricherUpdater {
    fun updateData(): List<LocationData>
}


typealias LocationData = Map<String, List<String>>
