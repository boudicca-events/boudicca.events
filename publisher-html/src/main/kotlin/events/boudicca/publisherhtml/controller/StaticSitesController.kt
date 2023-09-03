package events.boudicca.publisherhtml.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView

@Controller
@RequestMapping("/")
class StaticSitesController {

    @GetMapping("/about")
    fun getAbout(): ModelAndView {
        val data: MutableMap<String, Any> = HashMap()
        return ModelAndView("about", data)
    }

    @GetMapping("/impressum")
    fun getImpressum(): ModelAndView {
        val data: MutableMap<String, Any> = HashMap()
        return ModelAndView("impressum", data)
    }
}





