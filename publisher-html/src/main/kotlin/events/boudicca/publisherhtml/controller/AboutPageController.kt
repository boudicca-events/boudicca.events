package events.boudicca.publisherhtml.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView

@Controller
@RequestMapping("/")
class AboutPageController @Autowired constructor() {

    private val PAGE_TITLE = "About Boudicca";

    @GetMapping("/about")
    fun getAbout(): ModelAndView {
        val data: MutableMap<String, Any> = HashMap()
        data["title"] = PAGE_TITLE
        return ModelAndView("about", data)
    }
}





