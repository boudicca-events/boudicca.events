package base.boudicca.format

import base.boudicca.model.structured.VariantConstants
import java.lang.reflect.InvocationTargetException

class EnumFormatAdapter<E : Enum<E>>(private val enumClass: Class<E>) :
    AbstractFormatAdapter<E>(VariantConstants.FormatVariantConstants.ENUM_FORMAT_NAME) {
    override fun convertToString(value: E): String {
        return value.name
    }

    override fun fromString(value: String): E {
        try {
            @Suppress("UNCHECKED_CAST")
            return enumClass.getMethod("valueOf", String::class.java)
                .invoke(null, value) as E
        } catch (e: InvocationTargetException) {
            throw IllegalArgumentException("error getting enum constant", e)
        }
    }
}
