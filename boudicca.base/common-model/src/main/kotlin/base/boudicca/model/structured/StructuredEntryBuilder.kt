package base.boudicca.model.structured

class StructuredEntryBuilder(data: Map<Key, String> = emptyMap()) : AbstractStructuredBuilder<StructuredEntry, StructuredEntryBuilder>(data.toMutableMap()) {
    override fun build(): StructuredEntry {
        return data.toMap()
    }

    fun copy(): StructuredEntryBuilder {
        return StructuredEntryBuilder(data.toMutableMap())
    }
}
