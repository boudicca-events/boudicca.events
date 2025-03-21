package base.boudicca.model

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * supported values for the recurrence.type property
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
enum class RecurrenceType {
    ONCE, RARELY, REGULARLY
}
