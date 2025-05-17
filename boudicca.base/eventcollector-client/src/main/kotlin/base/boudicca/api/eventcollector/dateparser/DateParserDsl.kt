package base.boudicca.api.eventcollector.dateparser

import java.time.OffsetDateTime

fun dateParser(init: DateParser.() -> Unit): OffsetDateTime {
    val dateParser = DateParser()

    dateParser.init()

    return dateParser.parse()
}
