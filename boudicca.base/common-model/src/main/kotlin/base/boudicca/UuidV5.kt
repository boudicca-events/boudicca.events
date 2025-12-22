@file:Suppress("MagicNumber")

package base.boudicca

import java.nio.ByteBuffer
import java.security.MessageDigest
import java.text.Normalizer
import java.util.*

class UuidV5(private val namespace: UUID) {
    fun from(keys: List<String>): UUID {
        val normalized = keys.map(::normalize)
        val joined = normalized.joinToString(separator = "|") { "${it.length}:$it" }

        // RFC 4122: UUIDv5 = SHA1(namespaceUUID + name)
        val nsBytes = uuidToBytes(namespace)
        val nameBytes = joined.toByteArray(Charsets.UTF_8)
        val digest =
            MessageDigest.getInstance("SHA-1")
                .digest(nsBytes + nameBytes)

        // take the first 16 bytes (128 bits)
        val uuidBytes = digest.copyOfRange(0, 16)

        // set version (5) and variant (RFC 4122)
        uuidBytes[6] = (uuidBytes[6].toInt() and 0x0F or 0x50).toByte() // version 5
        uuidBytes[8] = (uuidBytes[8].toInt() and 0x3F or 0x80).toByte() // variant RFC 4122

        val bb = ByteBuffer.wrap(uuidBytes)
        return UUID(bb.long, bb.long)
    }

    private fun normalize(input: String): String = Normalizer.normalize(input.trim(), Normalizer.Form.NFC)

    private fun uuidToBytes(uuid: UUID): ByteArray {
        val bb = ByteBuffer.allocate(16)
        bb.putLong(uuid.mostSignificantBits)
        bb.putLong(uuid.leastSignificantBits)
        return bb.array()
    }
}
