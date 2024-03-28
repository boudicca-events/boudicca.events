package base.boudicca.api.eventcollector.debugger

enum class ValidationSeverity(val result: ValidationResult) {
    Info(ValidationResult.Info),
    Warn(ValidationResult.Warn),
    Error(ValidationResult.Error),
}