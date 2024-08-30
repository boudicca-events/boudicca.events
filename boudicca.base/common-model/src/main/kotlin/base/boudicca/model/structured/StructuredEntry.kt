package base.boudicca.model.structured

import base.boudicca.model.Entry
import java.util.*


typealias StructuredEntry = Map<Key, String>

fun StructuredEntry.toFlatEntry(): Entry {
    return KeyUtils.toFlatKeyValuePairs(this)
}

fun StructuredEntry.toEvent(): Optional<StructuredEvent> {
    return StructuredEvent.fromEntry(this)
}
