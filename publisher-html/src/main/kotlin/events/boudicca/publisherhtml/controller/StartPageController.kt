package events.boudicca.publisherhtml.controller

import com.github.jknack.handlebars.Handlebars
import com.github.jknack.handlebars.Template
import com.github.jknack.handlebars.Context
import com.github.jknack.handlebars.io.ClassPathTemplateLoader
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/")
class StartPageController {

    private val handlebars = Handlebars(ClassPathTemplateLoader("/templates"))

    @GetMapping("*")
    fun getIndex(model: Model): Model {
        // Define the data to be used in the template
        val data: MutableMap<String, Any> = HashMap()
        data["title"] = "My Page Title"

        // Compile the template and apply the data
        val template: Template = handlebars.compile("index")
        val html: String = template.apply(data)

        // Add the HTML to the model
        model.addAttribute("html", html)

        // Return the name of the template to be rendered
        return model
    }
}





