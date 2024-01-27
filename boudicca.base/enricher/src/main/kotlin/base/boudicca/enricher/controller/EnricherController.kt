package base.boudicca.enricher.controller

import base.boudicca.api.enricher.EnricherApi
import base.boudicca.api.enricher.model.EnrichRequestDTO
import base.boudicca.enricher.service.EnricherService
import base.boudicca.model.Event
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/")
class EnricherController(
    private val enricherService: EnricherService,
) : EnricherApi {

    @PostMapping(
        "enrich",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    @ResponseBody
    override fun enrich(@RequestBody enrichRequestDTO: EnrichRequestDTO): List<Event> {
        return enricherService.enrich(enrichRequestDTO)
    }

    @PostMapping("forceUpdate")
    override fun forceUpdate() {
        enricherService.forceUpdate()
    }

}