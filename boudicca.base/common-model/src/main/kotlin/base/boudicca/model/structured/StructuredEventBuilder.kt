package base.boudicca.model.structured

import java.time.OffsetDateTime

class StructuredEventBuilder(
    private val name: String,
    private val startDate: OffsetDateTime,
    data: Map<Key, String> = emptyMap()
) : AbstractStructuredBuilder<StructuredEvent, StructuredEventBuilder>(data.toMutableMap()) {

    fun withData(name: String, init: DataBuilder.() -> Unit) {
        val builder = DataBuilder(name)
        builder.init()
        val dataEntries = builder.build()

        this.data.putAll(dataEntries)
    }

    override fun build(): StructuredEvent {
        return StructuredEvent(
            name,
            startDate,
            data.toMap()
        )
    }
}
