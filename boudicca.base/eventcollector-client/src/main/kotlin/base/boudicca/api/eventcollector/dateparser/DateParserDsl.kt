package base.boudicca.api.eventcollector.dateparser

import base.boudicca.SemanticKeys
import base.boudicca.model.structured.StructuredEvent
import base.boudicca.model.structured.dsl.StructuredEventBuilder
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime

fun dateParser(init: DateParser.() -> Unit): DateParserResult {
    val dateParser = DateParser()

    dateParser.init()

    return dateParser.parse()
}

fun singleDateParser(init: DateParser.() -> Unit): OffsetDateTime {
    val dateParser = DateParser()

    dateParser.init()

    return dateParser.parseSingle()
}

fun localDateParser(init: DateParser.() -> Unit): LocalDate {
    val dateParser = DateParser()

    dateParser.init()

    return dateParser.parseLocalDate()
}

fun localTimeParser(init: DateParser.() -> Unit): LocalTime {
    val dateParser = DateParser()

    dateParser.init()

    return dateParser.parseLocalTime()
}

fun structuredEvent(
    name: String,
    dates: DateParserResult,
    init: StructuredEventBuilder.() -> Unit = {}
): List<StructuredEvent> {
    return dates.dates.map {
        val builder = StructuredEventBuilder(name, it.startDate)
        builder.withProperty(SemanticKeys.ENDDATE_PROPERTY, it.endDate)
        builder.init()
        builder.build()
    }
}
