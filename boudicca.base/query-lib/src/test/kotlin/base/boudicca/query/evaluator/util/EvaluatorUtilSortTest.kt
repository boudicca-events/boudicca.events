package base.boudicca.query.evaluator.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class EvaluatorUtilSortTest {
    @Test
    fun testEmptyList() {
        testWithList(
            listOf(),
        )
    }

    @Test
    fun testSingleList() {
        testWithList(
            listOf(1),
        )
    }

    @Test
    fun testSmallList() {
        testWithList(
            listOf(6, 1, 4),
        )
    }

    @Test
    fun testBiggerList() {
        testWithList(
            listOf(6, 1, 4, 5, 7, 1, 65, 9, 0, 3, 7, 2, 5, 57, 45, 23, 53, 3, 345, 1, 52, 2),
        )
    }

    private fun testWithList(list: List<Int>) {
        val solution = list.sorted()

        val toSortList = list.toMutableList()

        EvaluatorUtil.sort(0, list.size, SortableList(toSortList))

        assertEquals(solution, toSortList)
    }

    class SortableList(
        private val list: MutableList<Int>,
    ) : EvaluatorUtil.Sortable<MutableList<Int>> {
        override fun get(): MutableList<Int> = list

        override fun copy(): EvaluatorUtil.Sortable<MutableList<Int>> = SortableList(list.toMutableList())

        override fun compare(
            i: Int,
            j: Int,
        ): Int = list[i].compareTo(list[j])

        override fun swap(
            i: Int,
            j: Int,
        ) {
            val v1 = list[i]
            list[i] = list[j]
            list[j] = v1
        }

        override fun setFrom(
            i: Int,
            src: EvaluatorUtil.Sortable<MutableList<Int>>,
            j: Int,
        ) {
            val otherList = src.get()
            list[i] = otherList[j]
        }
    }
}
