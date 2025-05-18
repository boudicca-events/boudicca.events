package base.boudicca.api.eventcollector.dateparser

import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime

class DateParser {

    private val logger = KotlinLogging.logger {}

    private val tokens = mutableListOf<Pair<List<TokenType>, String>>()

    fun dayMonthYear(date: String) {
        TokenBuilder().dayMonthYear().with(date)
    }

    fun time(time: String) {
        TokenBuilder().time().with(time)
    }

    fun dayMonthYear(): TokenBuilder {
        return TokenBuilder().dayMonthYear()
    }

    fun time(): TokenBuilder {
        return TokenBuilder().time()
    }

    fun token(): TokenBuilder {
        return TokenBuilder()
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

    inner class TokenBuilder {
        private val tokenTypes = mutableListOf<TokenType>()

        fun dayMonthYear(): TokenBuilder {
            return day().month().year()
        }

        fun time(): TokenBuilder {
            return hours().minutes()
        }

        fun day(): TokenBuilder {
            tokenTypes.add(TokenType.DAY)
            return this
        }

        fun month(): TokenBuilder {
            tokenTypes.add(TokenType.MONTH)
            return this
        }

        fun year(): TokenBuilder {
            tokenTypes.add(TokenType.YEAR)
            return this
        }

        fun hours(): TokenBuilder {
            tokenTypes.add(TokenType.HOURS)
            return this
        }

        fun minutes(): TokenBuilder {
            tokenTypes.add(TokenType.MINUTES)
            return this
        }

        fun seconds(): TokenBuilder {
            tokenTypes.add(TokenType.SECONDS)
            return this
        }

        fun with(value: String) {
            this@DateParser.tokens.add(Pair(tokenTypes, value))
        }
    }
}
