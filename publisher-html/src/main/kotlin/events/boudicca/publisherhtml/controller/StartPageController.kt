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
        data["events"] = eventService.getAllEvents()
        data["filters"] = eventService.filters()
        return ModelAndView("index", data)
    }

    @GetMapping("/search")
    @ResponseBody
    fun search(
        @RequestParam("name", required = false) name: String?,
        @RequestParam("fromDate", required = false) fromDate: String?,
        @RequestParam("toDate", required = false) toDate: String?,
        @RequestParam("type", required = false) type: String?,
        @RequestParam("locationName", required = false) locationName: String?,
        @RequestParam("locationCity", required = false) locationCity: String?,
    ): ModelAndView {
        val data: MutableMap<String, Any> = HashMap()
        data["title"] = PAGE_TITLE

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
        data["events"] = eventService.search(
            SearchDTO().name(name).fromDate(fromDateParsed).toDate(toDateParsed).type(type).locationName(locationName)
                .locationCity(locationCity).offset(0)
        )
        data["filters"] = eventService.filters()
        return ModelAndView("index", data)
    }
}





