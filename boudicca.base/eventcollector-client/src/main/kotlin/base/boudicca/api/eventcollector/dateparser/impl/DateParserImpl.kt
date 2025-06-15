package base.boudicca.api.eventcollector.dateparser.impl

import base.boudicca.api.eventcollector.dateparser.DateParserResult

internal class DateParserImpl(private val inputTokens: List<String>) {

    fun parse(): DateParserResult {
        val tokens = inputTokens.map { Tokenizer.tokenize(it) }
        return Guesser(tokens).guess()
    }

}
