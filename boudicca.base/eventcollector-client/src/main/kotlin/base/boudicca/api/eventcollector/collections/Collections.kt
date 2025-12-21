package base.boudicca.api.eventcollector.collections

import base.boudicca.api.eventcollector.EventCollector
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.Collections
import java.util.concurrent.atomic.AtomicReference

object Collections {
    private val currentFullCollection = AtomicReference<FullCollection>()
    private val currentSingleCollections = ThreadLocal<SingleCollection>()
    private val currentHttpCalls = ThreadLocal<HttpCall>()
    private val pastFullCollections = Collections.synchronizedList(mutableListOf<FullCollection>())
    private val logger = KotlinLogging.logger {}

    fun startFullCollection() {
        if (currentFullCollection.get() != null) {
            logger.error { "a current full collection is already set, this seems like a bug" }
        }

        val fullCollection = FullCollection()
        fullCollection.startTime = System.currentTimeMillis()
        currentFullCollection.set(fullCollection)
    }

    fun endFullCollection() {
        val fullCollection = currentFullCollection.get()
        if (fullCollection == null) {
            logger.error { "no full single collection available, cannot end it" }
            return
        }
        fullCollection.endTime = System.currentTimeMillis()
        currentFullCollection.set(null)
        pastFullCollections.add(fullCollection)
    }

    fun startSingleCollection(collector: EventCollector) {
        if (currentSingleCollections.get() != null) {
            logger.error { "a current single collection is already set, this seems like a bug" }
        }
        val singleCollection = SingleCollection(collector.getName())
        singleCollection.startTime = System.currentTimeMillis()
        currentFullCollection.get()?.singleCollections?.add(singleCollection)
        currentSingleCollections.set(singleCollection)
    }

    fun endSingleCollection() {
        val singleCollection = currentSingleCollections.get()
        if (singleCollection == null) {
            logger.error { "no current single collection available, cannot end it" }
            return
        }
        singleCollection.endTime = System.currentTimeMillis()
        currentSingleCollections.set(null)
    }

    fun startHttpCall(url: String, postData: String? = null) {
        if (currentHttpCalls.get() != null) {
            logger.error { "a current http call is already set, this seems like a bug" }
        }
        val httpCall = HttpCall()
        httpCall.startTime = System.currentTimeMillis()
        httpCall.url = url
        httpCall.postData = postData
        currentSingleCollections.get()?.httpCalls?.add(httpCall)
        currentHttpCalls.set(httpCall)
    }

    fun endHttpCall(responseCode: Int) {
        val httpCall = currentHttpCalls.get()
        if (httpCall == null) {
            logger.error { "no current http call available, cannot end it" }
            return
        }
        httpCall.endTime = System.currentTimeMillis()
        httpCall.responseCode = responseCode
        currentHttpCalls.set(null)
    }

    fun getAllPastCollections(): List<FullCollection> {
        return pastFullCollections.toList()
    }

    fun getLastFullCollection(): FullCollection {
        return pastFullCollections.last()
    }

    fun getCurrentFullCollection(): FullCollection? {
        return currentFullCollection.get()
    }

    fun getCurrentSingleCollection(): SingleCollection? {
        return currentSingleCollections.get()
    }
}
