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

    // it should point to localhost
    page.navigate("https://boudicca.events/")

    val accessibilityScanResults: AxeResults = AxeBuilder(page).analyze()

    assertEquals( listOf<Any>(), accessibilityScanResults.violations)
  }
}
