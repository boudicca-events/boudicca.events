package base.boudicca.publisher.event.html.service

import base.boudicca.SemanticKeys
import base.boudicca.api.search.*
import base.boudicca.format.ListFormatAdapter
import base.boudicca.format.NumberFormatAdapter
import base.boudicca.keyfilters.KeySelector
import base.boudicca.model.EventCategory
import base.boudicca.model.structured.Key
import base.boudicca.model.structured.StructuredEvent
import base.boudicca.model.structured.VariantConstants
import base.boudicca.model.structured.VariantConstants.FormatVariantConstants
import base.boudicca.publisher.event.html.model.Location
import base.boudicca.publisher.event.html.model.LocationEvent
import base.boudicca.publisher.event.html.model.MapSearchResultDTO
import base.boudicca.publisher.event.html.model.SearchDTO
import base.boudicca.query.BoudiccaQueryBuilder.after
import base.boudicca.query.BoudiccaQueryBuilder.and
import base.boudicca.query.BoudiccaQueryBuilder.before
import base.boudicca.query.BoudiccaQueryBuilder.contains
import base.boudicca.query.BoudiccaQueryBuilder.durationLonger
import base.boudicca.query.BoudiccaQueryBuilder.durationShorter
import base.boudicca.query.BoudiccaQueryBuilder.equals
import base.boudicca.query.BoudiccaQueryBuilder.hasField
import base.boudicca.query.BoudiccaQueryBuilder.not
import base.boudicca.query.BoudiccaQueryBuilder.or
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.net.URI
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.jvm.optionals.getOrNull

// we do not want long-running events on our site, so we filter for events short then 30 days
const val DEFAULT_DURATION_SHORTER_VALUE = 24 * 30

private const val MAP_SEARCH_RESULT_COUNT = 200

