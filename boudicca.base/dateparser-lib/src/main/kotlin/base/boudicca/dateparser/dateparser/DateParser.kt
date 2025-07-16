package base.boudicca.dateparser.dateparser

import base.boudicca.dateparser.dateparser.impl.DateParserImpl
import io.github.oshai.kotlinlogging.KotlinLogging


object DateParser {
    internal val logger = KotlinLogging.logger {}

    fun parse(vararg tokens: String, dateParserConfig: DateParserConfig = DateParserConfig()): DateParserResult {
        return parse(tokens.toList(), dateParserConfig)
    }

    fun parse(tokens: List<String>, dateParserConfig: DateParserConfig = DateParserConfig()): DateParserResult {
        return DateParserImpl(dateParserConfig, tokens).parse()
    }
}
