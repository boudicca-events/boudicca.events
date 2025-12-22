package base.boudicca.fetcher

import io.github.oshai.kotlinlogging.KLogger

private const val MAX_RETRIES = 5
private const val SLEEP_TIME_BEFORE_RETRY: Long = 1000 * 60 // one minute

fun <T> retry(logger: KLogger, function: () -> T): T = retry(logger, { Thread.sleep(it) }, function)

fun <T> retry(logger: KLogger, sleeper: Sleeper, function: () -> T): T {
    var lastException: Throwable? = null
    repeat(MAX_RETRIES) { _ ->
        try {
            return function()
        } catch (e: Exception) {
            lastException = e
            logger.info(e) { "exception caught, retrying in 1 minute" }
            sleeper.sleep(SLEEP_TIME_BEFORE_RETRY)
        }
    }
    throw lastException ?: FetcherException("failed after $MAX_RETRIES attempts")
}
