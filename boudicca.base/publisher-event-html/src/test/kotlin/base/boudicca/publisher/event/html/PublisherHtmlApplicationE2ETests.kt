package base.boudicca.publisher.event.html

import base.boudicca.api.search.QueryDTO
import base.boudicca.api.search.SearchResultDTO
import base.boudicca.model.Event
import base.boudicca.publisher.event.html.fixture.E2ETestFixture
import base.boudicca.publisher.event.html.service.SearchServiceCaller
import base.boudicca.publisher.event.html.testdata.*
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import com.microsoft.playwright.options.ElementState
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.OffsetDateTime


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension::class)
@Tag("playwright")
class PublisherHtmlApplicationE2ETests: E2ETestFixture() {

  @LocalServerPort
  private val port = 0

  @MockBean
  lateinit var searchServiceCaller: SearchServiceCaller

  @ParameterizedTest
  @ArgumentsSource(E2EGeneralTestData::class)
  fun pageShouldHaveCorrectTitle(
    events: List<Event>,
    filters: Map<String, List<String>>
  ) {
    setupSearchServiceCaller(events, filters)

    page.navigate("http://localhost:$port/")
    assertThat(page).hasTitle("Boudicca")
  }

  @ParameterizedTest
  @ArgumentsSource(E2ESingleEventTestData::class)
  fun eventShouldBeVisibleInViewport(
    events: List<Event>,
    filters: Map<String, List<String>>
  ) {
    setupSearchServiceCaller(events, filters)

    page.navigate("http://localhost:$port/")

    assertThat(page.locator(".event")).isInViewport()
  }

  @ParameterizedTest
  @ArgumentsSource(E2ESingleEventTestData::class)
  fun eventDetailsShouldBeCorrect(
    events: List<Event>,
    filters: Map<String, List<String>>
  ) {
    setupSearchServiceCaller(events, filters)

    page.navigate("http://localhost:$port/")

    assertThat(page.locator(".event-details").first()).containsText("01.01.2023 um 13:00 Uhr")
    assertThat(page.locator(".event-details").last()).containsText("Theater, Linz")
    assertThat(page.locator(".event-title")).equals("Musical Event in Innenstadt")
  }

  @ParameterizedTest
  @ArgumentsSource(SingleEventWithA11YInformation::class)
  fun shouldDisplayA11YIconWhenTheEventIsAccessible(
    events: List<Event>,
    filters: Map<String, List<String>>
  ) {
    setupSearchServiceCaller(events, filters)

    page.navigate("http://localhost:$port/")

    assertThat(page.locator(".accessibility-details")).isVisible()
  }

  @ParameterizedTest
  @ArgumentsSource(E2EGeneralTestData::class)
  fun shouldListCorrectNumberOfEvents(
    events: List<Event>,
    filters: Map<String, List<String>>
  ) {
    setupSearchServiceCaller(events, filters)

    page.navigate("http://localhost:$port/")

    val events = page.querySelectorAll(".event")
    val eventSize = events.size

    assertTrue(eventSize == 30) { "Expected 30 events, but found $eventSize events." }
  }

  @ParameterizedTest
  @ArgumentsSource(E2ESingleEventTestData::class)
  fun shouldHaveEventUrl(
    events: List<Event>,
    filters: Map<String, List<String>>
  ) {
    setupSearchServiceCaller(events, filters)

    page.navigate("http://localhost:$port/")

    assertTrue(page.locator(".anchor-to-eventpage").getAttribute("href") == "https://www.event.page.at/")
  }

  @ParameterizedTest
  @ArgumentsSource(ListOfEventWithDifferentNameToBeSearchable::class)
  fun shouldBeAbleToSearchBetweenEvents(
    events: List<Event>,
    filters: Map<String, List<String>>
  ) {
    whenever(searchServiceCaller!!.search(any())).then {
      val queryArgument = it.arguments.first() as QueryDTO
      if (queryArgument.query.contains("Cultural")) {
        SearchResultDTO(
          listOf(
            Event("Cultural Event at Posthof", OffsetDateTime.now(), mapOf())
          ),
          1,
          null
        )
      } else {
        SearchResultDTO(
          events,
          events.size,
          null
        )
      }
    }
    whenever(searchServiceCaller!!.getFiltersFor(any())).thenReturn(filters)

    page.navigate("http://localhost:$port")

    page.getByTestId("search-field").fill("Cultural")
    page.getByTestId("search-field").press("Enter")

    val events = page.querySelectorAll(".event")
    val eventSize = events.size

    assertTrue(eventSize == 1) { "Expected 1 event, but found $eventSize events." }
    assertThat(page.locator(".event")).containsText("Cultural")
  }

  @ParameterizedTest
  @ArgumentsSource(ListOfFilterableEvents::class)
  fun shouldBeAbleToFilterEvents(
    events: List<Event>,
    filters: Map<String, List<String>>
  ) {
    whenever(searchServiceCaller!!.search(any())).then {
      val queryArgument = it.arguments.first() as QueryDTO
      if (queryArgument.query.contains("Linz")) {
        SearchResultDTO(
          listOf(
            Event("Cultural Event at Posthof", OffsetDateTime.now(), mapOf("city" to "Linz"))
          ),
          1,
          null
        )
      } else {
        SearchResultDTO(
          events,
          events.size,
          null
        )
      }
    }
    whenever(searchServiceCaller!!.getFiltersFor(any())).thenReturn(filters)

    page.navigate("http://localhost:$port")

    page.locator("button[id=\"filterButton\"]").click()

    // It is important to waitFor() the page to be in the desired
    // state *before* running analyze(). Otherwise, axe might not
    // find all the elements your test expects it to scan.
    page.locator("#drawer").waitFor()

    page.locator("[name='locationCity']").selectOption("Linz")
    page.locator("button[id='filterSearchButton']").click()

    page.waitForFunction(
      "() => !document.querySelector('#drawer').classList.contains('drawer-open')"
    )

    val events = page.querySelectorAll(".event")
    val eventSize = events.size

    assertTrue(eventSize == 1) { "Expected 1 event, but found $eventSize events." }
    assertThat(page.locator(".event")).containsText("Cultural")
  }

  @ParameterizedTest
  @ArgumentsSource(E2EGeneralTestData::class)
  fun shouldBeAbleToLoadMoreEvents(
    events: List<Event>,
    filters: Map<String, List<String>>
  ) {
    whenever(searchServiceCaller!!.search(any())).then {
      val queryArgument = it.arguments.first() as QueryDTO
      if (queryArgument.query.contains("category")) {
        SearchResultDTO(
          events + listOf(Event("Cultural Event at Posthof", OffsetDateTime.now(), mapOf("city" to "Linz"))),
          events.size + 1,
          null
        )
      } else {
        SearchResultDTO(
          events,
          events.size,
          null
        )
      }
    }
    whenever(searchServiceCaller!!.getFiltersFor(any())).thenReturn(filters)

    page.navigate("http://localhost:$port")

    page.locator("button[id='loadMoreButton']").click()

    val events = page.querySelectorAll(".event")
    val eventSize = events.size

    assertTrue(eventSize == 31) { "Expected 1 event, but found $eventSize events." }
    assertThat(page.locator(".event")).containsText("Cultural")
  }

  private fun setupSearchServiceCaller(events: List<Event>, filters: Map<String, List<String>>) {
    whenever(searchServiceCaller!!.search(any())).thenReturn(SearchResultDTO(events, events.size, null))
    whenever(searchServiceCaller!!.getFiltersFor(any())).thenReturn(filters)
  }
}
