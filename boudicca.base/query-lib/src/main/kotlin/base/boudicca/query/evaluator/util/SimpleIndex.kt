package base.boudicca.query.evaluator.util

import java.util.*

class SimpleIndex<T>(values: List<Pair<Int, T>>, comparator: Comparator<T>) {
    private val index =
        values
            .filter { it.second != null }
            .sortedWith(Comparator.comparing({ pair -> pair.second }, comparator))

    fun search(comparator: (T) -> Int): BitSet {
        val lower =
            EvaluatorUtil.binarySearch(0, index.size) { i ->
                val result = comparator.invoke(index[i].second)
                if (result == 0) {
                    if (i - 1 < 0 || comparator.invoke(index[i - 1].second) != 0) {
                        0
                    } else {
                        1
                    }
                } else {
                    result
                }
            }
        if (lower == -1) {
            return BitSet()
        }

        val upper =
            EvaluatorUtil.binarySearch(0, index.size) { i ->
                val result = comparator.invoke(index[i].second)
                if (result == 0) {
                    if (i + 1 >= index.size || comparator.invoke(index[i + 1].second) != 0) {
                        0
                    } else {
                        -1
                    }
                } else {
                    result
                }
            }

        val result = BitSet()
        for (i in lower..upper) {
            result.set(index[i].first)
        }
        return result
    }

    companion object {
        fun <T> create(values: List<T>, comparator: Comparator<T>): SimpleIndex<T> = SimpleIndex(values.mapIndexed { i, value -> Pair(i, value) }, comparator)
    }
}
