package base.boudicca.api.eventcollector.util

import java.net.URI
import java.net.URISyntaxException

fun String.toUri(): URI = URI(this)

fun String?.tryParseToUriOrNull(): URI? =
    try {
        this?.toUri()
    } catch (_: URISyntaxException) {
        null
    }

/**
 * Should work with any combination of '\n' or '\r' or '\r\n' used for newline
 */
fun String?.splitAtNewline() = this?.replace("\r", "\n")?.split("\n")?.filter { it.isNotBlank() }
