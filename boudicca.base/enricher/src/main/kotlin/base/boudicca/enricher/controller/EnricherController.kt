package base.boudicca.enricher.controller

import boudicca.base.model.enricher.EnrichRequestDTO
import base.boudicca.enricher.service.EnricherService
import base.boudicca.model.Event
import boudicca.base.api.enricher.EnricherApi
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

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
    override fun enrich(@RequestBody enrichRequestDTO: EnrichRequestDTO): List<Event> {
        return enricherService.enrich(enrichRequestDTO)
    }

    @PostMapping("forceUpdate")
    override fun forceUpdate() {
        enricherService.forceUpdate()
    }

}