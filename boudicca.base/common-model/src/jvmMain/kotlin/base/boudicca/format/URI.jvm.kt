package base.boudicca.format

actual typealias URI = java.net.URI

actual object URIParser {
    actual fun parseURI(uri: String): URI {
        return URI.create(uri)
    }

    actual fun uriToString(uri: URI): String {
        return uri.toASCIIString()
    }
}
