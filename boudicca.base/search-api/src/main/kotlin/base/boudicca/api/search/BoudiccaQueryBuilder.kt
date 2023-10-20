package base.boudicca.api.search

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object BoudiccaQueryBuilder {

    enum class Category {
        MUSIC,
        TECH,
        ART,
        OTHER
    }

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

    fun isQuery(category: Category): String {
        return "is ${category.name}"
    }

    fun after(localDate: LocalDate): String {
        return "after " + DateTimeFormatter.ISO_LOCAL_DATE.format(localDate)
    }

    fun before(localDate: LocalDate): String {
        return "before " + DateTimeFormatter.ISO_LOCAL_DATE.format(localDate)
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

    fun durationLonger(hours: Double): String {
        return "durationLonger $hours"
    }

    fun durationShorter(hours: Double): String {
        return "durationShorter $hours"
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