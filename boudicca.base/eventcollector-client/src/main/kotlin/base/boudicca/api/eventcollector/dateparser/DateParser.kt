package base.boudicca.api.eventcollector.dateparser

import base.boudicca.api.eventcollector.dateparser.impl.DateParserImpl

class DateParser(private val tokens: List<String>) {

    fun parse(): DateParserResult {
        val result = DateParserImpl(tokens).parse()
        require(result.dates.isNotEmpty()) { "could not parse any dates with following data: $this" }
        return result
    }

    override fun toString(): String {
        return "DateParser(tokens=$tokens)"
    }
}
