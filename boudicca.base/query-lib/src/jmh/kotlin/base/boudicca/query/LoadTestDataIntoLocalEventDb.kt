package base.boudicca.query

import base.boudicca.api.eventdb.ingest.EventDbIngestClient
import base.boudicca.model.Entry
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.nio.file.Path
import kotlin.io.path.readBytes

fun main() {
    val objectMapper =
        JsonMapper
            .builder()
            .addModule(JavaTimeModule())
            .addModule(KotlinModule.Builder().build())
            .build()

    val storeRead =
        objectMapper.readValue(
            Path.of("testdata.dump").readBytes(),
            object : TypeReference<List<Entry>>() {},
        )

    val ingestClient = EventDbIngestClient("http://localhost:8081", "ingest", "ingest")
    ingestClient.ingestEntries(storeRead)
}
