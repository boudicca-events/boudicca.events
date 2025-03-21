package base.boudicca.model.structured

import base.boudicca.model.KeyUtils

/**
 * represents a parsed KeyFilter of a Key-Value pair which consists of the name and all the variants (which are sorted canonically)
 * similar to a Key, but a KeyFilter also allows "*" as a propertyName and "*", "" (the empty string) as values for variants
 */
class KeyFilter(name: String, variants: List<Variant> = emptyList()) : AbstractKey<KeyFilter>(name, variants) {

    companion object {
        fun parse(keyFilter: String): KeyFilter {
            return KeyUtils.parseKeyFilter(keyFilter)
        }

        fun builder(propertyName: String): KeyFilterBuilder {
            return KeyFilterBuilder(propertyName)
        }
    }

    override fun toBuilder() = KeyFilterBuilder(name).withVariants(variants)

}
