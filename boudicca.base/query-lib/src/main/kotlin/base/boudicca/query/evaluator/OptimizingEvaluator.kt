package base.boudicca.query.evaluator

import base.boudicca.format.ListFormatAdapter
import base.boudicca.keyfilters.KeyFilters
import base.boudicca.model.Entry
import base.boudicca.model.structured.Key
import base.boudicca.model.structured.KeyFilter
import base.boudicca.model.structured.filterKeys
import base.boudicca.model.toStructuredEntry
import base.boudicca.query.AbstractDurationExpression
import base.boudicca.query.AfterExpression
import base.boudicca.query.AndExpression
import base.boudicca.query.BeforeExpression
import base.boudicca.query.ContainsExpression
import base.boudicca.query.DurationLongerExpression
import base.boudicca.query.DurationShorterExpression
import base.boudicca.query.EqualsExpression
import base.boudicca.query.Expression
import base.boudicca.query.HasFieldExpression
import base.boudicca.query.IsInLastSecondsExpression
import base.boudicca.query.IsInNextSecondsExpression
import base.boudicca.query.NotExpression
import base.boudicca.query.OrExpression
import base.boudicca.query.QueryException
import base.boudicca.query.Utils
import base.boudicca.query.evaluator.util.EvaluatorUtil
import base.boudicca.query.evaluator.util.FullTextIndex
import base.boudicca.query.evaluator.util.SimpleIndex
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.*
import java.util.concurrent.ConcurrentHashMap

private const val SORTING_RATIO = 3

@Suppress("detekt:TooManyFunctions")
class OptimizingEvaluator(rawEntries: Collection<Entry>, private val clock: Clock) : Evaluator {

    private val dateCache = ConcurrentHashMap<String, OffsetDateTime>()
    private val entries = Utils.order(rawEntries, dateCache)
    private val fullTextIndexCache = mutableMapOf<String, FullTextIndex>()
    private val simpleIndexCache = mutableMapOf<String, MutableMap<String, SimpleIndex<*>>>()
    private val allFields = getAllFields(entries)

    init {
        // init contains searches
        for (field in allFields) {
            getOrCreateFullTextIndex(field)
        }
    }

    override fun evaluate(expression: Expression, page: Page): QueryResult {
        val resultSet = evaluateExpression(expression)
        val resultSize = resultSet.cardinality()
        val orderedResult = if (resultSize > entries.size / SORTING_RATIO) {
            // roughly, if the resultset is bigger than a third of the total entries,
            // it is faster to iterate over all entries than to sort the resultset
            entries.filterIndexed { i, _ -> resultSet[i] }
        } else {
            Utils.order(resultSet.stream().mapToObj { entries[it] }.toList(), dateCache)
        }
        return QueryResult(
            orderedResult
                .drop(page.offset)
                .take(page.size)
                .toList(),
            resultSize
        )
    }

    private fun evaluateExpression(expression: Expression): BitSet = when (expression) {
        is EqualsExpression -> equalsExpression(expression)
        is ContainsExpression -> containsExpression(expression)
        is NotExpression -> notExpression(expression)
        is AndExpression -> andExpression(expression)
        is OrExpression -> orExpression(expression)
        is BeforeExpression -> beforeExpression(expression)
        is AfterExpression -> afterExpression(expression)
        is DurationLongerExpression -> durationLongerExpression(expression)
        is DurationShorterExpression -> durationShorterExpression(expression)
        is HasFieldExpression -> hasFieldExpression(expression)
        is IsInNextSecondsExpression -> isInNextSecondsExpression(expression)
        is IsInLastSecondsExpression -> isInLastSecondsExpression(expression)
        else -> throw QueryException("unknown expression kind $expression")
    }

    private fun hasFieldExpression(expression: HasFieldExpression): BitSet {
        val resultSet = BitSet()
        entries.forEachIndexed { i, entry ->
            //TODO this is not performant at all...
            if (entry.toStructuredEntry().filterKeys(expression.getKeyFilter()).any { it.second.isNotEmpty() }) {
                resultSet.set(i)
            }
        }
        return resultSet
    }

    private fun notExpression(expression: NotExpression): BitSet {
        val subEvents = evaluateExpression(expression.getChild())
        subEvents.flip(0, entries.size) //TODO this modifies the set, is that ok? can cause problems with caching
        return subEvents
    }

    private fun orExpression(expression: OrExpression): BitSet {
        val leftSubEvents = evaluateExpression(expression.getLeftChild())
        val rightSubEvents = evaluateExpression(expression.getRightChild())
        leftSubEvents.or(rightSubEvents)
        return leftSubEvents
    }

    private fun andExpression(expression: AndExpression): BitSet {
        val leftSubEvents = evaluateExpression(expression.getLeftChild())
        val rightSubEvents = evaluateExpression(expression.getRightChild())
        leftSubEvents.and(rightSubEvents)
        return leftSubEvents
    }

    private fun equalsExpression(expression: EqualsExpression): BitSet {
        val lowerCase = expression.getText().lowercase()
        return keyFilterSearch(expression.getKeyFilter()) { field ->
            val index = getOrCreateSimpleIndex("equals", field) {
                SimpleIndex<String?>(
                    entries.flatMapIndexed { entryIndex, entry ->
                        val value = entry[field]
                        if (EvaluatorUtil.isList(Key.parse(field))) {
                            if (value == null) {
                                emptyList()
                            } else {
                                ListFormatAdapter()
                                    .fromString(value)
                                    .map { Pair(entryIndex, it.lowercase()) }
                            }
                        } else {
                            listOf(Pair(entryIndex, value?.lowercase()))
                        }
                    }, Comparator.naturalOrder<String?>()
                )
            }
            index.search { it?.compareTo(lowerCase) ?: -1 }
        }
    }

