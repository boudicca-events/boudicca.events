package base.boudicca.format

import base.boudicca.model.structured.VariantConstants

class MarkdownFormatAdapter: AbstractFormatAdapter<String>(VariantConstants.FormatVariantConstants.MARKDOWN_FORMAT_NAME) {
    override fun fromString(value: String): String = value

    override fun convertToString(value: String): String = value
}
