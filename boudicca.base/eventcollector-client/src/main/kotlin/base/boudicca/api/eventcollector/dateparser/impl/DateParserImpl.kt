package base.boudicca.api.eventcollector.dateparser.impl

import base.boudicca.api.eventcollector.dateparser.DatePair
import base.boudicca.api.eventcollector.dateparser.DateParserResult
import base.boudicca.api.eventcollector.dateparser.HintType
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import kotlin.reflect.KClass

internal class DateParserImpl(private val inputTokens: List<Pair<List<HintType>, String>>) {

    private val logger = KotlinLogging.logger {}

    private var tokens = emptyList<Guess>()

    fun parse(): DateParserResult {
        processTokens()
        return buildResult()
    }

    fun tryParseSingle(): OffsetDateTime? {
        processTokens()
        return buildDate()
    }

    fun tryParseLocalDate(): LocalDate? {
        processTokens()
        return buildLocalDate()
    }

    fun tryParseLocalTime(): LocalTime? {
        processTokens()
        return buildLocalTime()
    }

    private fun processTokens() {
        splitAllTokens()
    }

    private fun splitAllTokens() {
        tokens = inputTokens.flatMap { genericSplit(it) }
    }

    private fun genericSplit(token: Pair<List<HintType>, String>): List<Guess> {
        val tokens = Tokenizer.tokenize(token.second)
        val guessResult = Guesser(token.first, tokens).guess()
        logger.debug { "guesser output is: $guessResult" }
        return guessResult
    }

    private fun buildDate(): OffsetDateTime? {
        val localDate = buildLocalDate() ?: return null

        val localTime = buildLocalTime()

        val timezone = ZoneId.of("Europe/Vienna") //TODO make configurable

        return if (localTime != null) {
            localDate.atTime(localTime).atZone(timezone)
        } else {
            localDate.atStartOfDay(timezone)
        }.toOffsetDateTime()
    }

    private fun buildResult(): DateParserResult {
        val localDates = buildAllLocalDates().toMutableList()

        if (localDates.isEmpty()) return DateParserResult(emptyList())

        val localTimes = buildAllLocalTimes().toMutableList()

        val timezone = ZoneId.of("Europe/Vienna") //TODO make configurable

        var lastLocalDate: LocalDate? = null
        var lastLocalTime: LocalTime? = null
        val results = mutableListOf<OffsetDateTime>()

        while (localDates.isNotEmpty() || localTimes.isNotEmpty()) {
            lastLocalDate = if (localDates.isNotEmpty()) localDates.removeFirst() else lastLocalDate
            lastLocalTime = if (localTimes.isNotEmpty()) localTimes.removeFirst() else lastLocalTime

            results.add(
                if (lastLocalTime != null) {
                    lastLocalDate!!.atTime(lastLocalTime).atZone(timezone)
                } else {
                    lastLocalDate!!.atStartOfDay(timezone)
                }.toOffsetDateTime()
            )
        }

        return if (results.size == 2) {
            DateParserResult(
                listOf(
                    DatePair(
                        results[0],
                        results[1],
                    )
                )
            )
        } else {
            DateParserResult(
                results.map { DatePair(it) })
        }
    }

    private fun buildLocalDate(): LocalDate? {
        val date = findToken(Date::class) ?: return null
        return LocalDate.of(date.year, date.month, date.day)
    }

    private fun buildLocalTime(): LocalTime? {
        val time = findToken(Time::class) ?: return null
        return LocalTime.of(time.hours, time.minutes, time.seconds ?: 0)
    }

    private fun buildAllLocalDates(): List<LocalDate> {
        return findAllTokens(Date::class).map { LocalDate.of(it.year, it.month, it.day) }
    }

    private fun buildAllLocalTimes(): List<LocalTime> {
        return findAllTokens(Time::class).map { LocalTime.of(it.hours, it.minutes, it.seconds ?: 0) }
    }

    private fun <T : Guess> findAllTokens(clazz: KClass<T>): List<T> {
        return tokens.filter { clazz.isInstance(it) }.map {
            @Suppress("UNCHECKED_CAST") it as T
        }
    }

    private fun <T : Guess> findToken(clazz: KClass<T>): T? {
        val result = tokens.find { clazz.isInstance(it) }
        if (result == null) {
            logger.debug { "could not find token of type $clazz" }
            return null
        }
        @Suppress("UNCHECKED_CAST") return result as T
    }

}
