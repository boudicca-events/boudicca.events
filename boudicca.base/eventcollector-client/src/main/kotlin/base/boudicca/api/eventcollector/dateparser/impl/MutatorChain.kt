package base.boudicca.api.eventcollector.dateparser.impl

import base.boudicca.api.eventcollector.dateparser.DateParserResult


internal class CheckIfSolvableStep : MutatorChain {
    override fun mutate(guess: Guess): DateParserResult? {
        return guess.solve()
    }
}

internal class FixedPatternStep(val next: MutatorChain) : MutatorChain {
    override fun mutate(guess: Guess): DateParserResult? {
        if (guess is Guesses) {
            val applied = Patterns.apply(guess.guesses, Patterns.PATTERNS_FIXED)
            val result = next.mutate(Guesses(applied))
            if (result != null) {
                return result
            }
        }
        return next.mutate(guess)
    }
}

internal class GroupingStep(val next: MutatorChain) : MutatorChain {
    override fun mutate(guess: Guess): DateParserResult? {
        var result = next.mutate(guess)
        if (result != null) {
            return result
        }
        if (guess is CanBeMapped) {
            val allGuesses = guess.getAllNestedGuesses()
            val joinedSeparatorsLists = allGuesses.map { joinAllSeparators(it) }
            val allSeparatorValues = collectSeparatorValues(joinedSeparatorsLists)
            for (separatorThreshold in allSeparatorValues) {
                val newGuess = guess.mapAllNestedGuesses { guess ->
                    val groups = mutableListOf<Guesses>()
                    var currentGroup = mutableListOf<Component>()
                    val joinedSeparatorsList = joinAllSeparators(guess)
                    for (component in joinedSeparatorsList) {
                        when (component) {
                            is Separator -> {
                                if (component.weight >= separatorThreshold) {
                                    if (currentGroup.isNotEmpty()) {
                                        groups.add(Guesses(currentGroup))
                                        currentGroup = mutableListOf()
                                    }
                                } else {
                                    currentGroup.add(component)
                                }
                            }

                            else -> currentGroup.add(component)
                        }
                    }
                    if (currentGroup.isNotEmpty()) {
                        groups.add(Guesses(currentGroup))
                    }
                    return@mapAllNestedGuesses Grouping(groups)
                }
                result = next.mutate(newGuess)
                if (result != null) {
                    return result
                }
            }
        }
        return null
    }

    private fun joinAllSeparators(guess: Guesses): List<Component> {
        val result = mutableListOf<Component>()
        var currentSeparatorWeight = 0

        for (component in guess.guesses) {
            when (component) {
                is Separator -> currentSeparatorWeight += component.weight
                is Any -> {
                    if (component.possibleTypes.isEmpty()) {
                        currentSeparatorWeight += calculateSeparatorWeight(component.value)
                    } else {
                        if (currentSeparatorWeight > 0) {
                            if (result.isNotEmpty()) {
                                result.add(Separator(currentSeparatorWeight))
                            }
                            currentSeparatorWeight = 0
                        }
                        result.add(component)
                    }
                }
            }
        }

        return result
    }

    private fun calculateSeparatorWeight(value: String): Int {
        return value.map {
            when (it) {
                '.', '-', '/', ':' -> 1
                ' ' -> 2
                else -> 3
            }
        }.sum()
    }

    private fun collectSeparatorValues(components: List<List<Component>>): List<Int> {
        return components.asSequence()
            .flatten()
            .filterIsInstance<Separator>()
            .map { it.weight }
            .distinct()
            .sortedDescending()
            .toList()
    }
}

internal class UntilStep(val next: MutatorChain) : MutatorChain {
    override fun mutate(guess: Guess): DateParserResult? {
        var result = next.mutate(guess)
        if (result != null) {
            return result
        }
        if (guess is Guesses) {
            val untilIndexes = findAllUntilIndexes(guess)
            for (untilIndex in untilIndexes) {
                if (untilIndex == 0 || untilIndex == guess.guesses.size - 1) {
                    continue
                }
                val tryUntil = listOf(
                    Guesses(guess.guesses.take(untilIndex)), Guesses(guess.guesses.drop(untilIndex + 1))
                )
                result = next.mutate(Until(tryUntil))
                if (result != null) {
                    return result
                }
            }
        }
        return null
    }

    private fun findAllUntilIndexes(guess: Guesses): List<Int> {
        return guess.guesses.mapIndexed { i, component -> Pair(i, component) }
            .filter { (_, component) -> isUntil(component) }
            .map { it.first }
    }

    private fun isUntil(component: Component): Boolean {
        if (component is Any) {
            val value = component.value.trim()
            if (setOf("-", "bis").contains(value)) {
                return true
            }
        }
        return false
    }
}

internal interface MutatorChain {
    fun mutate(guess: Guess): DateParserResult?
}
