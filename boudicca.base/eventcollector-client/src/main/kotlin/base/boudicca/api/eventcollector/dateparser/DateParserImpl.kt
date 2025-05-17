package base.boudicca.api.eventcollector.dateparser

import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId

class DateParserImpl(inputTokens: List<Pair<TokenType, String>>) {

    companion object {
        private val alphanumericSplitRegex = Regex("[^\\wäöüß]+")
        private val onlyNumerical = Regex("\\d+")
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

    private val tokens = inputTokens.toList()

    fun tryParse(): OffsetDateTime? {
        resolveUnknownTokens()
        return buildDate()
    }

    fun tryParseLocalDate(): LocalDate? {
        resolveUnknownTokens()
        return buildLocalDate()
    }

    fun tryParseLocalTime(): LocalTime? {
        resolveUnknownTokens()
        return buildLocalTime()
    }

    private fun resolveUnknownTokens() {
        //nothing yet, comes soon
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
        val dateToken = tokens.find { it.first == TokenType.DATE }
        if (dateToken == null) {
            logger.debug { "did not find any date tokens, cannot build date" }
            return null
        }
        logger.debug { "found date token: $dateToken" }
        return parseDateTokenToLocalDate(dateToken)
    }

    private fun buildLocalTime(): LocalTime? {
        val timeToken = tokens.find { it.first == TokenType.TIME }
        logger.debug { "found time token: $timeToken" }
        val localTime = parseTimeTokenToLocalTime(timeToken)
        return localTime
    }

    private fun parseTimeTokenToLocalTime(timeToken: Pair<TokenType, String>?): LocalTime? {
        if (timeToken == null) {
            return null
        }
        val splits = timeToken.second.split(alphanumericSplitRegex)
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .filter { it.matches(onlyNumerical) }

        try {
            val hour = splits[0].toInt()
            val minutes = splits[1].toInt()
            return LocalTime.of(hour, minutes)
        } catch (e: NumberFormatException) {
            logger.debug(e) { "could not parse numbers from timeToken: $timeToken" }
            return null
        }
    }

    private fun parseDateTokenToLocalDate(dateToken: Pair<TokenType, String>): LocalDate? {
        var splits = dateToken.second.split(alphanumericSplitRegex)
            .map { it.trim() }
            .filter { it.isNotBlank() }
        if (splits.size < 3) {
            logger.debug { "could not parse dateToken into at least 3 parts: $dateToken" }
            return null
        }
        splits = trimNoiseOnEnds(splits)
        if (splits.size != 3) {
            logger.debug { "could not parse dateToken into 3 parts: $dateToken, after trimming only $splits remained" }
            return null
        }
        try {
            val day = splits[0].toInt()
            val month = parseMonthToNumber(splits[1]) ?: return null
            val year = splits[2].toInt()
            return LocalDate.of(fixYear(year), month, day)
        } catch (e: NumberFormatException) {
            logger.debug(e) { "could not parse numbers from dateToken: $dateToken" }
            return null
        }
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

    private fun parseMonthToNumber(month: String): Int? {
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

}
