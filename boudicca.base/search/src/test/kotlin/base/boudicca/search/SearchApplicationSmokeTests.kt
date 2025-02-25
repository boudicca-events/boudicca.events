package base.boudicca.search

import base.boudicca.search.service.EventFetcher
import base.boudicca.search.service.SynchronizationService
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class SearchApplicationSmokeTests {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var synchronizationService: SynchronizationService

    @MockkBean
    private lateinit var eventFetcher: EventFetcher

    @Test
    fun contextLoads() {
        // This test will automatically pass if the application context loads successfully.
        // If there's any bean misconfiguration or other Spring-related errors, it will fail.
    }

    @Test
    fun baseWorkflow() {
        every { eventFetcher.fetchAllEvents() } returns setOf(
            mapOf("name" to "coolband"),
            mapOf("name" to "theatre")
        )
        synchronizationService.update()

        mockMvc.perform(
            post("/queryEntries").content("""{"query":"\"name\" contains \"band\""}""")
                .addDefaultContentType()
        ).andExpect(status().isOk())
            .andExpect(content().json("""{"result":[{"name":"coolband"}],"totalResults":1,"error":null}"""))
    }

    /** add default application/json content type */
    fun MockHttpServletRequestBuilder.addDefaultContentType(): MockHttpServletRequestBuilder {
        header("Content-Type", "application/json")
        return this
    }
}
