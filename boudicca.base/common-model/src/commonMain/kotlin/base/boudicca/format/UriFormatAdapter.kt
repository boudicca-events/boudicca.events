package base.boudicca.format

import base.boudicca.model.structured.VariantConstants
import base.boudicca.model.URI

class UriFormatAdapter : AbstractFormatAdapter<URI>(VariantConstants.FormatVariantConstants.URI_FORMAT_NAME) {
    override fun fromString(value: String): URI = URI(value)

    override fun convertToString(value: URI): String = value.toString()
}
