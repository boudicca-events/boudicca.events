package events.boudicca.publisherhtml.restcontroller

import events.boudicca.publisherhtml.service.EventService
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
        searchDTO: events.boudicca.publisherhtml.model.SearchDTO
    ): ModelAndView {
        val events = eventService.search(searchDTO)
        return ModelAndView("events/eventsRaw", mapOf("events" to events))
    }
}





