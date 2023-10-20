package base.boudicca.publisherhtml.restcontroller

import base.boudicca.publisherhtml.service.EventService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.ModelAndView

@RestController
@RequestMapping("/api")
class SearchRestController @Autowired constructor(private val eventService: EventService) {

    @GetMapping("/search")
    @ResponseBody
    fun search(
        searchDTO: base.boudicca.publisherhtml.model.SearchDTO
    ): ModelAndView {
        val events = eventService.search(searchDTO)
        return ModelAndView("events/eventsRaw", mapOf("events" to events))
    }
}





