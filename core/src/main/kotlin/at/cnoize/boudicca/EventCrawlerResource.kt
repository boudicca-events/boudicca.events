package at.cnoize.boudicca

import at.cnoize.boudicca.crawlerapi.CrawlerApi
import at.cnoize.boudicca.model.Event
import javax.ws.rs.POST
import javax.ws.rs.Path

class EventCrawlerResource : CrawlerApi {

    private val eventService = EventService()

    @POST
    @Path("add")
    override fun add(event: Event) {
        eventService.add(event)
    }

}