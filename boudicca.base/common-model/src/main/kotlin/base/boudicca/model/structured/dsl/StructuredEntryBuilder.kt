package base.boudicca.model.structured.dsl

import base.boudicca.model.structured.AbstractStructuredBuilder
import base.boudicca.model.structured.Key
import base.boudicca.model.structured.StructuredEntry

class StructuredEntryBuilder(
    data: Map<Key, String> = emptyMap(),
) : AbstractStructuredBuilder<StructuredEntry, StructuredEntryBuilder>(data.toMutableMap()) {
    override fun build(): StructuredEntry = data.toMap()

    fun copy(): StructuredEntryBuilder = StructuredEntryBuilder(data.toMutableMap())
}
