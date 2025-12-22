package base.boudicca.api.eventcollector.debugger

import base.boudicca.Property
import base.boudicca.api.eventcollector.debugger.color.blue
import base.boudicca.api.eventcollector.debugger.color.red
import base.boudicca.api.eventcollector.debugger.color.yellow
import base.boudicca.model.Event

class DataShouldContainPropertyWithFormat(
    private val property: Property<*>,
    private val format: Regex,
    private val severity: ValidationSeverity,
) : EventCollectorValidation {
    override fun validate(
        event: Event,
        verbose: Boolean,
    ): ValidationResult {
        val key = property.getKeyFilter()
        if (event.toStructuredEvent().filterKeys(key).none { it.second.contains(format) }) {
            when (severity) {
                ValidationSeverity.Info -> {
                    println("INFO: property $key expected to match format $format".blue())
                }

                ValidationSeverity.Warn -> {
                    println("WARN: property $key expected to match format $format".yellow())
                }

                ValidationSeverity.Error -> {
                    println("ERR: property $key expected to match format $format".red())
                }
            }
            return severity.result
        }
        return ValidationResult.Ok
    }
}
