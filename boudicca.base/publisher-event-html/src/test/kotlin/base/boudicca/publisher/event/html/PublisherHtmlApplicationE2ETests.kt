package base.boudicca.publisher.event.html

import base.boudicca.api.search.SearchResultDTO
import base.boudicca.model.Event
import base.boudicca.publisher.event.html.fixture.E2ETestFixture
import base.boudicca.publisher.event.html.service.SearchServiceCaller
import base.boudicca.publisher.event.html.testdata.E2EGeneralTestData
import base.boudicca.publisher.event.html.testdata.E2ESingleEventTestData
import base.boudicca.publisher.event.html.testdata.E2ESingleEventWithoutURL
import base.boudicca.publisher.event.html.util.SnapshotHandler.SnapshotHandler
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
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
  @ArgumentsSource(E2ESingleEventWithoutURL::class)
  fun eventImageShouldBeCorrect(
    events: List<Event>,
    filters: Map<String, List<String>>
  ) {
    setupSearchServiceCaller(events, filters)

    val snapshotHandler = SnapshotHandler("eventImageShouldBeCorrect.snapshot", "snapshot")

    page.navigate("http://localhost:$port/")

    val image = page.locator(".event-image").innerHTML()

    if (!snapshotHandler.exists()) {
      snapshotHandler.save(image)
    } else {
      assert(image == snapshotHandler.read())
    }
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

    assert(eventSize == 30) { "Expected 30 events, but found $eventSize events." }
  }

  // TODO: test filters
  // TODO: test event link "zur eventseite"
  // TODO: test event with correct image
  // TODO: test event with accessibility icon
  // TODO: test "mehr laden"

  private fun setupSearchServiceCaller(events: List<Event>, filters: Map<String, List<String>>) {
    whenever(searchServiceCaller!!.search(any())).thenReturn(SearchResultDTO(events, 1, null))
    whenever(searchServiceCaller!!.getFiltersFor(any())).thenReturn(filters)
  }
}
