package base.boudicca.format


expect class URI

expect object URIParser {
    fun parseURI(uri: String): URI
    fun uriToString(uri: URI): String
}
