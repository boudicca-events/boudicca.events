package base.boudicca.publisher.event.html.util

import base.boudicca.publisher.event.html.model.SearchDTO
import base.boudicca.publisher.event.html.service.EventService
import base.boudicca.publisher.event.html.service.EventServiceException

object SearchUtils {
    fun searchAndAddToModel(eventService: EventService, searchDTO: SearchDTO, data: MutableMap<String, Any>) {
        try {
            data["events"] = eventService.search(searchDTO)
        } catch (e: EventServiceException) {
            if (e.showToUser) {
                data["error"] = e.message ?: "unknown error"
            } else {
                throw RuntimeException("error calling eventservice", e)
            }
        }
    }
}