package base.boudicca.format

import java.net.URI

/**
 * some utils for working with URIs for properties
 */
object UrlUtils {
    fun parse(
        baseUrl: String,
        string: String?,
    ): URI? {
        if (string.isNullOrEmpty()) {
            return null
        }
        return parse(baseUrl.removeSuffix("/").trim() + "/" + string.removePrefix("/").trim())
    }

    fun parse(string: String?): URI? {
        if (string.isNullOrEmpty()) {
            return null
        }
        val trimmed = string.trim()
        return try {
            URI.create(string)
        } catch (_: IllegalArgumentException) {
            val fixedUrl = tryFixUrl(trimmed)
            URI.create(fixedUrl)
        }
    }

    // invalid urls everywhere -.-
    private fun tryFixUrl(url: String): String =
        url
            .replace("[", "%5B")
            .replace("]", "%5D")
            .replace(" ", "%20")
            .replace("|", "%7C")

    fun String.encodeURL(): String = java.net.URLEncoder.encode(this, "UTF-8")
}
