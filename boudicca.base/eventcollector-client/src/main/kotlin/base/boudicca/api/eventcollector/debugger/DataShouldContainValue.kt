package base.boudicca.api.eventcollector.debugger

import base.boudicca.api.eventcollector.debugger.color.blue
import base.boudicca.api.eventcollector.debugger.color.red
import base.boudicca.api.eventcollector.debugger.color.yellow
import base.boudicca.model.Event

class DataShouldContainValue(
    private val key: String,
    private val format: Regex,
    private val severity: ValidationSeverity,
) : EventCollectorValidation {
    override fun validate(
        event: Event,
        verbose: Boolean,
    ): ValidationResult {
        if (!event.data[key]?.contains(format)!!) {
            when (severity) {
                ValidationSeverity.Info -> {
                    println("INFO: key $key expected to match format $format".blue())
                }

                ValidationSeverity.Warn -> {
                    println("WARN: key $key expected to match format $format".yellow())
                }

                ValidationSeverity.Error -> {
                    println("ERR: key $key expected to match format $format".red())
                }
            }
            return severity.result
        }
        return ValidationResult.Ok
    }
}
