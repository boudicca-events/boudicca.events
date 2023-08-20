package events.boudicca.api.eventcollector.collections

import events.boudicca.api.eventcollector.EventCollector
import java.util.Collections
import java.util.concurrent.atomic.AtomicInteger


private val counter = AtomicInteger(0)

data class FullCollection(
    val id: Int,
    var startTime: Long,
    var endTime: Long,
    val singleCollections: MutableList<SingleCollection>
) {
    constructor() : this(counter.incrementAndGet(), 0, 0, Collections.synchronizedList(mutableListOf()))
}

data class SingleCollection(
    val id: Int,
    var startTime: Long,
    var endTime: Long,
    var collector: EventCollector?,
    val httpCalls: MutableList<HttpCall>
) {
    constructor() : this(counter.incrementAndGet(), 0, 0, null, mutableListOf())
}

data class HttpCall(
    val id: Int,
    var startTime: Long,
    var endTime: Long,
    var url: String?,
    var responseCode: Int,
) {
    constructor() : this(counter.incrementAndGet(), 0, 0, null, 0)
}
