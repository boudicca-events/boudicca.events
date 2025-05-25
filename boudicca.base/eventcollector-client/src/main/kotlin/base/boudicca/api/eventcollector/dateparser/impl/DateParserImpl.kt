package base.boudicca.api.eventcollector.dateparser.impl

import base.boudicca.api.eventcollector.dateparser.TokenType
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId

internal class DateParserImpl(private val inputTokens: List<Pair<List<TokenType>, String>>) {

    companion object {
        private val nonNumerical = Regex("\\D+")

        private val monthMappings = mapOf(
            "januar" to 1,
            "jänner" to 1,
            "februar" to 2,
            "märz" to 3,
            "april" to 4,
            "mai" to 5,
            "juni" to 6,
            "juli" to 7,
            "august" to 8,
            "september" to 9,
            "oktober" to 10,
            "november" to 11,
            "dezember" to 12,
        )
    }

    private val logger = KotlinLogging.logger {}

    private var tokens = emptyList<Pair<TokenType, String>>()

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
        tokens = inputTokens
            .flatMap { genericSplit(it) }
            .map {
                if (it.first.size != 1) {
                    logger.warn { "found invalid tokentype length for flattening: $it" }
                }
                Pair(it.first.first(), it.second)
            }
    }

    private fun genericSplit(token: Pair<List<TokenType>, String>): List<Pair<List<TokenType>, String>> {
        val wantedLength = token.first.size
        var splits = Tokenizer.tokenize(token.second)
            .filter { it.first != TokenizerType.SEPARATOR }
            .map { it.second.trim() }
            .filter { it.isNotBlank() }
        if (splits.size < wantedLength) {
            logger.debug { "could not parse token into at least $wantedLength parts: $token" }
            return listOf(token)
        }
        splits = trimNoiseOnEnds(splits)
        if (splits.size != wantedLength) {
            logger.debug { "could not parse token into $wantedLength parts: $token, after trimming only $splits remained" }
            return listOf(token)
        }
        return token.first.zip(splits) { tokenTypes, value -> Pair(listOf(tokenTypes), value) }
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
        try {
            val day = findToken(TokenType.DAY).parseToInt() ?: return null
            val month = parseMonthToNumber(findToken(TokenType.MONTH)) ?: return null
            val year = findToken(TokenType.YEAR).parseToInt() ?: return null
            return LocalDate.of(fixYear(year), month, day)
        } catch (e: NumberFormatException) {
            logger.debug(e) { "could not parse numbers for localdate" }
            return null
        }
    }

    private fun buildLocalTime(): LocalTime? {
        try {
            val hours = findToken(TokenType.HOURS).parseToInt() ?: 0
            val minutes = findToken(TokenType.MINUTES).parseToInt() ?: 0
            val seconds = findToken(TokenType.SECONDS).parseToInt() ?: 0
            return LocalTime.of(hours, minutes, seconds)
        } catch (e: NumberFormatException) {
            logger.debug(e) { "could not parse numbers for localtime" }
            return null
        }
    }

    private fun findToken(tokenType: TokenType): String? {
        val result = tokens.find { it.first == tokenType }
        if (result == null) {
            logger.debug { "could not find token of type $tokenType" }
        }
        return result?.second
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

    private fun trimNoiseOnEnds(splits: List<String>): List<String> {
        var result = splits
        if (result.first().contains(nonNumerical)) {
            result = result.drop(1)
        }
        if (result.last().contains(nonNumerical)) {
            result = result.dropLast(1)
        }
        return result
    }

    private fun parseMonthToNumber(month: String?): Int? {
        if (month == null) {
            return null
        }
        val result = month.toIntOrNull()
        if (result == null) {
            val lowercaseMonth = month.lowercase()
            for (entry in monthMappings) {
                if (entry.key.startsWith(lowercaseMonth)) {
                    return entry.value
                }
            }
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
