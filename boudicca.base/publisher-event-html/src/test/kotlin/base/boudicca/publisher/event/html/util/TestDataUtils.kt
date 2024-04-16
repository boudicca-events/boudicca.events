package base.boudicca.publisher.event.html.util

import base.boudicca.model.Event
import java.time.OffsetDateTime

fun buildEventList(numEvents: Int): List<Event> {
  val eventList = mutableListOf<Event>()
  val currentTime = OffsetDateTime.now()

  for (i in 1..numEvents) {
    val eventName = "event$i"
    val event = Event(eventName, currentTime, mapOf())
    eventList.add(event)
  }

  return eventList
}
