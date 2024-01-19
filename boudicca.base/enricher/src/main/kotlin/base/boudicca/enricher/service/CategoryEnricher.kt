package base.boudicca.enricher.service

import base.boudicca.model.Event
import base.boudicca.model.EventCategory
import base.boudicca.SemanticKeys
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Service

@Service
@Order(0)
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