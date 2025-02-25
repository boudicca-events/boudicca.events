package events.boudicca.publisher.event.html

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class PublisherHtmlApplicationSmokeTests {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun contextLoads() {
        // This test will automatically pass if the application context loads successfully.
        // If there's any bean misconfiguration or other Spring-related errors, it will fail.
    }

    @Test
    fun aboutUs() {
        mockMvc.perform(
            get("/about")
        ).andExpect(status().isOk())
    }

    @Test
    fun impressum() {
        mockMvc.perform(
            get("/impressum")
        ).andExpect(status().isOk())
    }

    @Test
    fun accessibility() {
        mockMvc.perform(
            get("/erklarung-zur-barrierefreiheit")
        ).andExpect(status().isOk())
    }
}
