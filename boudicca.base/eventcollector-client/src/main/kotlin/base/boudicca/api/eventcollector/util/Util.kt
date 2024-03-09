package base.boudicca.api.eventcollector.util

import org.slf4j.Logger

fun <T> retry(log: Logger, function: () -> T): T {
    return retry(log, { Thread.sleep(it) }, function)
}

fun <T> retry(log: Logger, sleeper: Sleeper, function: () -> T): T {
    var lastException: Throwable? = null
    for (i in 1..5) {
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