package base.boudicca.publisher.event.html

import base.boudicca.api.search.QueryDTO
import base.boudicca.api.search.SearchResultDTO
import base.boudicca.model.Event
import base.boudicca.publisher.event.html.fixture.E2ETestFixture
import base.boudicca.publisher.event.html.service.SearchServiceCaller
import base.boudicca.publisher.event.html.testdata.E2EGeneralTestData
import base.boudicca.publisher.event.html.testdata.E2ESingleEventTestData
import base.boudicca.publisher.event.html.testdata.ListOfEventWithDifferentNameToBeSearchable
import base.boudicca.publisher.event.html.testdata.ListOfFilterableEvents
import base.boudicca.publisher.event.html.testdata.SingleEventWithA11YInformation
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.OffsetDateTime

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension::class)
@Tag("uitest")
class PublisherHtmlApplicationE2ETests : E2ETestFixture() {
    @LocalServerPort
    private val port = 0

    @MockkBean
    private lateinit var searchServiceCaller: SearchServiceCaller

    @ParameterizedTest
    @ArgumentsSource(E2EGeneralTestData::class)
    fun pageShouldHaveCorrectTitle(events: List<Event>, filters: Map<String, List<String>>) {
        setupSearchServiceCaller(events, filters)

        page.navigate("http://localhost:$port/")
        assertThat(page).hasTitle("Boudicca.Events - find accessible events in Austria")
    }

    @ParameterizedTest
    @ArgumentsSource(E2ESingleEventTestData::class)
    fun eventShouldBeVisibleInViewport(events: List<Event>, filters: Map<String, List<String>>) {
        setupSearchServiceCaller(events, filters)

        page.navigate("http://localhost:$port/")

        assertThat(page.locator(".event")).isInViewport()
    }

    @ParameterizedTest
    @ArgumentsSource(E2ESingleEventTestData::class)
    fun eventDetailsShouldBeCorrect(events: List<Event>, filters: Map<String, List<String>>) {
        setupSearchServiceCaller(events, filters)

        page.navigate("http://localhost:$port/")

        assertThat(page.locator(".event-details").first()).containsText("01.01.2023 um 13:00 Uhr")
        assertThat(page.locator(".event-details").last()).containsText("Theater")
        assertThat(page.locator(".event-details").last()).containsText("Linz")
        assertThat(page.locator(".event-title")).equals("Musical Event in Innenstadt")
    }

    @ParameterizedTest
    @ArgumentsSource(SingleEventWithA11YInformation::class)
    fun shouldDisplayA11YIconWhenTheEventIsAccessible(events: List<Event>, filters: Map<String, List<String>>) {
        setupSearchServiceCaller(events, filters)

        page.navigate("http://localhost:$port/")

        assertThat(page.locator(".accessibility-details")).isVisible()
    }

    @ParameterizedTest
    @ArgumentsSource(E2EGeneralTestData::class)
    fun shouldListCorrectNumberOfEvents(events: List<Event>, filters: Map<String, List<String>>) {
        setupSearchServiceCaller(events, filters)

        page.navigate("http://localhost:$port/")

        val events = page.querySelectorAll(".event")
        val eventSize = events.size

        assertEquals(eventSize, 30) { "Expected 30 events, but found $eventSize events." }
    }

    @ParameterizedTest
    @ArgumentsSource(E2ESingleEventTestData::class)
    fun shouldHaveEventUrl(events: List<Event>, filters: Map<String, List<String>>) {
        setupSearchServiceCaller(events, filters)

        page.navigate("http://localhost:$port/")

        assertThat(page.locator(".anchor-to-eventmodal")).containsText("Zur Eventseite")
    }

    @ParameterizedTest
    @ArgumentsSource(ListOfEventWithDifferentNameToBeSearchable::class)
    fun shouldBeAbleToSearchBetweenEvents(events: List<Event>, filters: Map<String, List<String>>) {
        every { searchServiceCaller.search(any()) } answers {
            val queryArgument = it.invocation.args.first() as QueryDTO
            if (queryArgument.query.contains("Cultural")) {
                SearchResultDTO(
                    listOf(
                        Event("Cultural Event at Posthof", OffsetDateTime.now(), mapOf()),
                    ),
                    1,
                    null,
                )
            } else {
                SearchResultDTO(
                    events,
                    events.size,
                    null,
                )
            }
        }
        every { searchServiceCaller.getFiltersFor(any()) } returns filters

        page.navigate("http://localhost:$port")

        page.getByTestId("search-field").fill("Cultural")
        page.getByTestId("search-field").press("Enter")

        val events = page.querySelectorAll(".event")
        val eventSize = events.size

        assertEquals(eventSize, 1) { "Expected 1 event, but found $eventSize events." }
        assertThat(page.locator(".event")).containsText("Cultural")
    }

