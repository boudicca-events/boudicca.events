package events.boudicca.publisherhtml.restcontroller

import events.boudicca.search.openapi.model.SearchDTO
import events.boudicca.publisherhtml.service.EventService
import events.boudicca.publisherhtml.service.FilterDTO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.ModelAndView
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@RestController
@RequestMapping("/api")
class SearchRestController @Autowired constructor(private val eventService: EventService) {

    @GetMapping("/search")
    @ResponseBody
    fun search(
        @RequestParam("name", required = false) name: String?,
        @RequestParam("offset", required = false) offset: Int?,
        @RequestParam("fromDate", required = false) fromDate: String?,
        @RequestParam("toDate", required = false) toDate: String?,
        @RequestParam("type", required = false) type: String?,
        @RequestParam("locationName", required = false) locationName: String?,
        @RequestParam("locationCity", required = false) locationCity: String?,
    ): ModelAndView {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        val fromDateParsed = if (!fromDate.isNullOrBlank()) {
            LocalDate.parse(fromDate, formatter).atTime(0, 0, 0).atZone(ZoneId.systemDefault()).toOffsetDateTime()
        } else {
            null
        }
        val toDateParsed = if (!toDate.isNullOrBlank()) {
            LocalDate.parse(toDate, formatter).atTime(0, 0, 0).atZone(ZoneId.systemDefault()).toOffsetDateTime()
        } else {
            null
        }

        val events =
            eventService.search(SearchDTO().name(name).fromDate(fromDateParsed).toDate(toDateParsed), offset ?: 0, FilterDTO(type, locationName, locationCity))
        return ModelAndView("events/eventsRaw", mapOf("events" to events))
    }
}





