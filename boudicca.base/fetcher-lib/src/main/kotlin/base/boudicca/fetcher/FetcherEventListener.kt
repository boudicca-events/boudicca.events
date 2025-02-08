package base.boudicca.fetcher

interface FetcherEventListener {
    fun callStarted(url: String, content: String? = null)
    fun callEnded(responseCode: Int)
}
