package base.boudicca.publisher.event.html.fixture

import com.microsoft.playwright.*
import org.junit.jupiter.api.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
open class E2ETestFixture {
  var playwright: Playwright = Playwright.create()

  lateinit var browser: Browser
  lateinit var context: BrowserContext
  lateinit var page: Page

  @BeforeAll
  fun launchBrowser() {
    browser = playwright.chromium().launch(
      BrowserType
        .LaunchOptions()
        .setHeadless(false)
        .setSlowMo(10.0)
    )
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
}