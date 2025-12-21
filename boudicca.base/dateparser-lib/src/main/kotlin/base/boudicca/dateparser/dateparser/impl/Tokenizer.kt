package base.boudicca.dateparser.dateparser.impl

internal object Tokenizer {
    fun tokenize(text: String): List<Pair<TokenizerType, String>> {
        val charArray = text.toCharArray() // TODO test for unicode?
        val result = mutableListOf<Pair<TokenizerType, String>>()

        var curChar: Char
        var curCharType: TokenizerType? = null
        var curString = StringBuilder()

        for (element in charArray) {
            curChar = element
            val newCharType =
                if (curChar.isDigit()) {
                    TokenizerType.INT
                } else if (curChar.isLetter()) {
                    TokenizerType.STRING
                } else {
                    TokenizerType.SEPARATOR
                }
            if (newCharType != curCharType) {
                if (curCharType != null && curString.isNotEmpty()) {
                    result.add(Pair(curCharType, curString.toString()))
                    curString = StringBuilder()
                }
                curCharType = newCharType
            }
            curString.append(curChar)
        }

        if (curCharType != null && curString.isNotEmpty()) {
            result.add(Pair(curCharType, curString.toString()))
        }

        return result
    }
}
