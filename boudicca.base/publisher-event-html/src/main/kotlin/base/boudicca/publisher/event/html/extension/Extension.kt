package base.boudicca.publisher.event.html.extension

interface Extension {
    fun getHeaders(): List<LinkExtension> = emptyList()

    fun getFooters(): List<LinkExtension> = emptyList()
}

data class LinkExtension(val text: String, val url: String, val target: String = "_self", val svgName: String = "")
