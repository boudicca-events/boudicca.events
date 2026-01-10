package base.boudicca.api.eventcollector.ai.service

/**
 * A description of an enum type in text form that can be passed to LLMs.
 */
data class EnumDescription<T>(
    val values: List<EnumValueDescription<T>>,
    val description: String,
)

/**
 * A description of the specific enum value that can be passed to LLMs.
 */
data class EnumValueDescription<T>(
    val value: T,
    val description: String,
)
