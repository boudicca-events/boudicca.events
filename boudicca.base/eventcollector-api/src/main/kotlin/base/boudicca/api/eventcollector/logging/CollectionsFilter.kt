package base.boudicca.api.eventcollector.logging

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.encoder.Encoder
import ch.qos.logback.core.filter.Filter
import ch.qos.logback.core.spi.FilterReply
import base.boudicca.api.eventcollector.collections.Collections

class CollectionsFilter(private val encoder: Encoder<ILoggingEvent>) : Filter<ILoggingEvent>() {
    override fun decide(event: ILoggingEvent): FilterReply {
        val currentSingleCollection = Collections.getCurrentSingleCollection()
        if (currentSingleCollection != null) {
            currentSingleCollection.logLines.add(Pair(event.level.isGreaterOrEqual(Level.WARN), encoder.encode(event)))
            return FilterReply.DENY
        } else {
            val currentFullCollection = Collections.getCurrentFullCollection()
            if (currentFullCollection != null) {
                currentFullCollection.logLines.add(
                    Pair(
                        event.level.isGreaterOrEqual(Level.WARN),
                        encoder.encode(event)
                    )
                )
                return FilterReply.DENY
            }
        }
        return FilterReply.NEUTRAL
    }
}
