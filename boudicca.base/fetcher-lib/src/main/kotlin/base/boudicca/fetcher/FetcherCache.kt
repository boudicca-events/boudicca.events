package base.boudicca.fetcher

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.EOFException
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.ConcurrentHashMap

/**
 * implementations have to be thread-safe!
 */
interface FetcherCache {
    fun containsEntry(key: String): Boolean

    fun getEntry(key: String): String

    fun putEntry(
        key: String,
        entry: String,
    )
}

object NoopFetcherCache : FetcherCache {
    override fun containsEntry(key: String): Boolean = false

    override fun getEntry(key: String): String {
        error { "noop implementation does not contain $key" }
    }

    override fun putEntry(
        key: String,
        entry: String,
    ) {
        // do nothing
    }
}

class InMemoryFetcherCache : FetcherCache {
    private val cache = ConcurrentHashMap<String, String>()

    override fun containsEntry(key: String): Boolean = cache.containsKey(key)

    override fun getEntry(key: String): String = cache[key] ?: throw IllegalArgumentException("cache does not contain key $key")

    override fun putEntry(
        key: String,
        entry: String,
    ) {
        cache[key] = entry
    }
}

class FileBackedFetcherCache(
    private val file: File,
) : FetcherCache {
    private val cache = ConcurrentHashMap<String, String>()

    init {
        if (!file.exists() || file.isFile) {
            if (!file.exists()) {
                file.parentFile.mkdirs()
                file.createNewFile()
            }
            loadFile()
        } else {
            error { "invalid file $file specified" }
        }
    }

    private val outputStream = DataOutputStream(BufferedOutputStream(FileOutputStream(file, true)))

    private fun loadFile() {
        try {
            DataInputStream(BufferedInputStream(FileInputStream(file)))
                .use { inputStream ->
                    while (true) {
                        val keySize = inputStream.readInt()
                        val key = String(inputStream.readNBytes(keySize))
                        val entrySize = inputStream.readInt()
                        val entry = String(inputStream.readNBytes(entrySize))
                        cache[key] = entry
                    }
                }
        } catch (_: EOFException) {
            // just means we read all lines, the api of this stream is weird...
        }
    }

    override fun containsEntry(key: String): Boolean = cache.containsKey(key)

    override fun getEntry(key: String): String = cache[key] ?: throw IllegalArgumentException("cache does not contain key $key")

    override fun putEntry(
        key: String,
        entry: String,
    ) {
        cache[key] = entry
        synchronized(this) {
            val keyArray = key.toByteArray()
            outputStream.writeInt(keyArray.size)
            outputStream.write(keyArray)
            val entryArray = entry.toByteArray()
            outputStream.writeInt(entryArray.size)
            outputStream.write(entryArray)
            outputStream.flush()
        }
    }
}
