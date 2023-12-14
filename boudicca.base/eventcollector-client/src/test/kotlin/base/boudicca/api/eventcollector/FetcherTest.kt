package base.boudicca.api.eventcollector

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import java.time.InstantSource
import java.time.ZoneId
import java.util.concurrent.Callable

class FetcherTest {

    private val instantSource = object : InstantSource {
        var instant = Instant.now()

        fun setMillis(long: Long) {
            instant = Instant.ofEpochMilli(long)
        }

        fun addMillis(long: Long) {
            instant = Instant.ofEpochMilli(millis() + long)
        }

        override fun instant(): Instant {
            return instant
        }

    }

    private val httpClientWrapper = object : HttpClientWrapper {

        var callback: Callable<Pair<Int, String>> = Callable { throw IllegalStateException("not initialized") }

        override fun doGet(url: String): Pair<Int, String> {
            return callback.call()
        }

        override fun doPost(url: String, contentType: String, content: ByteArray): Pair<Int, String> {
            return callback.call()
        }

    }

    private val fetcher = Fetcher(
        instantSource.withZone(ZoneId.systemDefault()),
        { instantSource.setMillis(instantSource.millis() + it) },
        httpClientWrapper
    )

    @BeforeEach
    fun setup() {
        httpClientWrapper.callback = Callable { throw IllegalStateException("not initialized") }
        instantSource.setMillis(0)
    }

    @Test
    fun testSimpleGet() {
        httpClientWrapper.callback = Callable { Pair(200, "OK") }
        val body = fetcher.fetchUrl("url")
        assertEquals("OK", body)
    }

    @Test
    fun testSimpleFailException() {
        httpClientWrapper.callback = Callable { throw RuntimeException("intentional exception") }
        assertThrows<RuntimeException> {
            fetcher.fetchUrl("url")
        }
    }

    @Test
    fun testSimpleFailResponseCode() {
        httpClientWrapper.callback = Callable { Pair(400, "FAILED") }
        assertThrows<RuntimeException> {
            fetcher.fetchUrl("url")
        }
    }

    @Test
    fun testSimpleWait() {
        httpClientWrapper.callback = Callable {
            instantSource.addMillis(100)
            Pair(200, "OK")
        }
        fetcher.fetchUrl("url")
        assertEquals(100, instantSource.millis())
        fetcher.fetchUrl("url")
        //100 waittime + 100 for the call
        assertEquals(300, instantSource.millis())
    }

    @Test
    fun testMin100MSWait() {
        httpClientWrapper.callback = Callable {
            instantSource.addMillis(10) //really fast server
            Pair(200, "OK")
        }
        fetcher.fetchUrl("url")
        assertEquals(10, instantSource.millis())
        fetcher.fetchUrl("url")
        //100 waittime + 10 for the call
        assertEquals(120, instantSource.millis())
    }

    @Test
    fun test80PercentMSWait() {
        httpClientWrapper.callback = Callable {
            instantSource.addMillis(1000)
            Pair(200, "OK")
        }
        fetcher.fetchUrl("url")
        assertEquals(1000, instantSource.millis())
        fetcher.fetchUrl("url")
        //800 waittime + 1000 for the call
        assertEquals(2800, instantSource.millis())
    }

    @Test
    fun testRetryOnSecond() {
        var count = 0
        httpClientWrapper.callback = Callable {
            if (count++ == 0) {
                Pair(400, "FAILED")
            } else {
                Pair(200, "OK")
            }
        }
        val response = fetcher.fetchUrl("url")
        assertEquals("OK", response)
    }
}