package base.boudicca

import base.boudicca.format.*
import base.boudicca.format.URI
import base.boudicca.model.structured.*
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * Properties are helpers allowing you to easier work with getting/setting values in their correct type for events/entries.
 * You can use existing properties, where each property supports a different format variant, like the [TextProperty] or [ListProperty],
 * but you can also easily create support for a new format/type by extending [GenericProperty] or [Property].
 *
 * All our [SemanticKeys] already have properties defined for them that you can use, but you can create also create new ones on the fly just by calling the constructor like `TextProperty("newProperty")`
 */
interface Property<T> {
    fun parseToString(value: T): String

    fun parseFromString(string: String): T

    fun getKey(language: String? = null): Key
    fun getKeyFilter(language: String? = null): KeyFilter
}

abstract class AbstractProperty<T>(
    private val propertyName: String, private val adapter: AbstractFormatAdapter<T>
) : Property<T> {
    override fun getKey(language: String?): Key {
        return internalGetKey(false, language) {
            Key.builder(it)
        }
    }

    override fun getKeyFilter(language: String?): KeyFilter {
        return internalGetKey(true, language) {
            KeyFilter.builder(it)
        }
    }

    private fun <T : AbstractKey<T>> internalGetKey(
        alwaysIncludeFormat: Boolean,
        language: String?,
        builderFunction: (String) -> AbstractKeyBuilder<T>
    ): T {
        val builder = builderFunction(propertyName)
        if (!language.isNullOrEmpty()) {
            builder.withVariant(VariantConstants.LANGUAGE_VARIANT_NAME, language)
        }
        if (alwaysIncludeFormat || adapter.formatVariantValue.isNotEmpty()) {
            builder.withVariant(VariantConstants.FORMAT_VARIANT_NAME, adapter.formatVariantValue)
        }
        return builder.build()
    }
}

open class GenericProperty<T>(propertyName: String, private val adapter: AbstractFormatAdapter<T>) :
    AbstractProperty<T>(propertyName, adapter) {
    override fun parseToString(value: T): String = adapter.convertToString(value)

    override fun parseFromString(string: String): T = adapter.fromString(string)
}

@OptIn(ExperimentalJsExport::class)
@JsExport
class TextProperty(propertyName: String) : GenericProperty<String>(propertyName, TextFormatAdapter())

@OptIn(ExperimentalJsExport::class)
@JsExport
class MarkdownProperty(propertyName: String) : GenericProperty<String>(propertyName, MarkdownFormatAdapter())

@OptIn(ExperimentalJsExport::class)
@JsExport
class UriProperty(propertyName: String) : GenericProperty<URI>(propertyName, UriFormatAdapter())

@OptIn(ExperimentalJsExport::class)
@JsExport
class DateProperty(propertyName: String) : GenericProperty<Date>(propertyName, DateFormatAdapter())

@OptIn(ExperimentalJsExport::class)
@JsExport
class ListProperty(propertyName: String) : GenericProperty<List<String>>(propertyName, ListFormatAdapter())

@OptIn(ExperimentalJsExport::class)
@JsExport
class NumberProperty(propertyName: String) : GenericProperty<Number>(propertyName, NumberFormatAdapter())

@OptIn(ExperimentalJsExport::class)
@JsExport
class EnumProperty<E : Enum<E>>(propertyName: String, toEnum: (value: String) -> E) :
    GenericProperty<E>(propertyName, EnumFormatAdapter(toEnum))

@OptIn(ExperimentalJsExport::class)
@JsExport
class BooleanProperty(propertyName: String) : GenericProperty<Boolean>(propertyName, BooleanFormatAdapter())
