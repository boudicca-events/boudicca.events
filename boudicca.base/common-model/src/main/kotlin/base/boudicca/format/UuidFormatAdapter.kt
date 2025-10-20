package base.boudicca.format

import base.boudicca.model.structured.VariantConstants
import java.util.*

class UuidFormatAdapter : AbstractFormatAdapter<UUID>(VariantConstants.FormatVariantConstants.UUID_FORMAT_NAME) {
    override fun fromString(value: String): UUID = UUID.fromString(value)

    override fun convertToString(value: UUID): String = value.toString()
}
