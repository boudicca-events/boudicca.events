package base.boudicca.api.eventcollector.debugger

import base.boudicca.model.Event

class CustomValidation(
    val validationFunction: (event: Event, verbose: Boolean) -> ValidationResult,
    val severity: ValidationSeverity,
) : EventCollectorValidation {
    override fun validate(event: Event, verbose: Boolean): ValidationResult {
        return validationFunction(event, verbose)
    }
}