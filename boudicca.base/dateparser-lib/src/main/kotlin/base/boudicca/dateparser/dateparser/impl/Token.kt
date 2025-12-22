package base.boudicca.dateparser.dateparser.impl

@ConsistentCopyVisibility
internal data class Token private constructor(val value: String, val needSolving: Boolean, val possibleTypes: Set<ResultTypes>) {
    fun isSolved(): Boolean = !needSolving || possibleTypes.size == 1

    fun withTypes(newTypes: Set<ResultTypes>): Token = Token(value, needSolving, newTypes)

    fun minusType(type: ResultTypes): Token = Token(value, needSolving, possibleTypes.minus(type))

    companion object {
        fun create(value: String, possibleTypes: Set<ResultTypes>): Token = Token(value, possibleTypes.isNotEmpty(), possibleTypes)
    }

    override fun toString(): String {
        val sb = StringBuilder()
        if (needSolving) {
            sb.append("[")
        }
        sb.append(value.replace("\n", "\\n"))
        if (needSolving) {
            sb.append("|")
            for (resultType in ResultTypes.entries) {
                if (possibleTypes.contains(resultType)) {
                    sb.append(resultType.name.subSequence(0, 1))
                } else {
                    sb.append(".")
                }
            }
            sb.append("]")
        }
        return sb.toString()
    }
}
