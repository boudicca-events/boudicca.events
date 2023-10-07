package events.boudicca.search.query.evaluator.performance

import events.boudicca.SemanticKeys
import events.boudicca.openapi.ApiClient
import events.boudicca.openapi.api.EventIngestionResourceApi
import events.boudicca.openapi.model.Event
import java.io.FileInputStream
import java.io.ObjectInputStream
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.*

fun main() {
    val inStream = ObjectInputStream(FileInputStream("testdata.dump"))
    val loadedData = inStream.readObject()
    inStream.close()
    val (testData, _) = loadedData as Pair<Collection<Map<String, String>>, Map<String, TestDataGenerator.Metadata>>

    val apiClient = ApiClient()
    apiClient.updateBaseUri("http://localhost:8081")
    apiClient.setRequestInterceptor {
        it.header(
            "Authorization",
            "Basic " + Base64.getEncoder().encodeToString("ingest:ingest".encodeToByteArray())
        )
    }
    val ingestApi = EventIngestionResourceApi(apiClient)

    val startTime = System.currentTimeMillis()
    var count = 0
    for (event in testData) {
        count++
        if (count % 1000 == 0) {
            val currentTimeTaken = System.currentTimeMillis() - startTime
            println(
                "count=${count}/${testData.size}, " +
                        "took ${currentTimeTaken / 1000}s, " +
                        "will take ~${kotlin.math.ceil(currentTimeTaken / (count.toDouble() / testData.size.toDouble())) / 1000}s"
            )
        }
        val data = event.toMutableMap()
        val name = data.remove(SemanticKeys.NAME)
        val startDate = OffsetDateTime.parse(data.remove(SemanticKeys.STARTDATE), DateTimeFormatter.ISO_DATE_TIME)
        ingestApi.ingestAddPost(
            Event()
                .name(name)
                .startDate(startDate)
                .data(data)
        )
    }
}
