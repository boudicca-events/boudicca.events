package base.boudicca.dateparser.dateparser.impl

internal data class DebugTracing(
    private val operations: MutableList<Operation> = mutableListOf(),
) {
    fun startOperation(
        description: String,
        tokens: Tokens,
    ) = startOperation(description, listOf(tokens))

    fun startOperationWithChild(
        description: String,
        tokens: Tokens,
    ): DebugTracing = startOperationWithChild(description, listOf(tokens))

    fun startOperation(
        description: String,
        tokens: List<Tokens>,
    ) {
        operations.add(Operation(description, tokens, null, null))
    }

    fun startOperationWithChild(
        description: String,
        tokens: List<Tokens>,
    ): DebugTracing {
        val debugTracing = DebugTracing()
        operations.add(Operation(description, tokens, null, debugTracing))
        return debugTracing
    }

    fun endOperation(result: Any?) {
        operations.last().result = result
    }

    fun debugPrint(): String {
        val sb = StringBuilder()
        debugPrint(0, sb)
        return sb.toString()
    }

    private fun debugPrint(
        depth: Int,
        sb: StringBuilder,
    ) {
        for (operation in operations) {
            debugPrintIndentation(sb, depth)
            sb.append("+ ")
            sb.append(operation.description)
            sb.append(" = ")
            sb.append(operation.result)
            sb.appendLine()
            debugPrintIndentation(sb, depth)
            sb.append("| ")
            debugPrintTokens(sb, operation.newTokens)
            sb.appendLine()
            operation.childOperations?.debugPrint(depth + 1, sb)
        }
    }

    private fun debugPrintIndentation(
        sb: StringBuilder,
        depth: Int,
    ) {
        sb.append("| ".repeat(depth))
    }

    private fun debugPrintTokens(
        sb: StringBuilder,
        tokensList: List<Tokens>,
    ) {
        var firstTokens = true
        for (tokens in tokensList) {
            if (firstTokens) {
                firstTokens = false
            } else {
                sb.append(" + ")
            }
            sb.append(tokens.toString())
        }
    }
}

internal data class Operation(
    val description: String,
    val newTokens: List<Tokens>,
    var result: Any?,
    val childOperations: DebugTracing?,
)
