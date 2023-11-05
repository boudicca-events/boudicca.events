package base.boudicca.publisher.event.html.model

import org.springframework.web.bind.annotation.RequestParam

data class SearchDTO(
    @RequestParam("name", required = false) var name: String?,
    @RequestParam("offset", required = false) var offset: Int?,
    @RequestParam("fromDate", required = false) var fromDate: String?,
    @RequestParam("toDate", required = false) var toDate: String?,
    @RequestParam("category", required = false) var category: String?,
    @RequestParam("locationName", required = false) var locationName: String?,
    @RequestParam("locationCity", required = false) var locationCity: String?,
    @RequestParam("flags", required = false) var flags: List<String?>?,
    @RequestParam("durationShorter", required = false) var durationShorter: Double?,
    @RequestParam("durationLonger", required = false) var durationLonger: Double?,
    @RequestParam("bandName", required = false) var bandName: String?,
) {
    constructor() : this(null, null, null, null, null, null, null, null, null, null, null)
}