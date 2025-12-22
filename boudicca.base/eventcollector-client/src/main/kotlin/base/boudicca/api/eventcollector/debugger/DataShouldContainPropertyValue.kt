package base.boudicca.api.eventcollector.debugger

import base.boudicca.Property
import base.boudicca.api.eventcollector.debugger.color.blue
import base.boudicca.api.eventcollector.debugger.color.red
import base.boudicca.api.eventcollector.debugger.color.yellow
import base.boudicca.model.Event

class DataShouldContainPropertyValue<T>(private val property: Property<T>, private val value: T, private val severity: ValidationSeverity) : EventCollectorValidation {
    override fun validate(event: Event, verbose: Boolean): ValidationResult {
        val key = property.getKey()
        if (event.toStructuredEvent().getProperty(property).none { it == value }) {
            when (severity) {
                ValidationSeverity.Info ->
                    println("INFO: property $key expected to contain $value".blue())

                ValidationSeverity.Warn ->
                    println("WARN: property $key expected to contain $value".yellow())

                ValidationSeverity.Error ->
                    println("ERR: property $key expected to contain $value".red())
            }
            return severity.result
        }
        return ValidationResult.Ok
    }
}
