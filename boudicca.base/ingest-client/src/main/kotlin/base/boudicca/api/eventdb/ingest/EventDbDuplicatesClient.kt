package base.boudicca.api.eventdb.ingest

import java.util.UUID

interface EventDbDuplicatesClient {
    fun markDuplicates(duplicateIds: List<UUID>)
}
