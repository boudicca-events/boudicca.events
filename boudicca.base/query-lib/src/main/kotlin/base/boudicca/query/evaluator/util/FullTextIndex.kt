package base.boudicca.query.evaluator.util

import base.boudicca.format.ListFormatAdapter
import base.boudicca.model.Entry
import base.boudicca.model.structured.Key
import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.text.BreakIterator
import java.util.*

class FullTextIndex(entries: List<Entry>, field: String) {

    private val words = getWords(entries, field)
    private val index = createIndex()

    fun get(i: Int): Pair<Int, Int> {
        return Pair(index.getInt(i * 8), index.getInt(i * 8 + 4))
    }

    fun size(): Int {
        return index.capacity() / 8
    }

    fun getEntriesForWord(i: Int): BitSet {
        return words[i].second
    }

    fun containsSearch(text: String): BitSet {
        val searchWords = breakText(text.lowercase())
        val subResults = searchWords.map { word ->
            val (lower, upper) = containsSearchIndices(word)

            val result = BitSet()
            for (i in lower until upper) {
                result.or(words[get(i).first].second)
            }
            result
        }

        if (subResults.isEmpty()) {
            return BitSet()
        }

        val result = subResults.first()
        for (subResult in subResults.drop(1)) {
            result.and(subResult)
        }

        return result
    }

    private fun containsSearchIndices(lowerText: CharBuffer): Pair<Int, Int> {
        val lower = binarySearch { i ->
            val matches = startsWith(i, lowerText)
            if (matches) {
                if (i - 1 < 0 || !startsWith(i - 1, lowerText)) {
                    0
                } else {
                    1
                }
            } else {
                val (vI, sI) = get(i)
                val word = words[vI].first
                word.subSequence(sI, word.capacity()).compareTo(lowerText)
            }
        }
        if (lower == -1) {
            //nothing found
            return Pair(-1, -1)
        }
        val upper = binarySearch { i ->
            val matches = startsWith(i, lowerText)
            if (matches) {
                if (i + 1 >= size() || !startsWith(i + 1, lowerText)) {
                    0
                } else {
                    -1
                }
            } else {
                val (vI, sI) = get(i)
                val word = words[vI].first
                word.subSequence(sI, word.capacity()).compareTo(lowerText)
            }
        }
        return Pair(lower, upper + 1)
    }

    private fun startsWith(i: Int, lowerPrefix: CharBuffer): Boolean {
        val (vI, sI) = get(i)
        return words[vI].first.startsWith(lowerPrefix, sI, false) //ignore case already done by lowering everything
    }

    private fun binarySearch(comparator: (Int) -> Int): Int {
        return EvaluatorUtil.binarySearch(0, size(), comparator)
    }

    private fun getWords(entries: List<Entry>, field: String): List<Pair<CharBuffer, BitSet>> {
        val key = Key.parse(field)
        val words = mutableMapOf<CharBuffer, BitSet>()
        entries.forEachIndexed { entryIndex, entry ->
            if (!entry[field].isNullOrEmpty()) {
                val entryValue = entry[field]!!
                val values = if (EvaluatorUtil.isList(key)) {
                    ListFormatAdapter().fromString(entryValue)
                } else {
                    listOf(entryValue)
                }
                val newWords = values.flatMap { breakText(it.lowercase()) }

                newWords.forEach { newWord ->
                    if (words.containsKey(newWord)) {
                        words[newWord]!!.set(entryIndex)
                    } else {
                        val bitset = BitSet()
                        bitset.set(entryIndex)
                        words[newWord] = bitset
                    }
                }
            }
        }

        return words.toList()
    }

    private fun breakText(lowercase: String): MutableList<CharBuffer> {
        val iter = BreakIterator.getWordInstance(Locale.GERMAN)
        iter.setText(lowercase)
        var breakI = 0
        var newBreakI = iter.next()
        val newWords = mutableListOf<CharBuffer>()
        while (newBreakI != BreakIterator.DONE) {
            val newWord = CharBuffer.wrap(lowercase.substring(breakI, newBreakI).trim())
            newWords.add(newWord)
            breakI = newBreakI
            newBreakI = iter.next()
        }
        return newWords
    }

    private fun createIndex(): ByteBuffer {
        var count = 0
        words.forEach { (word, _) ->
            count += word.capacity()
        }

        val index = ByteBuffer.allocate(count * 8)

        words.forEachIndexed { wordI, word ->
            for (stringI in word.first.indices) {
                val i = --count
                index.putInt(i * 8, wordI)
                index.putInt(i * 8 + 4, stringI)
            }
        }

        return sort(index)
    }

    private fun sort(index: ByteBuffer): ByteBuffer {
        EvaluatorUtil.sort(
            0, index.capacity() / 8,
            SortableByteBuffer(index, words)
        )
        return index
    }

    class SortableByteBuffer(private val byteBuffer: ByteBuffer, private val values: List<Pair<CharBuffer, BitSet>>) :
        EvaluatorUtil.Sortable<ByteBuffer> {
        override fun get(): ByteBuffer {
            return byteBuffer
        }

        override fun copy(): EvaluatorUtil.Sortable<ByteBuffer> {
            return SortableByteBuffer(ByteBuffer.wrap(byteBuffer.array().copyOf()), values)
        }

        override fun compare(i: Int, j: Int): Int {
            val vI1 = byteBuffer.getInt(i * 8)
            val sI1 = byteBuffer.getInt(i * 8 + 4)
            val vI2 = byteBuffer.getInt(j * 8)
            val sI2 = byteBuffer.getInt(j * 8 + 4)
            val word1 = values[vI1].first
            val subValue1 = word1.subSequence(sI1, word1.capacity())
            val word2 = values[vI2].first
            val subValue2 = word2.subSequence(sI2, word2.capacity())
            return subValue1.compareTo(subValue2)
        }

        override fun swap(i: Int, j: Int) {
            val vI = byteBuffer.getInt(i * 8)
            val sI = byteBuffer.getInt(i * 8 + 4)
            byteBuffer.putInt(i * 8, byteBuffer.getInt(j * 8))
            byteBuffer.putInt(i * 8 + 4, byteBuffer.getInt(j * 8 + 4))
            byteBuffer.putInt(j * 8, vI)
            byteBuffer.putInt(j * 8 + 4, sI)
        }

        override fun setFrom(i: Int, src: EvaluatorUtil.Sortable<ByteBuffer>, j: Int) {
            val otherByteBuffer = src.get()
            byteBuffer.putInt(i * 8, otherByteBuffer.getInt(j * 8))
            byteBuffer.putInt(i * 8 + 4, otherByteBuffer.getInt(j * 8 + 4))
        }

    }
}

