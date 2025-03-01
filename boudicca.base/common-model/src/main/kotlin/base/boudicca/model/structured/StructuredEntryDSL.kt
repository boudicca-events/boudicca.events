package base.boudicca.model.structured

import java.time.OffsetDateTime

fun structuredEvent(name: String, startDate: OffsetDateTime, init: StructuredEventBuilder.() -> Unit = {}): StructuredEvent {
    val builder = StructuredEventBuilder(name, startDate)
    builder.init()
    return builder.build()
}
