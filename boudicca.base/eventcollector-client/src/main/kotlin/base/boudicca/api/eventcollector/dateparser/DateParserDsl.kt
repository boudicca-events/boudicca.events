package base.boudicca.api.eventcollector.dateparser

import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime

fun dateParser(init: DateParser.() -> Unit): OffsetDateTime {
    val dateParser = DateParser()

    dateParser.init()

    return dateParser.parse()
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
