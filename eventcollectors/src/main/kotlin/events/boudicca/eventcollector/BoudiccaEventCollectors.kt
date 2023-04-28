package events.boudicca.eventcollector

import JkuEventFetcher
import TechnologiePlauscherlFetcher
import events.boudicca.api.eventcollector.EventCollectorScheduler


fun main() {
    EventCollectorScheduler()
        .addEventCollector(PosthofFetcher())
        .addEventCollector(JkuEventFetcher())
        .addEventCollector(TechnologiePlauscherlFetcher())
        .run()
}