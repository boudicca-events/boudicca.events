package base.boudicca.query

import base.boudicca.query.evaluator.Evaluator
import base.boudicca.query.evaluator.Page
import base.boudicca.query.evaluator.QueryResult
import base.boudicca.query.parsing.Lexer
import base.boudicca.query.parsing.Parser

object BoudiccaQueryRunner {

    /**
     * parse the given query and return an Expression AST
     */
    fun parseQuery(query: String): Expression {
        val tokens = Lexer(query).lex()
        return Parser(tokens).parse()
    }

    /**
     * parse and evaluate the given string query, the given page and the given evaluator
     */
    fun evaluateQuery(query: String, page: Page, evaluator: Evaluator): QueryResult {
        return evaluator.evaluate(parseQuery(query), page)
    }
}