package base.boudicca.search.service.query

data class Page(
    val offset: Int,
    val size: Int,
)

val PAGE_ALL = Page(0, Int.MAX_VALUE)