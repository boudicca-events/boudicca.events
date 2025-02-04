package base.boudicca.api.eventcollector

import base.boudicca.api.eventcollector.fetcher.HttpClientWrapper
import base.boudicca.api.eventcollector.fetcher.InMemoryFetcherCache
import base.boudicca.api.eventcollector.fetcher.NoopFetcherCache
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

        override fun doPost(url: String, contentType: String, content: String): Pair<Int, String> {
            return callback.call()
        }

    }

    private val fetcher = Fetcher(
        clock = instantSource.withZone(ZoneId.systemDefault()),
        sleeper = { instantSource.setMillis(instantSource.millis() + it) },
        httpClient = httpClientWrapper
    )

    @BeforeEach
    fun setup() {
        httpClientWrapper.callback = Callable { throw IllegalStateException("not initialized") }
        instantSource.setMillis(0)
        Fetcher.fetcherCache = NoopFetcherCache
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
        //150 waittime + 100 for the call
        assertEquals(350, instantSource.millis())
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
    fun test150PercentMSWait() {
        httpClientWrapper.callback = Callable {
            instantSource.addMillis(1000)
            Pair(200, "OK")
        }
        fetcher.fetchUrl("url")
        assertEquals(1000, instantSource.millis())
        fetcher.fetchUrl("url")
        //1500 waittime + 1000 for the call
        assertEquals(3500, instantSource.millis())
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

    @Test
    fun testManualSetDelay() {
        val customFetcher = Fetcher(
            manualSetDelay = 6789,
            clock = instantSource.withZone(ZoneId.systemDefault()),
            sleeper = { instantSource.setMillis(instantSource.millis() + it) },
            httpClient = httpClientWrapper
        )
        httpClientWrapper.callback = Callable {
            Pair(200, "OK")
        }
        assertEquals(0, instantSource.millis())
        customFetcher.fetchUrl("url")
        assertEquals(6789, instantSource.millis())
    }

    @Test
    fun testNoCache() {
        var count = 0
        httpClientWrapper.callback = Callable {
            count++
            Pair(200, "OK")
        }
        assertEquals(0, count)
        fetcher.fetchUrl("url")
        assertEquals(1, count)
        fetcher.fetchUrl("url")
        assertEquals(2, count)
    }

    @Test
    fun testCache() {
        var count = 0
        Fetcher.fetcherCache = InMemoryFetcherCache
        httpClientWrapper.callback = Callable {
            count++
            Pair(200, "OK")
        }
        assertEquals(0, count)
        fetcher.fetchUrl("url")
        assertEquals(1, count)
        fetcher.fetchUrl("url")
        assertEquals(1, count)
    }
}