package base.boudicca

import base.boudicca.format.DateFormat
import base.boudicca.format.ListFormat
import base.boudicca.format.NumberFormat
import base.boudicca.model.structured.Key
import base.boudicca.model.structured.VariantConstants
import java.lang.reflect.InvocationTargetException
import java.net.URI
import java.time.OffsetDateTime

/**
 * Properties are helpers allowing you to easier work with getting/setting values in their correct type for events/entries.
 * You can use existing properties, where each property supports a different format variant, like the [TextProperty] or [ListProperty],
 * but you can also easily create support for a new format/type by extending [AbstractProperty] or [Property].
 *
 * All our [SemanticKeys] already have properties defined for them that you can use, but you can create also create new ones on the fly just by calling the constructor like `TextProperty("newProperty")`
 */
interface Property<T> {
    @Throws(IllegalArgumentException::class)
    fun parseToString(value: T): String

    @Throws(IllegalArgumentException::class)
    fun parseFromString(string: String): T

    fun getKey(language: String? = null): Key
}

abstract class AbstractProperty<T>(private val propertyName: String, private val formatValue: String) : Property<T> {
    override fun getKey(language: String?): Key {
        val builder = Key.builder(propertyName)
        if (!language.isNullOrEmpty()) {
            builder.withVariant(VariantConstants.LANGUAGE_VARIANT_NAME, language)
        }
        if (formatValue.isNotEmpty()) {
            builder.withVariant(VariantConstants.FORMAT_VARIANT_NAME, formatValue)
        }
        return builder.build()
    }
}

class TextProperty(propertyName: String) : AbstractProperty<String>(propertyName, VariantConstants.FormatVariantConstants.TEXT_FORMAT_NAME) {
    override fun parseToString(value: String): String {
        return value
    }

    override fun parseFromString(string: String): String {
        return string
    }
}

class MarkdownProperty(propertyName: String) : AbstractProperty<String>(propertyName, VariantConstants.FormatVariantConstants.MARKDOWN_FORMAT_NAME) {
    override fun parseToString(value: String): String {
        return value
    }

    override fun parseFromString(string: String): String {
        return string
    }
}

class UrlProperty(propertyName: String) : AbstractProperty<URI>(propertyName, VariantConstants.FormatVariantConstants.TEXT_FORMAT_NAME) {
    override fun parseToString(value: URI): String {
        return value.toString()
    }

    override fun parseFromString(string: String): URI {
        return URI.create(string)
    }
}

class DateProperty(propertyName: String) : AbstractProperty<OffsetDateTime>(propertyName, VariantConstants.FormatVariantConstants.DATE_FORMAT_NAME) {
    override fun parseToString(value: OffsetDateTime): String {
        return DateFormat.parseToString(value)
    }

    override fun parseFromString(string: String): OffsetDateTime {
        return DateFormat.parseFromString(string)
    }
}

class ListProperty(propertyName: String) : AbstractProperty<List<String>>(propertyName, VariantConstants.FormatVariantConstants.LIST_FORMAT_NAME) {
    override fun parseToString(value: List<String>): String {
        return ListFormat.parseToString(value)
    }

    override fun parseFromString(string: String): List<String> {
        return ListFormat.parseFromString(string)
    }
}

class NumberProperty(propertyName: String) : AbstractProperty<Number>(propertyName, VariantConstants.FormatVariantConstants.NUMBER_FORMAT_NAME) {
    override fun parseToString(value: Number): String {
        return NumberFormat.parseToString(value)
    }

    override fun parseFromString(string: String): Number {
        return NumberFormat.parseFromString(string)
    }
}

class EnumProperty<E : Enum<E>>(propertyName: String, private val enumClass: Class<E>) :
    AbstractProperty<E>(propertyName, VariantConstants.FormatVariantConstants.TEXT_FORMAT_NAME) {
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

class BooleanProperty(propertyName: String) : AbstractProperty<Boolean>(propertyName, VariantConstants.FormatVariantConstants.TEXT_FORMAT_NAME) {
    override fun parseToString(value: Boolean): String {
        return value.toString()
    }

    override fun parseFromString(string: String): Boolean {
        return string.toBoolean()
    }
}
