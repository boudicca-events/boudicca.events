package base.boudicca.publisher.event.html.extension

interface Extension {
    fun getHeaders(): List<HeaderExtension> {
        return emptyList()
    }
}

data class HeaderExtension(
    val text: String,
    val url: String,
    val target: String = "_self",
)