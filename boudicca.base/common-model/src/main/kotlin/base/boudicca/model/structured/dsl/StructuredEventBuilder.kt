package base.boudicca.model.structured.dsl

import base.boudicca.format.AbstractFormatAdapter
import base.boudicca.format.TextFormatAdapter
import base.boudicca.model.structured.AbstractStructuredBuilder
import base.boudicca.model.structured.Key
import base.boudicca.model.structured.StructuredEvent
import java.time.OffsetDateTime

class StructuredEventBuilder(
    private val name: String,
    private val startDate: OffsetDateTime,
    data: Map<Key, String> = emptyMap(),
) : AbstractStructuredBuilder<StructuredEvent, StructuredEventBuilder>(data.toMutableMap()) {
    fun withTextData(name: String, data: String, init: EntryDataBuilder<String>.() -> Unit = {}) {
        val builder = EntryDataBuilder(name, TextFormatAdapter())
        builder.data(data)
        builder.init()
        val dataEntries = builder.build()

        this.data.putAll(dataEntries)
    }

    fun <T> withData(name: String, format: AbstractFormatAdapter<T>, data: T? = null, init: EntryDataBuilder<T>.() -> Unit = {}) {
        val builder = EntryDataBuilder(name, format)
        if (data != null) {
            builder.data(data)
        }
        builder.init()
        val dataEntries = builder.build()

        this.data.putAll(dataEntries)
    }

    fun withText(name: String, init: EntryDataBuilder<String>.() -> Unit = {}) {
        with(name, TextFormatAdapter(), init)
    }

    fun <T> with(name: String, init: EntryDataBuilder<T>.() -> Unit = {}) {
        val builder = EntryDataBuilder<T>(name)
        builder.init()
        val dataEntries = builder.build()

        this.data.putAll(dataEntries)
    }

    fun <T> with(name: String, defaultFormatAdapter: AbstractFormatAdapter<T>, init: EntryDataBuilder<T>.() -> Unit = {}) {
        val builder = EntryDataBuilder(name, defaultFormatAdapter)
        builder.init()
        val dataEntries = builder.build()

        this.data.putAll(dataEntries)
    }

    override fun build(): StructuredEvent {
        return StructuredEvent(
            name,
            startDate,
            data.toMap(),
        )
    }
}
