package base.boudicca.publisher.event.html.restcontroller

import base.boudicca.publisher.event.html.model.MapSearchResultDTO
import base.boudicca.publisher.event.html.model.SearchDTO
import base.boudicca.publisher.event.html.service.EventService
import base.boudicca.publisher.event.html.util.SearchUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.ModelAndView

@RestController
@RequestMapping("/api")
class SearchRestController @Autowired constructor(
    private val eventService: EventService,
) {

    @GetMapping("/search")
    fun search(
        searchDTO: SearchDTO
    ): ModelAndView {
        val data: MutableMap<String, Any> = HashMap()
        SearchUtils.searchAndAddToModel(eventService, searchDTO, data)
        return ModelAndView("events/eventsRaw", data)
    }

    @GetMapping("/mapSearch", produces = ["application/json"])
    @ResponseBody
    fun mapSearch(
        searchDTO: SearchDTO
    ): MapSearchResultDTO {
        return eventService.mapSearch(searchDTO)
    }
}





