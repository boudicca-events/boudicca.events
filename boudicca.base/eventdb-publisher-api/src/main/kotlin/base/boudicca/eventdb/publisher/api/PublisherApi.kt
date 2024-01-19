package base.boudicca.eventdb.publisher.api

import base.boudicca.model.Entry
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Api("EventDB Publisher")
@RequestMapping("/entries")
interface PublisherApi {
    @ApiOperation("returns all entries from the event db")
    @GetMapping("")
    fun all(): Set<Entry>
}