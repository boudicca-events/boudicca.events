package base.boudicca.enricher.service

import base.boudicca.SemanticKeys
import base.boudicca.model.EventCategory
import base.boudicca.model.structured.StructuredEvent
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Service

@Service
@Order(EnricherOrderConstants.CategoryEnricherOrder)
class CategoryEnricher : Enricher {

    override fun enrich(event: StructuredEvent): StructuredEvent {
        if (event.getProperty(SemanticKeys.CATEGORY_PROPERTY).isNotEmpty()) {
            //already got category
            return event
        }
        var foundCategory = EventCategory.OTHER
        val type = event.getProperty(SemanticKeys.TYPE_PROPERTY).firstOrNull()
        if (type != null) {
            val category = EventCategory.getForType(type.second)
            if (category != null) {
                foundCategory = category
            }
        }
        return event.toBuilder().withProperty(SemanticKeys.CATEGORY_PROPERTY, foundCategory).build()
    }

}
