package base.boudicca.api.eventcollector.dateparser

import base.boudicca.api.eventcollector.dateparser.impl.DateParserImpl

class DateParser private constructor(private val tokens: List<String>) {

    companion object {
        fun parse(vararg tokens: String): DateParserResult {
            return parse(tokens.toList())
        }

        fun parse(tokens: List<String>): DateParserResult {
            return DateParser(tokens).parse()
        }
    }

    fun parse(): DateParserResult {
        val result = DateParserImpl(tokens).parse()
        require(result.dates.isNotEmpty()) { "could not parse any dates with following data: $this" }
        return result
    }

    override fun toString(): String {
        return "DateParser(tokens=$tokens)"
    }
}
