package base.boudicca.dateparser.dateparser.impl

import base.boudicca.dateparser.dateparser.DateParserConfig

internal class GuessesStep(
    private val config: DateParserConfig,
    private val debugTracing: DebugTracing,
    private val tokens: Tokens,
) {
    fun solve(): ListOfDatePairSolution? {
        val goodGuessesApplied = Tokens(Patterns.applyGoodGuesses(tokens.tokens))
        val goodAndWeakGuessesApplied = Tokens(Patterns.applyWeakGuesses(goodGuessesApplied.tokens))

        var result =
            ListOfDatePairStep(
                config,
                debugTracing.startOperationWithChild("applied good and weak guesses", goodAndWeakGuessesApplied),
                goodAndWeakGuessesApplied,
            ).solve()
        debugTracing.endOperation(result)

        if (result == null) {
            result =
                ListOfDatePairStep(
                    config,
                    debugTracing.startOperationWithChild("applied only good guesses", goodGuessesApplied),
                    goodGuessesApplied,
                ).solve()
            debugTracing.endOperation(result)
        }

        if (result == null) {
            result =
                ListOfDatePairStep(
                    config,
                    debugTracing.startOperationWithChild("applied no guesses", tokens),
                    tokens,
                ).solve()
            debugTracing.endOperation(result)
        }

        return result
    }
}
