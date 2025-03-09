package base.boudicca.model

actual class URI actual constructor(path: String) {

    val uri = java.net.URI(path)

    actual override fun toString(): String {
        return uri.toASCIIString()
    }
}
