package base.boudicca.api.eventcollector.util

import base.boudicca.SemanticKeys
import base.boudicca.dateparser.dateparser.DateParserResult
import base.boudicca.model.structured.StructuredEvent
import base.boudicca.model.structured.dsl.StructuredEventBuilder

fun structuredEvent(
    name: String,
    dates: DateParserResult,
    init: StructuredEventBuilder.() -> Unit = {},
): List<StructuredEvent> =
    dates.dates.map {
        val builder = StructuredEventBuilder(name, it.startDate)
        builder.withProperty(SemanticKeys.ENDDATE_PROPERTY, it.endDate)
        builder.init()
        builder.build()
    }
