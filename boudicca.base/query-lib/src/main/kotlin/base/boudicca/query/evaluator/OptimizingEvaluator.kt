package base.boudicca.query.evaluator

import base.boudicca.format.ListFormatAdapter
import base.boudicca.keyfilters.KeyFilters
import base.boudicca.model.Entry
import base.boudicca.model.structured.Key
import base.boudicca.model.structured.KeyFilter
import base.boudicca.model.structured.filterKeys
import base.boudicca.model.toStructuredEntry
import base.boudicca.query.*
import base.boudicca.query.evaluator.util.EvaluatorUtil
import base.boudicca.query.evaluator.util.FullTextIndex
import base.boudicca.query.evaluator.util.SimpleIndex
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException
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
            // roughly, if the resultset is bigger then a third of the total entries,
            // it is faster to iterate over all entries then to sort the resultset
            entries.filterIndexed { i, _ -> resultSet.get(i) }
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

    private fun evaluateExpression(expression: Expression): BitSet {
        when (expression) {
            is EqualsExpression -> {
                return equalsExpression(expression)
            }

            is ContainsExpression -> {
                return containsExpression(expression)
            }

            is NotExpression -> {
                return notExpression(expression)
            }

            is AndExpression -> {
                return andExpression(expression)
            }

            is OrExpression -> {
                return orExpression(expression)
            }

            is BeforeExpression -> {
                return beforeExpression(expression)
            }

            is AfterExpression -> {
                return afterExpression(expression)
            }

            is DurationLongerExpression -> {
                return durationLongerExpression(expression)
            }

            is DurationShorterExpression -> {
                return durationShorterExpression(expression)
            }

            is HasFieldExpression -> {
                return hasFieldExpression(expression)
            }

            is IsInNextSecondsExpression -> {
                return isInNextSecondsExpression(expression)
            }

            is IsInLastSecondsExpression -> {
                return isInLastSecondsExpression(expression)
            }

            else -> {
                throw QueryException("unknown expression kind $expression")
            }
        }
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
            if (it != null) {
                if (it >= duration) {
                    0
                } else {
                    -1
                }
            } else {
                -1
            }
        }
    }

    private fun durationShorterExpression(expression: DurationShorterExpression): BitSet {
        //TODO this still does not work 100%, we need a completely different handling here
        val index = getDurationIndex(expression)
        val duration = expression.getDuration().toDouble()
        return index.search {
            if (it != null) {
                if (it <= duration) {
                    0
                } else {
                    1
                }
            } else {
                -1
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
                if (it != null) {
                    if (it == expressionStartDate || it == expressionEndDate) {
                        0
                    } else if (!it.isAfter(expressionStartDate)) {
                        -1
                    } else if (!it.isBefore(expressionEndDate)) {
                        1
                    } else {
                        0
                    }
                } else {
                    -1
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
            if (!simpleIndexCache.containsKey(operation)) {
                val newCache = mutableMapOf<String, SimpleIndex<*>>()
                simpleIndexCache[operation] = newCache
                newCache
            } else {
                simpleIndexCache[operation]!!
            }
        }

        synchronized(operationCache) {
            val index = if (!operationCache.containsKey(fieldName)) {
                val index = indexCreator()
                operationCache[fieldName] = index
                index
            } else {
                operationCache[fieldName]!!
            }

            return index as SimpleIndex<T>
        }
    }

    private fun getOrCreateFullTextIndex(fieldName: String): FullTextIndex {
        synchronized(fullTextIndexCache) {
            if (fullTextIndexCache.containsKey(fieldName)) {
                return fullTextIndexCache[fieldName]!!
            } else {
                val index = FullTextIndex(entries, fieldName)
                fullTextIndexCache[fieldName] = index
                return index
            }
        }
    }

    private fun safeGetInstant(dateText: String?, dateCache: ConcurrentHashMap<String, OffsetDateTime>): Instant? {
        if (dateText == null) {
            return null
        }
        try {//TODO cache null
            return getInstant(dateText, dateCache)
        } catch (e: DateTimeParseException) {
            return null
        }
    }

    private fun getInstant(dateText: String, dataCache: ConcurrentHashMap<String, OffsetDateTime>): Instant {
        val offsetDateTime = if (dataCache.containsKey(dateText)) {
            dataCache[dateText]!!
        } else {
            EvaluatorUtil.parseDate(dateText, dataCache)
        }
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
