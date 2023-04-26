package events.boudicca.publisherhtml.controller

import com.github.jknack.handlebars.Handlebars
import com.github.jknack.handlebars.Template
import com.github.jknack.handlebars.io.ClassPathTemplateLoader
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView

@Controller
@RequestMapping("/")
class StartPageController {

    private val handlebars = Handlebars(ClassPathTemplateLoader("/templates/"))

    @GetMapping("*")
    fun getIndex(): ModelAndView {
        // Define the data to be used in the template
        val data: MutableMap<String, Any> = HashMap()
        data["title"] = "My Page Title"

        // Compile the template and apply the data
        val template: Template = handlebars.compile("index")
        val html: String = template.apply(data)

        // Create a ModelAndView object with the view name and the data to be displayed
        val mav = ModelAndView("index")
        mav.addObject("html", html)

        return mav
    }
}





