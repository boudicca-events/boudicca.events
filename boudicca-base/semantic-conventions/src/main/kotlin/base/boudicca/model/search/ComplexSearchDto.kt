package base.boudicca.model.search

data class ComplexSearchDto(
    val anyKeyExactMatch: Set<String>? = setOf(),
    val allKeyExactMatch: Set<String>? = setOf(),
    val anyKeyOrValueContains: Set<String>? = setOf(),
    val allKeyOrValueContains: Set<String>? = setOf(),
    val anyKeyOrValueExactMatch: Set<String>? = setOf(),
    val allKeyOrValueExactMatch: Set<String>? = setOf(),
    val anyValueForKeyContains: Set<List<String>>? = setOf(),
    val allValueForKeyContains: Set<List<String>>? = setOf(),
    val anyValueForKeyExactMatch: Set<List<String>>? = setOf(),
    val allValueForKeyExactMatch: Set<List<String>>? = setOf(),
)