package events.boudicca.eventcollector.util

import org.jsoup.Jsoup
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DescriptionUtilsTest {
    @Test
    fun testHtmlToText() {
        htmlToTextAssert("Hello", "<span>Hello</span>")
        htmlToTextAssert("Hello\nWorld", "<span>Hello<br />World</span>")
        htmlToTextAssert("Hello\nWorld", "<div>Hello</div><div>World</div>")
        htmlToTextAssert("Hello\nWorld", "<p>Hello</p><p>World</p>")
        htmlToTextAssert("Hello World", "<span>Hello</span><span>World</span>")
    }

    @Test
    fun testHtmlToMarkdown() {
        htmlToMarkdownAssert("Hello", "<span>Hello</span>")
        htmlToMarkdownAssert("Hello  \nWorld", "<span>Hello<br />World</span>")
        htmlToMarkdownAssert("Hello  \nWorld", "<div>Hello</div><div>World</div>")
        htmlToMarkdownAssert("Hello\n\nWorld", "<p>Hello</p><p>World</p>")
        htmlToMarkdownAssert("**Hello**", "<strong>Hello</strong>")
        htmlToMarkdownAssert("# Hello", "<h1>Hello</h1>")
        htmlToMarkdownAssert("* Hello\n* World", "<ul><li>Hello</li><li>World</li></ul>")
        htmlToMarkdownAssert("![](https://example.com \"Image\")", "<img src=\"https://example.com\" title=\"Image\" />")
        htmlToMarkdownAssert("[Link](https://example.com)", "<a href=\"https://example.com\">Link</a>")
    }

    private fun htmlToTextAssert(text: String, html: String) {
        assertEquals(text, htmlToDescriptionText(Jsoup.parse(html)))
    }

    private fun htmlToMarkdownAssert(text: String, html: String) {
        assertEquals(text, htmlToDescriptionMarkdown(Jsoup.parse(html)))
    }
}
