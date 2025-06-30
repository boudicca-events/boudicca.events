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
import org.springframework.web.servlet.view.RedirectView

@Controller
@RequestMapping("/")
class StartPageController @Autowired constructor(private val eventService: EventService) {

    @GetMapping("/")
    fun getIndex(): RedirectView {
        return RedirectView("/search")
    }

    @GetMapping("/search")
    @ResponseBody
    fun search(
        searchDTO: SearchDTO
    ): ModelAndView {
        val data: MutableMap<String, Any> = HashMap()
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
    fun sources(): ModelAndView {
        val data: MutableMap<String, Any> = HashMap()
        data["sources"] = eventService.getSources()
        return ModelAndView("sources", data)
    }

    @GetMapping("/map")
    @ResponseBody
    fun map(
        searchDTO: SearchDTO
    ): ModelAndView {
        val data: MutableMap<String, Any> = HashMap()
        SearchUtils.searchAndAddToModel(eventService, searchDTO, data)
        data["filters"] = eventService.filters()
        return ModelAndView("map", data)
    }
}





