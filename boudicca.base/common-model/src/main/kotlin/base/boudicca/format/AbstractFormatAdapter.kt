package base.boudicca.format

import base.boudicca.model.structured.Variant
import base.boudicca.model.structured.VariantConstants

abstract class AbstractFormatAdapter<T>(val formatVariantValue: String) {
    abstract fun fromString(value: String): T

    abstract fun convertToString(value: T): String

    val variant = Variant(VariantConstants.FORMAT_VARIANT_NAME, formatVariantValue)
}
