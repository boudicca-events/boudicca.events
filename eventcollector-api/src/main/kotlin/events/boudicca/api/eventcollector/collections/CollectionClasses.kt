package events.boudicca.api.eventcollector.collections

import events.boudicca.api.eventcollector.EventCollector

data class FullCollection(
    var startTime: Long,
    var endTime: Long,
    val singleCollections: MutableList<SingleCollection>
) {
    constructor() : this(0, 0, mutableListOf())
}

data class SingleCollection(
    var startTime: Long,
    var endTime: Long,
    var collector: EventCollector?
) {
    constructor() : this(0, 0, null)
}
