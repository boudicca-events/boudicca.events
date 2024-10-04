package base.boudicca.model.structured

/**
 * constants to use with our variants. has values for known formats, and their values
 */
object VariantConstants {
    const val FORMAT_VARIANT_NAME = "format"
    const val LANGUAGE_VARIANT_NAME = "lang"

    const val ANY_VARIANT_SELECTOR = "*"
    const val NO_VARIANT_SELECTOR = ""

    object FormatVariantConstants {
        const val TEXT_FORMAT_NAME = ""
        const val NUMBER_FORMAT_NAME = "number"
        const val DATE_FORMAT_NAME = "date"
        const val LIST_FORMAT_NAME = "list"
        const val MARKDOWN_FORMAT_NAME = "markdown"
    }

    object LanguageVariantConstants {
        const val DEFAULT_LANGUAGE_NAME = ""
    }
}