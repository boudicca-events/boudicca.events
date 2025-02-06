package base.boudicca.search.service

import base.boudicca.api.eventdb.publisher.EventDBException
import base.boudicca.model.Entry
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationEventPublisher
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

@Service
class SynchronizationService @Autowired constructor(
    private val eventPublisher: ApplicationEventPublisher,
    private val eventFetcher: EventFetcher
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    private val updateLock = ReentrantLock()

    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.HOURS)
    fun update() {
        updateEvents()
    }

    private fun updateEvents() {
        updateLock.lock()
        try {
            try {
                val entries = eventFetcher.fetchAllEvents()
                eventPublisher.publishEvent(EntriesUpdatedEvent(entries))
            } catch (e: EventDBException) {
                logger.warn("could not reach eventdb, retrying in 30s", e)
                //if eventdb is currently down, retry in 30 seconds
                //this mainly happens when both are deployed at the same time
                Thread {
                    Thread.sleep(30000)
                    updateEvents()
                }.start()
            }
        } finally {
            updateLock.unlock()
        }
    }
}

data class EntriesUpdatedEvent(val entries: Set<Entry>)
