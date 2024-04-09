package base.boudicca.publisher.event.html

import base.boudicca.api.search.SearchResultDTO
import base.boudicca.model.Event
import base.boudicca.publisher.event.html.service.SearchServiceCaller
import com.deque.html.axecore.playwright.AxeBuilder
import com.deque.html.axecore.results.AxeResults
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.*

import base.boudicca.publisher.event.html.testdata.A11YTestData
import org.junit.jupiter.params.provider.ArgumentsSource

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension::class)
@Tag("playwright")
class PublisherHtmlApplicationA11YTests: E2ETestFixture() {

  @LocalServerPort
  private val port = 0

  @MockBean
  lateinit var searchServiceCaller: SearchServiceCaller

  @ParameterizedTest
  @ArgumentsSource(A11YTestData::class)
  fun shouldNotHaveAutomaticallyDetectableAccessibilityIssues(
    events: List<Event>,
    filters: Map<String, List<String>>
  ) {
    whenever(searchServiceCaller!!.search(any())).thenReturn(SearchResultDTO(events, 1, null))
    whenever(searchServiceCaller!!.getFiltersFor(any())).thenReturn(filters)

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
    whenever(searchServiceCaller!!.search(any())).thenReturn(SearchResultDTO(events, 1, null))
    whenever(searchServiceCaller!!.getFiltersFor(any())).thenReturn(filters)

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
}
