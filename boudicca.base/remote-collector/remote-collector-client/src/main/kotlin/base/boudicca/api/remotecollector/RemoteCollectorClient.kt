package base.boudicca.api.remotecollector

import base.boudicca.api.remotecollector.model.EventCollection

interface RemoteCollectorClient {
    fun collectEvents(): EventCollection
}
