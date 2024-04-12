package base.boudicca.query.evaluator.util

class SimpleIndex<T>(values: List<T>, comparator: Comparator<T>) {
    private val index = values
        .mapIndexed { index, t -> Pair(index, t) }
        .filter { it.second != null }
        .sortedWith(Comparator.comparing({ pair -> pair.second }, comparator))

    fun search(comparator: (T) -> Int): Set<Int> {
        val lower = EvaluatorUtil.binarySearch(0, index.size) { i ->
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
            return emptySet()
        }

        val upper = EvaluatorUtil.binarySearch(0, index.size) { i ->
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

        val result = mutableSetOf<Int>()
        for (i in lower..upper) {
            result.add(index[i].first)
        }
        return result
    }

}

