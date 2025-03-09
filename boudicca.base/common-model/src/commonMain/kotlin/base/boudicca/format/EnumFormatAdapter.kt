package base.boudicca.format

import base.boudicca.model.structured.VariantConstants

class EnumFormatAdapter<E : Enum<E>>(private val parseEnumValue: (value: String) -> E) :
    AbstractFormatAdapter<E>(VariantConstants.FormatVariantConstants.ENUM_FORMAT_NAME) {

    override fun convertToString(value: E): String {
        return value.name
    }

    override fun fromString(value: String): E {
        try {
            return parseEnumValue(value)

//            @Suppress("UNCHECKED_CAST") return enumClass.getMethod("valueOf", String::class)
//                .invoke(null, value) as E
        } catch (e: Exception) {
            throw IllegalArgumentException("error getting enum constant", e)
        }
    }
}
