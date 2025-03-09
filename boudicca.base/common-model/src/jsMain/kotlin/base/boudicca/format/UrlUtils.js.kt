package base.boudicca.format

external fun encodeURI(uri: String): String

actual fun String.encodeURL(): String = encodeURI(this)
