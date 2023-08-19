package events.boudicca.api.eventcollector.collections

import events.boudicca.api.eventcollector.EventCollector
import java.util.Collections
import java.util.concurrent.atomic.AtomicReference

object Collections {

    private val currentFullCollection = AtomicReference<FullCollection>()
    private val currentSingleCollections = ThreadLocal<SingleCollection>()
    private val pastFullCollections = Collections.synchronizedList(mutableListOf<FullCollection>())

    fun startFullCollection() {
        if (currentFullCollection.get() != null) {
            println("a current full collection is already set, this seems like a bug")
        }

        val fullCollection = FullCollection()
        fullCollection.startTime = System.currentTimeMillis()
        currentFullCollection.set(fullCollection)
    }

    fun endFullCollection() {
        val fullCollection = currentFullCollection.get()
        fullCollection.endTime = System.currentTimeMillis()
        currentFullCollection.set(null)
        pastFullCollections.add(fullCollection)
    }

    fun startSingleCollection(collector: EventCollector) {
        if (currentSingleCollections.get() != null) {
            println("a current single collection is already set, this seems like a bug")
        }
        val singleCollection = SingleCollection()
        singleCollection.startTime = System.currentTimeMillis()
        singleCollection.collector = collector
        currentFullCollection.get().singleCollections.add(singleCollection)
        currentSingleCollections.set(singleCollection)
    }

    fun endSingleCollection() {
        val singleCollection = currentSingleCollections.get()
        singleCollection.endTime = System.currentTimeMillis()
        currentSingleCollections.set(null)
    }

    fun getAllPastCollections(): List<FullCollection> {
        return pastFullCollections
    }

    fun getCurrentFullCollection(): FullCollection? {
        return currentFullCollection.get()
    }
}