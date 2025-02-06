package base.boudicca.publisher.event.html.util

import base.boudicca.publisher.event.html.model.SearchDTO
import base.boudicca.publisher.event.html.service.EventService
import base.boudicca.publisher.event.html.service.EventServiceException
import org.slf4j.LoggerFactory

object SearchUtils {

    private val LOG = LoggerFactory.getLogger(SearchUtils::class.java)

    fun searchAndAddToModel(eventService: EventService, searchDTO: SearchDTO, data: MutableMap<String, Any>) {
        try {
            data["events"] = eventService.search(searchDTO)
        } catch (e: EventServiceException) {
            if (e.showToUser) {
                data["error"] = e.message ?: "unknown error"
            } else {
                throwException(e)
            }
        } catch (e: Exception) {
            throwException(e)
        }
    }

    private fun throwException(e: Exception) {
        val newException = RuntimeException("error calling eventservice", e)
        //spring hides all the interesting bits so log it here
        LOG.error("error calling eventservice", newException)
        throw newException
    }
}
