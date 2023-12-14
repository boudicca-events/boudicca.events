package boudicca.base.api.enricher

import base.boudicca.model.Event
import boudicca.base.model.enricher.EnrichRequestDTO
import io.swagger.annotations.Api
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@Api("Enricher")
interface EnricherApi {
    @PostMapping(
        "enrich",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun enrich(@RequestBody enrichRequestDTO: EnrichRequestDTO): List<Event>

    @PostMapping("forceUpdate")
    fun forceUpdate()
}