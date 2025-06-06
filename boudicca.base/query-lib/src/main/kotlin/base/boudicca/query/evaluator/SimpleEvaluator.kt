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
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeParseException
import java.util.concurrent.ConcurrentHashMap

@Suppress("detekt:LongMethod", "detekt:CyclomaticComplexMethod")
class SimpleEvaluator(rawEntries: Collection<Entry>) : Evaluator {

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

            else -> {
                throw QueryException("unknown expression kind $expression")
            }
        }
    }

    private fun parseList(keyValuePair: Pair<Key, String>): List<String> {
        return ListFormatAdapter().fromString(keyValuePair.second)
    }

    private fun getLocalStartDate(dateText: String): LocalDate =
        DateFormatAdapter().fromString(dateText)
            .atZoneSameInstant(ZoneId.of("Europe/Vienna"))
            .toLocalDate()

}
