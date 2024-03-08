package base.boudicca.api.eventcollector.logging

import base.boudicca.api.eventcollector.collections.Collections
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
            currentSingleCollection.logLines.add(String(encoder.encode(event)))
            if (isErrorLevel(event)) {
                currentSingleCollection.errorCount++
            } else if (isWarnLevel(event)) {
                currentSingleCollection.warningCount++
            }
            return getFilterReply()
        } else {
            val currentFullCollection = Collections.getCurrentFullCollection()
            if (currentFullCollection != null) {
                currentFullCollection.logLines.add(String(encoder.encode(event)))
                if (isErrorLevel(event)) {
                    currentFullCollection.errorCount++
                } else if (isWarnLevel(event)) {
                    currentFullCollection.warningCount++
                }
                return getFilterReply()
            }
        }
        return FilterReply.NEUTRAL
    }

    private fun isWarnLevel(event: ILoggingEvent): Boolean {
        return !isErrorLevel(event) && event.level.isGreaterOrEqual(Level.WARN)
    }

    private fun isErrorLevel(event: ILoggingEvent): Boolean {
        return event.level.isGreaterOrEqual(Level.ERROR)
    }

    private fun getFilterReply(): FilterReply {
        return if (alsoLog) {
            FilterReply.NEUTRAL
        } else {
            FilterReply.DENY
        }
    }
}
