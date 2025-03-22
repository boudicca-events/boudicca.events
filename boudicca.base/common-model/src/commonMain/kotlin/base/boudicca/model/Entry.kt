package base.boudicca.model;

import base.boudicca.model.structured.KeyUtils
import base.boudicca.model.structured.StructuredEntry
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * a simple, unparsed, entry. used mainly for serializing and sending/receiving it. for actually working with the values please consider transforming it into a [StructuredEntry]
 */
typealias Entry = Map<String, String>

@OptIn(ExperimentalJsExport::class)
@JsExport
fun Entry.toStructuredEntry(): StructuredEntry {
    return KeyUtils.toStructuredKeyValuePairs(this)
}

@OptIn(ExperimentalJsExport::class)
@JsExport
fun Entry.toEvent(): Event? {
    return Event.fromEntry(this)
}
