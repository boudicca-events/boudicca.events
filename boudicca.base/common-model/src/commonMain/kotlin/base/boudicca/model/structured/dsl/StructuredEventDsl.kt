package base.boudicca.model.structured.dsl

import base.boudicca.format.Date
import base.boudicca.model.structured.StructuredEvent

fun structuredEvent(name: String, startDate: Date, init: StructuredEventBuilder.() -> Unit = {}): StructuredEvent {
    val builder = StructuredEventBuilder(name, startDate)
    builder.init()
    return builder.build()
}

fun modify(event: StructuredEvent, init: StructuredEventBuilder.() -> Unit = {}): StructuredEvent {
    val builder = event.toBuilder()
    builder.init()
    return builder.build()
}
