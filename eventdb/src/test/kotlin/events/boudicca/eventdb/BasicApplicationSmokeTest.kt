package events.boudicca.eventdb

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class BasicApplicationSmokeTest {

    @Test
    fun contextLoads() {
        // This test will automatically pass if the application context loads successfully.
        // If there's any bean misconfiguration or other Spring-related errors, it will fail.
    }

    @Test
    fun failingTest() {
        fail("fail smoketest")
    }
}