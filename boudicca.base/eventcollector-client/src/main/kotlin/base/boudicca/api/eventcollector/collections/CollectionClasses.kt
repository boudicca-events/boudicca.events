package base.boudicca.api.eventcollector.collections

import java.util.*
import java.util.Collections

data class FullCollection(
    val id: UUID,
    var startTime: Long,
    var endTime: Long,
    val singleCollections: MutableList<SingleCollection>,
    val logLines: MutableList<String>,
    var errorCount: Int,
    var warningCount: Int,
) {
    constructor() : this(
        UUID.randomUUID(),
        0,
        0,
        Collections.synchronizedList(mutableListOf()),
        Collections.synchronizedList(mutableListOf()),
        0,
        0
    )

    override fun toString(): String {
        return """
                FullCollection(
                    id=$id, 
                    startTime=$startTime, 
                    endTime=$endTime, 
                    singleCollections=$singleCollections, 
                    logLines=$logLines, 
                    errorCount=$errorCount, 
                    warningCount=$warningCount
                )""".trimIndent()
    }

    /**
     * gets the error count for this full collection + each single collection
     */
    fun getTotalErrorCount(): Int {
        return errorCount + singleCollections.sumOf { it.errorCount }
    }

    /**
     * gets the warning count for this full collection + each single collection
     */
    fun getTotalWarningCount(): Int {
        return warningCount + singleCollections.sumOf { it.warningCount }
    }

    /**
     * gets all log lines for this full collection + each single collection. please note that those loglines
     * are not sorted by time but by single-/fullcollection
     */
    fun getAllLogLines(): List<String> {
        return logLines + singleCollections.flatMap { it.logLines }
    }

}

data class SingleCollection(
    val id: UUID,
    val collectorName: String,
    var startTime: Long,
    var endTime: Long,
    var totalEventsCollected: Int,
    val httpCalls: MutableList<HttpCall>,
    val logLines: MutableList<String>,
    var errorCount: Int,
    var warningCount: Int,
) {
    constructor(collectorName: String) : this(
        UUID.randomUUID(),
        collectorName,
        0,
        0,
        0,
        mutableListOf(),
        mutableListOf(),
        0,
        0
    )

    override fun toString(): String {
        return """
            SingleCollection(
                id=$id, 
                collectorName='$collectorName', 
                startTime=$startTime, 
                endTime=$endTime, 
                totalEventsCollected=$totalEventsCollected, 
                httpCalls=$httpCalls, 
                logLines=$logLines, 
                errorCount=$errorCount, 
                warningCount=$warningCount
            )""".trimIndent()
    }
}

data class HttpCall(
    var startTime: Long,
    var endTime: Long,
    var url: String?,
    var postData: String?,
    var responseCode: Int,
) {
    constructor() : this(0, 0, null, null, 0)
}
