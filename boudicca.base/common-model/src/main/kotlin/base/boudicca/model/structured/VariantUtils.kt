package base.boudicca.model.structured

fun numberFormat(): Variant {
    return Variant(VariantConstants.FORMAT_VARIANT_NAME, VariantConstants.FormatVariantConstants.NUMBER_FORMAT_NAME)
}

fun dateFormat(): Variant {
    return Variant(VariantConstants.FORMAT_VARIANT_NAME, VariantConstants.FormatVariantConstants.DATE_FORMAT_NAME)
}

fun listFormat(): Variant {
    return Variant(VariantConstants.FORMAT_VARIANT_NAME, VariantConstants.FormatVariantConstants.MARKDOWN_FORMAT_NAME)
}

fun jsonFormat(): Variant {
    return Variant(VariantConstants.FORMAT_VARIANT_NAME, VariantConstants.FormatVariantConstants.MARKDOWN_FORMAT_NAME)
}

fun enumFormat(): Variant {
    return Variant(VariantConstants.FORMAT_VARIANT_NAME, VariantConstants.FormatVariantConstants.MARKDOWN_FORMAT_NAME)
}

fun markdownFormat(): Variant {
    return Variant(VariantConstants.FORMAT_VARIANT_NAME, VariantConstants.FormatVariantConstants.MARKDOWN_FORMAT_NAME)
}

fun lang(value: String) : Variant {
    return Variant(VariantConstants.LANGUAGE_VARIANT_NAME, value)
}

