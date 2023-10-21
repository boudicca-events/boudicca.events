package base.boudicca.enricher.service

import base.boudicca.Event
import base.boudicca.EventCategory
import base.boudicca.SemanticKeys
import org.springframework.stereotype.Service

@Service
class CategoryEnricher : Enricher {

    override fun enrich(e: Event): Event {
        val data = e.data.toMutableMap()
        data[SemanticKeys.CATEGORY] = EventCategory.OTHER.name
        val type = data[SemanticKeys.TYPE]
        if (!type.isNullOrBlank()) {
            val category = EventCategory.getForType(type)
            if (category != null) {
                data[SemanticKeys.CATEGORY] = category.name
            }
        }
        return Event(e.name, e.startDate, data)
    }

}