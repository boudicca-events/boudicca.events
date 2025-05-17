package base.boudicca.api.eventcollector.dateparser

import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.OffsetDateTime

class DateParser {

    private val logger = KotlinLogging.logger {}

    private val tokens = mutableListOf<Pair<TokenType, String>>()

    fun date(date: String) {
        tokens.add(Pair(TokenType.DATE, date))
    }

    fun time(time: String) {
        tokens.add(Pair(TokenType.TIME, time))
    }

    fun tryParse(): OffsetDateTime? {
        val result = DateParserImpl(tokens).tryParse()
        logger.debug { "parsed $result from data: $this" }
        return result
    }

    fun parse(): OffsetDateTime {
        return requireNotNull(tryParse()) { "could not parse OffsetDateTime with following data: $this" }
    }

    override fun toString(): String {
        return "DateParser(tokens=$tokens)"
    }
}
