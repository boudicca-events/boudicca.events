package base.boudicca.publisher.event.html.service

import io.github.oshai.kotlinlogging.KotlinLogging
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.instrumentation.javahttpclient.JavaHttpClientTelemetry
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO
import kotlin.math.max

private const val WANTED_WIDTH = 300
private const val WANTED_HEIGHT = 250

private const val HOURS_TO_REFRESH = 22L

@Service
class PictureProxyService(
    otel: OpenTelemetry,
) {
    private val logger = KotlinLogging.logger {}

    private val idToImageCache = ConcurrentHashMap<UUID, Future<CacheEntry>>()
    private val urlToIdCacheEntry = ConcurrentHashMap<String, UUID>()

    private val executorService = Executors.newVirtualThreadPerTaskExecutor()

    private val httpClient =
        JavaHttpClientTelemetry
            .builder(otel)
            .build()
            .newHttpClient(
                HttpClient
                    .newBuilder()
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build(),
            )

    fun submitPicture(url: String): UUID {
        val cachedUUID = urlToIdCacheEntry[url]
        if (cachedUUID != null && idToImageCache.containsKey(cachedUUID)) {
            checkForRefresh(url, cachedUUID)
            return cachedUUID
        }

        return triggerRefresh(url, UUID.randomUUID())
    }

    fun getPicture(uuid: UUID): Optional<ByteArray> = idToImageCache[uuid]?.get()?.optionalPicture ?: Optional.empty()

    private fun triggerRefresh(
        url: String,
        uuid: UUID,
    ): UUID {
        idToImageCache[uuid] =
            executorService.submit(
                Callable {
                    fetchAndResizeUrl(url)
                },
            )
        urlToIdCacheEntry[url] = uuid
        return uuid
    }

    private fun fetchAndResizeUrl(url: String): CacheEntry {
        try {
            val request =
                HttpRequest
                    .newBuilder()
                    .GET()
                    .uri(URI.create(url))
                    .build()
            val response = httpClient.send(request, BodyHandlers.ofByteArray())

            val optional =
                if (response.statusCode() != HttpURLConnection.HTTP_OK) {
                    logger.warn { "response code is invalid ${response.statusCode()} for $url" }
                    Optional.empty()
                } else {
                    val body = response.body()
                    if (body.isEmpty()) {
                        logger.warn { "empty body for $url" }
                        Optional.empty()
                    } else {
                        try {
                            Optional.of(resize(body))
                        } catch (e: RuntimeException) {
                            logger.warn(e) { "error resizing image $url" }
                            Optional.empty()
                        }
                    }
                }

            return CacheEntry(optional)
        } catch (e: Exception) {
            logger.error(e) { "got exception while trying to fetch and resize the image with url $url" }
            return CacheEntry(Optional.empty())
        }
    }

    private fun checkForRefresh(
        url: String,
        uuid: UUID,
    ) {
        val entry = idToImageCache[uuid]
        if (entry == null || (entry.isDone && shouldRefresh(entry.get().dateAdded))) {
            triggerRefresh(url, uuid)
        }
    }

    private fun resize(picture: ByteArray): ByteArray {
        val bufferedImage = ImageIO.read(ByteArrayInputStream(picture))

        requireNotNull(bufferedImage) {
            "unsupported file format"
        }

        val (width, height) = calcResizedDimensions(bufferedImage.width, bufferedImage.height)

        val scaledImage = bufferedImage.getScaledInstance(width, height, 0)

        val scaledBufferedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        scaledBufferedImage.graphics.drawImage(scaledImage, 0, 0, null)

        val outputStream = ByteArrayOutputStream()
        ImageIO.write(scaledBufferedImage, "jpeg", outputStream)
        return outputStream.toByteArray()
    }

    private fun calcResizedDimensions(
        width: Int,
        height: Int,
    ): Pair<Int, Int> {
        val widthScaleFactor = WANTED_WIDTH / width.toDouble()
        val heightScaleFactor = WANTED_HEIGHT / height.toDouble()
        val maxScaleFactor = max(widthScaleFactor, heightScaleFactor)
        return if (maxScaleFactor >= 1) {
            Pair(width, height)
        } else {
            Pair((width * maxScaleFactor).toInt(), (height * maxScaleFactor).toInt())
        }
    }

    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.HOURS)
    fun cleanUp() {
        val idToImageIterator = idToImageCache.iterator()
        while (idToImageIterator.hasNext()) {
            val entry = idToImageIterator.next()
            if (entry.value.isDone && shouldEvict(entry.value.get().dateAdded)) {
                idToImageIterator.remove()
            }
        }
        val urlToIdIterator = urlToIdCacheEntry.iterator()
        while (urlToIdIterator.hasNext()) {
            val entry = urlToIdIterator.next()
            if (!idToImageCache.containsKey(entry.value)) {
                urlToIdIterator.remove()
            }
        }
    }

    private fun shouldEvict(dateAdded: Instant): Boolean = dateAdded.isBefore(Instant.now().minus(1, ChronoUnit.DAYS))

    private fun shouldRefresh(dateAdded: Instant): Boolean = dateAdded.isBefore(Instant.now().minus(HOURS_TO_REFRESH, ChronoUnit.HOURS))

    data class CacheEntry(
        val optionalPicture: Optional<ByteArray>,
        val dateAdded: Instant = Instant.now(),
    )
}
