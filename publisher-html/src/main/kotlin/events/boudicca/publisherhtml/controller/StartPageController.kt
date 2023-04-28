package events.boudicca.publisherhtml.controller

import events.boudicca.publisherhtml.service.EventService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView

@Controller
@RequestMapping("/")
class StartPageController @Autowired constructor(private val eventService: EventService) {

    @GetMapping("/")
    fun getIndex(): ModelAndView {
        val data: MutableMap<String, Any> = HashMap()
        data["title"] = "Boudicca"
        data["events"] = eventService.getAllEvents()
        println(eventService.getAllEvents()) //TODO do something with me
        return ModelAndView("index", data)
    }
}





