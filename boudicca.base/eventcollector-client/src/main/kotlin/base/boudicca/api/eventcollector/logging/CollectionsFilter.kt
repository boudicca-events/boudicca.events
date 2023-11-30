package base.boudicca.api.eventcollector.logging

import base.boudicca.api.eventcollector.collections.Collections
import base.boudicca.api.eventcollector.collections.LogLevel
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.encoder.Encoder
import ch.qos.logback.core.filter.Filter
import ch.qos.logback.core.spi.FilterReply

class CollectionsFilter(private val encoder: Encoder<ILoggingEvent>) : Filter<ILoggingEvent>() {
    companion object {
        @Volatile
        var alsoLog: Boolean = false
    }

    override fun decide(event: ILoggingEvent): FilterReply {
        val currentSingleCollection = Collections.getCurrentSingleCollection()
        if (currentSingleCollection != null) {
            currentSingleCollection.logLines.add(Pair(translateLogLevel(event), encoder.encode(event)))
            return getFilterReply()
        } else {
            val currentFullCollection = Collections.getCurrentFullCollection()
            if (currentFullCollection != null) {
                currentFullCollection.logLines.add(
                    Pair(
                        translateLogLevel(event),
                        encoder.encode(event)
                    )
                )
                return getFilterReply()
            }
        }
        return FilterReply.NEUTRAL
    }

    private fun translateLogLevel(event: ILoggingEvent): LogLevel {
        return if (event.level.isGreaterOrEqual(Level.ERROR)) {
            LogLevel.ERROR
        } else if (event.level.isGreaterOrEqual(Level.WARN)) {
            LogLevel.WARNING
        } else {
            LogLevel.INFO
        }
    }

    private fun getFilterReply(): FilterReply {
        return if (alsoLog) {
            FilterReply.NEUTRAL
        } else {
            FilterReply.DENY
        }
    }
}
