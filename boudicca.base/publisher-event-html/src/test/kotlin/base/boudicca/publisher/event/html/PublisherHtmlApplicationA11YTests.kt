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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.web.server.LocalServerPort
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.OffsetDateTime


@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension::class)
@Tag("playwright")
class PublisherHtmlApplicationA11YTests {

  @LocalServerPort
  private val port = 0

  var playwright: Playwright = Playwright.create()

  @MockBean
  lateinit var searchServiceCaller: SearchServiceCaller

  @Test
  fun shouldNotHaveAutomaticallyDetectableAccessibilityIssues() {
    val browser: Browser = playwright.chromium().launch()
    val context: BrowserContext = browser.newContext()
    val page: Page = context.newPage()

    whenever(searchServiceCaller!!.search(any())).thenReturn(
      SearchResultDTO(
        listOf(
          Event(
            "hello",
            OffsetDateTime.now(),
            mapOf()
          )
        ), 1, null
      )
    )

    whenever(searchServiceCaller!!.getFiltersFor(any())).thenReturn(
      mapOf(
        SemanticKeys.LOCATION_NAME to listOf("Location1", "Location2", "Location3"),
        SemanticKeys.LOCATION_CITY to listOf("City1", "City2", "City3"),
        SemanticKeys.CONCERT_BANDLIST to listOf("Band1", "Band2", "Band3")
      )
    )

    page.navigate("http://localhost:$port/")

    val accessibilityScanResults: AxeResults = AxeBuilder(page).analyze()

    assertEquals(listOf<Any>(), accessibilityScanResults.violations)
  }
}
