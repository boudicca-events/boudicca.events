package base.boudicca.api.eventcollector.fetcher

interface HttpClientWrapper {
    fun doGet(url: String): Pair<Int, String>
    fun doPost(url: String, contentType: String, content: String): Pair<Int, String>
}
