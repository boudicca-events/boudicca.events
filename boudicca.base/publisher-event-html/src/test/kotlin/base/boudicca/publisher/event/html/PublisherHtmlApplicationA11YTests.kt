package base.boudicca.publisher.event.html

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


@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Tag("playwright")
class PublisherHtmlApplicationA11YTests {

  @LocalServerPort
  private val port = 0

  var playwright: Playwright = Playwright.create()

  @Test
  fun shouldNotHaveAutomaticallyDetectableAccessibilityIssues() {
    val browser: Browser = playwright.chromium().launch()
    val context: BrowserContext = browser.newContext()
    val page: Page = context.newPage()

    page.navigate("http://localhost:$port/");

    val accessibilityScanResults: AxeResults = AxeBuilder(page).analyze()

    assertEquals(listOf<Any>(), accessibilityScanResults.violations)
  }
}