//TODO we should think about reducing the size of this class, maybe we can split out all the property selecting methods
@Service
@Suppress("detekt:TooManyFunctions")
class EventService @Autowired constructor(
    private val pictureProxyService: PictureProxyService,
    private val caller: SearchServiceCaller,
    @Value("\${boudicca.search.additionalFilter:}") private val additionalFilter: String,
) {
    companion object {
        private val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy 'um' HH:mm 'Uhr'", Locale.GERMAN)
        private val localDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        private val additionalMapQueryParts = listOf(hasField(SemanticKeys.LOCATION_OSM_ID))
        private val logger = KotlinLogging.logger {}
    }

    @Throws(EventServiceException::class)
    fun search(searchDTO: SearchDTO): List<Map<String, Any?>> {
        val searchResult = caller.search(QueryDTO(generateQuery(searchDTO), searchDTO.offset ?: 0))
        return mapEvents(searchResult)
    }

    @Throws(EventServiceException::class)
    fun mapSearch(searchDTO: SearchDTO): MapSearchResultDTO {
        val searchResultDTO =
            caller.search(
                QueryDTO(
                    generateQuery(searchDTO, additionalMapQueryParts), searchDTO.offset ?: 0,
                    MAP_SEARCH_RESULT_COUNT
                )
            )
        return mapMapSearch(searchResultDTO)
    }

    fun generateQuery(searchDTO: SearchDTO, additionalQueryParts: List<String> = emptyList()): String {
        val name = searchDTO.name
        val query = if (name != null && name.startsWith('!')) {
            name.substring(1)
        } else {
            setDefaults(searchDTO)
            buildQuery(searchDTO, additionalQueryParts)
        }
        return query
    }

    @Suppress("detekt:CyclomaticComplexMethod")
    private fun buildQuery(searchDTO: SearchDTO, additionalQueryParts: List<String> = emptyList()): String {
        val queryParts = mutableListOf<String>()
        if (!searchDTO.name.isNullOrBlank()) {
            queryParts.add(contains("*", searchDTO.name!!))
        }
        addSubqueryOfFieldConnectedByOr(queryParts, SemanticKeys.CATEGORY, searchDTO.category)
        addSubqueryOfFieldConnectedByOr(queryParts, SemanticKeys.LOCATION_CITY, searchDTO.locationCities)
        addSubqueryOfFieldConnectedByOr(queryParts, SemanticKeys.LOCATION_NAME, searchDTO.locationNames)
        if (!searchDTO.fromDate.isNullOrBlank()) {
            queryParts.add(after(SemanticKeys.STARTDATE, LocalDate.parse(searchDTO.fromDate!!, localDateFormatter)))
        }
        if (!searchDTO.toDate.isNullOrBlank()) {
            queryParts.add(before(SemanticKeys.STARTDATE, LocalDate.parse(searchDTO.toDate!!, localDateFormatter)))
        }
        if (searchDTO.durationShorter != null) {
            queryParts.add(durationShorter(SemanticKeys.STARTDATE, SemanticKeys.ENDDATE, searchDTO.durationShorter!!))
        }
        if (searchDTO.durationLonger != null) {
            queryParts.add(durationLonger(SemanticKeys.STARTDATE, SemanticKeys.ENDDATE, searchDTO.durationLonger!!))
        }
        for (flag in (searchDTO.flags ?: emptyList()).filter { !it.isNullOrBlank() }) {
            queryParts.add(equals(flag!!, "true"))
        }
        addSubqueryOfFieldConnectedByOr(queryParts, SemanticKeys.TAGS, searchDTO.tags)
        addSubqueryOfFieldConnectedByOr(queryParts, SemanticKeys.CONCERT_BANDLIST, searchDTO.bandNames)
        if (searchDTO.includeRecurring != true) {
            queryParts.add(
                or(
                    not(hasField(SemanticKeys.RECURRENCE_TYPE)),
                    equals(SemanticKeys.RECURRENCE_TYPE, "ONCE")
                )
            )
        }
        if (additionalFilter.isNotBlank()) {
            queryParts.add(additionalFilter)
        }
        if (!searchDTO.sportParticipation.isNullOrBlank()) {
            queryParts.add(equals("sport.participation", searchDTO.sportParticipation!!))
        }
        queryParts.addAll(additionalQueryParts)
        return and(queryParts)
    }

    fun filters(): Filters {
        val filters = caller.getFiltersFor(
            FilterQueryDTO(
                listOf(
                    FilterQueryEntryDTO(SemanticKeys.LOCATION_NAME),
                    FilterQueryEntryDTO(SemanticKeys.LOCATION_CITY),
                    FilterQueryEntryDTO(SemanticKeys.TAGS),
                    FilterQueryEntryDTO(SemanticKeys.CONCERT_BANDLIST),
                )
            )
        )
        return Filters(
            EventCategory.entries
                .map { Triple(it.name, frontEndName(it), frontEndId(it.name)) }
                .sortedWith(Comparator.comparing({ it.second }, String.CASE_INSENSITIVE_ORDER)),
            filters[SemanticKeys.LOCATION_NAME]!!.sortedWith(String.CASE_INSENSITIVE_ORDER).map { Triple(it, it, frontEndId(it)) },
            filters[SemanticKeys.LOCATION_CITY]!!.sortedWith(String.CASE_INSENSITIVE_ORDER).map { Triple(it, it, frontEndId(it)) },
            filters[SemanticKeys.TAGS]!!.sortedWith(String.CASE_INSENSITIVE_ORDER).map { Triple(it, it, frontEndId(it)) },
            filters[SemanticKeys.CONCERT_BANDLIST]!!.sortedWith(String.CASE_INSENSITIVE_ORDER).map { Triple(it, it, frontEndId(it)) },
        )
    }

    private fun mapEvents(result: SearchResultDTO): List<Map<String, Any?>> {
        checkResult(result)
        return result.result.map { mapEvent(it.toStructuredEvent()) }
    }

    private fun checkResult(result: SearchResultDTO) {
        if (result.error != null) {
            throw EventServiceException("error executing query search: " + result.error, null, true)
        }
    }

    private fun mapEvent(event: StructuredEvent): Map<String, Any?> {
        return mapOf(
            "name" to event.name,
            "startDate" to formatDate(event.startDate),
            "startDateISO" to event.startDate.toString(),
            "description" to getRichTextProperty(event, SemanticKeys.DESCRIPTION),
            "url" to getTextProperty(event, SemanticKeys.URL),
            "locationName" to getTextProperty(event, SemanticKeys.LOCATION_NAME),
            "city" to getTextProperty(event, SemanticKeys.LOCATION_CITY),
            "tags" to getListProperty(event, SemanticKeys.TAGS),
            "category" to mapCategory(getTextProperty(event, SemanticKeys.CATEGORY)),
            "pictureUuid" to getPictureUuid(event),
            "pictureAltText" to getTextProperty(event, SemanticKeys.PICTURE_ALT_TEXT),
            "accessibilityProperties" to getAllAccessibilityValues(event),
            "pictureCopyright" to getTextProperty(event, SemanticKeys.PICTURE_COPYRIGHT),
        )
    }

    private fun getPictureUuid(event: StructuredEvent): String? {
        val pictureUrl = getTextProperty(event, SemanticKeys.PICTURE_URL)
        return if (pictureUrl.isNullOrEmpty()) {
            null
        } else {
            pictureProxyService.submitPicture(pictureUrl).toString()
        }
    }

    private fun getTextProperty(event: StructuredEvent, propertyName: String): String? {
        return getPropertyForFormats(event, propertyName, listOf(FormatVariantConstants.TEXT_FORMAT_NAME))
            .map { it.second }
            .getOrNull()
    }

    private fun getNumberProperty(event: StructuredEvent, propertyName: String): Number? {
        return getPropertyForFormats(event, propertyName, listOf(FormatVariantConstants.NUMBER_FORMAT_NAME))
            .map { it.second }
            .map { NumberFormatAdapter().fromString(it) }
            .getOrNull()
    }

    private fun getRichTextProperty(event: StructuredEvent, propertyName: String): RichText? {
        return getPropertyForFormats(
            event,
            propertyName,
            listOf(FormatVariantConstants.MARKDOWN_FORMAT_NAME, FormatVariantConstants.TEXT_FORMAT_NAME)
        )
            .map { RichText(getIsMarkdownFromFormat(it.first), it.second) }
            .getOrNull()
    }

    private fun getListProperty(event: StructuredEvent, propertyName: String): List<String>? {
        return getPropertyForFormats(
            event,
            propertyName,
            listOf(FormatVariantConstants.LIST_FORMAT_NAME, FormatVariantConstants.TEXT_FORMAT_NAME)
        ) //use text as a wonky fallback for now
            .map {
                try {
                    ListFormatAdapter().fromString(it.second)
                } catch (e: IllegalArgumentException) {
                    logger.warn(e) { "could not parse value for key '${it.first}' to 'list' format: ${it.second}" }
                    null
                }
            }
            .orElseGet { null }
    }

    private fun getPropertyForFormats(
        event: StructuredEvent,
        propertyName: String,
        formatVariants: List<String>
    ): Optional<Pair<Key, String>> {
        return KeySelector.builder(propertyName)
            .thenVariant(
                VariantConstants.LANGUAGE_VARIANT_NAME,
                listOf(
                    getPreferredLanguage(),
                    VariantConstants.LanguageVariantConstants.DEFAULT_LANGUAGE_NAME,
                    VariantConstants.ANY_VARIANT_SELECTOR
                )
            )
            .thenVariant(VariantConstants.FORMAT_VARIANT_NAME, formatVariants)
            .build()
            .selectSingle(event)
    }

    private fun getIsMarkdownFromFormat(key: Key): Boolean {
        val format = key.variants.firstOrNull { it.variantName == VariantConstants.FORMAT_VARIANT_NAME }
        if (format == null) {
            return false
        }
        return format.variantValue == FormatVariantConstants.MARKDOWN_FORMAT_NAME
    }

    private fun getPreferredLanguage(): String {
        return "de" //TODO make user be able to choose this
    }

    private fun getAllAccessibilityValues(event: StructuredEvent): List<String> {
        val list = mutableListOf<String>()
        for (keyValuePair in event.data) {
            if (keyValuePair.key.name.startsWith("accessibility.")) {
                val accessibilityValue = keyValuePair.value
                if (accessibilityValue.toBoolean()) {
                    list.add(
                        when (keyValuePair.key.name) {
                            SemanticKeys.ACCESSIBILITY_ACCESSIBLETOILETS -> "Barrierefreie Toiletten"
                            SemanticKeys.ACCESSIBILITY_ACCESSIBLESEATS -> "Rollstuhlplatz"
                            SemanticKeys.ACCESSIBILITY_ACCESSIBLEENTRY -> "Barrierefreier Zugang"
                            SemanticKeys.ACCESSIBILITY_AKTIVPASSLINZ -> "Aktivpass möglich"
                            SemanticKeys.ACCESSIBILITY_KULTURPASS -> "Kulturpass möglich"
                            else -> keyValuePair.key.name
                        }
                    )
                }
            }
        }
        return list.sortedWith(String.CASE_INSENSITIVE_ORDER)
    }

    private fun mapCategory(categoryString: String?): String? {
        if (categoryString != null) {
            val category = try {
                EventCategory.valueOf(categoryString)
            } catch (e: IllegalArgumentException) {
                logger.warn(e) { "$categoryString is not a valid category" }
                null
            }
            if (category != null) {
                return when (category) {
                    EventCategory.MUSIC -> "music"
                    EventCategory.ART -> "miscArt"
                    EventCategory.TECH -> "tech"
                    EventCategory.SPORT -> "sport"
                    EventCategory.OTHER -> null
                }
            }
        }
        return null
    }

    private fun formatDate(startDate: OffsetDateTime): String {
        return formatter.format(startDate.atZoneSameInstant(ZoneId.of("Europe/Vienna")))
    }

    private fun frontEndName(category: EventCategory): String {
        return when (category) {
            EventCategory.MUSIC -> "Musik"
            EventCategory.ART -> "Kunst"
            EventCategory.TECH -> "Technologie"
            EventCategory.SPORT -> "Sport"
            EventCategory.OTHER -> "Andere"
            else -> "???"
        }
    }

    private fun frontEndId(name: String): String {
        return name.trim().replace(Regex("\\s"), "-")
    }

    private fun setDefaults(searchDTO: SearchDTO) {
        if (searchDTO.fromDate.isNullOrBlank()) {
            searchDTO.fromDate = LocalDate.now().format(localDateFormatter)
        }
        if (searchDTO.durationShorter == null) {
            searchDTO.durationShorter = DEFAULT_DURATION_SHORTER_VALUE.toDouble()
        }
    }

    private fun mapMapSearch(result: SearchResultDTO): MapSearchResultDTO {
        if (!result.error.isNullOrEmpty()) {
            return MapSearchResultDTO(result.error, emptyList())
        } else {
            val events = result.result.map { it.toStructuredEvent() }

            val byLocationName = events.groupBy { getTextProperty(it, SemanticKeys.LOCATION_NAME) ?: "" }

            fun <T> findFirst(events: List<StructuredEvent>, lookup: (StructuredEvent) -> T?): T? {
                return events.firstNotNullOfOrNull(lookup)
            }

            return MapSearchResultDTO(null, byLocationName.mapNotNull {
                val locationName = it.key
                val locationUrl = findFirst(it.value) { event -> getTextProperty(event, SemanticKeys.LOCATION_URL) }
                val locationLat =
                    findFirst(it.value) { event -> getNumberProperty(event, SemanticKeys.LOCATION_COORDINATES_LAT) }
                val locationLon =
                    findFirst(it.value) { event -> getNumberProperty(event, SemanticKeys.LOCATION_COORDINATES_LON) }
                if (locationLat == null || locationLon == null) {
                    null
                } else {
                    Location(
                        locationName,
                        locationUrl,
                        locationLat.toDouble(),
                        locationLon.toDouble(),
                        it.value.sortedBy { event -> event.startDate }.map { event ->
                            LocationEvent(
                                getTextProperty(event, SemanticKeys.NAME) ?: "",
                                getTextProperty(event, SemanticKeys.URL)
                            )
                        }
                    )
                }
            })
        }
    }

    private fun addSubqueryOfFieldConnectedByOr(queryParts: MutableList<String>, semanticKeyField: String, searchInput: List<String?>?) {
        val subqueryParts = mutableListOf<String>()
        for (searchInputElement in (searchInput ?: emptyList()).filter { !it.isNullOrBlank() }) {
            subqueryParts.add(equals(semanticKeyField, searchInputElement!!))
        }
        if(subqueryParts.isNotEmpty()) {
            queryParts.add(or(subqueryParts))
        }
    }

    data class Filters(
        val categories: List<Triple<String, String, String>>,
        val locationNames: List<Triple<String, String, String>>,
        val locationCities: List<Triple<String, String, String>>,
        val tags: List<Triple<String, String, String>>,
        val bandNames: List<Triple<String, String, String>>,
    )

    data class RichText(val isMarkdown: Boolean, val value: String)
}
