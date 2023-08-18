package events.boudicca

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import events.boudicca.model.Event
import events.boudicca.model.EventKey
import events.boudicca.model.InternalEventProperties
import io.quarkus.runtime.ShutdownEvent
import io.quarkus.scheduler.Scheduled
import java.io.IOException
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantLock
import javax.enterprise.event.Observes
import javax.inject.Singleton
import kotlin.io.path.exists
import kotlin.io.path.readBytes
import kotlin.io.path.writeBytes


@Singleton
class EventService {

    private val events = ConcurrentHashMap<EventKey, Pair<Event, InternalEventProperties>>()
    private val lastSeenCollectors = ConcurrentHashMap<String, Long>()
    private val persistLock = ReentrantLock()
    private val needsPersist = AtomicBoolean(false)
    private val storePath = autoDetectStorePath()
    private val objectMapper = JsonMapper.builder().addModule(JavaTimeModule())
        .addModule(KotlinModule.Builder().build()).build()

    init {
        if (storePath.isNotBlank()) {
            val store = Path.of(storePath)
            if (store.exists()) {
                val storeRead = objectMapper.readValue(
                    store.readBytes(),
                    object : TypeReference<List<Event>>() {})
                storeRead.forEach {
                    add(it)
                }
                needsPersist.set(false)
            } else {
                println("did not find store to read from")
            }
        } else {
            println("no store path set, not reading nor saving anything")
        }
    }

    fun list(): Set<Event> {
        return events.values.map { it.first }.toSet()
    }

    fun add(event: Event) {
        val eventKey = EventKey(event)
        val duplicate = events[eventKey]
        //some cheap logging for finding duplicate events between different collectors
        if (duplicate != null && duplicate.first.data?.get(SemanticKeys.COLLECTORNAME) != event.data?.get(SemanticKeys.COLLECTORNAME)) {
            println("event $event will overwrite $duplicate")
        }

        events[eventKey] = Pair(event, InternalEventProperties(System.currentTimeMillis()))
        if (event.data?.containsKey(SemanticKeys.COLLECTORNAME) == true) {
            lastSeenCollectors[event.data[SemanticKeys.COLLECTORNAME]!!] = System.currentTimeMillis()
        }
        needsPersist.set(true)
    }

    private val MAX_AGE = Duration.ofDays(3).toMillis()

    @Scheduled(every = "P1D")
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
            println("removing event because it got too old: $it")
            events.remove(EventKey(it.first))
            needsPersist.set(true)
        }
    }

    fun onStop(@Observes ignored: ShutdownEvent?) {
        persist()
    }

    @Scheduled(every = "PT30s")
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
                val bytes = objectMapper.writeValueAsBytes(events.values.map { it.first })
                try {
                    Path.of(storePath)
                        .writeBytes(bytes, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)
                } catch (e: IOException) {
                    println("error persisting store")
                    e.printStackTrace()
                }
                needsPersist.set(false)
            }
        } finally {
            persistLock.unlock()
        }
    }

    private fun autoDetectStorePath(): String {
        var url = System.getenv("BOUDICCA_STORE_PATH")
        if (url != null && url.isNotBlank()) {
            return url
        }
        url = System.getProperty("boudiccaStorePath")
        if (url != null && url.isNotBlank()) {
            return url
        }
        return ""
    }
}
