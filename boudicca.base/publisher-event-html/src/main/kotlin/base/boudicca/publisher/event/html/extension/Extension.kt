package base.boudicca.publisher.event.html.extension

interface Extension {
    fun getHeaders(): List<LinkExtension> {
        return emptyList()
    }

    fun getFooters(): List<LinkExtension> {
        return emptyList()
    }
}

data class LinkExtension(
    val text: String,
    val url: String,
    val target: String = "_self",
)
