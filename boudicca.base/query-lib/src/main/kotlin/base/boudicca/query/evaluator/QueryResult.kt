package base.boudicca.query.evaluator

import base.boudicca.model.Entry

data class QueryResult(val result: List<Entry>, val totalResults: Int, val error: String? = null)
