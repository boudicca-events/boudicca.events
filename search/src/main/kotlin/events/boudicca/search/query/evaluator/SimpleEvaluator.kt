package events.boudicca.search.query.evaluator

import events.boudicca.EventCategory
import events.boudicca.SemanticKeys
import events.boudicca.search.model.Event
import events.boudicca.search.model.SearchResultDTO
import events.boudicca.search.query.*
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.function.Function

class SimpleEvaluator(rawEvents: Collection<Event>) : Evaluator {

    private val events = rawEvents
        .toList()
        .sortedWith(
            Comparator
                .comparing<Event, ZonedDateTime> { it.startDate }
                .thenComparing(Function { it.name })
        )
        .map { EvaluatorUtil.mapEventToMap(it) }

    override fun evaluate(expression: Expression, page: Page): SearchResultDTO {
        val results = events
            .filter { matchesExpression(expression, it) }
        return SearchResultDTO(results
            .drop(page.offset)
            .take(page.size)
            .map { EvaluatorUtil.toEvent(it) }
            .toList(), results.size)
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
                    if (!event.containsKey(SemanticKeys.STARTDATE)) {
                        return false
                    }
                    val startDate = LocalDate.parse(event[SemanticKeys.STARTDATE]!!, DateTimeFormatter.ISO_DATE_TIME)
                    return startDate.isEqual(expression.getDate()) || startDate.isBefore(expression.getDate())
                } catch (e: DateTimeParseException) {
                    return false
                }
            }

            is AfterExpression -> {
                try {
                    if (!event.containsKey(SemanticKeys.STARTDATE)) {
                        return false
                    }
                    val startDate = LocalDate.parse(event[SemanticKeys.STARTDATE]!!, DateTimeFormatter.ISO_DATE_TIME)
                    return startDate.isEqual(expression.getDate()) || startDate.isAfter(expression.getDate())
                } catch (e: DateTimeParseException) {
                    return false
                }
            }

            is IsExpression -> {
                if (!event.containsKey(SemanticKeys.TYPE)) {
                    return false
                }
                val category = EventCategory.getForType(event[SemanticKeys.TYPE])
                val expressionCategory = expression.getText().uppercase()
                if (category == null) {
                    return expressionCategory == "OTHER"
                }
                return expressionCategory == category.name
            }

            is DurationLongerExpression -> {
                val duration = EvaluatorUtil.getDuration(event)
                return duration >= expression.getNumber().toDouble()
            }

            is DurationShorterExpression -> {
                val duration = EvaluatorUtil.getDuration(event)
                return duration <= expression.getNumber().toDouble()
            }

            else -> {
                throw IllegalStateException("unknown expression kind $expression")
            }
        }
    }
}