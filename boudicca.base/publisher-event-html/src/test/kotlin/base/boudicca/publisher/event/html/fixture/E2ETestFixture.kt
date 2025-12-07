package base.boudicca.publisher.event.html.fixture

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance

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
                .setHeadless(true)
                .setSlowMo(100.0)
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
