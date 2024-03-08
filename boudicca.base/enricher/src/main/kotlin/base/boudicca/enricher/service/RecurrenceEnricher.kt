package base.boudicca.enricher.service

import base.boudicca.SemanticKeys
import base.boudicca.model.Event
import org.springframework.stereotype.Service

@Service
class RecurrenceEnricher : Enricher {

    override fun enrich(events: List<Event>): List<Event> {
        val nameGroups = events.groupBy { it.name }

        return events.map {
            if (groupIsRecurring(nameGroups[it.name]!!)) {
                addRecurrence(it)
            } else {
                it
            }
        }
    }

    private fun groupIsRecurring(group: List<Event>): Boolean {
        //TODO arbitrary number 3, add better detection
        return group.size > 3
    }

    private fun addRecurrence(event: Event): Event {
        //honor existing recurrence value
        if (event.data.containsKey(SemanticKeys.RECURRENCE)) {
            return event
        }
        val data = event.data.toMutableMap()
        data[SemanticKeys.RECURRENCE] = "recurring"
        return Event(event.name, event.startDate, data)
    }

}