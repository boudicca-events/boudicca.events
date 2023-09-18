package events.boudicca.publisherhtml.service

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

@Service
class PictureProxyService {

    private val cache = ConcurrentHashMap<String, CacheEntry>()

    private val httpClient = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build()

    fun getPicture(url: String): Optional<ByteArray> {
        val cached = cache[url]
        if (cached != null) {
            return cached.optionalPicture
        }

        val request = HttpRequest.newBuilder().GET()
            .uri(URI.create(url))
            .build()
        val response = httpClient.send(request, BodyHandlers.ofByteArray())

        val optional = if (response.statusCode() != 200) {
            Optional.empty()
        } else {
            val body = response.body()
            if (body.isEmpty()) {
                Optional.empty()
            } else {
                Optional.of(body)
            }
        }

        cache[url] = CacheEntry(optional)

        return optional
    }

    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.HOURS)
    fun cleanUp() {
        val iterator = cache.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (isTooOld(entry.value.dateAdded)) {
                iterator.remove()
            }
        }
    }

    private fun isTooOld(dateAdded: Instant): Boolean {
        return dateAdded.isBefore(Instant.now().minus(1, ChronoUnit.DAYS))
    }


    data class CacheEntry(
        val optionalPicture: Optional<ByteArray>,
        val dateAdded: Instant = Instant.now(),
    )
}
