package base.boudicca.publisher.event.html

import base.boudicca.SemanticKeys
import base.boudicca.api.search.SearchResultDTO
import base.boudicca.model.Event
import base.boudicca.publisher.event.html.service.SearchServiceCaller
import com.deque.html.axecore.playwright.AxeBuilder
import com.deque.html.axecore.results.AxeResults
import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.web.server.LocalServerPort
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.OffsetDateTime
import java.util.stream.Stream


@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("playwright")
class PublisherHtmlApplicationA11YTests {
  // TODO: generalize the class to have before and after hooks
  // TODO: run the test in headed mode on local
  // TODO: have the test report on a github
  // TODO: find a way to keep the mock data in

  @LocalServerPort
  private val port = 0

  var playwright: Playwright = Playwright.create()

  lateinit var browser: Browser
  lateinit var context: BrowserContext
  lateinit var page: Page

  @MockBean
  lateinit var searchServiceCaller: SearchServiceCaller

  @BeforeAll
  fun launchBrowser() {
    browser = playwright.chromium().launch()
  }

  @BeforeEach
  fun createContextAndPage() {
    context = browser.newContext()
    page = context.newPage()
  }

  @AfterAll
  fun closeBrowser() {
    playwright.close()
  }

  @AfterEach
  fun closeContext() {
    context.close()
  }

  @ParameterizedTest
  @MethodSource("testData")
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

  companion object {
    @JvmStatic
    // TODO: find a way to pass data from a file of class to it
    fun testData(): Stream<Arguments> {
      return Stream.of(
        Arguments.of(
          listOf(
            Event("hello1", OffsetDateTime.now(), mapOf()),
            Event("hello2", OffsetDateTime.now(), mapOf()),
            Event("hello3", OffsetDateTime.now(), mapOf()),
            Event("hello4", OffsetDateTime.now(), mapOf())
          ),
          mapOf(
            SemanticKeys.LOCATION_NAME to listOf("Location1", "Location2", "Location3"),
            SemanticKeys.LOCATION_CITY to listOf("City1", "City2", "City3"),
            SemanticKeys.CONCERT_BANDLIST to listOf("Band1", "Band2", "Band3")
          )
        ),
      )
    }
  }
}
