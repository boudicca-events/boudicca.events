package base.boudicca.dateparser.dateparser.impl

object Utils {
    internal fun tryTypeStealing(groups: List<Tokens>, groupToTypeSteal: Int): Tokens {
        val defaultReturn = groups[groupToTypeSteal]
        if (groupToTypeSteal + 1 == groups.size) {
            return defaultReturn
        }
        for (token in groups[groupToTypeSteal + 1].tokens) {
            if (token.possibleTypes.size == 1) {
                val type = token.possibleTypes.single()
                val resultTokens = mutableListOf<Token>()
                for (resultToken in defaultReturn.tokens) {
                    if (!resultToken.isSolved() && resultToken.possibleTypes.contains(type)) {
                        resultTokens.add(resultToken.withTypes(setOf(type)))
                    } else {
                        resultTokens.add(resultToken)
                    }
                }
                return Tokens(resultTokens)
            }
        }
        return defaultReturn
    }
}
