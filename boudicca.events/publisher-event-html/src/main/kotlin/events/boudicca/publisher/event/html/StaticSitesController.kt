package events.boudicca.publisher.event.html

import base.boudicca.publisher.event.html.extension.Extension
import base.boudicca.publisher.event.html.extension.LinkExtension
import org.springframework.stereotype.Component
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

    @GetMapping("/erklarung-zur-barrierefreiheit")
    fun getErklarungZurBarriereFreiheit(): ModelAndView {
        val data: MutableMap<String, Any> = HashMap()
        return ModelAndView("erklarung-zur-barrierefreiheit", data)
    }

    @GetMapping("/data-privacy")
    fun getDataPrivacy(): ModelAndView {
        val data: MutableMap<String, Any> = HashMap()
        return ModelAndView("data_privacy", data)
    }
}

@Component
class BoudiccaEventsExtension : Extension {
    val urlGithub = "https://github.com/boudicca-events/boudicca.events"
    val urlDiscord = "https://discord.gg/dwtsPZWCVA"

    override fun getHeaders(): List<LinkExtension> {
        return listOf(
            LinkExtension("Über uns", "/about"),
            LinkExtension("Github", urlGithub, "_blank", "github"),
            LinkExtension("Discord", urlDiscord, "_blank", "discord"),
        )
    }

    override fun getFooters(): List<LinkExtension> {
        return listOf(
            LinkExtension("Über uns", "/about"),
            LinkExtension("Impressum", "/impressum"),
            LinkExtension("Data Privacy", "/data-privacy"),
            LinkExtension("GitHub", urlGithub, "_blank", "github"),
            LinkExtension("Discord", urlDiscord, "_blank", "discord"),
            LinkExtension("Erklärung zur Barrierefreiheit", "/erklarung-zur-barrierefreiheit"),
        )
    }
}
