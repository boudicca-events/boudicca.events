package base.boudicca.api.eventcollector.debugger

import base.boudicca.model.Event

fun interface EventCollectorValidation {
    fun validate(event: Event, verbose: Boolean): ValidationResult
}
