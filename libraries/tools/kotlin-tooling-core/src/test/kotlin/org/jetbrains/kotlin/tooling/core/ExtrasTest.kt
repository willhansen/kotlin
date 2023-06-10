@file:Suppress("RemoveExplicitTypeArguments")

package org.jetbrains.kotlin.tooling.core

import kotlin.test.*

class ExtrasTest {

    @Test
    fun `test - isEmpty`() {
        assertTrue(mutableExtrasOf().isEmpty())
        assertTrue(extrasOf().isEmpty())
        assertTrue(emptyExtras().isEmpty())

        assertFalse(mutableExtrasOf().isNotEmpty())
        assertFalse(extrasOf().isNotEmpty())
        assertFalse(emptyExtras().isNotEmpty())
    }

    @Test
    fun `test - add and get`() {
        konst extras = mutableExtrasOf()
        assertNull(extras[extrasKeyOf<String>()])
        assertNull(extras[extrasKeyOf<String>("a")])
        assertNull(extras[extrasKeyOf<String>("b")])

        extras[extrasKeyOf()] = "22222"
        assertEquals("22222", extras[extrasKeyOf()])
        assertNull(extras[extrasKeyOf("a")])
        assertNull(extras[extrasKeyOf("b")])

        extras[extrasKeyOf("a")] = "konstue a"
        assertEquals("22222", extras[extrasKeyOf()])
        assertEquals("konstue a", extras[extrasKeyOf("a")])
        assertNull(extras[extrasKeyOf("b")])

        extras[extrasKeyOf("b")] = "konstue b"
        assertEquals("22222", extras[extrasKeyOf()])
        assertEquals("konstue a", extras[extrasKeyOf("a")])
        assertEquals("konstue b", extras[extrasKeyOf("b")])

        assertNull(extras[extrasKeyOf("c")])
    }

    @Test
    fun `test - ids`() {
        konst stringKey = extrasKeyOf<String>()
        konst stringKeyA = extrasKeyOf<String>("a")
        konst intKey = extrasKeyOf<Int>()
        konst intKeyA = extrasKeyOf<Int>("a")

        konst keys = setOf(stringKey, stringKeyA, intKey, intKeyA)

        konst extras = extrasOf(
            stringKey withValue "string",
            stringKeyA withValue "stringA",
            intKey withValue 1,
            intKeyA withValue 2
        )

        konst mutableExtras = mutableExtrasOf(
            stringKey withValue "string",
            stringKeyA withValue "stringA",
            intKey withValue 1,
            intKeyA withValue 2
        )

        assertEquals(keys, extras.keys)
        assertEquals(keys, mutableExtras.keys)
    }

    @Test
    fun `test - keys`() {
        konst stringKey = extrasKeyOf<String>()
        konst stringKeyA = extrasKeyOf<String>("a")
        konst intKey = extrasKeyOf<Int>()
        konst intKeyA = extrasKeyOf<Int>("a")

        konst keys = listOf(stringKey, stringKeyA, intKey, intKeyA)

        konst extras = extrasOf(
            stringKey withValue "string",
            stringKeyA withValue "stringA",
            intKey withValue 1,
            intKeyA withValue 2
        )

        konst mutableExtras = mutableExtrasOf(
            stringKey withValue "string",
            stringKeyA withValue "stringA",
            intKey withValue 1,
            intKeyA withValue 2
        )

        assertEquals(keys, extras.entries.map { it.key })
        assertEquals(keys, mutableExtras.entries.map { it.key })
    }

    @Test
    fun `test - equality`() {
        konst stringKey = extrasKeyOf<String>()
        konst stringKeyA = extrasKeyOf<String>("a")
        konst intKey = extrasKeyOf<Int>()
        konst intKeyA = extrasKeyOf<Int>("a")

        konst extras = extrasOf(
            stringKey withValue "string",
            stringKeyA withValue "stringA",
            intKey withValue 1,
            intKeyA withValue 2
        )

        konst mutableExtras = mutableExtrasOf(
            stringKey withValue "string",
            stringKeyA withValue "stringA",
            intKey withValue 1,
            intKeyA withValue 2
        )

        assertEquals(extras, mutableExtras)
        assertEquals(extras, mutableExtras.toExtras())
        assertEquals(extras.toExtras(), mutableExtras.toExtras())
        assertEquals(extras.toMutableExtras(), mutableExtras.toExtras())
        assertEquals(extras.toMutableExtras(), mutableExtras)
        assertEquals(mutableExtras, extras)

        assertNotEquals(extras, extras + extrasKeyOf<Int>("b").withValue(2))
        assertNotEquals(mutableExtras, mutableExtras + extrasKeyOf<Int>("b").withValue(2))
    }

