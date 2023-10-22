package base.boudicca.search.service

import base.boudicca.Entry
import base.boudicca.api.eventdb.publisher.EventDB
import base.boudicca.api.eventdb.publisher.EventDBException
import base.boudicca.search.BoudiccaSearchProperties
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
    private val boudiccaSearchProperties: BoudiccaSearchProperties
) {

    private val LOG = LoggerFactory.getLogger(this.javaClass)

    private val publisherApi: EventDB = createEventPublisherApi()
    private val updateLock = ReentrantLock()

    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.HOURS)
    fun update() {
        updateEvents()
    }

    private fun updateEvents() {
        updateLock.lock()
        try {
            try {
                val entries = publisherApi.getAllEntries().toSet()
                eventPublisher.publishEvent(EntriesUpdatedEvent(entries))
            } catch (e: EventDBException) {
                LOG.warn("could not reach eventdb, retrying in 30s", e)
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

    private fun createEventPublisherApi(): EventDB {
        return EventDB(boudiccaSearchProperties.eventDB.url)
    }
}

data class EntriesUpdatedEvent(val entries: Set<Entry>)