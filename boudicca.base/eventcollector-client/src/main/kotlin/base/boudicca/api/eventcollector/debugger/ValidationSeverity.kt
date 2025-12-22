package base.boudicca.api.eventcollector.debugger

enum class ValidationSeverity(
    val result: ValidationResult,
) {
    // Important: keep the order for comparability
    Error(ValidationResult.Error),
    Warn(ValidationResult.Warn),
    Info(ValidationResult.Info),
}
