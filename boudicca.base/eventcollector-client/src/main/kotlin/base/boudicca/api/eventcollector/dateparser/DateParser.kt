package base.boudicca.api.eventcollector.dateparser

import base.boudicca.api.eventcollector.dateparser.impl.DateParserImpl
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime

class DateParser {

    private val logger = KotlinLogging.logger {}

    private val tokens = mutableListOf<Pair<List<HintType>, String>>()

    fun dayMonthYear(date: String) {
        HintBuilder().dayMonthYear().with(date)
    }

    fun time(time: String) {
        HintBuilder().time().with(time)
    }

    fun any(text: String) {
        HintBuilder().any().with(text)
    }

    fun dayMonthYear(): HintBuilder {
        return HintBuilder().dayMonthYear()
    }

    fun time(): HintBuilder {
        return HintBuilder().time()
    }

    fun any(): HintBuilder {
        return HintBuilder().any()
    }

    fun token(): HintBuilder {
        return HintBuilder()
    }

    fun tryParseSingle(): OffsetDateTime? {
        val result = DateParserImpl(tokens).tryParseSingle()
        logger.debug { "parsed $result from data: $this" }
        return result
    }

    fun parseSingle(): OffsetDateTime {
        return requireNotNull(tryParseSingle()) { "could not parse OffsetDateTime with following data: $this" }
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

    fun parse(): DateParserResult {
        val result = DateParserImpl(tokens).parse()
        require(result.dates.isNotEmpty()) { "could not parse any dates with following data: $this" }
        logger.debug { "parsed $result from data: $this" }
        return result
    }

    override fun toString(): String {
        return "DateParser(tokens=$tokens)"
    }

    inner class HintBuilder {
        private val hintTypes = mutableListOf<HintType>()

        fun dayMonthYear(): HintBuilder {
            return day().month().year()
        }

        fun time(): HintBuilder {
            return hours().minutes()
        }

        fun any(): HintBuilder {
            hintTypes.add(HintType.ANY)
            return this
        }

        fun day(): HintBuilder {
            hintTypes.add(HintType.DAY)
            return this
        }

        fun month(): HintBuilder {
            hintTypes.add(HintType.MONTH)
            return this
        }

        fun year(): HintBuilder {
            hintTypes.add(HintType.YEAR)
            return this
        }

        fun hours(): HintBuilder {
            hintTypes.add(HintType.HOURS)
            return this
        }

        fun minutes(): HintBuilder {
            hintTypes.add(HintType.MINUTES)
            return this
        }

        fun seconds(): HintBuilder {
            hintTypes.add(HintType.SECONDS)
            return this
        }

        fun with(value: String) {
            this@DateParser.tokens.add(Pair(hintTypes, value))
        }
    }
}
