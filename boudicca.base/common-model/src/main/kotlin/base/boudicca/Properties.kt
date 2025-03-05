package base.boudicca

import base.boudicca.format.*
import base.boudicca.model.structured.Key
import base.boudicca.model.structured.VariantConstants
import java.lang.reflect.InvocationTargetException
import java.net.URI
import java.time.OffsetDateTime

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
    fun getKeyFilter(language: String? = null): Key
}

// TODO: can we somehow streamline this? right now we have markdown and url using the string adapter, which is bad
abstract class AbstractProperty<T>(
    private val propertyName: String,
    private val adapter: AbstractFormatAdapter<String>
) :
    Property<T> {
    override fun getKey(language: String?): Key {
        return internalGetKey(false, language)
    }

    override fun getKeyFilter(language: String?): Key {
        return internalGetKey(true, language)
    }

    private fun internalGetKey(alwaysIncludeFormat: Boolean, language: String?): Key {
        val builder = Key.builder(propertyName)
        if (!language.isNullOrEmpty()) {
            builder.withVariant(VariantConstants.LANGUAGE_VARIANT_NAME, language)
        }
        if (adapter.formatVariantValue.isNotEmpty()) {
            builder.withVariant(VariantConstants.FORMAT_VARIANT_NAME, adapter.formatVariantValue)
        }
        return builder.build()
    }
}

open class GenericProperty<T>(private val propertyName: String, private val adapter: AbstractFormatAdapter<T>) :
    Property<T> {
    override fun parseToString(value: T): String = adapter.convertToString(value)

    override fun parseFromString(string: String): T = adapter.fromString(string)

    override fun getKey(language: String?): Key {
        return internalGetKey(false, language)
    }

    override fun getKeyFilter(language: String?): Key {
        return internalGetKey(true, language)
    }

    private fun internalGetKey(alwaysIncludeFormat: Boolean, language: String?): Key {
        val builder = Key.builder(propertyName)
        if (!language.isNullOrEmpty()) {
            builder.withVariant(VariantConstants.LANGUAGE_VARIANT_NAME, language)
        }
        if (adapter.formatVariantValue.isNotEmpty()) {
            builder.withVariant(VariantConstants.FORMAT_VARIANT_NAME, adapter.formatVariantValue)
        }
        return builder.build()
    }
}

class TextProperty(propertyName: String) : GenericProperty<String>(propertyName, TextFormatAdapter())

class MarkdownProperty(propertyName: String) : GenericProperty<String>(propertyName, MarkdownFormatAdapter())

class UrlProperty(propertyName: String) :
    AbstractProperty<URI>(propertyName, TextFormatAdapter(VariantConstants.FormatVariantConstants.URL_FORMAT_NAME)) {
    override fun parseToString(value: URI): String {
        return value.toString()
    }

    override fun parseFromString(string: String): URI {
        return URI.create(string)
    }
}

class DateProperty(propertyName: String) : GenericProperty<OffsetDateTime>(propertyName, DateFormatAdapter())

class ListProperty(propertyName: String) : GenericProperty<List<String>>(propertyName, ListFormatAdapter())

class NumberProperty(propertyName: String) : GenericProperty<Number>(propertyName, NumberFormatAdapter())

class EnumProperty<E : Enum<E>>(propertyName: String, private val enumClass: Class<E>) :
    AbstractProperty<E>(propertyName, TextFormatAdapter(VariantConstants.FormatVariantConstants.ENUM_FORMAT_NAME)) {
    override fun parseToString(value: E): String {
        return value.name
    }

    override fun parseFromString(string: String): E {
        try {
            @Suppress("UNCHECKED_CAST")
            return enumClass.getMethod("valueOf", String::class.java).invoke(null, string.uppercase()) as E
        } catch (e: InvocationTargetException) {
            throw IllegalArgumentException("error getting enum constant", e)
        }
    }
}

class BooleanProperty(propertyName: String) : AbstractProperty<Boolean>(
    propertyName,
    TextFormatAdapter(VariantConstants.FormatVariantConstants.BOOLEAN_FORMAT_NAME)
) {
    override fun parseToString(value: Boolean): String {
        return value.toString()
    }

    override fun parseFromString(string: String): Boolean {
        return string.toBoolean()
    }
}
