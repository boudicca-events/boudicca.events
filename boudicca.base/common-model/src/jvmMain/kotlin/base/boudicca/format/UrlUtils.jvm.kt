package base.boudicca.format

actual fun String.encodeURL(): String =
    java.net.URLEncoder.encode(this, "UTF-8")
