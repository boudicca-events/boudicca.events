package base.boudicca.fetcher

interface HttpClientWrapper {
    fun doGet(url: String): Pair<Int, String>

    fun doPost(url: String, contentType: String, content: String): Pair<Int, String>
}