    @ParameterizedTest
    @ArgumentsSource(ListOfFilterableEvents::class)
    fun shouldBeAbleToFilterEvents(events: List<Event>, filters: Map<String, List<String>>) {
        every { searchServiceCaller.search(any()) } answers {
            val queryArgument = it.invocation.args.first() as QueryDTO
            if (queryArgument.query.contains("Linz")) {
                SearchResultDTO(
                    listOf(
                        Event("Cultural Event at Posthof", OffsetDateTime.now(), mapOf("city" to "Linz")),
                    ),
                    1,
                    null,
                )
            } else {
                SearchResultDTO(
                    events,
                    events.size,
                    null,
                )
            }
        }
        every { searchServiceCaller.getFiltersFor(any()) } returns filters

        page.navigate("http://localhost:$port")

        page.locator("button[id='filterButton']").click()

        // It is important to waitFor() the page to be in the desired
        // state *before* running analyze(). Otherwise, axe might not
        // find all the elements your test expects it to scan.
        page.locator("#drawer").waitFor()

        page.locator("xpath=//label[@for='locationCities-Linz' and @class='toggleFilterLabels']").click()
        page.locator("button[id='filterSearchButton']").click()

        assertThat(page.locator("#drawer")).isHidden()

        val events = page.querySelectorAll(".event")
        val eventSize = events.size

        assertEquals(eventSize, 1) { "Expected 1 event, but found $eventSize events." }
        assertThat(page.locator(".event")).containsText("Cultural")
    }

    @ParameterizedTest
    @ArgumentsSource(ListOfFilterableEvents::class)
    fun shouldBeAbleToResetFilteredEvents(events: List<Event>, filters: Map<String, List<String>>) {
        every { searchServiceCaller.search(any()) } answers {
            val queryArgument = it.invocation.args.first() as QueryDTO
            if (queryArgument.query.contains("Linz")) {
                SearchResultDTO(
                    listOf(
                        Event("Cultural Event at Posthof", OffsetDateTime.now(), mapOf("city" to "Linz")),
                    ),
                    1,
                    null,
                )
            } else {
                SearchResultDTO(
                    events,
                    events.size,
                    null,
                )
            }
        }
        every { searchServiceCaller.getFiltersFor(any()) } returns filters

        page.navigate("http://localhost:$port")

        page.locator("button[id='filterButton']").click()
        page.locator("#drawer").waitFor()

        page.locator("xpath=//label[@for='locationCities-Linz' and @class='toggleFilterLabels']").click()
        page.locator("button[id='filterSearchButton']").click()

        assertThat(page.locator("#drawer")).isHidden()

        val events = page.querySelectorAll(".event")
        val eventSize = events.size

        assertEquals(eventSize, 1) { "Expected 1 event, but found $eventSize events." }
        assertThat(page.locator(".event")).containsText("Cultural")

        // reopen the drawer and reset the filter
        page.locator("button[id='filterButton']").click()
        page.locator("#drawer").waitFor()

        page.locator("button[id='resetSearchForm']").click()
        page.locator("button[id='filterSearchButton']").click()

        val eventsAfterResettingFilter = page.querySelectorAll(".event")
        val eventSizeAfterRestingFilter = eventsAfterResettingFilter.size

        assertEquals(
            eventSizeAfterRestingFilter,
            3,
        ) { "Expected 3 event, but found $eventSizeAfterRestingFilter events." }
    }

    @ParameterizedTest
    @ArgumentsSource(E2EGeneralTestData::class)
    fun shouldBeAbleToLoadMoreEvents(events: List<Event>, filters: Map<String, List<String>>) {
        every { searchServiceCaller.search(any()) } answers {
            val queryArgument = it.invocation.args.first() as QueryDTO
            if (queryArgument.offset == 30) {
                SearchResultDTO(
                    listOf(
                        Event(
                            "Cultural Event at Posthof",
                            OffsetDateTime.now(),
                            mapOf("city" to "Linz", "url" to "http://url.com"),
                        ),
                    ),
                    1,
                    null,
                )
            } else {
                SearchResultDTO(
                    events,
                    events.size,
                    null,
                )
            }
        }
        every { searchServiceCaller.getFiltersFor(any()) } returns filters

        page.navigate("http://localhost:$port")

        page.locator("button[id='loadMoreButton']").click()

        val events = page.querySelectorAll(".event")
        val eventSize = events.size

        assertEquals(eventSize, 31) { "Expected 31 event, but found $eventSize events." }
    }

    private fun setupSearchServiceCaller(events: List<Event>, filters: Map<String, List<String>>) {
        every { searchServiceCaller.search(any()) } returns SearchResultDTO(events, events.size, null)
        every { searchServiceCaller.getFiltersFor(any()) } returns filters
    }
}
