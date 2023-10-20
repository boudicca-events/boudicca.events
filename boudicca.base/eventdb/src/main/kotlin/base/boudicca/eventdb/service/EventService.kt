package base.boudicca.eventdb.service

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DatabindException
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import base.boudicca.SemanticKeys
import base.boudicca.eventdb.model.Event
import base.boudicca.eventdb.model.EventKey
import base.boudicca.eventdb.model.InternalEventProperties
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.io.IOException
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantLock
import kotlin.io.path.exists
import kotlin.io.path.readBytes
import kotlin.io.path.writeBytes


@Service
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON) //even if this is the default, we REALLY have to make sure there is only one
class EventService @Autowired constructor(@Value("\${boudicca.store.path}") private val storePath: String) {

    private val LOG = LoggerFactory.getLogger(this::class.java)
    private val events = ConcurrentHashMap<EventKey, Pair<Event, InternalEventProperties>>()
    private val lastSeenCollectors = ConcurrentHashMap<String, Long>()
    private val persistLock = ReentrantLock()
    private val needsPersist = AtomicBoolean(false)
    private val objectMapper = JsonMapper.builder().addModule(JavaTimeModule())
        .addModule(KotlinModule.Builder().build()).build()

    init {
        if (storePath.isNotBlank()) {
            val store = Path.of(storePath)
            if (store.exists()) {
                try {
                    val storeRead = objectMapper.readValue(
                        store.readBytes(),
                        object : TypeReference<List<Pair<Event, InternalEventProperties>>>() {})

                    storeRead.forEach {
                        events[EventKey(it.first)] = it
                    }

                    needsPersist.set(false)
                } catch (e: DatabindException) {
                    LOG.info("store had wrong format, retrying with old format")
                    val storeRead = objectMapper.readValue(
                        store.readBytes(),
                        object : TypeReference<List<Event>>() {})

                    storeRead.forEach {
                        add(it)
                    }

                    needsPersist.set(true) //to write in the new format
                }
            } else {
                LOG.info("did not find store to read from")
            }
        } else {
            LOG.info("no store path set, not reading nor saving anything")
        }
    }

    fun list(): Set<Event> {
        return events.values.map { it.first }.toSet()
    }

    fun add(event: Event) {
        val eventKey = EventKey(event)
        val duplicate = events[eventKey]
        //some cheap logging for finding duplicate events between different collectors
        if (duplicate != null && duplicate.first.data?.get(SemanticKeys.COLLECTORNAME) != event.data?.get(
                SemanticKeys.COLLECTORNAME)) {
            LOG.warn("event $event will overwrite $duplicate")
        }

        events[eventKey] = Pair(event, InternalEventProperties(System.currentTimeMillis()))
        if (event.data?.containsKey(SemanticKeys.COLLECTORNAME) == true) {
            lastSeenCollectors[event.data[SemanticKeys.COLLECTORNAME]!!] = System.currentTimeMillis()
        }
        needsPersist.set(true)
    }

    private val MAX_AGE = Duration.ofDays(3).toMillis()

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.DAYS)
    fun cleanup() {
        val toRemoveEvents = events.values
            .filter {
                if (it.first.data?.containsKey(SemanticKeys.COLLECTORNAME) == true) {
                    val collectorName = it.first.data!![SemanticKeys.COLLECTORNAME]!!
                    it.second.timeAdded + MAX_AGE < (lastSeenCollectors[collectorName] ?: Long.MIN_VALUE)
                } else {
                    false
                }
            }

        toRemoveEvents.forEach {
            LOG.debug("removing event because it got too old: {}", it)
            events.remove(EventKey(it.first))
            needsPersist.set(true)
        }
    }

    @PreDestroy
    fun onStop() {
        persist()
    }

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.MINUTES)
    fun periodicSave() {
        persist()
    }

    init {
        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                persist()
            }
        })
    }

    fun persist() {
        if (storePath.isBlank()) {
            return
        }
        persistLock.lock()
        try {
            if (needsPersist.get()) {
                val bytes = objectMapper.writeValueAsBytes(events.values)
                try {
                    Path.of(storePath)
                        .writeBytes(bytes, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)
                } catch (e: IOException) {
                    LOG.error("error persisting store", e)
                }
                needsPersist.set(false)
            }
        } finally {
            persistLock.unlock()
        }
    }
}
