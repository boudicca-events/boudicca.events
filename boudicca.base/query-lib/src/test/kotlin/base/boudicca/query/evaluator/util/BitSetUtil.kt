package base.boudicca.query.evaluator.util

import java.util.BitSet

fun bitsetOf(vararg indexes: Int): BitSet {
    val bitset = BitSet()
    for (i in indexes) {
        bitset.set(i)
    }
    return bitset
}
