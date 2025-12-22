package base.boudicca.api.eventcollector.debugger

import base.boudicca.model.Event

class CustomValidation(
    private val validationFunction: (event: Event, verbose: Boolean, severity: ValidationSeverity) -> ValidationResult,
    private val severity: ValidationSeverity,
) : EventCollectorValidation {
    override fun validate(
        event: Event,
        verbose: Boolean,
    ): ValidationResult = validationFunction(event, verbose, severity)
}
