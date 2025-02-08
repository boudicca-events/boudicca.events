package base.boudicca.fetcher

import org.slf4j.Logger

private const val MAX_RETRIES = 5
private const val SLEEP_TIME_BEFORE_RETRY:Long = 1000 * 60 //one minute

fun <T> retry(logger: Logger, function: () -> T): T {
    return retry(logger, { Thread.sleep(it) }, function)
}

fun <T> retry(logger: Logger, sleeper: Sleeper, function: () -> T): T {
    var lastException: Throwable? = null
    repeat(MAX_RETRIES) { _ ->
        @Suppress("detekt.TooGenericExceptionCaught") //it is rethrown if needed
        try {
            return function()
        } catch (e: Exception) {
            lastException = e
            logger.info("exception caught, retrying in 1 minute", e)
            sleeper.sleep(SLEEP_TIME_BEFORE_RETRY)
        }
    }
    throw lastException ?: FetcherException("failed after $MAX_RETRIES attempts")
}
