package events.boudicca.eventcollector

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.GZIPOutputStream
import java.util.zip.ZipException

class LocalFetchFetcherCacheFromOnlineBoudiccaTest {
    @Test
    fun `decompresses gzip content and writes to target path`(
        @TempDir dir: Path,
    ) {
        val content = "test event data"

        unzipAndSave(ByteArrayInputStream(gzip(content)), dir.resolve("fetcher.cache"))

        assertThat(Files.readString(dir.resolve("fetcher.cache"))).isEqualTo(content)
    }

    @Test
    fun `throws on corrupt input`(
        @TempDir dir: Path,
    ) {
        assertThrows<ZipException> {
            unzipAndSave(ByteArrayInputStream(byteArrayOf(1, 2, 3)), dir.resolve("fetcher.cache"))
        }
    }

    private fun gzip(content: String): ByteArray {
        val baos = ByteArrayOutputStream()
        GZIPOutputStream(baos).use { it.write(content.toByteArray()) }
        return baos.toByteArray()
    }
}
