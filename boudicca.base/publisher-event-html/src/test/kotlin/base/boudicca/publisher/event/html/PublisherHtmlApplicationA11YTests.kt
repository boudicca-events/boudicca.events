package base.boudicca.publisher.event.html

import base.boudicca.api.search.SearchResultDTO
import base.boudicca.model.Event
import base.boudicca.publisher.event.html.fixture.E2ETestFixture
import base.boudicca.publisher.event.html.service.SearchServiceCaller
import base.boudicca.publisher.event.html.testdata.A11YTestData
import com.deque.html.axecore.playwright.AxeBuilder
import com.deque.html.axecore.results.AxeResults
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.*

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension::class)
@Tag("a11ytests")
class PublisherHtmlApplicationA11YTests : E2ETestFixture() {

    @LocalServerPort
    private val port = 0

    @MockkBean
    private lateinit var searchServiceCaller: SearchServiceCaller

    @ParameterizedTest
    @ArgumentsSource(A11YTestData::class)
    fun shouldNotHaveAutomaticallyDetectableAccessibilityIssues(
        events: List<Event>,
        filters: Map<String, List<String>>
    ) {
        setupSearchServiceCaller(events, filters)

        page.navigate("http://localhost:$port/")

        val accessibilityScanResults: AxeResults = AxeBuilder(page).analyze()
        assertEquals(listOf<Any>(), accessibilityScanResults.violations)
    }

    @ParameterizedTest
    @ArgumentsSource(A11YTestData::class)
    fun drawerShouldNotHaveAutomaticallyDetectableAccessibilityViolations(
        events: List<Event>,
        filters: Map<String, List<String>>
    ) {
        setupSearchServiceCaller(events, filters)

        page.navigate("http://localhost:$port/")

        page.locator("button[id=\"filterButton\"]").click()

        // It is important to waitFor() the page to be in the desired
        // state *before* running analyze(). Otherwise, axe might not
        // find all the elements your test expects it to scan.
        page.locator("#drawer").waitFor()

        val accessibilityScanResults: AxeResults = AxeBuilder(page)
            .include(Arrays.asList("#drawer"))
            .analyze()

        assertEquals(listOf<Any>(), accessibilityScanResults.violations)
    }

    private fun setupSearchServiceCaller(events: List<Event>, filters: Map<String, List<String>>) {
        every { searchServiceCaller.search(any()) } returns SearchResultDTO(events, 1, null)
        every { searchServiceCaller.getFiltersFor(any()) } returns filters
    }
}
