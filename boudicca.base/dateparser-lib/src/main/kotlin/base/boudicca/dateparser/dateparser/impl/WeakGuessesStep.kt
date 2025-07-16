package base.boudicca.dateparser.dateparser.impl

internal class WeakGuessesStep(private val debugTracing: DebugTracing, private val tokens: Tokens) {
    fun solve(): ListOfDatePairSolution? {

        val applied = Patterns.applyWeakGuesses(tokens.tokens)
        var result = ListOfDatePairStep(
            debugTracing.startOperationWithChild("applied weak guesses", Tokens(applied)), Tokens(applied)
        ).solve()
        debugTracing.endOperation(result)

        if (result == null) {
            result = ListOfDatePairStep(
                debugTracing.startOperationWithChild("trying without weak guesses", tokens), tokens
            ).solve()
            debugTracing.endOperation(result)
        }

        return result
    }
}
