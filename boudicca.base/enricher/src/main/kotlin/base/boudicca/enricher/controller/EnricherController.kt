package base.boudicca.enricher.controller

import base.boudicca.Event
import base.boudicca.enricher.model.EnrichRequestDTO
import base.boudicca.enricher.service.EnricherService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/")
class EnricherController @Autowired constructor(
    private val enricherService: EnricherService
) {

    @PostMapping(
        "enrich",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun enrich(@RequestBody enrichRequestDTO: EnrichRequestDTO): List<Event> {
        return enricherService.enrich(enrichRequestDTO)
    }

    @PostMapping("forceUpdate")
    fun forceUpdate() {
        enricherService.forceUpdate()
    }

}