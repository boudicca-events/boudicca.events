package events.boudicca.api.eventcollector.collections

import events.boudicca.api.eventcollector.EventCollector
import java.util.Collections
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger


data class FullCollection(
    val id: UUID,
    var startTime: Long,
    var endTime: Long,
    val singleCollections: MutableList<SingleCollection>
) {
    constructor() : this(UUID.randomUUID(), 0, 0, Collections.synchronizedList(mutableListOf()))

    override fun toString(): String {
        return "FullCollection(\nid=$id, \nstartTime=$startTime, \nendTime=$endTime, \nsingleCollections=$singleCollections)"
    }


}

data class SingleCollection(
    val id: UUID,
    var startTime: Long,
    var endTime: Long,
    var collector: EventCollector?,
    val httpCalls: MutableList<HttpCall>,
    val logLines: MutableList<Pair<Boolean, ByteArray>>,
) {
    constructor() : this(UUID.randomUUID(), 0, 0, null, mutableListOf(), mutableListOf())

    override fun toString(): String {
        return "SingleCollection(\nid=$id, \nstartTime=$startTime, \nendTime=$endTime, \ncollector=$collector, \nhttpCalls=$httpCalls, \nlogLines=${logLines.map { Pair(it.first, String(it.second)) }})"
    }

}

data class HttpCall(
    var startTime: Long,
    var endTime: Long,
    var url: String?,
    var responseCode: Int,
) {
    constructor() : this(0, 0, null, 0)
}
