package events.boudicca.publisherhtml.controller

import events.boudicca.publisherhtml.service.EventService
import events.boudicca.search.openapi.model.SearchDTO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.servlet.ModelAndView
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Controller
@RequestMapping("/")
class StartPageController @Autowired constructor(private val eventService: EventService) {

    private val PAGE_TITLE = "Boudicca";

    @GetMapping("/")
    fun getIndex(): ModelAndView {
        val data: MutableMap<String, Any> = HashMap()
        data["title"] = PAGE_TITLE
        data["events"] = eventService.search(SearchDTO())
        data["filters"] = eventService.filters()
        return ModelAndView("index", data)
    }

    @GetMapping("/search")
    @ResponseBody
    fun search(
        @RequestParam("name", required = false) name: String?,
        @RequestParam("fromDate", required = false) fromDate: String?,
        @RequestParam("toDate", required = false) toDate: String?,
        @RequestParam("category", required = false) category: String?,
        @RequestParam("locationName", required = false) locationName: String?,
        @RequestParam("locationCity", required = false) locationCity: String?,
        @RequestParam("flags", required = false) flags: List<String?>?,
    ): ModelAndView {
        val data: MutableMap<String, Any> = HashMap()
        data["title"] = PAGE_TITLE

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        val fromDateParsed = if (!fromDate.isNullOrBlank()) {
            LocalDate.parse(fromDate, formatter).atStartOfDay().atZone(ZoneId.of("Europe/Vienna")).toOffsetDateTime()
        } else {
            null
        }
        val toDateParsed = if (!toDate.isNullOrBlank()) {
            LocalDate.parse(toDate, formatter).atStartOfDay().atZone(ZoneId.of("Europe/Vienna")).toOffsetDateTime()
        } else {
            null
        }
        data["events"] = eventService.search(
            SearchDTO().name(name).fromDate(fromDateParsed).toDate(toDateParsed).category(category).locationName(locationName)
                .locationCity(locationCity).offset(0).flags(flags)
        )
        data["filters"] = eventService.filters()
        return ModelAndView("index", data)
    }
}





