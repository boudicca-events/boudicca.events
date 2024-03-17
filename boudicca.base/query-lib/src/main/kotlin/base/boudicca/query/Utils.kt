package base.boudicca.query

import base.boudicca.model.Entry
import base.boudicca.SemanticKeys
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.function.Function

object Utils {
    private const val DEFAULT_SIZE = 30

    fun offset(events: List<Entry>, offset: Int?, size: Int?): List<Entry> {
        return events.drop(offset ?: 0).take(size ?: DEFAULT_SIZE)
    }

    fun order(entries: Collection<Entry>): List<Entry> {
        return entries.toList()
            .map { Pair(it, getStartDate(it[SemanticKeys.STARTDATE])) }
            .sortedWith(
                Comparator
                    .comparing<Pair<Entry, OffsetDateTime>, OffsetDateTime> { it.second }
                    .thenComparing(Function { it.first[SemanticKeys.NAME] ?: "" })
            )
            .map { it.first }
    }

    private fun getStartDate(dateText: String?): OffsetDateTime {
        return try {
            OffsetDateTime.parse(dateText, DateTimeFormatter.ISO_DATE_TIME)
                .atZoneSameInstant(ZoneId.of("Europe/Vienna"))
                .toOffsetDateTime()
        } catch (e: Exception) {
            Instant.ofEpochMilli(0).atOffset(ZoneOffset.MIN)
        }
    }
}