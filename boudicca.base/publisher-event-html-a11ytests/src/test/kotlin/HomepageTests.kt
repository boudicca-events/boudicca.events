import com.deque.html.axecore.playwright.*
import com.deque.html.axecore.results.*

import org.junit.jupiter.api.*
import com.microsoft.playwright.*

import org.junit.jupiter.api.Assertions.*

class HomepageTests {
  @Test
  fun shouldNotHaveAutomaticallyDetectableAccessibilityIssues() {
    val playwright: Playwright = Playwright.create()
    val browser: Browser = playwright.chromium().launch()
    val context: BrowserContext = browser.newContext()
    val page: Page = context.newPage()

    // TODO: add a config to run the OnlineHtmlPublisher in advance
    page.navigate("http://localhost:8080/")

    val accessibilityScanResults: AxeResults = AxeBuilder(page).analyze()

    assertEquals(listOf<Any>(), accessibilityScanResults.violations)
  }
}
