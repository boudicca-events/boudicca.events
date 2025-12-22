package base.boudicca.entrydb.service

import base.boudicca.SemanticKeys
import base.boudicca.UuidV5
import base.boudicca.entrydb.BoudiccaEntryDbProperties
import base.boudicca.entrydb.model.InternalEventProperties
import base.boudicca.model.Entry
import base.boudicca.model.Event
import base.boudicca.model.structured.KeyFilter
import base.boudicca.model.structured.StructuredEntry
import base.boudicca.model.structured.filterKeys
import base.boudicca.model.structured.getProperty
import base.boudicca.model.structured.toBuilder
import base.boudicca.model.structured.toFlatEntry
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
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantLock
import kotlin.io.path.exists
import kotlin.io.path.readBytes
import kotlin.io.path.writeBytes

private const val MAX_AGE_IN_DAYS = 3L
private val MAX_AGE = Duration.ofDays(MAX_AGE_IN_DAYS).toMillis()

@Service
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON) // even if this is the default, we REALLY have to make sure there is only one
class EntryService
@Autowired
constructor(private val boudiccaEntryDbProperties: BoudiccaEntryDbProperties) {
    private val logger = KotlinLogging.logger {}
    private val entries = ConcurrentHashMap<UUID, Pair<Entry, InternalEventProperties>>()
    private val lastSeenCollectors = ConcurrentHashMap<String, Long>()
    private val persistLock = ReentrantLock()
    private val needsPersist = AtomicBoolean(false)
    private val objectMapper =
        JsonMapper
            .builder()
            .addModule(JavaTimeModule())
            .addModule(KotlinModule.Builder().build())
            .build()
    private val uuidV5Generator = UuidV5(UUID.fromString(boudiccaEntryDbProperties.uuidv5Namespace))

    init {
        if (!boudiccaEntryDbProperties.store.path.isNullOrBlank()) {
            val store = Path.of(boudiccaEntryDbProperties.store.path)
            if (store.exists()) {
                try {
                    loadStoreV3(store)
                } catch (_: DatabindException) {
                    logger.info { "store had wrong format, retrying with old format v2" }
                    try {
                        loadStoreV2(store)
                    } catch (_: DatabindException) {
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
        val storeRead =
            objectMapper.readValue(
                store.readBytes(),
                object : TypeReference<List<Event>>() {},
            )

        storeRead.forEach {
            add(Event.toEntry(it))
        }

        needsPersist.set(true) // to write in the new format
    }

    private fun loadStoreV2(store: Path) {
        val storeRead =
            objectMapper.readValue(
                store.readBytes(),
                object : TypeReference<List<Pair<Event, InternalEventProperties>>>() {},
            )

        storeRead.forEach {
            val entry = Event.toEntry(it.first)
            entries[getEntryKey(entry.toStructuredEntry())] = Pair(entry, it.second)
        }

        needsPersist.set(true) // to write in the new format
    }

    private fun loadStoreV3(store: Path) {
        val storeRead =
            objectMapper.readValue(
                store.readBytes(),
                object : TypeReference<List<Pair<Entry, InternalEventProperties>>>() {},
            )

        storeRead.forEach {
            entries[getEntryKey(it.first.toStructuredEntry())] = it
        }

        needsPersist.set(false)
    }

    fun all(): Set<Entry> = entries.values.map { it.first }.toSet()

    fun get(boudiccaId: UUID): Entry? = entries[boudiccaId]?.first

    fun add(entry: Entry) {
        // TODO we should do UTF8 normalization on all keys and values
        // TODO we should do some kind of validation on the keys and types/formats
        val structuredEntry =
            entry
                .filterKeys { !it.startsWith("boudicca.") }
                .toStructuredEntry()

        val boudiccaId = getEntryKey(structuredEntry)
        val modifiedEntry =
            structuredEntry
                .toBuilder()
                .withProperty(SemanticKeys.BOUDICCA_ID_PROPERTY, boudiccaId)

        // we reflatten the entry to make sure keys are canonical
        entries[boudiccaId] =
            Pair(modifiedEntry.build().toFlatEntry(), InternalEventProperties(System.currentTimeMillis()))

        val collectorName = structuredEntry.getProperty(SemanticKeys.COLLECTORNAME_PROPERTY)
        if (collectorName.isNotEmpty()) {
            lastSeenCollectors[collectorName.first().second] = System.currentTimeMillis()
        }

        needsPersist.set(true)
    }

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.DAYS)
    fun cleanup() {
        val toRemoveEvents =
            entries.entries
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
        Runtime.getRuntime().addShutdownHook(
            object : Thread() {
                override fun run() {
                    persist()
                }
            },
        )
    }

    fun persist() {
        if (boudiccaEntryDbProperties.store.path.isNullOrBlank()) {
            return
        }
        persistLock.lock()
        try {
            if (needsPersist.get()) {
                val bytes = objectMapper.writeValueAsBytes(entries.values)
                try {
                    // TODO make more resilient saving, aka save then move
                    Path
                        .of(boudiccaEntryDbProperties.store.path)
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

    private fun getEntryKey(entry: StructuredEntry): UUID {
        val keys =
            if (!boudiccaEntryDbProperties.entryKeyNames.isNullOrEmpty()) {
                boudiccaEntryDbProperties.entryKeyNames
            } else {
                entry.keys.map { it.toKeyString() }
            }

        val values =
            keys
                .mapNotNull { key ->
                    entry.filterKeys(KeyFilter.parse(key)).firstOrNull()?.second
                }

        return uuidV5Generator.from(values)
    }
}
