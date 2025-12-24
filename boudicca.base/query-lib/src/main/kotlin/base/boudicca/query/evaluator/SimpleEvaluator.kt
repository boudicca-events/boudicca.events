package base.boudicca.query.evaluator

import base.boudicca.format.DateFormatAdapter
import base.boudicca.format.ListFormatAdapter
import base.boudicca.model.Entry
import base.boudicca.model.structured.Key
import base.boudicca.model.structured.StructuredEntry
import base.boudicca.model.structured.filterKeys
import base.boudicca.model.structured.toFlatEntry
import base.boudicca.model.toStructuredEntry
import base.boudicca.query.AfterExpression
import base.boudicca.query.AndExpression
import base.boudicca.query.BeforeExpression
import base.boudicca.query.ContainsExpression
import base.boudicca.query.DurationLongerExpression
import base.boudicca.query.DurationShorterExpression
import base.boudicca.query.EqualsExpression
import base.boudicca.query.Expression
import base.boudicca.query.FieldAndNumberExpression
import base.boudicca.query.HasFieldExpression
import base.boudicca.query.IsInLastSecondsExpression
import base.boudicca.query.IsInNextSecondsExpression
import base.boudicca.query.NotExpression
import base.boudicca.query.OrExpression
import base.boudicca.query.QueryException
import base.boudicca.query.Utils
import base.boudicca.query.evaluator.util.EvaluatorUtil
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException
import java.util.concurrent.ConcurrentHashMap

@Suppress("detekt:LongMethod", "detekt:CyclomaticComplexMethod")
class SimpleEvaluator(
    rawEntries: Collection<Entry>,
    private val clock: Clock = Clock.systemDefaultZone(),
) : Evaluator {
    private val dateCache = ConcurrentHashMap<String, OffsetDateTime>()
    private val events = Utils.order(rawEntries, dateCache).map { it.toStructuredEntry() }

    override fun evaluate(
        expression: Expression,
        page: Page,
    ): QueryResult {
        val results = events.filter { matchesExpression(expression, it) }
        return QueryResult(
            results
                .drop(page.offset)
                .take(page.size)
                .map { it.toFlatEntry() }
                .toList(),
            results.size,
        )
    }

    private fun matchesExpression(
        expression: Expression,
        entry: StructuredEntry,
    ): Boolean {
        return when (expression) {
            is EqualsExpression -> {
                entry
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
                entry
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
                !matchesExpression(expression.getChild(), entry)
            }

            is AndExpression -> {
                matchesExpression(expression.getLeftChild(), entry) &&
                    matchesExpression(expression.getRightChild(), entry)
            }

            is OrExpression -> {
                matchesExpression(expression.getLeftChild(), entry) ||
                    matchesExpression(expression.getRightChild(), entry)
            }

            is BeforeExpression -> {
                fun matchBeforeExpression(
                    expression: BeforeExpression,
                    entry: StructuredEntry,
                ): Boolean =
                    try {
                        val dateTexts = EvaluatorUtil.getDateValues(entry, expression.getKeyFilter())
                        dateTexts
                            .any {
                                val startDate = getLocalStartDate(it)
                                startDate.isEqual(expression.date) || startDate.isBefore(expression.date)
                            }
                    } catch (_: DateTimeParseException) {
                        false
                    }
                matchBeforeExpression(expression, entry)
            }

            is AfterExpression -> {
                fun matchAfterExpression(
                    expression: AfterExpression,
                    entry: StructuredEntry,
                ): Boolean {
                    try {
                        val dateTexts = EvaluatorUtil.getDateValues(entry, expression.getKeyFilter())
                        return dateTexts
                            .any {
                                val startDate = getLocalStartDate(it)
                                startDate.isEqual(expression.date) || startDate.isAfter(expression.date)
                            }
                    } catch (_: DateTimeParseException) {
                        return false
                    }
                }
                matchAfterExpression(expression, entry)
            }

            is DurationLongerExpression -> {
                val duration =
                    EvaluatorUtil.getDuration(
                        expression.getStartDateKeyFilter(),
                        expression.getEndDateKeyFilter(),
                        entry,
                        dateCache,
                    )
                duration >= expression.getDuration().toDouble()
            }

            is DurationShorterExpression -> {
                val duration =
                    EvaluatorUtil.getDuration(
                        expression.getStartDateKeyFilter(),
                        expression.getEndDateKeyFilter(),
                        entry,
                        dateCache,
                    )
                duration <= expression.getDuration().toDouble()
            }

            is HasFieldExpression -> {
                entry.filterKeys(expression.getKeyFilter()).any { it.second.isNotEmpty() }
            }

            is IsInNextSecondsExpression -> {
                val expressionStartDate = clock.instant()
                val expressionEndDate = clock.instant().plusSeconds(expression.getNumber().toLong())
                isInRange(entry, expression, expressionStartDate, expressionEndDate)
            }

            is IsInLastSecondsExpression -> {
                val expressionStartDate = clock.instant().minusSeconds(expression.getNumber().toLong())
                val expressionEndDate = clock.instant()
                isInRange(entry, expression, expressionStartDate, expressionEndDate)
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
        expressionEndDate: Instant?,
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
        } catch (_: DateTimeParseException) {
            return false
        }
    }

    private fun parseList(keyValuePair: Pair<Key, String>): List<String> = ListFormatAdapter().fromString(keyValuePair.second)

    private fun getLocalStartDate(dateText: String): LocalDate =
        DateFormatAdapter()
            .fromString(dateText)
            .atZoneSameInstant(clock.zone)
            .toLocalDate()

    private fun getInstant(dateText: String): Instant = DateFormatAdapter().fromString(dateText).toInstant()
}
