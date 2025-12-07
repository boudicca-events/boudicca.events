package events.boudicca.enricher.service.location

import base.boudicca.SemanticKeys
import base.boudicca.TextProperty
import base.boudicca.enricher.service.Enricher
import base.boudicca.enricher.service.ForceUpdateEvent
import base.boudicca.model.structured.Key
import base.boudicca.model.structured.StructuredEvent
import events.boudicca.enricher.service.EnricherOrderConstants
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.event.EventListener
import org.springframework.core.annotation.Order
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock


@Service
@Order(EnricherOrderConstants.LocationEnricherOrder)
class LocationEnricher @Autowired constructor(
    private val updater: LocationEnricherUpdater
) : Enricher {

    private val updateLock = ReentrantLock()
    private var data = emptyList<LocationData>()

    override fun enrich(event: StructuredEvent): StructuredEvent {
        for (locationData in data) {
            if (matches(event, locationData)) {
                val builder = event.toBuilder()
                for (locationDatum in locationData) {
                    builder.withProperty(TextProperty(locationDatum.key), locationDatum.value.first())
                }
                return builder.build()
            }
        }
        return event
    }

    private fun matches(event: StructuredEvent, locationData: Map<String, List<String>>): Boolean {
        return listOf(
            SemanticKeys.LOCATION_NAME_PROPERTY,
            SemanticKeys.LOCATION_ADDRESS_PROPERTY
        ).any { property ->
            val location = locationData[property.getKey().name] ?: return@any false

            event.getProperty(property)
                .map(Pair<Key, String>::second)
                .any { eventLocation ->
                eventLocation in location
            }
        }
    }

    @EventListener
    fun onEventsUpdate(ignored: ForceUpdateEvent) {
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

fun interface LocationEnricherUpdater {
    fun updateData(): List<LocationData>
}

typealias LocationData = Map<String, List<String>>
