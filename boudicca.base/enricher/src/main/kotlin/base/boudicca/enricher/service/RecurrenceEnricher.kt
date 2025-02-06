package base.boudicca.enricher.service

import base.boudicca.SemanticKeys
import base.boudicca.model.RecurrenceType
import base.boudicca.model.structured.StructuredEvent
import org.springframework.stereotype.Service

@Service
class RecurrenceEnricher : Enricher {

    override fun enrich(events: List<StructuredEvent>): List<StructuredEvent> {
        val nameGroups = events.groupBy { it.name }

        return events.map {
            if (groupIsRecurring(nameGroups[it.name]!!)) {
                addRecurrence(it)
            } else {
                it
            }
        }
    }

    private fun groupIsRecurring(group: List<StructuredEvent>): Boolean {
        //TODO arbitrary number 3, add better detection
        return group.size > 3
    }

    private fun addRecurrence(event: StructuredEvent): StructuredEvent {
        //honor existing recurrence value
        if (event.getProperty(SemanticKeys.RECURRENCE_TYPE_PROPERTY).isNotEmpty()) {
            return event
        }
        return event.toBuilder().withProperty(SemanticKeys.RECURRENCE_TYPE_PROPERTY, RecurrenceType.REGULARLY).build()
    }

}
