package base.boudicca.model

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * supported values for the registration property
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
enum class Registration {
    TICKET, PRE_SALES_ONLY, REGISTRATION, FREE
}
