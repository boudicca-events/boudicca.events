package base.boudicca.publisher.event.html.controller

import base.boudicca.publisher.event.html.model.SearchDTO
import base.boudicca.publisher.event.html.service.EventService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.servlet.ModelAndView

@Controller
@RequestMapping("/")
class StartPageController @Autowired constructor(private val eventService: EventService) {

    private val PAGE_TITLE = "Boudicca"

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
        searchDTO: SearchDTO
    ): ModelAndView {
        val data: MutableMap<String, Any> = HashMap()
        data["title"] = PAGE_TITLE
        data["events"] = eventService.search(searchDTO)
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
}





