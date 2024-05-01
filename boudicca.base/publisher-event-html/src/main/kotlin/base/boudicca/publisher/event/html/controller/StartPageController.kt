package base.boudicca.publisher.event.html.controller

import base.boudicca.publisher.event.html.model.SearchDTO
import base.boudicca.publisher.event.html.service.EventService
import base.boudicca.publisher.event.html.util.SearchUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.servlet.ModelAndView

@Controller
@RequestMapping("/")
class StartPageController @Autowired constructor(private val eventService: EventService) {

    private val PAGE_TITLE = "Boudicca.Events - find accessible events in Austria"

    @GetMapping("/")
    fun getIndex(): ModelAndView {
        val data: MutableMap<String, Any> = HashMap()
        data["title"] = PAGE_TITLE
        SearchUtils.searchAndAddToModel(eventService, SearchDTO(), data)
        data["filters"] = eventService.filters()
        return ModelAndView("index", data)
    }

    @GetMapping("/search")
    @ResponseBody
    fun search(
        searchDTO: SearchDTO
    ): ModelAndView {
        val data: MutableMap<String, Any> = HashMap()
        data["title"] = PAGE_TITLE
        SearchUtils.searchAndAddToModel(eventService, searchDTO, data)
        data["filters"] = eventService.filters()
        return ModelAndView("index", data)
    }

    @GetMapping("/generate", produces = ["text/plain"])
    @ResponseBody
    fun generateQuery(
        searchDTO: SearchDTO
    ): String {
        return eventService.generateQuery(searchDTO)
    }

    @GetMapping("/sources")
    @ResponseBody
    fun sources(
        searchDTO: SearchDTO
    ): ModelAndView {
        val data: MutableMap<String, Any> = HashMap()
        data["sources"] = eventService.getSources()
        return ModelAndView("sources", data)
    }
}





