package base.boudicca.dateparser.dateparser.impl

import base.boudicca.dateparser.dateparser.DateParser
import base.boudicca.dateparser.dateparser.DateParserConfig
import base.boudicca.dateparser.dateparser.DateParserResult
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.api.trace.StatusCode


internal class DateParserImpl(
    private val dateParserConfig: DateParserConfig,
    private val inputTokens: List<String>,
    private val otel: OpenTelemetry
) {

    fun parse(): DateParserResult {
        val span = otel.getTracer("DateParser")
            .spanBuilder("parse date")
            .setSpanKind(SpanKind.INTERNAL)
            .setAttribute("input", inputTokens.joinToString())
            .setAttribute("config", dateParserConfig.toString())
            .startSpan()
        try {
            span.makeCurrent().use {
                val tokenGroups = inputTokens.map { Tokenizer.tokenize(it) }
                val tokens = tokenGroups.map { mapAndValidateTokens(it) }
                    .reduce { acc, tokens -> acc + Token.create("\n", emptySet()) + tokens }

                val debugTracing = DebugTracing()
                val result = GuessesStep(
                    dateParserConfig,
                    debugTracing.startOperationWithChild("parser start", Tokens(tokens)), Tokens(tokens)
                ).solve()
                debugTracing.endOperation(result)

                //mainly for tests
                if (dateParserConfig.alwaysPrintDebugTracing) {
                    println(debugTracing.debugPrint())
                }

                if (result != null && result.isSolved() && result.datePairs.isNotEmpty()) {
                    val parserResult = result.toDateParserResult(dateParserConfig.timezone, dateParserConfig.clock)
                    span.setStatus(StatusCode.OK)
                        .setAttribute("result", parserResult.toString())
                    DateParser.logger.debug { "did successfully parse inputs $inputTokens into result: $parserResult\n${debugTracing.debugPrint()}" }
                    return parserResult
                } else {
                    span.setStatus(StatusCode.ERROR)
                    DateParser.logger.error { "could not parse inputs $inputTokens\n${debugTracing.debugPrint()}" }
                    throw IllegalArgumentException("could not parse any dates with following data: $inputTokens")
                }
            }
        } finally {
            span.end()
        }

    }

    @Suppress("CyclomaticComplexMethod", "MagicNumber")
    private fun mapAndValidateTokens(tokens: List<Pair<TokenizerType, String>>): List<Token> {
        return tokens.map {
            val possibleTypes = mutableSetOf<ResultTypes>()
            if (it.first == TokenizerType.STRING) {
                if (MonthMappings.mapMonthToInt(it.second) != null) {
                    possibleTypes.add(ResultTypes.MONTH)
                }
            } else if (it.first == TokenizerType.INT) {
                val num = it.second.toIntOrNull()
                if (num != null) {
                    if (0 <= num && num <= 99 || 1900 <= num && num <= 3000) { //shortform 0-99 + longform 1900-3000
                        possibleTypes.add(ResultTypes.YEAR)
                    }
                    if (1 <= num && num <= 12) {
                        possibleTypes.add(ResultTypes.MONTH)
                    }
                    if (1 <= num && num <= 31) {
                        possibleTypes.add(ResultTypes.DAY)
                    }
                    if (0 <= num && num <= 24) {
                        possibleTypes.add(ResultTypes.HOURS)
                    }
                    if (0 <= num && num <= 59) {
                        possibleTypes.add(ResultTypes.MINUTES)
                        possibleTypes.add(ResultTypes.SECONDS)
                    }
                }
            }
            Token.create(it.second, possibleTypes)
        }
    }

}
