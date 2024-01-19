package base.boudicca.eventdb.controller

import base.boudicca.eventdb.publisher.api.PublisherApi
import base.boudicca.model.Entry
import base.boudicca.eventdb.service.EntryService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RestController

@RestController
class PublisherController @Autowired constructor(
    private val entryService: EntryService,
) : PublisherApi {

    override fun all(): Set<Entry> {
        return entryService.all()
    }

}