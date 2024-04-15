package base.boudicca.query

import base.boudicca.SemanticKeys
import base.boudicca.model.Entry
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Function

object Utils {
    private const val DEFAULT_SIZE = 30

    fun offset(events: List<Entry>, offset: Int?, size: Int?): List<Entry> {
        return events.drop(offset ?: 0).take(size ?: DEFAULT_SIZE)
    }

    fun order(entries: Collection<Entry>, dateCache: ConcurrentHashMap<String, OffsetDateTime>): List<Entry> {
        return entries.toList()
            .map { Pair(it, getStartDate(it[SemanticKeys.STARTDATE], dateCache)) }
            .sortedWith(
                Comparator
                    .comparing<Pair<Entry, OffsetDateTime>, OffsetDateTime> { it.second }
                    .thenComparing(Function { it.first[SemanticKeys.NAME] ?: "" })
            )
            .map { it.first }
    }

    private fun getStartDate(
        dateText: String?,
        startDateCache: ConcurrentHashMap<String, OffsetDateTime>
    ): OffsetDateTime {
        if (dateText == null) {
            return Instant.ofEpochMilli(0).atOffset(ZoneOffset.MIN)
        }
        if (startDateCache.containsKey(dateText)) {
            return startDateCache[dateText]!!
        }
        return try {
            val offsetDateTime = OffsetDateTime.parse(dateText, DateTimeFormatter.ISO_DATE_TIME)
                .atZoneSameInstant(ZoneId.of("Europe/Vienna"))
                .toOffsetDateTime()
            startDateCache[dateText] = offsetDateTime
            offsetDateTime
        } catch (e: Exception) {
            Instant.ofEpochMilli(0).atOffset(ZoneOffset.MIN)
        }
    }
}