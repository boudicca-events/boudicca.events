package base.boudicca.api.eventcollector.dateparser.impl

import base.boudicca.api.eventcollector.dateparser.DateParserResult


internal class Guesser(private val tokenGroups: List<List<Pair<TokenizerType, String>>>) {

    fun guess(): DateParserResult {
        val guesses = tokenGroups.map { mapAndValidateTokens(it) }
            .reduce { acc, guesses -> acc + listOf(Separator(10000)) + guesses }

        val chain = buildChain()
        val result = chain.mutate(Guesses(guesses))
        if (result != null) {
            return result
        }
        throw IllegalArgumentException("could not solve, blabla") //TODO
    }

    private fun buildChain(): MutatorChain {
        return FixedPatternStep(UntilStep(GroupingStep(CheckIfSolvableStep())))
    }

    private fun mapAndValidateTokens(tokens: List<Pair<TokenizerType, String>>): List<Component> {
        return tokens.map {
            val possibleTypes = mutableSetOf<GuesserType>()
            if (it.first == TokenizerType.STRING) {
                if (MonthMappings.mapMonthToInt(it.second) != null) {
                    possibleTypes.add(GuesserType.MONTH)
                }
            } else if (it.first == TokenizerType.INT) {
                val num = it.second.toIntOrNull()
                if (num != null) {
                    if (0 <= num && num <= 99 || 1900 <= num && num <= 3000) { //shortform 0-99 + longform 1900-3000
                        possibleTypes.add(GuesserType.YEAR)
                    }
                    if (1 <= num && num <= 12) {
                        possibleTypes.add(GuesserType.MONTH)
                    }
                    if (1 <= num && num <= 31) {
                        possibleTypes.add(GuesserType.DAY)
                    }
                    if (0 <= num && num <= 24) {
                        possibleTypes.add(GuesserType.HOURS)
                    }
                    if (0 <= num && num <= 59) {
                        possibleTypes.add(GuesserType.MINUTES)
                        possibleTypes.add(GuesserType.SECONDS)
                    }
                }
            }
            Any(it.second, possibleTypes)
        }
    }

    enum class GuesserType {
        DAY, MONTH, YEAR, HOURS, MINUTES, SECONDS;
    }

}