    private fun containsExpression(expression: ContainsExpression): BitSet {
        return keyFilterSearch(expression.getKeyFilter()) { field ->
            val index = getOrCreateFullTextIndex(field)
            index.containsSearch(expression.getText())
        }
    }

    private fun beforeExpression(expression: BeforeExpression): BitSet {
        val expressionStartDate = Instant.MIN
        val expressionEndDate = toInstant(expression.getDate().plusDays(1))
        return isInDateRangeQuery(expression.getKeyFilter(), expressionStartDate, expressionEndDate)
    }

    private fun afterExpression(expression: AfterExpression): BitSet {
        val expressionStartDate = toInstant(expression.getDate())
        val expressionEndDate = Instant.MAX
        return isInDateRangeQuery(expression.getKeyFilter(), expressionStartDate, expressionEndDate)
    }

    private fun durationLongerExpression(expression: DurationLongerExpression): BitSet {
        //TODO this still does not work 100%, we need a completely different handling here
        val index = getDurationIndex(expression)
        val duration = expression.getDuration().toDouble()
        return index.search {
            when {
                it != null && it >= duration -> 0
                else -> -1
            }
        }
    }

    private fun durationShorterExpression(expression: DurationShorterExpression): BitSet {
        //TODO this still does not work 100%, we need a completely different handling here
        val index = getDurationIndex(expression)
        val duration = expression.getDuration().toDouble()
        return index.search {
            when {
                it != null && it <= duration -> 0
                it != null -> 1
                else -> -1
            }
        }
    }

    private fun isInNextSecondsExpression(expression: IsInNextSecondsExpression): BitSet {
        val expressionStartDate = clock.instant()
        val expressionEndDate = clock.instant().plusSeconds(expression.getNumber().toLong())
        return isInDateRangeQuery(expression.getKeyFilter(), expressionStartDate, expressionEndDate)
    }

    private fun isInLastSecondsExpression(expression: IsInLastSecondsExpression): BitSet {
        val expressionStartDate = clock.instant().minusSeconds(expression.getNumber().toLong())
        val expressionEndDate = clock.instant()
        return isInDateRangeQuery(expression.getKeyFilter(), expressionStartDate, expressionEndDate)
    }

    private fun isInDateRangeQuery(
        keyFilter: KeyFilter,
        expressionStartDate: Instant,
        expressionEndDate: Instant
    ): BitSet {
        return keyFilterSearch(keyFilter) { field ->
            val index = getOrCreateInstantIndex(field)
            index.search {
                when {
                    it != null && (it == expressionStartDate || it == expressionEndDate) -> 0
                    it != null && !it.isAfter(expressionStartDate) -> -1
                    it != null && !it.isBefore(expressionEndDate) -> 1
                    it != null -> 0
                    else -> -1
                }
            }
        }
    }

    private fun getDurationIndex(expression: AbstractDurationExpression): SimpleIndex<Double?> {
        //TODO this index name generation can be wrong when & are used in keys
        val index =
            getOrCreateSimpleIndex(
                "duration",
                expression.getStartDateKeyFilter().toKeyString() + "&" + expression.getEndDateKeyFilter()
            ) {
                SimpleIndex.create(entries.map {
                    EvaluatorUtil.getDuration(
                        expression.getStartDateKeyFilter(),
                        expression.getEndDateKeyFilter(),
                        it.toStructuredEntry(), dateCache
                    )
                }, Comparator.naturalOrder())
            }
        return index
    }

    private fun toInstant(localDate: LocalDate): Instant {
        return localDate.atStartOfDay().atZone(clock.zone).toInstant()
    }

    private fun getOrCreateInstantIndex(fieldName: String): SimpleIndex<Instant?> {
        val index = getOrCreateSimpleIndex("instant", fieldName) {
            SimpleIndex.create(entries.map { safeGetInstant(it[fieldName], dateCache) }, Comparator.naturalOrder())
        }
        return index
    }

    private fun keyFilterSearch(keyFilter: KeyFilter, search: (String) -> BitSet): BitSet {
        val allFieldsToCheck = allFields.filter { KeyFilters.doesKeyMatchFilter(Key.parse(it), keyFilter) }
        val result = BitSet()
        for (field in allFieldsToCheck) {
            result.or(search(field))
        }
        return result
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> getOrCreateSimpleIndex(
        operation: String,
        fieldName: String,
        indexCreator: () -> SimpleIndex<T>
    ): SimpleIndex<T> {
        val operationCache = synchronized(simpleIndexCache) {
            simpleIndexCache.getOrPut(operation) { mutableMapOf() }
        }

        return synchronized(operationCache) {
            operationCache.getOrPut(fieldName) { indexCreator() } as SimpleIndex<T>
        }
    }

    private fun getOrCreateFullTextIndex(fieldName: String): FullTextIndex {
        return synchronized(fullTextIndexCache) {
            fullTextIndexCache.getOrPut(fieldName) { FullTextIndex(entries, fieldName) }
        }
    }

    private fun safeGetInstant(dateText: String?, dateCache: ConcurrentHashMap<String, OffsetDateTime>): Instant? {
        return dateText?.let {
            runCatching { getInstant(it, dateCache) }.getOrNull()
        }
    }

    private fun getInstant(dateText: String, dataCache: ConcurrentHashMap<String, OffsetDateTime>): Instant {
        val offsetDateTime = dataCache[dateText] ?: EvaluatorUtil.parseDate(dateText, dataCache)
        return offsetDateTime.toInstant()
    }


    private fun getAllFields(entries: List<Map<String, String>>): Set<String> {
        val allFields = mutableSetOf<String>()

        for (entry in entries) {
            for (key in entry.keys) {
                allFields.add(key)
            }
        }

        return allFields
    }

}
