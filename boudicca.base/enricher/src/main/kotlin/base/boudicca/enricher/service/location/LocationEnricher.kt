package base.boudicca.enricher.service.location

import base.boudicca.model.Event
import base.boudicca.SemanticKeys
import base.boudicca.enricher.service.Enricher
import base.boudicca.enricher.service.ForceUpdateEvent
import org.slf4j.LoggerFactory
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

    override fun enrich(e: Event): Event {
        for (locationData in data) {
            if (matches(e.data, locationData)) {
                val enrichedData = e.data.toMutableMap()
                for (locationDatum in locationData) {
                    enrichedData[locationDatum.key] = locationDatum.value.first()
                }
                return Event(e.name, e.startDate, enrichedData)
            }
        }
        return e
    }

    private fun matches(eventData: Map<String, String>, locationData: Map<String, List<String>>): Boolean {
        for (locationDatumKey in listOf(SemanticKeys.LOCATION_NAME, SemanticKeys.LOCATION_ADDRESS)) {
            val locationDatumValue = locationData[locationDatumKey]
            if (locationDatumValue != null) {
                val eventDatum = eventData[locationDatumKey]
                if (eventDatum != null) {
                    for (locationDatumLine in locationDatumValue) {
                        if (eventDatum == locationDatumLine) {
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
