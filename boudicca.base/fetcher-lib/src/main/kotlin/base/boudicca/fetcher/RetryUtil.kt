package base.boudicca.fetcher

import org.slf4j.Logger

fun <T> retry(logger: Logger, function: () -> T): T {
    return retry(logger, { Thread.sleep(it) }, function)
}

fun <T> retry(logger: Logger, sleeper: Sleeper, function: () -> T): T {
    var lastException: Throwable? = null
    (1..5).forEach { _ ->
        try {
            return function()
        } catch (e: Exception) {
            lastException = e
            logger.info("exception caught, retrying in 1 minute", e)
            sleeper.sleep(1000 * 60)
        }
    }
    throw lastException!!
}
