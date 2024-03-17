package base.boudicca.query

import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * helper methods to safely create a boudicca query for searching. all methods operating on fields and text safely escape text passed into them.
 * you can nest the calls to methods like
 * `and(
 *      not(contains("field","text")),
 *      equals("field2",text2")
 * )`
 * see the query documentation for more information about the operators
 */
object BoudiccaQueryBuilder {

    fun and(subQueries: Iterable<String>): String {
        return booleanMultiQuery(subQueries, "and")
    }

    fun and(vararg subQueries: String): String {
        return booleanMultiQuery(subQueries.toList(), "and")
    }

    fun or(subQueries: Iterable<String>): String {
        return booleanMultiQuery(subQueries, "or")
    }

    fun or(vararg subQueries: String): String {
        return booleanMultiQuery(subQueries.toList(), "or")
    }

    fun not(query: String): String {
        if (query.isEmpty()) {
            throw IllegalArgumentException("query is not allowed to be empty")
        }
        return "not ($query)"
    }

    fun after(dateFieldName: String, localDate: LocalDate): String {
        return escapeText(dateFieldName) + " after " + escapeText(DateTimeFormatter.ISO_LOCAL_DATE.format(localDate))
    }

    fun before(dateFieldName: String, localDate: LocalDate): String {
        return escapeText(dateFieldName) + " before " + escapeText(DateTimeFormatter.ISO_LOCAL_DATE.format(localDate))
    }

    fun equals(field: String, value: String): String {
        if (field.isEmpty()) {
            throw IllegalArgumentException("field is not allowed to be empty")
        }
        return escapeText(field) + " equals " + escapeText(value)
    }

    fun contains(field: String, value: String): String {
        if (field.isEmpty()) {
            throw IllegalArgumentException("field is not allowed to be empty")
        }
        return escapeText(field) + " contains " + escapeText(value)
    }

    fun durationLonger(startDateField: String, endDateField: String, hours: Double): String {
        return "duration ${escapeText(startDateField)} ${escapeText(endDateField)} longer $hours"
    }

    fun durationShorter(startDateField: String, endDateField: String, hours: Double): String {
        return "duration ${escapeText(startDateField)} ${escapeText(endDateField)} shorter $hours"
    }

    fun hasField(field: String): String {
        return "hasField ${escapeText(field)}"
    }

    fun escapeText(text: String): String {
        return "\"" + text.replace("\\", "\\\\").replace("\"", "\\\"") + "\""
    }

    private fun booleanMultiQuery(
        subQueries: Iterable<String>,
        operator: String
    ): String {
        if (subQueries.count() == 0) {
            throw IllegalArgumentException("you have to pass at least one subquery")
        }
        if (subQueries.any { it.isEmpty() }) {
            throw IllegalArgumentException("subQueries are not allowed to be empty")
        }
        return subQueries.joinToString(" $operator ") { "($it)" }
    }
}