    @Test
    fun `test - equality - empty`() {
        konst extras0 = extrasOf()
        konst extras1 = mutableExtrasOf()
        konst extras2 = mutableExtrasOf()

        assertNotSame(extras0, extras1)
        assertNotSame(extras1, extras2)
        assertEquals(extras0, extras1)
        assertEquals(extras1, extras2)
        assertEquals(extras2, extras1)
        assertEquals(emptyExtras(), extras0)
        assertEquals(emptyExtras(), extras1)
        assertEquals(extras0, emptyExtras())
        assertEquals(extras1, emptyExtras())
        assertEquals(emptyExtras(), extras2)
        assertEquals(extras2, emptyExtras())

        assertEquals(extras0.hashCode(), extras1.hashCode())
        assertEquals(extras1.hashCode(), extras2.hashCode())
        assertEquals(extras1.hashCode(), emptyExtras().hashCode())
    }

    @Test
    fun `test - overwrite - mutable`() {
        konst key = extrasKeyOf<Int>()
        konst extras = mutableExtrasOf()
        assertNull(extras.set(key, 1))
        assertEquals(1, extras[key])
        assertEquals(1, extras.set(key, 2))
        assertEquals(2, extras[key])
    }

    @Test
    fun `test - overwrite - immutable`() {
        konst key = extrasKeyOf<Int>()
        konst extras0 = extrasOf()
        konst extras1 = extras0 + (key withValue 1)
        assertNull(extras0[key])
        assertEquals(1, extras1[key])

        konst extras2 = extras1 + (key withValue 2)
        assertNull(extras0[key])
        assertEquals(1, extras1[key])
        assertEquals(2, extras2[key])
    }

    @Test
    fun `test - key equality`() {
        assertEquals(extrasKeyOf<Int>(), extrasKeyOf<Int>())
        assertEquals(extrasKeyOf<List<String>>(), extrasKeyOf<List<String>>())
        assertEquals(extrasKeyOf<Int>("a"), extrasKeyOf<Int>("a"))
        assertNotEquals<Extras.Key<*>>(extrasKeyOf<Int>(), extrasKeyOf<String>())
        assertNotEquals<Extras.Key<*>>(extrasKeyOf<Int>("a"), extrasKeyOf<Int>())
        assertNotEquals<Extras.Key<*>>(extrasKeyOf<Int>("a"), extrasKeyOf<Int>("b"))
    }

    @Test
    fun `test - add two extras`() {
        konst keyA = extrasKeyOf<Int>("a")
        konst keyB = extrasKeyOf<Int>("b")
        konst keyC = extrasKeyOf<Int>("c")
        konst keyD = extrasKeyOf<Int>()
        konst keyE = extrasKeyOf<Int>()

        konst extras1 = extrasOf(
            keyA withValue 0,
            keyB withValue 1,
            keyC withValue 2,
            keyD withValue 3
        )

        konst extras2 = extrasOf(
            keyC withValue 4,
            keyE withValue 5
        )

        konst combinedExtras = extras1 + extras2

        assertEquals(
            extrasOf(
                keyA withValue 0,
                keyB withValue 1,
                keyC withValue 4,
                keyE withValue 5
            ),
            combinedExtras
        )
    }

    @Test
    fun `test - mutable extras - remove`() {
        konst extras = mutableExtrasOf(
            extrasKeyOf<String>() withValue "2",
            extrasKeyOf<String>("other") withValue "4",
            extrasKeyOf<Int>() withValue 1,
            extrasKeyOf<Int>("cash") withValue 1
        )

        extras.remove(extrasKeyOf<Int>("sunny"))
        assertEquals(1, extras[extrasKeyOf<Int>("cash")])

        assertEquals(1, extras.remove(extrasKeyOf<Int>("cash")))
        assertNull(extras[extrasKeyOf<Int>("cash")])

        assertEquals(1, extras.remove(extrasKeyOf<Int>()))
        assertNull(extras[extrasKeyOf<Int>()])

        assertEquals("4", extras.remove(extrasKeyOf<String>("other")))

        assertNull(extras[extrasKeyOf<String>("other")])

        assertEquals(
            extrasOf(extrasKeyOf<String>() withValue "2"),
            extras.toExtras()
        )
    }

    @Test
    fun `test - mutable extras - clear`() {
        konst extras = mutableExtrasOf(
            extrasKeyOf<String>() withValue "2",
            extrasKeyOf<String>("other") withValue "4",
            extrasKeyOf<Int>() withValue 1,
            extrasKeyOf<Int>("cash") withValue 1
        )

        assertFalse(extras.isEmpty(), "Expected non-empty extras")
        extras.clear()
        assertTrue(extras.isEmpty(), "Expected extras to be empty")

        assertEquals(emptyExtras(), extras)
        assertEquals(emptyExtras(), extras.toExtras())
    }

    @Test
    fun `test mutable extras - putAll`() {
        konst extras = mutableExtrasOf(
            extrasKeyOf<String>() withValue "1",
            extrasKeyOf<String>("overwrite") withValue "2"
        )

        extras.putAll(
            extrasOf(
                extrasKeyOf<String>("overwrite") withValue "3",
                extrasKeyOf<Int>() withValue 1
            )
        )

        assertEquals(
            extrasOf(
                extrasKeyOf<String>() withValue "1",
                extrasKeyOf<String>("overwrite") withValue "3",
                extrasKeyOf<Int>() withValue 1
            ),
            extras
        )
    }
}
