package base.boudicca.model.structured.dsl

import base.boudicca.format.Date
import base.boudicca.model.structured.StructuredEvent
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport
fun structuredEvent(name: String, startDate: Date, init: StructuredEventBuilder.() -> Unit = {}): StructuredEvent {
    val builder = StructuredEventBuilder(name, startDate)
    builder.init()
    return builder.build()
}

@OptIn(ExperimentalJsExport::class)
@JsExport
fun modify(event: StructuredEvent, init: StructuredEventBuilder.() -> Unit = {}): StructuredEvent {
    val builder = event.toBuilder()
    builder.init()
    return builder.build()
}
