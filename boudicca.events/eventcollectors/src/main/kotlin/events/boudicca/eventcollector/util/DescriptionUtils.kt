package events.boudicca.eventcollector.util

import base.boudicca.SemanticKeys
import base.boudicca.model.structured.dsl.StructuredEventBuilder
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.jsoup.safety.Cleaner
import org.jsoup.safety.Safelist
import org.jsoup.select.Elements
import org.jsoup.select.Nodes

private val breakElements = setOf("br", "p", "div", "li", "tr", "h1", "h2", "h3", "h4", "h5", "h6")

fun <T : Node> StructuredEventBuilder.withDescription(
    vararg nodes: Node,
    includeImage: Boolean = true,
) {
    withDescription(Nodes(*nodes), includeImage)
}

fun <T : Node> StructuredEventBuilder.withDescription(
    nodes: List<T>,
    includeImage: Boolean = true,
) {
    withProperty(SemanticKeys.DESCRIPTION_TEXT_PROPERTY, htmlToDescriptionText(nodes))
    withProperty(SemanticKeys.DESCRIPTION_MARKDOWN_PROPERTY, htmlToDescriptionMarkdown(nodes, includeImage))
}

fun htmlToDescriptionText(vararg nodes: Node): String = htmlToDescriptionText(Nodes(*nodes))

fun <T : Node> htmlToDescriptionText(nodes: List<T>): String {
    val sb = StringBuilder()
    htmlToDescriptionTextInternal(nodes, sb)
    return sb.trim().toString()
}

private fun htmlToDescriptionTextInternal(
    nodes: List<Node>,
    sb: StringBuilder,
) {
    nodes.forEach { htmlToDescriptionTextInternal(it, sb) }
}

private fun htmlToDescriptionTextInternal(
    node: Node,
    sb: StringBuilder,
) {
    if (node is TextNode) {
        sb.append(node.text().trim())
    } else if (node is Element) {
        for (child in node.childNodes()) {
            htmlToDescriptionTextInternal(child, sb)
        }
        if (breakElements.contains(node.tagName())) {
            sb.append('\n')
        } else {
            if (sb.isNotEmpty() && sb.last() != ' ') {
                sb.append(' ')
            }
        }
    }
}

fun htmlToDescriptionMarkdown(
    vararg elements: Element,
    includeImage: Boolean = true,
): String = htmlToDescriptionMarkdown(Elements(*elements), includeImage)

fun <T : Node> htmlToDescriptionMarkdown(
    nodes: List<T>,
    includeImage: Boolean = true,
): String {
    // remove unsafe tags like script, iframe, embed, etc.
    val safelist = Safelist.relaxed()
    if (!includeImage) {
        safelist.removeTags("img")
    }
    val cleaner = Cleaner(safelist)
    val shell = Document.createShell(nodes.firstOrNull()?.ownerDocument()?.baseUri() ?: "")
    shell.body().appendChildren(nodes.map { it.clone() })
    val cleaned = cleaner.clean(shell)

    val builder = FlexmarkHtmlConverter.builder()
    builder
        .set(FlexmarkHtmlConverter.BR_AS_EXTRA_BLANK_LINES, false)
        .set(FlexmarkHtmlConverter.SETEXT_HEADINGS, false)
    return builder.build().convert(cleaned).trim()
}
