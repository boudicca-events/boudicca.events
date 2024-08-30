package base.boudicca.model;

import base.boudicca.model.structured.KeyUtils
import base.boudicca.model.structured.StructuredEntry
import java.util.*

typealias Entry = Map<String, String>

fun Entry.toStructuredEntry(): StructuredEntry {
    return KeyUtils.toStructuredKeyValuePairs(this)
}

fun Entry.toEvent(): Optional<Event> {
    return Event.fromEntry(this)
}