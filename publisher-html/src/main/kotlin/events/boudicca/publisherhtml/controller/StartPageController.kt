package events.boudicca.publisherhtml.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView

@Controller
@RequestMapping("/")
class StartPageController {

    @GetMapping("/")
    fun getIndex(): ModelAndView {
        val data: MutableMap<String, Any> = HashMap()
        data["title"] = "Boudicca"

        return ModelAndView("index", data)
    }
}





