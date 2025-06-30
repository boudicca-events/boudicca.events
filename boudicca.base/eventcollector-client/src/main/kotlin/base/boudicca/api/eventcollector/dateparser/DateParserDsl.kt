package base.boudicca.api.eventcollector.dateparser

import base.boudicca.SemanticKeys
import base.boudicca.model.structured.StructuredEvent
import base.boudicca.model.structured.dsl.StructuredEventBuilder
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime

fun structuredEvent(
    name: String,
    dates: DateParserResult,
    init: StructuredEventBuilder.() -> Unit = {}
): List<StructuredEvent> {
    return dates.dates.map {
        val builder = StructuredEventBuilder(name, it.startDate)
        builder.withProperty(SemanticKeys.ENDDATE_PROPERTY, it.endDate)
        builder.init()
        builder.build()
    }
}
