package base.boudicca.api.eventcollector.webui

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.servlet.ModelAndView
import java.util.*

@ConditionalOnProperty("boudicca.collector.webui.enabled", havingValue = "true")
@Controller
@RequestMapping("/")
class WebuiController(
    private val webuiService: WebuiService,
) {
    @GetMapping("/")
    fun getIndex(): ModelAndView = ModelAndView("index", webuiService.getIndexData())

    @GetMapping("/singleCollection")
    fun singleCollection(
        @RequestParam("id") uuid: UUID,
    ): ModelAndView = ModelAndView("singleCollection", webuiService.getSingleCollectionData(uuid))

    @GetMapping("/fullCollection")
    fun fullCollection(
        @RequestParam("id") uuid: UUID,
    ): ModelAndView = ModelAndView("fullCollection", webuiService.getFullCollectionData(uuid))

    @GetMapping("/lastCapturedCalls", produces = ["application/octet-stream"])
    @ResponseBody
    fun lastCapturedCalls(): ByteArray = webuiService.getLastCapturedCalls()
}
