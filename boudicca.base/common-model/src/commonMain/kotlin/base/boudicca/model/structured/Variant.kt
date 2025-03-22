package base.boudicca.model.structured

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * a variant in the form of variantName=variantValue, but already parsed
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
data class Variant(val variantName: String, val variantValue: String) : Comparable<Variant> {

    init {
        validate()
    }

    private fun validate() {
        require(variantName != "*") { "variant name is not allowed to be '*'" }
        require(variantName != "") { "variant name is not allowed to be the empty string" }
        require(!variantName.contains("=")) { "variant name $variantName is not allowed to contain a '='" }
        require(!variantName.contains(":")) { "variant name $variantName is not allowed to contain a ':'" }
        require(!variantValue.contains("=")) { "variant value $variantValue is not allowed to contain a '='" }
        require(!variantValue.contains(":")) { "variant value $variantValue is not allowed to contain a ':'" }
    }

    fun toKeyString(): String {
        return "${variantName}=${variantValue}"
    }

    companion object {
        val COMPARATOR = compareBy<Variant> { it.variantName }.thenBy { it.variantValue }
    }

    override fun compareTo(other: Variant): Int {
        return COMPARATOR.compare(this, other)
    }
}
