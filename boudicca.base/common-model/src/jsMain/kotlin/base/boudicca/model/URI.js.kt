package base.boudicca.model

actual class URI actual constructor(private val path: String) {

    actual override fun toString(): String {
        return path
    }
}
