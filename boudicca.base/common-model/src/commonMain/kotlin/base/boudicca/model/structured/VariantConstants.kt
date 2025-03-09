package base.boudicca.model.structured

/**
 * constants to use with our variants. has values for known formats, and their values
 */
@Suppress("unused") // Constants are maintained for API completeness and may currently be unused
object VariantConstants {
    const val FORMAT_VARIANT_NAME = "format"
    const val LANGUAGE_VARIANT_NAME = "lang"
    const val SOURCE_VARIANT_NAME = "source"

    const val ANY_VARIANT_SELECTOR = "*"
    const val NO_VARIANT_SELECTOR = ""

    object FormatVariantConstants {
        const val TEXT_FORMAT_NAME = ""
        const val NUMBER_FORMAT_NAME = "number"
        const val BOOLEAN_FORMAT_NAME = "boolean"
        const val DATE_FORMAT_NAME = "date"
        const val LIST_FORMAT_NAME = "list"
        const val MARKDOWN_FORMAT_NAME = "markdown"

        // for now we treat enums and json as plain text
        // TODO discuss in https://github.com/boudicca-events/boudicca.events/issues/662 how to handle this
        const val URI_FORMAT_NAME = ""
        const val ENUM_FORMAT_NAME = ""
        const val JSON_FORMAT_NAME = ""
    }

    object LanguageVariantConstants {
        const val DEFAULT_LANGUAGE_NAME = ""
    }
}
