package base.boudicca.format

import base.boudicca.model.URI

/**
 * some utils for working with URIs for properties
 */
object UrlUtils {
    fun parse(string: String?): URI? {
        if (string.isNullOrEmpty()) {
            return null
        }
        val trimmed = string.trim()
        return try {
            URI(string)
        } catch (_: IllegalArgumentException) {
            val fixedUrl = tryFixUrl(trimmed)
            URI(fixedUrl)
        }
    }

    //invalid urls everywhere -.-
    private fun tryFixUrl(url: String): String {
        return url
            .replace("[", "%5B")
            .replace("]", "%5D")
            .replace(" ", "%20")
    }
}

expect fun String.encodeURL(): String
