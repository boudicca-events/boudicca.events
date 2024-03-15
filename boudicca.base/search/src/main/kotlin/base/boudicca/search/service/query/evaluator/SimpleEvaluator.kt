package base.boudicca.search.service.query.evaluator

import base.boudicca.model.Entry
import base.boudicca.api.search.model.ResultDTO
import base.boudicca.search.service.query.*
import base.boudicca.search.service.util.Utils
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class SimpleEvaluator(rawEntries: Collection<Entry>) : Evaluator {

    private val events = Utils.order(rawEntries)

    override fun evaluate(expression: Expression, page: Page): ResultDTO {
        val results = events.filter { matchesExpression(expression, it) }
        return ResultDTO(
            results
                .drop(page.offset)
                .take(page.size)
                .toList(),
            results.size
        )
    }

    private fun matchesExpression(expression: Expression, event: Map<String, String>): Boolean {
        when (expression) {
            is EqualsExpression -> {
                if (expression.getFieldName() == "*") {
                    return event.values.any { it.equals(expression.getText(), true) }
                }
                return event.containsKey(expression.getFieldName())
                        && event[expression.getFieldName()].equals(expression.getText(), true)
            }

            is ContainsExpression -> {
                if (expression.getFieldName() == "*") {
                    return event.values.any { it.contains(expression.getText(), true) }
                }
                return event.containsKey(expression.getFieldName())
                        && event[expression.getFieldName()]!!.contains(expression.getText(), true)
            }

            is NotExpression -> {
                return !matchesExpression(expression.getChild(), event)
            }

            is AndExpression -> {
                return matchesExpression(expression.getLeftChild(), event)
                        && matchesExpression(expression.getRightChild(), event)
            }

            is OrExpression -> {
                return matchesExpression(expression.getLeftChild(), event)
                        || matchesExpression(expression.getRightChild(), event)
            }

            is BeforeExpression -> {
                try {
                    val dateFieldName = expression.getFieldName()
                    if (!event.containsKey(dateFieldName)) {
                        return false
                    }
                    val startDate = getLocalStartDate(event[dateFieldName]!!)
                    return startDate.isEqual(expression.getDate()) || startDate.isBefore(expression.getDate())
                } catch (e: DateTimeParseException) {
                    return false
                }
            }

            is AfterExpression -> {
                try {
                    val dateFieldName = expression.getFieldName()
                    if (!event.containsKey(dateFieldName)) {
                        return false
                    }
                    val startDate = getLocalStartDate(event[dateFieldName]!!)
                    return startDate.isEqual(expression.getDate()) || startDate.isAfter(expression.getDate())
                } catch (e: DateTimeParseException) {
                    return false
                }
            }

            is DurationLongerExpression -> {
                val duration =
                    EvaluatorUtil.getDuration(expression.getStartDateField(), expression.getEndDateField(), event)
                return duration >= expression.getDuration().toDouble()
            }

            is DurationShorterExpression -> {
                val duration =
                    EvaluatorUtil.getDuration(expression.getStartDateField(), expression.getEndDateField(), event)
                return duration <= expression.getDuration().toDouble()
            }

            is HasFieldExpression -> {
                return event.containsKey(expression.getFieldName()) && event[expression.getFieldName()]!!.isNotEmpty()
            }

            else -> {
                throw QueryException("unknown expression kind $expression")
            }
        }
    }

    private fun getLocalStartDate(dateText: String): LocalDate =
        OffsetDateTime.parse(dateText, DateTimeFormatter.ISO_DATE_TIME)
            .atZoneSameInstant(ZoneId.of("Europe/Vienna"))
            .toLocalDate()

}