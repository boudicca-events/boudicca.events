package base.boudicca.model

import base.boudicca.model.structured.KeyUtils
import base.boudicca.model.structured.StructuredEntry
import java.util.Optional

/**
 * a simple, unparsed, entry. used mainly for serializing and sending/receiving it. for actually working with the values please consider transforming it into a [StructuredEntry]
 */
typealias Entry = Map<String, String>

fun Entry.toStructuredEntry(): StructuredEntry {
    return KeyUtils.toStructuredKeyValuePairs(this)
}

fun Entry.toEvent(): Optional<Event> {
    return Event.fromEntry(this)
}
