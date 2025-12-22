package base.boudicca.dateparser.dateparser.impl

internal data class Tokens(val tokens: List<Token>) {
    fun isInteresting(): Boolean = tokens.any { it.needSolving }

    fun map(mapper: (token: Token) -> Token): Tokens = Tokens(tokens.map(mapper))

    override fun toString(): String {
        val sb = StringBuilder()
        for (token in tokens) {
            sb.append(token.toString())
        }
        return sb.toString()
    }
}
