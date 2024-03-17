package base.boudicca.query.evaluator

import java.time.Duration
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit

object EvaluatorUtil {
    fun getDuration(startDateField: String, endDateField: String, event: Map<String, String>): Double {
        if (!event.containsKey(startDateField) || !event.containsKey(endDateField)) {
            return 0.0
        }
        return try {
            val startDate = OffsetDateTime.parse(event[startDateField]!!, DateTimeFormatter.ISO_DATE_TIME)
            val endDate = OffsetDateTime.parse(event[endDateField]!!, DateTimeFormatter.ISO_DATE_TIME)
            Duration.of(endDate.toEpochSecond() - startDate.toEpochSecond(), ChronoUnit.SECONDS)
                .toMillis()
                .toDouble() / 1000.0 / 60.0 / 60.0
        } catch (e: DateTimeParseException) {
            0.0
        }
    }
}