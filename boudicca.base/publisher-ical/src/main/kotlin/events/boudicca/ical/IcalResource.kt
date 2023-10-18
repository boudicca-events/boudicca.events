package events.boudicca.ical

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class IcalResource @Autowired constructor(private val calendarService: CalendarService) {

    @GetMapping(
        "/calendar.ics",
        produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE],
    )
    fun getAllEvents(@RequestParam query: String): ResponseEntity<ByteArray> {
        val calendarFile = calendarService.getEvents(query)
        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment;filename=calendar.ics")
            .body(calendarFile)

    }
}
