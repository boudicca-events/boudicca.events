package base.boudicca.publisher.event.html.service

import org.commonmark.node.AbstractVisitor
import org.commonmark.node.Image
import org.commonmark.node.Link
import org.commonmark.node.Node
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import org.springframework.stereotype.Service
import java.net.URI

@Service
class MarkdownService(
    private val pictureProxyService: PictureProxyService,
) {
    val parser: Parser = Parser.builder().build()

    val renderer: HtmlRenderer =
        HtmlRenderer
            .builder()
            .escapeHtml(true)
            .sanitizeUrls(true)
            .build()

    fun renderMarkdown(markdown: String): String {
        val document = parser.parse(markdown)

        process(document)

        return renderer.render(document).trim()
    }

    private fun process(document: Node) {
        document.accept(
            object : AbstractVisitor() {
                override fun visit(image: Image?) {
                    if (image != null) {
                        val proxyImageUUID = pictureProxyService.submitPicture(image.destination)
                        image.destination = "/picture?uuid=$proxyImageUUID"
                    }
                }

                override fun visit(link: Link?) {
                    if (link != null && !isLinkOk(link)) {
                        link.unlink()
                    }
                }

                private fun isLinkOk(link: Link): Boolean {
                    try {
                        val destination = link.destination
                        val uri = URI.create(destination)
                        return uri.isAbsolute
                    } catch (_: Exception) {
                        return false
                    }
                }
            },
        )
    }
}
