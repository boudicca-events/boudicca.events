package base.boudicca.eventdb

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class EventDBApplicationSmokeTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun contextLoads() {
        // This test will automatically pass if the application context loads successfully.
        // If there's any bean misconfiguration or other Spring-related errors, it will fail.
    }

    @Test
    @DirtiesContext
    fun baseWorkflow() {
        mockMvc
            .perform(
                post("/ingest/entry")
                    .content("""{"name":"event1"}""")
                    .addDefaultAuth()
                    .addDefaultContentType(),
            ).andExpect(status().isOk())
        mockMvc
            .perform(
                post("/ingest/entries")
                    .content("""[{"name":"event2"},{"name":"event3"}]""")
                    .addDefaultAuth()
                    .addDefaultContentType(),
            ).andExpect(status().isOk())

        mockMvc
            .perform(get("/entries"))
            .andExpect(status().isOk())
            .andExpect(content().json("""[{"name":"event1"},{"name":"event2"},{"name":"event3"}]"""))
    }

    @Test
    fun wrongCredentials() {
        mockMvc
            .perform(
                post("/ingest/entry")
                    .content("""{"name":"event1"}""")
                    // uses no credentials
                    .addDefaultContentType(),
            ).andExpect(status().isUnauthorized())

        mockMvc
            .perform(
                post("/ingest/entry")
                    .content("""{"name":"event1"}""")
                    // uses ingest:whatever as wrong auth
                    .header("Authorization", "Basic aW5nZXN0OndoYXRldmVy")
                    .addDefaultContentType(),
            ).andExpect(status().isUnauthorized())
    }

    /** add default ingest:ingest auth */
    fun MockHttpServletRequestBuilder.addDefaultAuth(): MockHttpServletRequestBuilder {
        header("Authorization", "Basic aW5nZXN0OmluZ2VzdA==")
        return this
    }

    /** add default application/json content type */
    fun MockHttpServletRequestBuilder.addDefaultContentType(): MockHttpServletRequestBuilder {
        header("Content-Type", "application/json")
        return this
    }
}
