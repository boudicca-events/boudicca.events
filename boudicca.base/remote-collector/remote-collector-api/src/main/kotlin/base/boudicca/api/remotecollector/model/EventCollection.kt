package base.boudicca.api.remotecollector.model

import base.boudicca.model.Event

data class EventCollection(
    val events: List<Event>,
    val httpCalls: List<HttpCall>?,
    val logLines: List<String>?,
    val warningCount: Int?,
    val errorCount: Int?,
)