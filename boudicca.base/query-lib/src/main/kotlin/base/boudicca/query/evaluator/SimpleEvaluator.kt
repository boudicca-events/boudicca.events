package base.boudicca.query.evaluator

import base.boudicca.format.DateFormatAdapter
import base.boudicca.format.ListFormatAdapter
import base.boudicca.model.Entry
import base.boudicca.model.structured.Key
import base.boudicca.model.structured.StructuredEntry
import base.boudicca.model.structured.filterKeys
import base.boudicca.model.structured.toFlatEntry
import base.boudicca.model.toStructuredEntry
import base.boudicca.query.*
import base.boudicca.query.evaluator.util.EvaluatorUtil
import java.time.*
import java.time.format.DateTimeParseException
import java.util.concurrent.ConcurrentHashMap

@Suppress("detekt:LongMethod", "detekt:CyclomaticComplexMethod")
class SimpleEvaluator(rawEntries: Collection<Entry>, private val clock: Clock) : Evaluator {

    private val dateCache = ConcurrentHashMap<String, OffsetDateTime>()
    private val events = Utils.order(rawEntries, dateCache).map { it.toStructuredEntry() }

    override fun evaluate(expression: Expression, page: Page): QueryResult {
        val results = events.filter { matchesExpression(expression, it) }
        return QueryResult(
            results
                .drop(page.offset)
                .take(page.size)
                .map { it.toFlatEntry() }
                .toList(),
            results.size
        )
    }

    private fun matchesExpression(expression: Expression, entry: StructuredEntry): Boolean {
        when (expression) {
            is EqualsExpression -> {
                return entry
                    .filterKeys(expression.getKeyFilter())
                    .filter { EvaluatorUtil.isTextMarkdownOrList(it.first) }
                    .any {
                        if (EvaluatorUtil.isList(it.first)) {
                            parseList(it).any { listValue -> listValue.equals(expression.getText(), true) }
                        } else {
                            it.second.equals(expression.getText(), true)
                        }
                    }
            }

            is ContainsExpression -> {
                return entry
                    .filterKeys(expression.getKeyFilter())
                    .filter { EvaluatorUtil.isTextMarkdownOrList(it.first) }
                    .any {
                        if (EvaluatorUtil.isList(it.first)) {
                            parseList(it).any { listValue -> listValue.contains(expression.getText(), true) }
                        } else {
                            it.second.contains(expression.getText(), true)
                        }
                    }
            }

            is NotExpression -> {
                return !matchesExpression(expression.getChild(), entry)
            }

            is AndExpression -> {
                return matchesExpression(expression.getLeftChild(), entry)
                        && matchesExpression(expression.getRightChild(), entry)
            }

            is OrExpression -> {
                return matchesExpression(expression.getLeftChild(), entry)
                        || matchesExpression(expression.getRightChild(), entry)
            }

            is BeforeExpression -> {
                try {
                    val dateTexts = EvaluatorUtil.getDateValues(entry, expression.getKeyFilter())
                    return dateTexts
                        .any {
                            val startDate = getLocalStartDate(it)
                            startDate.isEqual(expression.getDate()) || startDate.isBefore(expression.getDate())
                        }
                } catch (e: DateTimeParseException) {
                    return false
                }
            }

            is AfterExpression -> {
                try {
                    val dateTexts = EvaluatorUtil.getDateValues(entry, expression.getKeyFilter())
                    return dateTexts
                        .any {
                            val startDate = getLocalStartDate(it)
                            startDate.isEqual(expression.getDate()) || startDate.isAfter(expression.getDate())
                        }
                } catch (e: DateTimeParseException) {
                    return false
                }
            }

            is DurationLongerExpression -> {
                val duration =
                    EvaluatorUtil.getDuration(
                        expression.getStartDateKeyFilter(),
                        expression.getEndDateKeyFilter(),
                        entry, dateCache
                    )
                return duration >= expression.getDuration().toDouble()
            }

            is DurationShorterExpression -> {
                val duration =
                    EvaluatorUtil.getDuration(
                        expression.getStartDateKeyFilter(),
                        expression.getEndDateKeyFilter(),
                        entry, dateCache
                    )
                return duration <= expression.getDuration().toDouble()
            }

            is HasFieldExpression -> {
                return entry.filterKeys(expression.getKeyFilter()).any { it.second.isNotEmpty() }
            }

            is IsInNextSecondsExpression -> {
                val expressionStartDate = clock.instant()
                val expressionEndDate = clock.instant().plusSeconds(expression.getNumber().toLong())
                return isInRange(entry, expression, expressionStartDate, expressionEndDate)
            }

            is IsInLastSecondsExpression -> {
                val expressionStartDate = clock.instant().minusSeconds(expression.getNumber().toLong())
                val expressionEndDate = clock.instant()
                return isInRange(entry, expression, expressionStartDate, expressionEndDate)
            }

            else -> {
                throw QueryException("unknown expression kind $expression")
            }
        }
    }

    private fun isInRange(
        entry: StructuredEntry,
        expression: FieldAndNumberExpression,
        expressionStartDate: Instant?,
        expressionEndDate: Instant?
    ): Boolean {
        try {
            val entryDates = EvaluatorUtil.getDateValues(entry, expression.getKeyFilter())
            return entryDates
                .any {
                    val entryDate = getInstant(it)
                    entryDate == expressionStartDate ||
                            entryDate == expressionEndDate ||
                            (entryDate.isAfter(expressionStartDate) && entryDate.isBefore(expressionEndDate))
                }
        } catch (e: DateTimeParseException) {
            return false
        }
    }

    private fun parseList(keyValuePair: Pair<Key, String>): List<String> {
        return ListFormatAdapter().fromString(keyValuePair.second)
    }

    private fun getLocalStartDate(dateText: String): LocalDate =
        DateFormatAdapter().fromString(dateText)
            .atZoneSameInstant(clock.zone)
            .toLocalDate()

    private fun getInstant(dateText: String): Instant =
        DateFormatAdapter().fromString(dateText).toInstant()

}
