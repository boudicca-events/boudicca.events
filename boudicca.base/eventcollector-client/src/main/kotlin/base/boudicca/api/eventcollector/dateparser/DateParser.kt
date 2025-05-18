package base.boudicca.api.eventcollector.dateparser

import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime

class DateParser {

    private val logger = KotlinLogging.logger {}

    private val tokens = mutableListOf<Pair<List<TokenType>, String>>()

    fun dayMonthYear(date: String) {
        tokens.add(Pair(listOf(TokenType.DAY, TokenType.MONTH, TokenType.YEAR), date))
    }

    fun time(time: String) {
        tokens.add(Pair(listOf(TokenType.HOURS, TokenType.MINUTES), time))
    }

    fun dayMonthYearTime(date: String) {
        tokens.add(
            Pair(
                listOf(TokenType.DAY, TokenType.MONTH, TokenType.YEAR, TokenType.HOURS, TokenType.MINUTES),
                date
            )
        )
    }

    fun tryParse(): OffsetDateTime? {
        val result = DateParserImpl(tokens).tryParse()
        logger.debug { "parsed $result from data: $this" }
        return result
    }

    fun parse(): OffsetDateTime {
        return requireNotNull(tryParse()) { "could not parse OffsetDateTime with following data: $this" }
    }

    fun tryParseLocalDate(): LocalDate? {
        val result = DateParserImpl(tokens).tryParseLocalDate()
        logger.debug { "parsed $result from data: $this" }
        return result
    }

    fun parseLocalDate(): LocalDate {
        return requireNotNull(tryParseLocalDate()) { "could not parse LocalDate with following data: $this" }
    }

    fun tryParseLocalTime(): LocalTime? {
        val result = DateParserImpl(tokens).tryParseLocalTime()
        logger.debug { "parsed $result from data: $this" }
        return result
    }

    fun parseLocalTime(): LocalTime {
        return requireNotNull(tryParseLocalTime()) { "could not parse LocalTime with following data: $this" }
    }

    override fun toString(): String {
        return "DateParser(tokens=$tokens)"
    }
}
