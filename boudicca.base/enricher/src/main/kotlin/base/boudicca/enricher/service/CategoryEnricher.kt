package base.boudicca.enricher.service

import base.boudicca.SemanticKeys
import base.boudicca.model.EventCategory
import base.boudicca.model.structured.StructuredEvent
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Service

@Service
@Order(0)
class CategoryEnricher : Enricher {

    override fun enrich(e: StructuredEvent): StructuredEvent {
        if (e.getProperty(SemanticKeys.CATEGORY_PROPERTY).isNotEmpty()) {
            //already got category
            return e
        }
        var foundCategory = EventCategory.OTHER
        val type = e.getProperty(SemanticKeys.TYPE_PROPERTY).firstOrNull()
        if (type != null) {
            val category = EventCategory.getForType(type.second)
            if (category != null) {
                foundCategory = category
            }
        }
        return e.toBuilder().withProperty(SemanticKeys.CATEGORY_PROPERTY, foundCategory).build()
    }

}
