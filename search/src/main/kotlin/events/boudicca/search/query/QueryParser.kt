package events.boudicca.search.query

import events.boudicca.search.model.Event

class QueryParser(private val query: String) {
    fun parse(): Evaluator {
        val tokens = Lexer(query).lex()
        return object : Evaluator {
            override fun evaluate(event: Event): Boolean {
                return false
            }
        }
    }
}
