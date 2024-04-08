package base.boudicca.publisher.event.html

import com.microsoft.playwright.APIRequest
import com.microsoft.playwright.Browser
import com.microsoft.playwright.junit.OptionsFactory
import com.microsoft.playwright.junit.Options;

class E2ECustomOptions : OptionsFactory {
  override fun getOptions(): Options {
    return Options()
      .setHeadless(false)
      .setContextOption(
        Browser.NewContextOptions()
          .setBaseURL("https://github.com")
      )
      .setApiRequestOptions(
        APIRequest.NewContextOptions()
          .setBaseURL("https://playwright.dev")
      )
  }
}
