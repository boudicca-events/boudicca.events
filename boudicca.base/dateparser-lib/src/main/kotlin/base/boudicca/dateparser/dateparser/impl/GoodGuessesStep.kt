package base.boudicca.dateparser.dateparser.impl

internal class GoodGuessesStep(private val debugTracing: DebugTracing, private val tokens: Tokens) {
    fun solve(): ListOfDatePairSolution? {
        val applied = Patterns.applyGoodGuesses(tokens.tokens)

        var result = WeakGuessesStep(
            debugTracing.startOperationWithChild("applied good guesses", Tokens(applied)),
            Tokens(applied)
        ).solve()
        debugTracing.endOperation(result)

        if (result == null) {
            result = WeakGuessesStep(
                debugTracing.startOperationWithChild("removed good guesses again", tokens),
                tokens
            ).solve()
            debugTracing.endOperation(result)
        }

        return result
    }
}
