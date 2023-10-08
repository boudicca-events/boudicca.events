package events.boudicca.publisherhtml.extension

interface Extension {
    fun getHeaders(): List<HeaderExtension> {
        return emptyList()
    }
}

data class HeaderExtension(
    val text: String,
    val url: String,
)