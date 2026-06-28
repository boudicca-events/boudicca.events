package events.boudicca.eventcollector.util

import base.boudicca.fetcher.Fetcher
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

/**
 * utility function calling Jsoup.parse(...,url) on the fetched url. the second parameter is important so jsoup can resolve absolute urls automatically,
 * which is needed for example for the DescriptionUtils
 */
fun Fetcher.fetchUrlAndParse(url: String): Document = Jsoup.parse(this.fetchUrl(url), url)

/**
 * utility function calling Jsoup.parse(...,url) on the fetched url. the second parameter is important so jsoup can resolve absolute urls automatically,
 * which is needed for example for the DescriptionUtils
 */
fun Fetcher.fetchUrlPostAndParse(
    url: String,
    contentType: String,
    content: String,
): Document = Jsoup.parse(this.fetchUrlPost(url, contentType, content), url)
