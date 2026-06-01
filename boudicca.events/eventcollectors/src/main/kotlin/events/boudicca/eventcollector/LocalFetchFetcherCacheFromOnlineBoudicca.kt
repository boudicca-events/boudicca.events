package events.boudicca.eventcollector

import java.io.InputStream
import java.net.HttpURLConnection.HTTP_OK
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.zip.GZIPInputStream

fun main() {
    val httpClient = HttpClient.newHttpClient()
    httpClient.use {
        val response =
            it.send(
                HttpRequest
                    .newBuilder()
                    .GET()
                    .uri(URI.create("https://collectors.boudicca.events/lastCapturedCalls"))
                    .build(),
                HttpResponse.BodyHandlers.ofInputStream(),
            )
        if (response.statusCode() == HTTP_OK) {
            unzipAndSave(response.body(), Path.of("./fetcher.cache"))
        } else {
            error("invalid response code: ${response.statusCode()}")
        }
    }
}

internal fun unzipAndSave(
    body: InputStream,
    target: Path,
) {
    GZIPInputStream(body).use { Files.copy(it, target, StandardCopyOption.REPLACE_EXISTING) }
}
