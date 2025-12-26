package base.boudicca.model.structured

import base.boudicca.Property
import base.boudicca.format.UrlUtils
import java.net.URI

abstract class AbstractStructuredBuilder<T, B : AbstractStructuredBuilder<T, B>>(
    protected val data: MutableMap<Key, String> = mutableMapOf(),
) {

    fun withProperty(
        property: Property<List<String>>,
        vararg value: String,
        variants: List<Variant> = emptyList(),
    ): B {
        return withProperty(property, listOf(*value), variants)
    }

    fun withProperty(
        property: Property<URI>,
        value: String?,
        variants: List<Variant> = emptyList(),
    ): B {
        return withProperty(property, UrlUtils.parse(value), variants)
    }

    fun <P> withProperty(
        property: Property<P>,
        value: P?,
        variants: List<Variant> = emptyList(),
    ): B {
        if (value == null) {
            @Suppress("UNCHECKED_CAST")
            return this as B
        }
        return withKeyValuePair(
            property
                .getKey()
                .toBuilder()
                .withVariants(variants)
                .build(),
            property.parseToString(value),
        )
    }

    fun withKeyValuePair(
        key: Key,
        value: String?,
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
