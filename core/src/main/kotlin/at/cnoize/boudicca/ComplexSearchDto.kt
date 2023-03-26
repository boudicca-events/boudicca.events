package at.cnoize.boudicca

data class ComplexSearchDto (
    val anyKeyExactMatch: Set<String>? = setOf(),
    val allKeyExactMatch: Set<String>? = setOf(),
    val anyKeyOrValueContains: Set<String>? = setOf(),
    val allKeyOrValueContains: Set<String>? = setOf(),
    val anyKeyOrValueExactMatch: Set<String>? = setOf(),
    val allKeyOrValueExactMatch: Set<String>? = setOf(),
    val anyValueForKeyContains: Set<Pair<String, String>>? = setOf(),
    val allValueForKeyContains: Set<Pair<String, String>>? = setOf(),
    val anyValueForKeyExactMatch: Set<Pair<String, String>>? = setOf(),
    val allValueForKeyExactMatch: Set<Pair<String, String>>? = setOf(),
)
