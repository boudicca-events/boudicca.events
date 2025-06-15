package base.boudicca.query.evaluator.util

import base.boudicca.format.DateFormatAdapter
import base.boudicca.keyfilters.KeyFilters
import base.boudicca.keyfilters.KeySelector
import base.boudicca.model.structured.*
import java.time.Duration
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import java.util.concurrent.ConcurrentHashMap
import kotlin.jvm.optionals.getOrNull

private val MILLIS_PER_HOUR = Duration.ofHours(1).toMillis()

object EvaluatorUtil {
    fun getDuration(
        startDateKeyFilter: KeyFilter,
        endDateKeyFilter: KeyFilter,
        entry: StructuredEntry,
        dataCache: ConcurrentHashMap<String, OffsetDateTime>
    ): Double {
        val startDateText = selectDateValue(entry, startDateKeyFilter)
        val endDateText = selectDateValue(entry, endDateKeyFilter)
        if (startDateText.isNullOrEmpty() || endDateText.isNullOrEmpty()) {
            return 0.0
        }
        return try {
            val startDate = parseDate(startDateText, dataCache)
            val endDate = parseDate(endDateText, dataCache)
            Duration.of(endDate.toEpochSecond() - startDate.toEpochSecond(), ChronoUnit.SECONDS)
                .toMillis()
                .toDouble() / MILLIS_PER_HOUR
        } catch (e: DateTimeParseException) {
            0.0
        }
    }

    private fun selectDateValue(entry: StructuredEntry, keyFilter: KeyFilter): String? {
        return KeySelector.builder(keyFilter)
            .thenVariant(
                VariantConstants.FORMAT_VARIANT_NAME,
                listOf(
                    VariantConstants.FormatVariantConstants.DATE_FORMAT_NAME,
                    VariantConstants.FormatVariantConstants.TEXT_FORMAT_NAME
                )
            ).build()
            .selectSingle(entry)
            .map { it.second }
            .getOrNull()
    }

    fun getDateValues(entry: StructuredEntry, dateKeyFilter: KeyFilter): List<String> {
        return entry
            .filterKeys(dateKeyFilter)
            .filter { isDate(it.first) }
            .map { it.second }
    }

    fun parseDate(
        dateText: String,
        dataCache: ConcurrentHashMap<String, OffsetDateTime>
    ): OffsetDateTime {
        return if (dataCache.containsKey(dateText)) {
            dataCache[dateText]!!
        } else {
//            try {
            val parsedDate = DateFormatAdapter().fromString(dateText)
            dataCache[dateText] = parsedDate
            parsedDate
//            } catch (e: DateTimeParseException) {
//                dataCache[dateText] = null //TODO make nullable cache
//                null
//            }
        }
    }

    fun binarySearch(start: Int, length: Int, comparator: (Int) -> Int): Int {
        var lower = start
        var upper = start + length - 1

        while (lower <= upper) {
            val i = (lower + upper) / 2
            val result = comparator.invoke(i)
            if (result == 0) {
                return i
            } else if (result < 0) {
                lower = i + 1
            } else {
                upper = i - 1
            }
        }
        return -1
    }

    //impl copied from Arrays.mergeSort....
    fun <T> sort(start: Int, length: Int, sortable: Sortable<T>) {
        val aux = sortable.copy()
        mergeSort(aux, sortable, start, start + length, -start)
    }

    interface Sortable<T> {
        fun get(): T
        fun copy(): Sortable<T>
        fun compare(i: Int, j: Int): Int
        fun swap(i: Int, j: Int)
        fun setFrom(i: Int, src: Sortable<T>, j: Int)
    }

    private const val INSERTIONSORT_THRESHOLD = 7

    private fun <T> mergeSort(
        src: Sortable<T>,
        dest: Sortable<T>,
        low: Int,
        high: Int,
        off: Int
    ) {
        var low = low
        var high = high
        val length = high - low

        // Insertion sort on smallest arrays
        if (length < INSERTIONSORT_THRESHOLD) {
            for (i in low until high) {
                var j = i
                while (j > low && dest.compare(j - 1, j) > 0) {
                    dest.swap(j, j - 1)
                    j--
                }
            }
            return
        }

        // Recursively sort halves of dest into src
        val destLow = low
        val destHigh = high
        low += off
        high += off
        val mid = (low + high) ushr 1
        mergeSort(dest, src, low, mid, -off)
        mergeSort(dest, src, mid, high, -off)

        //TODO maybe?
        // If list is already sorted, just copy from src to dest.  This is an
        // optimization that results in faster sorts for nearly ordered lists.
//        if ((src[mid - 1] as Comparable<*>).compareTo(src[mid]) <= 0) {
//            System.arraycopy(src, low, dest, destLow, length)
//            return
//        }

        // Merge sorted halves (now in src) into dest
        var i = destLow
        var p = low
        var q = mid
        while (i < destHigh) {
            if (q >= high || p < mid && (src.compare(p, q) <= 0)) dest.setFrom(i, src, p++)
            else dest.setFrom(i, src, q++)
            i++
        }
    }

    fun isTextMarkdownOrList(key: Key): Boolean {
        return KeyFilters.doesContainVariantValue(
            key, VariantConstants.FORMAT_VARIANT_NAME,
            listOf(
                VariantConstants.FormatVariantConstants.TEXT_FORMAT_NAME,
                VariantConstants.FormatVariantConstants.MARKDOWN_FORMAT_NAME,
                VariantConstants.FormatVariantConstants.LIST_FORMAT_NAME,
            )
        )
    }

    fun isList(key: Key): Boolean {
        return KeyFilters.doesContainVariantValue(
            key, VariantConstants.FORMAT_VARIANT_NAME,
            listOf(
                VariantConstants.FormatVariantConstants.LIST_FORMAT_NAME,
            )
        )
    }

    fun isDate(key: Key): Boolean {
        return KeyFilters.doesContainVariantValue(
            key, VariantConstants.FORMAT_VARIANT_NAME,
            listOf(
                VariantConstants.FormatVariantConstants.DATE_FORMAT_NAME,
                VariantConstants.FormatVariantConstants.TEXT_FORMAT_NAME, //fallback for now...
            )
        )
    }
}
