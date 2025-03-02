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
    data: Map<Key, String> = emptyMap()
) : AbstractStructuredBuilder<StructuredEvent, StructuredEventBuilder>(data.toMutableMap()) {

    fun withTextData(
        name: String,
        data: String,
        init: DataBuilder<String>.() -> Unit = {}
    ) {
        val builder = DataBuilder(name, TextFormatAdapter())
        builder.data(data)
        builder.init()
        val dataEntries = builder.build()

        this.data.putAll(dataEntries)
    }

    fun <T> withData(
        name: String,
        format: AbstractFormatAdapter<T>,
        data: T? = null,
        init: DataBuilder<T>.() -> Unit = {}
    ) {
        val builder = DataBuilder(name, format)
        if (data != null) {
            builder.data(data)
        }
        builder.init()
        val dataEntries = builder.build()

        this.data.putAll(dataEntries)
    }

    fun withText(
        name: String,
        init: DataBuilder<String>.() -> Unit = {}
    ) {
        with(name, TextFormatAdapter(), init)
    }

    fun <T> with(
        name: String,
        init: DataBuilder<T>.() -> Unit = {}
    ) {
        val builder = DataBuilder<T>(name)
        builder.init()
        val dataEntries = builder.build()

        this.data.putAll(dataEntries)
    }

    fun <T> with(
        name: String,
        defaultFormatAdapter: AbstractFormatAdapter<T>,
        init: DataBuilder<T>.() -> Unit = {}
    ) {
        val builder = DataBuilder(name, defaultFormatAdapter)
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
