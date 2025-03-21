package base.boudicca.format

import org.w3c.dom.url.URL

actual typealias URI = URL

actual object URIParser {
    actual fun parseURI(uri: String): URI {
        return URL(uri)
    }

    actual fun uriToString(uri: URI): String {
        return uri.href
    }
}
