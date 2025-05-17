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

    private fun resolveUnknownTokens() {
        //nothing yet, comes soon
    }

    private fun buildDate(): OffsetDateTime? {
        val dateToken = tokens.find { it.first == TokenType.DATE }
        if (dateToken == null) {
            logger.debug { "did not find any date tokens, cannot build date" }
            return null
        }
        logger.debug { "found date token: $dateToken" }
        val localDate = parseDateTokenToLocalDate(dateToken) ?: return null

        val timeToken = tokens.find { it.first == TokenType.TIME }
        logger.debug { "found time token: $timeToken" }
        val localTime = parseTimeTokenToLocalTime(timeToken)

        val timezone = ZoneId.of("Europe/Vienna") //TODO make configurable

        return if (localTime != null) {
            localDate.atTime(localTime).atZone(timezone)
        } else {
            localDate.atStartOfDay(timezone)
        }.toOffsetDateTime()
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
        val splits = dateToken.second.split(alphanumericSplitRegex)
            .map { it.trim() }
            .filter { it.isNotBlank() }
        if (splits.size != 3) {
            logger.debug { "could not parse dateToken into 3 parts: $dateToken" }
            return null
        }
        try {
            val day = splits[0].toInt()
            val month = parseMonthToNumber(splits[1]) ?: return null
            val year = splits[2].toInt()
            return LocalDate.of(year, month, day)
        } catch (e: NumberFormatException) {
            logger.debug(e) { "could not parse numbers from dateToken: $dateToken" }
            return null
        }
    }

    private fun parseMonthToNumber(month: String): Int? {
        val result = month.toIntOrNull()
            ?: monthMappings[month.lowercase()]
        if (result == null) {
            logger.debug { "could neither parse month '$month' as int nor resolve at from the mapping" }
        }
        return result
    }

}
