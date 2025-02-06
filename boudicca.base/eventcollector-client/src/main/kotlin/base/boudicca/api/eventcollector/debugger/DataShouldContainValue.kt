package base.boudicca.api.eventcollector.debugger

import base.boudicca.api.eventcollector.debugger.color.blue
import base.boudicca.api.eventcollector.debugger.color.red
import base.boudicca.api.eventcollector.debugger.color.yellow
import base.boudicca.model.Event

class DataShouldContainValue(
    val key: String,
    val value: Regex,
    val severity: ValidationSeverity,
) : EventCollectorValidation {
    override fun validate(event: Event, verbose: Boolean): ValidationResult {
        if (!event.data[key]?.contains(value)!!) {
            when (severity) {
                ValidationSeverity.Info ->
                    println("INFO: key $key expected to contain $value".blue())

                ValidationSeverity.Warn ->
                    println("WARN: key $key expected to contain $value".yellow())

                ValidationSeverity.Error ->
                    println("ERR: key $key expected to contain $value".red())
            }
            return severity.result
        }
        return ValidationResult.Ok
    }
}
