package base.boudicca.publisher.event.html.model

import org.springframework.web.bind.annotation.RequestParam

data class SearchDTO(
    @RequestParam("name", required = false) var name: String?,
    @RequestParam("offset", required = false) var offset: Int?,
    @RequestParam("fromDate", required = false) var fromDate: String?,
    @RequestParam("toDate", required = false) var toDate: String?,
    @RequestParam("category", required = false) var category: String?,
    @RequestParam("locationNames", required = false) var locationNames: List<String?>?,
    @RequestParam("locationCities", required = false) var locationCities: List<String?>?,
    @RequestParam("flags", required = false) var flags: List<String?>?,
    @RequestParam("durationShorter", required = false) var durationShorter: Double?,
    @RequestParam("durationLonger", required = false) var durationLonger: Double?,
    @RequestParam("bandNames", required = false) var bandNames: List<String?>?,
    @RequestParam("includeRecurring", required = false) var includeRecurring: Boolean?,
    @RequestParam("sportParticipation", required = false) var sportParticipation: String?,
) {
    constructor() : this(null, null, null, null, null, null, null, null, null, null, null, null, null)
}
