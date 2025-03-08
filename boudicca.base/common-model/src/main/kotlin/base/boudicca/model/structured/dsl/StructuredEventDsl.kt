package base.boudicca.model.structured.dsl

import base.boudicca.model.structured.StructuredEvent
import java.time.OffsetDateTime

fun structuredEvent(name: String, startDate: OffsetDateTime, init: StructuredEventBuilder.() -> Unit = {}): StructuredEvent {
    val builder = StructuredEventBuilder(name, startDate)
    builder.init()
    return builder.build()
}

fun modify(event: StructuredEvent, init: StructuredEventBuilder.() -> Unit = {}): StructuredEvent {
    val builder = event.toBuilder()
    builder.init()
    return builder.build()
}
