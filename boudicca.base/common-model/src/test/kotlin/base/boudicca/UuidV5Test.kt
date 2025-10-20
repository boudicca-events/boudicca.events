@file:Suppress("SpellCheckingInspection", "VariableNaming")
package base.boudicca

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import java.util.*

class UuidV5Test {

    // RFC 4122 predefined namespaces
    private val DNS_NS  = UUID.fromString("6ba7b810-9dad-11d1-80b4-00c04fd430c8")
    private val URL_NS  = UUID.fromString("6ba7b811-9dad-11d1-80b4-00c04fd430c8")

    // System under test
    private val v5 = UuidV5(DNS_NS)

    // -- Core behavior --

    @Test
    fun `same inputs yield identical UUIDs`() {
        val keys = listOf("Alice", "ProjectX", "Admin")
        val a = v5.from(keys)
        val b = v5.from(keys)
        assertEquals(a, b)
    }

    @Test
    fun `different inputs yield different UUIDs`() {
        val a = v5.from(listOf("Alice", "ProjectX", "Admin"))
        val b = v5.from(listOf("Bob",   "ProjectX", "Admin"))
        assertNotEquals(a, b)
    }

    @Test
    fun `order matters`() {
        val a = v5.from(listOf("A", "B", "C"))
        val b = v5.from(listOf("B", "A", "C"))
        assertNotEquals(a, b)
    }

    @Test
    fun `different namespace changes result`() {
        val keys = listOf("Alice", "ProjectX", "Admin")
        val withDns = UuidV5(DNS_NS).from(keys)
        val withUrl = UuidV5(URL_NS).from(keys)
        assertNotEquals(withDns, withUrl)
    }

    // -- Normalization & joining rules (trim + NFC, case-sensitive, length-prefix) --

    @Test
    fun `trimming whitespace yields same UUID`() {
        val x = v5.from(listOf("  Alice ", " ProjectX", "Admin  "))
        val y = v5.from(listOf("Alice", "ProjectX", "Admin"))
        assertEquals(x, y)
    }

    @Test
    fun `NFC normalization makes precomposed and combining accents equal`() {
        // "Café" vs "Cafe\u0301" (e + combining acute)
        val x = v5.from(listOf("Café", "Foo", "Bar"))
        val y = v5.from(listOf("Cafe\u0301", "Foo", "Bar"))
        assertEquals(x, y)
    }

    @Test
    fun `case differences DO change output (case-sensitive by design)`() {
        val upper = v5.from(listOf("BAR"))
        val lower = v5.from(listOf("bar"))
        assertNotEquals(upper, lower)
    }

    @Test
    fun `length-prefix avoids delimiter ambiguity`() {
        val x = v5.from(listOf("a", "bc", ""))
        val y = v5.from(listOf("ab", "c", ""))
        assertNotEquals(x, y)
    }

    // -- Edge cases --

    @Test
    fun `empty strings handled consistently`() {
        val a = v5.from(listOf("", "", ""))
        val b = v5.from(listOf("", "", ""))
        assertEquals(a, b)
    }

    @Test
    fun `empty list still produces a stable UUID (golden)`() {
        val u = v5.from(emptyList())
        // Golden for DNS namespace with empty list
        assertEquals(UUID.fromString("4ebd0208-8328-5d69-8c44-ec50939c0967"), u)
    }

    @Test
    fun `unicode inputs are stable`() {
        val a = v5.from(listOf("東京", "straße", "Ångström"))
        val b = v5.from(listOf("東京", "straße", "Ångström"))
        assertEquals(a, b)
    }

    @Test
    fun `large input list is supported and deterministic`() {
        val parts = (1..2000).map { "part-$it" }
        val a = v5.from(parts)
        val b = v5.from(parts)
        assertEquals(a, b)
    }

    // -- RFC correctness bits --

    @Test
    fun `version and variant are correct`() {
        val u = v5.from(listOf("Alice", "ProjectX", "Admin"))
        assertEquals(5, u.version())  // UUIDv5
        assertEquals(2, u.variant())  // IETF RFC 4122
    }

    // -- Golden for quick regression detection (algorithm + joining scheme) --

    @Test
    fun `golden vector for DNS namespace and sample keys`() {
        val u = v5.from(listOf("Alice", "ProjectX", "Admin"))
        // Precomputed with this implementation: SHA1(ns + "5:Alice|8:ProjectX|5:Admin")
        assertEquals(UUID.fromString("0ef6bc3f-48ce-52fe-95ac-c1b99841c4fe"), u)
    }
}
