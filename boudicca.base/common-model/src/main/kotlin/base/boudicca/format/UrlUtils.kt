package base.boudicca.format

import java.net.URI

object UrlUtils {
    fun parse(string: String?): URI? {
        if (string.isNullOrEmpty()) {
            return null
        }
        val trimmed = string.trim()
        return try {
            URI.create(string)
        } catch (e: IllegalArgumentException) {
            val fixedUrl = tryFixUrl(trimmed)
            URI.create(fixedUrl)
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
