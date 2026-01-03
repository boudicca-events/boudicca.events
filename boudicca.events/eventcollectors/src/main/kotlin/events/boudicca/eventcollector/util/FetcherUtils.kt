package events.boudicca.eventcollector.util

import base.boudicca.fetcher.Fetcher
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

fun Fetcher.fetchUrlAndParse(url: String): Document = Jsoup.parse(this.fetchUrl(url), url)
