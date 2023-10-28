package base.boudicca.eventdb.controller

import base.boudicca.model.Entry
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@RequestMapping("/entries")
interface PublisherApi {
    @GetMapping("")
    fun all(): Set<Entry>
}