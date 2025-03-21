package base.boudicca.format

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
            URIParser.parseURI(string)
        } catch (_: IllegalArgumentException) {
            val fixedUrl = tryFixUrl(trimmed)
            URIParser.parseURI(fixedUrl)
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
