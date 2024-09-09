package base.boudicca.model.structured

import base.boudicca.Property

abstract class AbstractStructuredBuilder<T, B : AbstractStructuredBuilder<T, B>>(protected val data: MutableMap<Key, String> = mutableMapOf()) {

    @Throws(IllegalArgumentException::class)
    fun <P> withProperty(property: Property<P>, value: P?): B {
        return withProperty(property, null, value)
    }

    @Throws(IllegalArgumentException::class)
    fun <P> withProperty(
        property: Property<P>,
        language: String?,
        value: P?
    ): B {
        if (value == null) {
            @Suppress("UNCHECKED_CAST")
            return this as B
        }
        return withKeyValuePair(
            property.getKey(language),
            property.parseToString(value)
        )
    }

    fun withKeyValuePair(
        key: Key,
        value: String?
    ): B {
        if (value.isNullOrEmpty()) {
            @Suppress("UNCHECKED_CAST")
            return this as B
        }

        data[key] = value
        @Suppress("UNCHECKED_CAST")
        return this as B
    }

    abstract fun build(): T
}