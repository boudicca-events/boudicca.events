package base.boudicca.query

import base.boudicca.query.evaluator.Evaluator
import base.boudicca.query.evaluator.Page
import base.boudicca.query.evaluator.QueryResult
import base.boudicca.query.parsing.Lexer
import base.boudicca.query.parsing.Parser
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.trace.StatusCode

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
    fun evaluateQuery(query: String, page: Page, evaluator: Evaluator, openTelemetry: OpenTelemetry = OpenTelemetry.noop()): QueryResult {
        val startSpan =
            openTelemetry.getTracer("BoudiccaQueryRunner")
                .spanBuilder("execute query")
                .setAttribute("query", query.toString())
                .setAttribute("page", page.toString())
                .startSpan()
        try {
            return startSpan.makeCurrent().use {
                val expression = parseQuery(query)
                startSpan.addEvent("expression parsed")

                val result = evaluator.evaluate(expression, page)

                startSpan.setAttribute("totalResults", result.totalResults.toLong())
                startSpan.setStatus(StatusCode.OK)
                if (result.error != null) {
                    startSpan.setStatus(StatusCode.ERROR)
                    startSpan.setAttribute("error", result.error)
                }
                return result
            }
        } finally {
            startSpan.end()
        }
    }
}
