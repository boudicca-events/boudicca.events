package base.boudicca.search.service

import base.boudicca.api.eventdb.publisher.EventDBException
import base.boudicca.model.Entry
import io.github.oshai.kotlinlogging.KotlinLogging
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.trace.SpanKind
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationEventPublisher
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

private const val DEFAULT_RETRY_TIME_MILLIS = 30000L

@Service
class SynchronizationService
    @Autowired
    constructor(
        private val eventPublisher: ApplicationEventPublisher,
        private val eventFetcher: EventFetcher,
        private val otel: OpenTelemetry,
    ) {
        private val logger = KotlinLogging.logger {}

        private val updateLock = ReentrantLock()

        @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.HOURS)
        fun update() {
            updateEvents()
        }

        private fun updateEvents() {
            val span =
                otel
                    .getTracer("SynchronizationService")
                    .spanBuilder("synchronize entries")
                    .setSpanKind(SpanKind.CLIENT)
                    .startSpan()
            try {
                span.makeCurrent().use {
                    updateLock.lock()
                    try {
                        try {
                            val entries = eventFetcher.fetchAllEvents()
                            eventPublisher.publishEvent(EntriesUpdatedEvent(entries))
                        } catch (e: EventDBException) {
                            logger.warn(e) { "could not reach eventdb, retrying in 30s" }
                            // if eventdb is currently down, retry in 30 seconds
                            // this mainly happens when both are deployed at the same time
                            Thread {
                                Thread.sleep(DEFAULT_RETRY_TIME_MILLIS)
                                updateEvents()
                            }.start()
                        }
                    } finally {
                        updateLock.unlock()
                    }
                }
            } finally {
                span.end()
            }
        }
    }

data class EntriesUpdatedEvent(
    val entries: Set<Entry>,
)
