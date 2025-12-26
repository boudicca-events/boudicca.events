package base.boudicca.publisher.event.html.service

import java.util.Optional
import java.util.UUID

interface PictureProxyService {
    fun submitPicture(url: String): UUID

    fun getPicture(uuid: UUID): Optional<ByteArray>
}
