package base.boudicca.api.eventcollector.util

import org.slf4j.Logger
import java.net.URI
import java.net.URISyntaxException

fun <T> retry(log: Logger, function: () -> T): T {
    return retry(log, { Thread.sleep(it) }, function)
}

fun <T> retry(log: Logger, sleeper: Sleeper, function: () -> T): T {
    var lastException: Throwable? = null
    (1..5).forEach { _ ->
        try {
            return function()
        } catch (e: Exception) {
            lastException = e
            log.info("exception caught, retrying in 1 minute", e)
            sleeper.sleep(1000 * 60)
        }
    }
    throw lastException!!
}

fun String.toUri(): URI = URI(this)

fun String?.tryParseToUriOrNull(): URI? = try {
    this?.toUri()
} catch (_: URISyntaxException) {
    null
}

/**
 * Should work with any combination of '\n' or '\r' or '\r\n' used for newline
 */
fun String?.splitAtNewline() = this?.replace("\r", "\n")?.split("\n")?.filter { it.isNotBlank() }
