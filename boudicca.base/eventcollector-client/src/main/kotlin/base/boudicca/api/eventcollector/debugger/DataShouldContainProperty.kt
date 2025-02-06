package base.boudicca.api.eventcollector.debugger

import base.boudicca.Property
import base.boudicca.api.eventcollector.debugger.color.blue
import base.boudicca.api.eventcollector.debugger.color.red
import base.boudicca.api.eventcollector.debugger.color.yellow
import base.boudicca.model.Event

class DataShouldContainProperty(
    private val property: Property<*>,
    private val severity: ValidationSeverity,
) : EventCollectorValidation {
    override fun validate(event: Event, verbose: Boolean): ValidationResult {
        if (event.toStructuredEvent().getProperty(property).isEmpty()) {
            when (severity) {
                ValidationSeverity.Info ->
                    println("INFO: should have property ${property.getKey()}".blue())

                ValidationSeverity.Warn ->
                    println("WARN: expected property ${property.getKey()}".yellow())

                ValidationSeverity.Error ->
                    println("ERR: expected property ${property.getKey()}".red())
            }
            return severity.result
        }
        return ValidationResult.Ok
    }
}
