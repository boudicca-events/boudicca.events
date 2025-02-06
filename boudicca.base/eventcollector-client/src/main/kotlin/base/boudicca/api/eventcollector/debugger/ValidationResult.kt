package base.boudicca.api.eventcollector.debugger

enum class ValidationResult {
    // important: keep the order for comparability
    Error,
    Warn,
    Info,
    Ok,
}
