package base.boudicca.format

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.net.URI

class UrlUtilsTest {
    @Test
    fun simpleUrlParse() {
        //parsing should be done without changes
        runTest("http://posthof.at", "http://posthof.at")
        runTest("https://www.posthof.at", "https://www.posthof.at")
    }

    @Test
    fun simpleUrlInvalid() {
        //invalid urls should throw
        runTestFailing("asd/asd/asd\\\\/////")
    }

    @Test
    fun emptyInputs() {
        //empty inputs should return null
        runTest(null, "")
        runTest(null, null)
    }

    @Test
    fun invalidEscaping() {
        //some urls we find are invalid because they are not escaped properly, we try to fix it
        runTest("https://www.posthof.at/weird%5B%20%5Durl", "https://www.posthof.at/weird[ ]url")
        runTest("https://www.posthof.at/weird%7Curl", "https://www.posthof.at/weird|url")
    }

    @Test
    fun baseUrls() {
        //for easier working with relative urls we have a separate api
        runTest("https://www.posthof.at/picture.img", "https://www.posthof.at/", "picture.img")
        runTest("https://www.posthof.at/picture.img", "https://www.posthof.at", "picture.img")
        runTest("https://www.posthof.at/picture.img", "https://www.posthof.at/", "/picture.img")
        runTest("https://www.posthof.at/picture.img", "https://www.posthof.at", "/picture.img")
    }

    @Test
    fun baseUrlsEmptyRelative() {
        //for easier working with relative urls we have a separate api
        runTest(null, "https://www.posthof.at/", "")
        runTest(null, "https://www.posthof.at/", null)
    }

    private fun runTest(expected: String?, baseUrl: String, toParse: String?) {
        assertEquals(expected?.run { URI(expected) }, UrlUtils.parse(baseUrl, toParse))
    }

    private fun runTest(expected: String?, toParse: String?) {
        assertEquals(expected?.run { URI(expected) }, UrlUtils.parse(toParse))
    }

    private fun runTestFailing(toParse: String?) {
        assertThrows<IllegalArgumentException> { UrlUtils.parse(toParse) }
    }
}
