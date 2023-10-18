package events.boudicca.search.service.query

object QueryParser {
    fun parseQuery(query: String): Expression {
        val tokens = Lexer(query).lex()
        return Parser(tokens).parse()
    }
}