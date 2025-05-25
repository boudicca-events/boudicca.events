package base.boudicca.api.eventcollector.dateparser.impl

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

    fun tryParse(): OffsetDateTime? {
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
        val guessResult = Guesser(token.first, tokens).guess().filter { it !is Noise }
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

    private fun buildLocalDate(): LocalDate? {
        val date = findToken(Date::class)
        if (date == null) {
            try {
                val day = findToken(Day::class)?.value.parseToInt() ?: return null
                val month = parseMonthToNumber(findToken(Month::class)?.value) ?: return null
                val year = findToken(Year::class)?.value.parseToInt() ?: return null
                return LocalDate.of(fixYear(year), month, day)
            } catch (e: NumberFormatException) {
                logger.debug(e) { "could not parse numbers for localdate" }
                return null
            }
        } else {
            try {
                val day = date.day.parseToInt() ?: return null
                val month = parseMonthToNumber(date.month) ?: return null
                val year = date.year.parseToInt() ?: return null
                return LocalDate.of(fixYear(year), month, day)
            } catch (e: NumberFormatException) {
                logger.debug(e) { "could not parse numbers for localdate" }
                return null
            }
        }
    }

    private fun buildLocalTime(): LocalTime? {
        val time = findToken(Time::class)
        if (time == null) {
            try {
                val hours = findToken(Hours::class)?.value.parseToInt() ?: 0
                val minutes = findToken(Minutes::class)?.value.parseToInt() ?: 0
                val seconds = findToken(Seconds::class)?.value.parseToInt() ?: 0
                return LocalTime.of(hours, minutes, seconds)
            } catch (e: NumberFormatException) {
                logger.debug(e) { "could not parse numbers for localtime" }
                return null
            }
        } else {
            try {
                val hours = time.hours.parseToInt() ?: 0
                val minutes = time.minutes.parseToInt() ?: 0
                val seconds = time.seconds.parseToInt() ?: 0
                return LocalTime.of(hours, minutes, seconds)
            } catch (e: NumberFormatException) {
                logger.debug(e) { "could not parse numbers for localtime" }
                return null
            }
        }
    }

    private fun <T : Guess> findToken(clazz: KClass<T>): T? {
        val result = tokens.find { clazz.isInstance(it) }
        if (result == null) {
            logger.debug { "could not find token of type $clazz" }
            return null
        }
        @Suppress("UNCHECKED_CAST")
        return result as T
    }

    private fun fixYear(year: Int): Int {
        return if (year < 70) { //we get some problems in the year 2070 with this...
            2000 + year
        } else if (year < 100) {
            1900 + year
        } else {
            year
        }
    }

    private fun parseMonthToNumber(month: String?): Int? {
        if (month == null) {
            return null
        }
        var result = month.toIntOrNull()
        if (result == null) {
            result = MonthMappings.mapMonthToInt(month)
        }
        if (result == null) {
            logger.debug { "could neither parse month '$month' as int nor resolve at from the mapping" }
        }
        return result
    }

    private fun String?.parseToInt(): Int? {
        if (this == null) {
            return null
        }
        val result = this.toIntOrNull()
        if (result == null) {
            logger.debug { "could not parse $this to int" }
        }
        return result
    }
}
