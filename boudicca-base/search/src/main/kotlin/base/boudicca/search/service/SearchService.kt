package base.boudicca.search.service

import base.boudicca.model.Entry
import base.boudicca.SemanticKeys
import base.boudicca.model.EventCategory
import base.boudicca.search.model.Filters
import base.boudicca.search.model.QueryDTO
import base.boudicca.search.model.ResultDTO
import base.boudicca.search.model.SearchDTO
import base.boudicca.search.service.util.Utils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private const val SEARCH_TYPE_ALL = "ALL"
private const val SEARCH_TYPE_OTHER = "OTHER"

@Service
class SearchService @Autowired constructor(
    private val queryService: QueryService,
) {

    @Volatile
    private var entries = emptyList<Entry>()

    @Volatile
    private var locationNames = emptySet<String>()

    @Volatile
    private var locationCities = emptySet<String>()

    @Deprecated("use queries instead")
    fun search(searchDTO: SearchDTO): ResultDTO {
        val query = createQuery(searchDTO)
        return if (query.isNotBlank()) {
            queryService.query(QueryDTO(query, searchDTO.offset))
        } else {
            Utils.offset(entries, searchDTO.offset, searchDTO.size)
        }
    }

    @EventListener
    fun onEventsUpdate(event: EntriesUpdatedEvent) {
        this.entries = Utils.order(event.entries)
        locationNames = this.entries
            .mapNotNull { it[SemanticKeys.LOCATION_NAME] }
            .filter { it.isNotBlank() }
            .toSet()
        locationCities = this.entries
            .mapNotNull { it[SemanticKeys.LOCATION_CITY] }
            .filter { it.isNotBlank() }
            .toSet()
    }

    private fun createQuery(searchDTO: SearchDTO): String {
        val queryParts = mutableListOf<String>()
        val name = searchDTO.name
        if (!name.isNullOrBlank()) {
            queryParts.add("\"*\" contains " + escape(name))
        }
        val category = searchDTO.category
        if (!category.isNullOrBlank() && category != SEARCH_TYPE_ALL) {
            queryParts.add(SemanticKeys.CATEGORY + " equals " + escape(category))
        }
        val locationCity = searchDTO.locationCity
        if (!locationCity.isNullOrBlank()) {
            queryParts.add(SemanticKeys.LOCATION_CITY + " equals " + escape(locationCity))
        }
        val locationName = searchDTO.locationName
        if (!locationName.isNullOrBlank()) {
            queryParts.add(SemanticKeys.LOCATION_NAME + " equals " + escape(locationName))
        }
        val fromDate = searchDTO.fromDate
        if (fromDate != null) {
            queryParts.add(SemanticKeys.STARTDATE + " after " + formatDate(fromDate))
        }
        val toDate = searchDTO.toDate
        if (toDate != null) {
            queryParts.add(SemanticKeys.STARTDATE + " before " + formatDate(toDate))
        }
        val durationShorter = searchDTO.durationShorter
        if (durationShorter != null) {
            queryParts.add(
                "duration ${escape(SemanticKeys.STARTDATE)} ${escape(SemanticKeys.ENDDATE)} shorter "
                        + formatNumber(durationShorter)
            )
        }
        val durationLonger = searchDTO.durationLonger
        if (durationLonger != null) {
            queryParts.add(
                "duration ${escape(SemanticKeys.STARTDATE)} ${escape(SemanticKeys.ENDDATE)} longer "
                        + formatNumber(durationLonger)
            )
        }
        for (flag in (searchDTO.flags ?: emptyList()).filter { !it.isNullOrBlank() }) {
            queryParts.add(escape(flag!!) + " equals \"true\"")
        }
        return queryParts.joinToString(" and ")
    }

    private fun formatNumber(durationShorter: Double): String {
        return durationShorter.toString()
    }

    private fun formatDate(date: OffsetDateTime): String {
        return date.atZoneSameInstant(ZoneId.of("Europe/Vienna")).format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    private fun escape(name: String): String {
        return "\"" + name.replace("\\", "\\\\").replace("\"", "\\\"") + "\""
    }

    fun filters(): Filters {
        return Filters(
            getCategories(),
            getLocationNames(),
            getLocationCities(),
        )
    }

    @Deprecated("do use the enum directly")
    private fun getCategories(): Set<String> {
        val categories = EventCategory.entries.map(EventCategory::name).toMutableSet()
        categories.add(SEARCH_TYPE_ALL)
        categories.add(SEARCH_TYPE_OTHER)
        return categories.toSet()
    }

    private fun getLocationNames(): Set<String> {
        return locationNames
    }

    private fun getLocationCities(): Set<String> {
        return locationCities
    }

}