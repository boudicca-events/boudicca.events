package base.boudicca.dateparser.dateparser

import base.boudicca.dateparser.dateparser.impl.DateParserImpl
import io.github.oshai.kotlinlogging.KotlinLogging
import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.OpenTelemetry

object DateParser {
    internal val logger = KotlinLogging.logger {}

    fun parse(
        vararg tokens: String,
        dateParserConfig: DateParserConfig = DateParserConfig(),
        otel: OpenTelemetry = GlobalOpenTelemetry.get(),
    ): DateParserResult = parse(tokens.toList(), dateParserConfig, otel)

    fun parse(
        tokens: List<String>,
        dateParserConfig: DateParserConfig = DateParserConfig(),
        otel: OpenTelemetry = GlobalOpenTelemetry.get(),
    ): DateParserResult = DateParserImpl(dateParserConfig, tokens, otel).parse()
}
