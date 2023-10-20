package base.boudicca.publisherhtml.service

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO
import kotlin.math.max

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
                Optional.of(resize(body))
            }
        }

        cache[url] = CacheEntry(optional)

        return optional
    }

    private fun resize(picture: ByteArray): ByteArray {
        val bufferedImage = ImageIO.read(ByteArrayInputStream(picture))

        val (width, height) = calcResizedDimensions(bufferedImage.width, bufferedImage.height)

        val scaledImage = bufferedImage.getScaledInstance(width, height, 0)

        val scaledBufferedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        scaledBufferedImage.graphics.drawImage(scaledImage, 0, 0, null)

        val outputStream = ByteArrayOutputStream()
        ImageIO.write(scaledBufferedImage, "jpeg", outputStream)
        return outputStream.toByteArray()
    }

    private val WANTED_WIDTH = 300
    private val WANTED_HEIGHT = 250
    private fun calcResizedDimensions(width: Int, height: Int): Pair<Int, Int> {
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
