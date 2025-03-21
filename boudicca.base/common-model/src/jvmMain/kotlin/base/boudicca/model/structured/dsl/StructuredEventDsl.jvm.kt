package base.boudicca.model.structured.dsl

import base.boudicca.model.structured.StructuredEvent
import base.boudicca.toBoudiccaOffsetDateTime
import java.time.OffsetDateTime

fun structuredEvent(
    name: String,
    startDate: OffsetDateTime,
    init: StructuredEventBuilder.() -> Unit = {}
): StructuredEvent {
    val builder = StructuredEventBuilder(name, startDate.toBoudiccaOffsetDateTime())
    builder.init()
    return builder.build()
}
