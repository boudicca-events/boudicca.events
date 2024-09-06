package base.boudicca.model.structured

import base.boudicca.Property

abstract class AbstractStructuredBuilder<T>(protected val data: MutableMap<Key, String> = mutableMapOf()) {

    @Throws(IllegalArgumentException::class)
    fun <P> withProperty(property: Property<P>, value: P?): AbstractStructuredBuilder<T> {
        return withProperty(property, null, value)
    }

    @Throws(IllegalArgumentException::class)
    fun <P> withProperty(
        property: Property<P>,
        language: String?,
        value: P?
    ): AbstractStructuredBuilder<T> {
        if (value == null) {
            return this
        }
        return withKeyValuePair(
            property.getKey(language),
            property.parseToString(value)
        )
    }

    fun withKeyValuePair(
        key: Key,
        value: String?
    ): AbstractStructuredBuilder<T> {
        if (value.isNullOrEmpty()) {
            return this
        }

        data[key] = value
        return this
    }

    abstract fun build(): T
}