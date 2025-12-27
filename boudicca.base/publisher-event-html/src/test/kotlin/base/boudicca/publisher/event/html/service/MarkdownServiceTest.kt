package base.boudicca.publisher.event.html.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

class MarkdownServiceTest {
    @Test
    fun testEmptyString() {
        assertEquals("", render(""))
    }

    @Test
    fun testSimpleText() {
        assertEquals("<p>Test</p>", render("Test"))
    }

    @Test
    fun testSimpleBoldText() {
        assertEquals("<p>This is <em>Markdown</em></p>", render("This is *Markdown*"))
    }

    @Test
    fun testHeaders() {
        assertEquals("<h1>header</h1>", render("# header"))
        assertEquals("<h2>header</h2>", render("## header"))
    }

    @Test
    fun testSimpleHtmlEscape() {
        assertEquals("<pre><code>&lt;/a&gt;\n</code></pre>", render("    </a>"))
        assertEquals("<p>&lt;/a&gt;</p>", render("</a>"))
        assertEquals("<p>&lt;img src=&quot;/url&quot; /&gt;</p>", render("<img src=\"/url\" />"))
        assertEquals("<p>test &lt;span&gt;test&lt;/span&gt;</p>", render("test <span>test</span>"))
    }

    @Test
    fun testSimpleLinkReferenceOnlyFullUrlAllowed() {
        assertEquals(
            "<p></p>",
            render(
                """
                [foo]: /url "title"

                [foo]
                """.trimIndent(),
            ),
        )
        assertEquals(
            "<p><a rel=\"nofollow\" href=\"https://example.com/url\" title=\"title\">foo</a></p>",
            render(
                """
                [foo]: https://example.com/url "title"

                [foo]
                """.trimIndent(),
            ),
        )
    }

    @Test
    fun testSimpleLink() {
        assertEquals("<p></p>", render("[link](/uri \"title\")"))
        assertEquals("<p><a rel=\"nofollow\" href=\"http://example.com/uri\" title=\"title\">link</a></p>", render("[link](http://example.com/uri \"title\")"))
    }

    @Test
    fun testAutoLinks() {
        assertEquals("<p><a rel=\"nofollow\" href=\"http://foo.bar.baz\">http://foo.bar.baz</a></p>", render("<http://foo.bar.baz>"))
        assertEquals("<p><a rel=\"nofollow\" href=\"mailto:foo@bar.example.com\">foo@bar.example.com</a></p>", render("<foo@bar.example.com>"))
    }

    @Test
    fun testSimpleImage() {
        assertEquals("<p><img src=\"/picture?uuid=7b212721-0ecd-4a8a-af10-a29085d41976\" alt=\"foo\" title=\"title\" /></p>", render("![foo](/url \"title\")"))
        assertEquals("<p><img src=\"/picture?uuid=7b212721-0ecd-4a8a-af10-a29085d41976\" alt=\"foo\" title=\"title\" /></p>", render("![foo](https://example.com/url \"title\")"))
    }

    private fun render(markdown: String): String = MarkdownService(MockPictureProxyService()).renderMarkdown(markdown)

    class MockPictureProxyService : PictureProxyService {
        override fun submitPicture(url: String): UUID = UUID.fromString("7b212721-0ecd-4a8a-af10-a29085d41976")

        override fun getPicture(uuid: UUID): Optional<ByteArray> = Optional.empty<ByteArray>()
    }
}
