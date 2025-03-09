package base.boudicca.eventdb.service

import base.boudicca.SemanticKeys
import base.boudicca.eventdb.BoudiccaEventDbProperties
import base.boudicca.eventdb.model.EntryKey
import base.boudicca.eventdb.model.InternalEventProperties
import base.boudicca.model.Entry
import base.boudicca.model.Event
import base.boudicca.model.structured.*
import base.boudicca.model.toStructuredEntry
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DatabindException
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PreDestroy
import org.springframework.beans.factory.annotation.Autowired
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
class EntryService @Autowired constructor(
    private val boudiccaEventDbProperties: BoudiccaEventDbProperties
) {

    private val logger = KotlinLogging.logger {}
    private val entries = ConcurrentHashMap<EntryKey, Pair<Entry, InternalEventProperties>>()
    private val lastSeenCollectors = ConcurrentHashMap<String, Long>()
    private val persistLock = ReentrantLock()
    private val needsPersist = AtomicBoolean(false)
    private val objectMapper = JsonMapper.builder().addModule(JavaTimeModule())
        .addModule(KotlinModule.Builder().build()).build()

    init {
        if (!boudiccaEventDbProperties.store.path.isNullOrBlank()) {
            val store = Path.of(boudiccaEventDbProperties.store.path)
            if (store.exists()) {
                try {
                    loadStoreV3(store)
                } catch (e: DatabindException) {
                    logger.info { "store had wrong format, retrying with old format v2" }
                    try {
                        loadStoreV2(store)
                    } catch (e: DatabindException) {
                        logger.info { "store had wrong format, retrying with old format v1" }
                        loadStoreV1(store)
                    }
                }
            } else {
                logger.info { "did not find store to read from" }
            }
        } else {
            logger.info { "no store path set, not reading nor saving anything" }
        }
    }

    private fun loadStoreV1(store: Path) {
        val storeRead = objectMapper.readValue(
            store.readBytes(),
            object : TypeReference<List<Event>>() {})

        storeRead.forEach {
            add(Event.toEntry(it))
        }

        needsPersist.set(true) //to write in the new format
    }

    private fun loadStoreV2(store: Path) {
        val storeRead = objectMapper.readValue(
            store.readBytes(),
            object : TypeReference<List<Pair<Event, InternalEventProperties>>>() {})

        storeRead.forEach {
            val entry = Event.toEntry(it.first)
            entries[getEntryKey(entry.toStructuredEntry())] = Pair(entry, it.second)
        }

        needsPersist.set(true) //to write in the new format
    }

    private fun loadStoreV3(store: Path) {
        val storeRead = objectMapper.readValue(
            store.readBytes(),
            object : TypeReference<List<Pair<Entry, InternalEventProperties>>>() {})

        storeRead.forEach {
            entries[getEntryKey(it.first.toStructuredEntry())] = it
        }

        needsPersist.set(false)
    }

    fun all(): Set<Entry> {
        return entries.values.map { it.first }.toSet()
    }

    fun add(entry: Entry) {
        val structuredEntry = entry.toStructuredEntry()

        val eventKey = getEntryKey(structuredEntry)

        //TODO should we do format validation here?
        //we reflatten the entry to make sure keys are canonical
        entries[eventKey] = Pair(structuredEntry.toFlatEntry(), InternalEventProperties(System.currentTimeMillis()))

        val collectorName = structuredEntry.getProperty(SemanticKeys.COLLECTORNAME_PROPERTY)
        if (collectorName.isNotEmpty()) {
            lastSeenCollectors[collectorName.first().second] = System.currentTimeMillis()
        }

        needsPersist.set(true)
    }

    private val MAX_AGE = Duration.ofDays(3).toMillis()

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.DAYS)
    fun cleanup() {
        val toRemoveEvents = entries.entries
            .filter {
                val entry = it.value.first
                if (entry.containsKey(SemanticKeys.COLLECTORNAME)) {
                    val collectorName = entry[SemanticKeys.COLLECTORNAME]!!
                    it.value.second.timeAdded + MAX_AGE < (lastSeenCollectors[collectorName] ?: Long.MIN_VALUE)
                } else {
                    false
                }
            }

        toRemoveEvents.forEach {
            logger.debug { "removing event because it got too old: ${it.value.first}" }
            entries.remove(it.key)
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
        if (boudiccaEventDbProperties.store.path.isNullOrBlank()) {
            return
        }
        persistLock.lock()
        try {
            if (needsPersist.get()) {
                val bytes = objectMapper.writeValueAsBytes(entries.values)
                try {
                    //TODO make more resilient saving, aka save then move
                    Path.of(boudiccaEventDbProperties.store.path)
                        .writeBytes(bytes, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)
                } catch (e: IOException) {
                    logger.error(e) { "error persisting store" }
                }
                needsPersist.set(false)
            }
        } finally {
            persistLock.unlock()
        }
    }

    private fun getEntryKey(entry: StructuredEntry): EntryKey {
        val keys =
            if (!boudiccaEventDbProperties.entryKeyNames.isNullOrEmpty()) {
                boudiccaEventDbProperties.entryKeyNames
            } else {
                entry.keys.map { it.toKeyString() }
            }

        @Suppress("UNCHECKED_CAST")
        return keys
            .map {
                it to entry.filterKeys(KeyFilter.parse(it)).firstOrNull()?.second
            }
            .filter { it.second != null }
            .toMap() as EntryKey
    }
}
