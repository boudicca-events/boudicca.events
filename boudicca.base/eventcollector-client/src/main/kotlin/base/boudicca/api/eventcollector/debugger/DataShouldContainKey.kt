package base.boudicca.api.eventcollector.debugger

import base.boudicca.api.eventcollector.debugger.color.blue
import base.boudicca.api.eventcollector.debugger.color.red
import base.boudicca.api.eventcollector.debugger.color.yellow
import base.boudicca.model.Event

class DataShouldContainKey(
    val key: String,
    val severity: ValidationSeverity,
): EventCollectorValidation {
    override fun validate(event: Event, verbose: Boolean): ValidationResult {
        if (!event.data.containsKey(key)) {
            when (severity) {
                ValidationSeverity.Info ->
                    println("INFO: should have key $key".blue())

                ValidationSeverity.Warn ->
                    println("WARN: expected key $key".yellow())

                ValidationSeverity.Error ->
                    println("ERR: expected key $key".red())
            }
            return severity.result
        }
        return ValidationResult.Ok
    }
}